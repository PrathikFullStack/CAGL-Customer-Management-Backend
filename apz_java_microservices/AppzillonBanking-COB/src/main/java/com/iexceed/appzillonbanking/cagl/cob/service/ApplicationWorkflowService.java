package com.iexceed.appzillonbanking.cagl.cob.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplnWorkflow;
import com.iexceed.appzillonbanking.cagl.cob.payload.ApplicationList;
import com.iexceed.appzillonbanking.cagl.cob.payload.BulkUploadRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.BulkUploadRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.OnboardingWorkflowRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.WorkflowRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.PopulateapplnWFRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.PopulateapplnWFRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.WorkFlowDetails;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves and applies workflow stage transitions across tb_ob_application_master (update) and
 * tb_ob__appln_workflow (insert).
 *
 * The next-stage/next-status decision is resolved by CustOnboardWorkflowProcess.process(...),
 * which returns nextStageId/nextWorkflowStatus (and nextRole) for us to persist.
 *
 * Two entry points share this logic:
 *  - updateStage(...)   : single/direct stage-update requests
 *  - saveExcelData(...) : bulk Excel approve/reject upload
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationWorkflowService {

	private final TbObApplicationMasterRepository applicationMasterRepository;

	private final AuditService auditService;
	@Autowired
	CustOnboardWorkflowProcess Custprocess;
	@PersistenceContext
	private EntityManager entityManager;

	private static final List<String> EXCEL_EXPECTED_HEADERS = Arrays.asList("APPLICATION ID", "APPROVED/REJECTED");

	private record NextStageDetails(String nextStageId, String nextWorkflowStatus) {}

	private static final int STATUS_CODE_SUCCESS = 0;
	private static final int STATUS_CODE_PARTIAL_SUCCESS = 1;
	private static final int STATUS_CODE_FAILURE = 2;

	private static final String PROCESS_STATUS_SUCCESS = "SUCCESS";
	private static final String CRT_WORKFLOW_ID = "OBCRT";
	private static final String REJECT_ACTION = "REJECT";
	private static final String YELLOW_CHANNEL = "YELLOW";
	private static final String GREEN_CHANNEL = "GREEN";
	private static final String CRT_ROLE = "CRT";

	// ============================== Update Stage ==============================

	/**
	 * Core stage-update logic, shared by both the direct update-stage flow and the Excel
	 * bulk-upload flow. For each application, delegates the next-stage/next-status decision to
	 * CustOnboardWorkflowProcess.process(...) and applies the result to tb_ob_application_master
	 * and tb_ob__appln_workflow.
	 */
	@Transactional
	public Response updateStage(PopulateapplnWFRequest request,Header header) {
		log.info("Entering updateStage(request)");

		try {
			PopulateapplnWFRequestFields reqFields = request.getRequestObj();
			WorkFlowDetails workflow = reqFields.getWorkflow();

			String roleError = validateUserRole(reqFields.getUserRole());
			if (roleError != null) {
				log.warn("updateStage rejected: {}", roleError);
				return buildErrorResponse(roleError);
			}

			List<ApplicationList> applicationDetailList = reqFields.getApplicationDetailList();
			if (CollectionUtils.isEmpty(applicationDetailList)) {
				log.warn("updateStage called with no applications to process");
				return buildSuccessResponse("No applications supplied to process", null);
			}

			int successCount = 0;
			List<Map<String, String>> failures = new ArrayList<>();

			for (ApplicationList applnRec : applicationDetailList) {
				try {
					TbObApplicationMaster master = applicationMasterRepository
							.findByApplicationId(applnRec.getApplicationId())
							.orElseThrow(() -> new NoSuchElementException(
									"No tb_ob_application_master row for applicationId=" + applnRec.getApplicationId()));

					JSONObject processResult = invokeWorkflowProcess(request, workflow, applnRec, header);

					if (!isProcessResultSuccess(processResult)) {
						throw new IllegalStateException(extractProcessErrorMessage(processResult));
					}

					NextStageDetails nextStageDetails = extractNextStageDetails(processResult);
					String nextStageId = nextStageDetails.nextStageId();
					String nextWorkflowStatus = nextStageDetails.nextWorkflowStatus();

					updateApplicationMaster(master, applnRec, reqFields, workflow, nextStageId, nextWorkflowStatus);

					successCount++;

				} catch (Exception e) {
					log.error("updateStage failed for applicationId={}: {}", applnRec.getApplicationId(),
							e.getMessage());
					Map<String, String> failure = new HashMap<>();
					failure.put("applicationId", applnRec.getApplicationId());
					failure.put("error", e.getMessage());
					failures.add(failure);
				}
			}

			int totalCount = applicationDetailList.size();
			int failedCount = failures.size();

			Map<String, Object> resultData = new HashMap<>();
			resultData.put("totalCount", totalCount);
			resultData.put("successCount", successCount);
			resultData.put("failedCount", failedCount);
			resultData.put("failures", failures);

			log.info("updateStage completed: total={}, success={}, failed={}", totalCount, successCount, failedCount);

			if (failedCount > 0) {
				String errorMsg = "Stage update failed for " + failedCount + " of " + totalCount
						+ " application(s). All changes have been rolled back. Please try again.";
				throw new RuntimeException(errorMsg);
			}
			return buildSuccessResponse(
					"Stage updated successfully for all " + totalCount + " application(s)", resultData);

		} catch (RuntimeException e) {
			log.error("updateStage rolling back: {}", e.getMessage());
			throw e;  // MUST propagate out so @Transactional triggers rollback
		} catch (Exception e) {
			log.error("updateStage failed with an unexpected error", e);
			throw new RuntimeException("Unexpected error while updating stage: " + e.getMessage(), e);
		} finally {
			log.info("Exiting updateStage(request)");
		}
	}

	/**
	 * Builds the OnboardingWorkflowRequest/Header pair for CustOnboardWorkflowProcess.process(...)
	 * and returns the parsed responseObj as a JSONObject.
	 */
	private JSONObject invokeWorkflowProcess(PopulateapplnWFRequest reqFields, WorkFlowDetails workflow,
											 ApplicationList applnRec,Header header) {
		log.info("Entering invokeWorkflowProcess() for applicationId={}", applnRec.getApplicationId());

		WorkflowRequestFields processFields = new WorkflowRequestFields();
		processFields.setWorkflowId(workflow.getWorkflowId());
		processFields.setCurrentStage(workflow.getStage());
		processFields.setAction(workflow.getAction());
		processFields.setApplicationId(applnRec.getApplicationId());

		OnboardingWorkflowRequest processRequest = new OnboardingWorkflowRequest();
		processRequest.setAppId(reqFields.getAppId());
		processRequest.setInterfaceName(reqFields.getInterfaceName());
		processRequest.setUserId(reqFields.getUserId());
		processRequest.setRequestObj(processFields);
		processRequest.setPresentRole(reqFields.getRequestObj().getUserRole());

		Response processResponse = Custprocess.process(processRequest, header);
		JSONObject processResult = extractProcessResponseObject(processResponse);

		log.info("Exiting invokeWorkflowProcess() for applicationId={} with result={}",
				applnRec.getApplicationId(), processResult);
		return processResult;
	}

	private JSONObject extractProcessResponseObject(Response processResponse) {
		String responseObj = processResponse != null && processResponse.getResponseBody() != null
				? processResponse.getResponseBody().getResponseObj()
				: null;

		if (responseObj == null || responseObj.isEmpty()) {
			throw new IllegalStateException("CustOnboardWorkflowProcess.process() returned an empty response");
		}

		return new JSONObject(responseObj);
	}

	private boolean isProcessResultSuccess(JSONObject processResult) {
		return PROCESS_STATUS_SUCCESS.equalsIgnoreCase(processResult.optString("status", ""));
	}

	private String extractProcessErrorMessage(JSONObject processResult) {
		String errorMessage = processResult.isNull("errorMessage") ? null : processResult.optString("errorMessage", null);
		String errorCode = processResult.isNull("errorCode") ? null : processResult.optString("errorCode", null);

		if (errorMessage != null && !errorMessage.isEmpty()) {
			return errorCode != null && !errorCode.isEmpty() ? "[" + errorCode + "] " + errorMessage : errorMessage;
		}
		return "CustOnboardWorkflowProcess.process() did not return " + PROCESS_STATUS_SUCCESS;
	}

	/**
	 * Updates tb_ob_application_master to the resolved next stage/status.
	 */
	private void updateApplicationMaster(TbObApplicationMaster master, ApplicationList applnRec,
										 PopulateapplnWFRequestFields reqFields, WorkFlowDetails workflow, String nextStageId,
										 String nextWorkflowStatus) {
		log.info("Entering updateApplicationMaster() for applicationId={}", applnRec.getApplicationId());

		String fromStage = master.getStage();
		String fromWfStage = master.getWfStage();

		master.setStage(nextStageId);
		master.setRemarks(workflow.getRemarks());
		master.setUpdatedBy(reqFields.getCreatedBy());
		master.setUpdatedByRole(reqFields.getUserRole());
		master.setUpdatedTs(LocalDateTime.now());
		master.setVersion(String.valueOf(applnRec.getVersionNum()));
		master.setWfStage(nextWorkflowStatus);

		applicationMasterRepository.save(master);

		Map<String, Object> transitionPayload = new HashMap<>();
		transitionPayload.put("workflowId", workflow.getWorkflowId());
		transitionPayload.put("action", workflow.getAction());
		transitionPayload.put("fromStage", fromStage);
		transitionPayload.put("fromWfStage", fromWfStage);
		transitionPayload.put("toStage", nextStageId);
		transitionPayload.put("toWfStage", nextWorkflowStatus);
		transitionPayload.put("remarks", workflow.getRemarks());

		auditService.saveStageTransitionAudit(master, reqFields.getCreatedBy(), reqFields.getUserName(),
				reqFields.getUserRole(), reqFields.getAppId(), String.valueOf(applnRec.getVersionNum()),
				nextStageId, nextWorkflowStatus, transitionPayload);

		log.info("Exiting updateApplicationMaster() for applicationId={}", applnRec.getApplicationId());
	}


	/**
	 * Validates that the acting user's role is permitted to perform a stage update.
	 * Returns null when valid, or an error message when not.
	 */
	private String validateUserRole(String userRole) {
		log.info("Entering validateUserRole() with userRole={}", userRole);

		String error = null;
		if (userRole == null || userRole.isEmpty()) {
			error = "User role is missing in the request";
		}

		log.info("Exiting validateUserRole() with error={}", error);
		return error;
	}

	// ============================== Excel Bulk Upload ==============================

	/**
	 * Bulk approve/reject upload. Parses the sheet, then applies each row's decision via
	 * updateStage(...), reusing the exact same update/insert logic used by the direct
	 * stage-update flow.
	 */
	public Response saveExcelData(BulkUploadRequest apiRequest,Header header) {
		log.info("Entering saveExcelData()");

		try {
			BulkUploadRequestFields requestObj = apiRequest.getRequestObj();
			String base64Data = requestObj.getBase64Data();

			if (!isValidBase64(base64Data)) {
				return buildErrorResponse("Incoming"
						+ " BASE64 is Invalid");
			}

			byte[] decodedBytes;
			try {
				decodedBytes = Base64.getDecoder().decode(base64Data);
			} catch (IllegalArgumentException e) {
				return buildErrorResponse("Invalid Base64 data");
			}

			String userRole = requestObj.getUserRole();
			String roleError = validateUserRole(userRole);
			if (roleError != null) {
				return buildErrorResponse(roleError);
			}

			return processExcelWorkbook(decodedBytes, apiRequest,header);

		} catch (Exception e) {
			log.error("saveExcelData failed with an unexpected error", e);
			return buildErrorResponse("Unexpected error while processing Excel data: " + e.getMessage());
		} finally {
			log.info("Exiting saveExcelData()");
		}
	}

	/**
	 * Parses the workbook and applies each valid row via updateStage(...).
	 */
	private Response processExcelWorkbook(byte[] decodedBytes, BulkUploadRequest apiRequest,Header header)
			throws IOException {
		log.info("Entering processExcelWorkbook()");
		BulkUploadRequestFields requestObj = apiRequest.getRequestObj();

		try (InputStream inputStream = new ByteArrayInputStream(decodedBytes);
			 Workbook workbook = new XSSFWorkbook(inputStream)) {

			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (!rowIterator.hasNext()) {
				return buildErrorResponse("Excel is empty - no rows found");
			}

			Row titleRow = rowIterator.next();
			for (int i = 0; i < EXCEL_EXPECTED_HEADERS.size(); i++) {
				Cell cell = titleRow.getCell(i);
				if (cell == null || !EXCEL_EXPECTED_HEADERS.get(i).equalsIgnoreCase(cell.toString().trim())) {
					return buildErrorResponse("Column titles do not match expected titles");
				}
			}

			List<Row> validRows = new ArrayList<>();
			List<String> applicationIds = new ArrayList<>();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				String applicationId = extractCellValue(row.getCell(0));
				if (applicationId != null && !applicationId.isEmpty()) {
					applicationIds.add(applicationId);
					validRows.add(row);
				}
			}

			if (validRows.isEmpty()) {
				return buildErrorResponse("No valid rows with an application id were found");
			}

			Map<String, TbObApplicationMaster> masterMap = new HashMap<>();
			applicationMasterRepository.findAllById(applicationIds)
					.forEach(m -> masterMap.put(m.getApplicationId(), m));

			int rowNum = 1;
			for (Row row : validRows) {
				rowNum++;

				String applicationId = extractCellValue(row.getCell(0));
				TbObApplicationMaster master = masterMap.get(applicationId);

				if (master == null) {
					return buildErrorResponse(
							"No application found for applicationId=" + applicationId + " (row " + rowNum + ")");
				}

				String decision = extractCellValue(row.getCell(1));
				String rowRemarks = extractCellValue(row.getCell(2));
				String effectiveRemarks = (rowRemarks != null && !rowRemarks.isEmpty()) ? rowRemarks
						: requestObj.getRemarks();

				PopulateapplnWFRequest updateStageRequest = buildUpdateStageRequest(master, decision,
						effectiveRemarks, apiRequest);

				Response rowResponse = updateStage(updateStageRequest,header);
				if (isFailureResponse(rowResponse)) {
					return buildErrorResponse("Row " + rowNum + " (applicationId=" + applicationId + "): "
							+ extractResponseMessage(rowResponse));
				}
			}

			log.info("processExcelWorkbook completed for {} application(s)", validRows.size());
			return buildSuccessResponse("Excel data processed successfully for " + validRows.size()
					+ " application(s)", null);
		} finally {
			log.info("Exiting processExcelWorkbook()");
		}
	}

	private PopulateapplnWFRequest buildUpdateStageRequest(TbObApplicationMaster master, String decision,
														   String remarks, BulkUploadRequest apiRequest) {
		log.info("Entering buildUpdateStageRequest() for applicationId={}", master.getApplicationId());

		BulkUploadRequestFields requestObj = apiRequest.getRequestObj();
		WorkFlowDetails workflow = new WorkFlowDetails();
		workflow.setWorkflowId(CRT_WORKFLOW_ID);
		workflow.setStage(master.getWfStage());
		workflow.setCurrentRole(requestObj.getUserRole());
		workflow.setRemarks(remarks);
		workflow.setAction(resolveWorkflowAction(decision, master.getChannelType(), requestObj.getUserRole()));

		ApplicationList applicationList = new ApplicationList();
		applicationList.setApplicationId(master.getApplicationId());
		applicationList.setVersionNum(Integer.valueOf(master.getVersion()));

		PopulateapplnWFRequestFields fields = new PopulateapplnWFRequestFields();
		fields.setWorkflow(workflow);
		fields.setCreatedBy(requestObj.getUserId());
		fields.setUserRole(requestObj.getUserRole());
		fields.setUserName(requestObj.getUserName());
		fields.setApplicationDetailList(Collections.singletonList(applicationList));

		PopulateapplnWFRequest request = new PopulateapplnWFRequest();
		request.setAppId(apiRequest.getAppId());
		request.setInterfaceName(apiRequest.getInterfaceName());
		request.setUserId(apiRequest.getUserId());

		request.setRequestObj(fields);

		log.info("Exiting buildUpdateStageRequest() for applicationId={}", master.getApplicationId());
		return request;
	}

	private boolean isValidBase64(String str) {
		log.debug("Entering isValidBase64()");

		boolean valid;
		if (str == null || str.length() % 4 != 0) {
			valid = false;
		} else {
			try {
				Base64.getDecoder().decode(str);
				valid = true;
			} catch (IllegalArgumentException e) {
				valid = false;
			}
		}

		log.debug("Exiting isValidBase64() with valid={}", valid);
		return valid;
	}

	private String extractCellValue(Cell cell) {
		log.debug("Entering extractCellValue()");

		String value;
		if (cell == null) {
			value = "";
		} else {
			value = switch (cell.getCellType()) {
				case STRING -> cell.getStringCellValue().trim();
				case NUMERIC -> {
					if (DateUtil.isCellDateFormatted(cell)) {
						yield new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
					}
					BigDecimal cellValue = BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros();
					yield cellValue.scale() > 0 ? cellValue.setScale(2, RoundingMode.HALF_UP).toPlainString()
							: cellValue.toPlainString();
				}
				case BOOLEAN -> String.valueOf(cell.getBooleanCellValue()).trim();
				case FORMULA -> {
					FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper()
							.createFormulaEvaluator();
					CellValue evaluatedValue = evaluator.evaluate(cell);
					yield switch (evaluatedValue.getCellType()) {
						case STRING -> evaluatedValue.getStringValue().trim();
						case NUMERIC -> {
							BigDecimal val = BigDecimal.valueOf(evaluatedValue.getNumberValue()).stripTrailingZeros();
							yield val.scale() > 0 ? val.setScale(2, RoundingMode.HALF_UP).toPlainString()
									: val.toPlainString();
						}
						case BOOLEAN -> String.valueOf(evaluatedValue.getBooleanValue()).trim();
						default -> "";
					};
				}
				default -> "";
			};
		}

		log.debug("Exiting extractCellValue() with value={}", value);
		return value;
	}

	// ============================== Shared Response Helpers ==============================

	private Response buildResponse(int statusCode, String message, Map<String, Object> data) {
		log.info("Entering buildResponse() with statusCode={}, message={}", statusCode, message);

		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();

		Map<String, Object> resultObject = new LinkedHashMap<>();
		resultObject.put("statusCode", statusCode);
		resultObject.put("message", message);
		if (!CollectionUtils.isEmpty(data)) {
			resultObject.putAll(data);
		}

		JSONArray responseArray = new JSONArray();
		responseArray.put(new JSONObject(resultObject));
		responseBody.setResponseObj(responseArray.toString());

		if (statusCode == STATUS_CODE_SUCCESS) {
			CommonUtils.generateHeaderForSuccess(responseHeader);
		} else {
			CommonUtils.generateHeaderForFailure(responseHeader, message);
		}

		response.setResponseBody(responseBody);
		response.setResponseHeader(responseHeader);

		log.info("Exiting buildResponse()");
		return response;
	}

	private Response buildSuccessResponse(String message, Map<String, Object> data) {
		return buildResponse(STATUS_CODE_SUCCESS, message, data);
	}

	private Response buildPartialSuccessResponse(String message, Map<String, Object> data) {
		return buildResponse(STATUS_CODE_PARTIAL_SUCCESS, message, data);
	}

	public Response buildErrorResponse(String message) {
		return buildErrorResponse(message, null);
	}

	public Response buildErrorResponse(String message, Map<String, Object> data) {
		return buildResponse(STATUS_CODE_FAILURE, message, data);
	}

	private boolean isFailureResponse(Response response) {
		log.debug("Entering isFailureResponse()");

		boolean failure;
		try {
			String responseObj = response.getResponseBody() != null ? response.getResponseBody().getResponseObj()
					: null;
			if (responseObj == null || responseObj.isEmpty()) {
				failure = true;
			} else {
				JSONArray responseArray = new JSONArray(responseObj);
				int statusCode = responseArray.length() > 0
						? responseArray.getJSONObject(0).optInt("statusCode", STATUS_CODE_FAILURE)
						: STATUS_CODE_FAILURE;
				failure = statusCode != STATUS_CODE_SUCCESS;
			}
		} catch (Exception e) {
			log.warn("Could not parse response statusCode, treating as failure", e);
			failure = true;
		}

		log.debug("Exiting isFailureResponse() with failure={}", failure);
		return failure;
	}

	private String extractResponseMessage(Response response) {
		log.debug("Entering extractResponseMessage()");

		String message = null;
		try {
			String responseObj = response.getResponseBody() != null ? response.getResponseBody().getResponseObj()
					: null;
			if (responseObj != null && !responseObj.isEmpty()) {
				JSONArray responseArray = new JSONArray(responseObj);
				if (responseArray.length() > 0) {
					message = responseArray.getJSONObject(0).optString("message");
				}
			}
		} catch (Exception e) {
			log.warn("Could not parse response message", e);
		}

		log.debug("Exiting extractResponseMessage() with message={}", message);
		return message;
	}

	private NextStageDetails extractNextStageDetails(JSONObject processResult) {
		log.info("Entering extractNextStageDetails()");

		String nextStageId = processResult.isNull("nextStageId") ? null
				: processResult.optString("nextStageId", null);
		String nextWorkflowStatus = processResult.isNull("nextWorkflowStatus") ? null
				: processResult.optString("nextWorkflowStatus", null);

		log.info("Exiting extractNextStageDetails() with nextStageId={}, nextWorkflowStatus={}", nextStageId,
				nextWorkflowStatus);
		return new NextStageDetails(nextStageId, nextWorkflowStatus);
	}

	private String resolveWorkflowAction(String decision, String channelType, String userRole) {
		log.info("Entering resolveWorkflowAction() with decision={}, channelType={}", decision, channelType);

		String resolved = decision;

		if (userRole.equalsIgnoreCase(CRT_ROLE)) {
			boolean isRejectDecision = "REJECTED".equalsIgnoreCase(decision)
					|| REJECT_ACTION.equalsIgnoreCase(decision);
			boolean isApproveDecision = "APPROVED".equalsIgnoreCase(decision) || "ACCEPT".equalsIgnoreCase(decision)
					|| "ACCEPTED".equalsIgnoreCase(decision);

			if (isRejectDecision) {
				resolved = REJECT_ACTION;
			} else if (isApproveDecision) {
				if (YELLOW_CHANNEL.equalsIgnoreCase(channelType)) {
					resolved = "CRTNEXT";
				} else if (GREEN_CHANNEL.equalsIgnoreCase(channelType)) {
					resolved = "CRTSUBMIT";
				}
			}

		}
		log.info("Exiting resolveWorkflowAction() with resolvedAction={}", resolved);
		return resolved;
	}

}