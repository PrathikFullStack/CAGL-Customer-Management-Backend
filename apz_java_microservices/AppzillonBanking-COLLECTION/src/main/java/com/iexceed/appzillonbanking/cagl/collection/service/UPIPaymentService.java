package com.iexceed.appzillonbanking.cagl.collection.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoMaitriRetry;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUaobAuditLogs;
import com.iexceed.appzillonbanking.cagl.collection.payload.*;
import com.iexceed.appzillonbanking.cagl.collection.repository.ab.TbUaobAuditLogsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.collection.constants.CommonConstants;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoQRDtls;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoQRDtlsId;
import com.iexceed.appzillonbanking.cagl.collection.repository.ab.TbUacoQRDtlsRepo;
import com.iexceed.appzillonbanking.cagl.collection.util.GenerateQR;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import com.iexceed.appzillonbanking.interfaceAdapter.service.InterfaceAdapter;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UPIPaymentService {

	@Autowired
	private InterfaceAdapter interfaceAdapter;

	@Autowired
	private TbUacoQRDtlsRepo tbUacoQRDtlsRepo;

	@Autowired
	private TbUaobAuditLogsRepository auditLogsRepository;

	private static final String QR_UPI_PAYMENT_INTF = "QRUPIPayment";
	private static final String BULK_STATUS_CHECK_INTF = "BulkStatusCheck";
	public static final String MAITRI_UPI_COLLECTION_INTF = "MaitriUpiCollection";
	private static final Logger logger = LogManager.getLogger(UPIPaymentService.class);

	public Mono<Response> qrUPIPayment(QRUPIPaymentRequest qrUPIPaymentRequest, Header header) {
		try {
			String custTxnId = qrUPIPaymentRequest.getRequestObj().getCustomerId() + System.currentTimeMillis();
			qrUPIPaymentRequest.getRequestObj().setCustomerTxnId(custTxnId);
			logger.debug("Request to QRUPIPayment::{}", qrUPIPaymentRequest);
			Mono<Object> responseMono = interfaceAdapter.callExternalService(header, qrUPIPaymentRequest,
					QR_UPI_PAYMENT_INTF, true);
			return responseMono.flatMap(val -> {
				logger.debug("API response::{}", val);
				return Mono.just(validateQRResponse(val, qrUPIPaymentRequest));
			});
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
			ResponseHeader responseHeader = new ResponseHeader();
			CommonUtils.generateHeaderForGenericError(responseHeader);
			Response response = Response.builder().responseHeader(responseHeader).responseBody(null).build();
			return Mono.just(response);
		}
	}

	private Response validateQRResponse(Object qrAPIResp, QRUPIPaymentRequest qrUPIPaymentRequest) {
		ResponseBody responseBody = new ResponseBody();
		ResponseHeader responseHeader = new ResponseHeader();
		if (qrAPIResp instanceof HashMap<?, ?> || qrAPIResp instanceof ArrayList<?>) {
			try {
				String extApiResponse = new ObjectMapper().writeValueAsString(qrAPIResp);

				String payloadStr = null;
				try {
					payloadStr = new ObjectMapper()
							.writeValueAsString(qrUPIPaymentRequest.getRequestObj().getPayload());
				} catch (Exception e) {
					logger.error("Payload serialization error", e);
				}

				TbUacoQRDtls tbUacoQRDtls = TbUacoQRDtls.builder().appId(qrUPIPaymentRequest.getAppId())
						.customerId(qrUPIPaymentRequest.getRequestObj().getCustomerId())
						.billNumber(qrUPIPaymentRequest.getRequestObj().getBillNumber()).intentLink(null)
						.remarks(qrUPIPaymentRequest.getRequestObj().getRemarks())
						.customerName(qrUPIPaymentRequest.getRequestObj().getCustomerName()).apiStatusCode(null)
						.customerTxnId(qrUPIPaymentRequest.getRequestObj().getCustomerTxnId())
						.createTs(new Timestamp(new Date().getTime())).status("INITIATED")
						.payload(payloadStr)
						.kendraId(qrUPIPaymentRequest.getRequestObj().getKendraId())
						.branchId(qrUPIPaymentRequest.getRequestObj().getBranchId())
						.build();

				if (!CommonUtils.checkStringNullOrEmpty(extApiResponse)) {
					JSONObject responseJSON = new JSONObject(extApiResponse);
					tbUacoQRDtls.setApiStatusCode(String.valueOf(responseJSON.getInt("statusCode")));
					tbUacoQRDtls.setAmount(qrUPIPaymentRequest.getRequestObj().getAmount());
					JSONObject dataObj = responseJSON.optJSONObject("data");
					String fpTxnId = null;
					if (dataObj != null && !dataObj.isNull("fpTxnId")) {
						fpTxnId = dataObj.optString("fpTxnId", null);
					}
					tbUacoQRDtls.setMasterTxnId(fpTxnId);

					tbUacoQRDtls.setMasterTxnId(responseJSON.getJSONObject("data").getString("fpTxnId"));
					if (responseJSON.has("status") && Boolean.TRUE.equals(responseJSON.getBoolean("status"))
							&& responseJSON.has("statusCode") && responseJSON.getInt("statusCode") == 10000) {
						logger.debug("Success response from the API");
						tbUacoQRDtls.setIntentLink(responseJSON.getJSONObject("data").getString("intentLink"));
						String base64QR = GenerateQR
								.generateQRCode(responseJSON.getJSONObject("data").getString("intentLink"));
						logger.debug("base64QR::{}", base64QR);
						responseJSON.getJSONObject("data").put("intentBase64", base64QR);
						responseBody.setResponseObj(responseJSON.toString(0));
						CommonUtils.generateHeaderForSuccess(responseHeader);
					} else {
						logger.debug("Failure response status from the API.");
						CommonUtils.generateHeaderForFailure(responseHeader,
								responseJSON.has("message") ? responseJSON.getString("message") : "");
						responseBody.setResponseObj(extApiResponse);
					}
				}
				logger.debug("Final data to tbUacoQRDtls::{}", tbUacoQRDtls);
				tbUacoQRDtlsRepo.save(tbUacoQRDtls);
			} catch (Exception e) {
				logger.error(CommonConstants.EXCEP_OCCURED, e);
				CommonUtils.generateHeaderForGenericError(responseHeader);
			}
		}
		return Response.builder().responseHeader(responseHeader).responseBody(responseBody).build();
	}

	/**
	 * Method to fetch the QR UPI Payment Status
	 * 
	 * @param qrUPIPaymentRequest
	 * @param header
	 * @return
	 */
	public Response refreshQRUPIPayment(QRUPIPaymentRequest qrUPIPaymentRequest, Header header) {
		ResponseHeader responseHeader = new ResponseHeader();
		Response apiResponse = new Response();
		try {
			logger.debug("Request to refreshQRUPIPayment::{}", qrUPIPaymentRequest);
			TbUacoQRDtlsId tbUacoQRDtlsId = TbUacoQRDtlsId.builder().appId(qrUPIPaymentRequest.getAppId())
					.billNumber(qrUPIPaymentRequest.getRequestObj().getBillNumber())
					.customerId(qrUPIPaymentRequest.getRequestObj().getCustomerId()).build();
			Optional<TbUacoQRDtls> tbUacoQRDtlsOpt = tbUacoQRDtlsRepo.findById(tbUacoQRDtlsId);
			logger.debug("refreshQRUPIPayment tbUacoQRDtlsOpt::{}", tbUacoQRDtlsOpt);
			if (tbUacoQRDtlsOpt.isPresent()) {
				CommonUtils.generateHeaderForSuccess(responseHeader);
				JSONObject responseJSON = new JSONObject();
				responseJSON.put("txnId", tbUacoQRDtlsOpt.get().getCustomerTxnId());
				responseJSON.put("status", tbUacoQRDtlsOpt.get().getStatus());
				ResponseBody responseBody = ResponseBody.builder().responseObj(responseJSON.toString()).build();
				apiResponse.setResponseHeader(responseHeader);
				apiResponse.setResponseBody(responseBody);
			} else {
				CommonUtils.generateHeaderForNoResult(responseHeader);
				ResponseBody responseBody = ResponseBody.builder().responseObj(null).build();
				apiResponse.setResponseHeader(responseHeader);
				apiResponse.setResponseBody(responseBody);
			}
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
			CommonUtils.generateHeaderForGenericError(responseHeader);
			ResponseBody responseBody = ResponseBody.builder().responseObj(null).build();
			apiResponse.setResponseHeader(responseHeader);
			apiResponse.setResponseBody(responseBody);
		}
		return apiResponse;
	}

	/**
	 * Method to fetch the QR UPI Payment Statuses based on list of memberIds.
	 * 
	 * @param qrUPIPaymentRequest
	 * @param header
	 * @return
	 */
	public Response verifyQRPaymentStatus(VerifyQRPaymentRequest verifyQRPaymentRequest, Header header) {
		ResponseHeader responseHeader = new ResponseHeader();
		Response apiResponse = new Response();
		try {
			logger.debug("Request to verifyQRPayment::{}", verifyQRPaymentRequest);
			if (!CommonUtils.checkStringNullOrEmpty(verifyQRPaymentRequest.getRequestObj().getCustIds())) {
				List<String> memberList = Arrays.asList(verifyQRPaymentRequest.getRequestObj().getCustIds().split("~"));
				List<TbUacoQRDtls> tbUacoQRDtlsLst = tbUacoQRDtlsRepo.findByCustomerIdIn(memberList);
				logger.debug("verifyQRPayment tbUacoQRDtlsList::{}", tbUacoQRDtlsLst);
				if (!tbUacoQRDtlsLst.isEmpty()) {
					CommonUtils.generateHeaderForSuccess(responseHeader);
					List<Map<String, String>> filteredQRDtlsLst = tbUacoQRDtlsLst.stream()
							.map(tbUacoQRDtls -> Map.of("customerId", tbUacoQRDtls.getCustomerId(), "status",
									tbUacoQRDtls.getStatus(), "billNumber", tbUacoQRDtls.getBillNumber()))
							.toList();
					String qrStatusesStr = new ObjectMapper().writeValueAsString(filteredQRDtlsLst);
					ResponseBody responseBody = ResponseBody.builder().responseObj(qrStatusesStr).build();
					apiResponse.setResponseHeader(responseHeader);
					apiResponse.setResponseBody(responseBody);
				} else {
					CommonUtils.generateHeaderForNoResult(responseHeader);
					ResponseBody responseBody = ResponseBody.builder().responseObj(null).build();
					apiResponse.setResponseHeader(responseHeader);
					apiResponse.setResponseBody(responseBody);
				}
			} else {
				CommonUtils.generateHeaderForFailure(responseHeader, "Invalid Request");
				ResponseBody responseBody = ResponseBody.builder().responseObj(null).build();
				apiResponse.setResponseHeader(responseHeader);
				apiResponse.setResponseBody(responseBody);
			}
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
			CommonUtils.generateHeaderForGenericError(responseHeader);
			ResponseBody responseBody = ResponseBody.builder().responseObj(null).build();
			apiResponse.setResponseHeader(responseHeader);
			apiResponse.setResponseBody(responseBody);
		}
		return apiResponse;
	}

	/**
	 * Method to update the status of the QR UPI Payment (To be used only for
	 * internal testing, Since the actual update happens from the Fingpay invoking
	 * exposed API)
	 * 
	 * @param qrUPIPaymentRequest
	 * @param header
	 * @return
	 */
	public Response updateQRUPIPaymentStatus(QRUPIPaymentRequest qrUPIPaymentRequest, Header header) {
		ResponseHeader responseHeader = new ResponseHeader();
		Response apiResponse = new Response();
		try {
			logger.debug("Request to updateQRUPIPaymentStatus::{}", qrUPIPaymentRequest);
			TbUacoQRDtlsId tbUacoQRDtlsId = TbUacoQRDtlsId.builder().appId(qrUPIPaymentRequest.getAppId())
					.billNumber(qrUPIPaymentRequest.getRequestObj().getBillNumber())
					.customerId(qrUPIPaymentRequest.getRequestObj().getCustomerId()).build();
			Optional<TbUacoQRDtls> tbUacoQRDtlsOpt = tbUacoQRDtlsRepo.findById(tbUacoQRDtlsId);
			logger.debug("updateQRUPIPaymentStatus tbUacoQRDtlsOpt::{}", tbUacoQRDtlsOpt);
			if (tbUacoQRDtlsOpt.isPresent()) {
				TbUacoQRDtls tbUacoQRDtls = tbUacoQRDtlsOpt.get();
				tbUacoQRDtls.setStatus(qrUPIPaymentRequest.getRequestObj().getStatus());
				tbUacoQRDtlsRepo.save(tbUacoQRDtlsOpt.get());
				CommonUtils.generateHeaderForSuccess(responseHeader);
			} else {
				CommonUtils.generateHeaderForNoResult(responseHeader);
			}
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
			CommonUtils.generateHeaderForGenericError(responseHeader);
		}
		ResponseBody responseBody = ResponseBody.builder().responseObj(null).build();
		apiResponse.setResponseHeader(responseHeader);
		apiResponse.setResponseBody(responseBody);
		return apiResponse;
	}

	public Mono<Response> bulkStatusCheck(BulkStatusRequest bulkStatusRequest, Header header) {

		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();

		TbUaobAuditLogs auditLog = new TbUaobAuditLogs();

		try {
			logger.debug("Request to BulkStatusCheck::{}", bulkStatusRequest);
			ObjectMapper mapper = new ObjectMapper();

			Timestamp now = new Timestamp(System.currentTimeMillis());
			auditLog.setSeqId(UUID.randomUUID().toString());
			auditLog.setApiName("BulkStatusCheck");
			auditLog.setAppId(bulkStatusRequest.getAppId());

			if (bulkStatusRequest.getRequestObj() != null
					&& bulkStatusRequest.getRequestObj().getMerchantTranIds() != null
					&& !bulkStatusRequest.getRequestObj().getMerchantTranIds().isEmpty()) {
				auditLog.setApplicationId(
						bulkStatusRequest.getRequestObj().getMerchantTranIds()
								.get(0));
			}
			auditLog.setReqTs(now);
			auditLog.setCreateTs(now);
			auditLog.setStatus("INITIATED");
			auditLog.setApiStatus("REQUEST_SENT");
			auditLog.setRequestPayload(
					mapper.writeValueAsString(bulkStatusRequest));
			auditLogsRepository.save(auditLog);

			header.setInterfaceId(BULK_STATUS_CHECK_INTF);
			Mono<Object> responseMono = interfaceAdapter.callExternalService(
					header, bulkStatusRequest, BULK_STATUS_CHECK_INTF, true
			);
			return responseMono.map(val -> {

				try {
					String responseJson;
					if (val instanceof String) {
						responseJson = (String) val;
					} else {
						responseJson = mapper.writeValueAsString(val);
					}
					logger.debug("External API raw response :: {}", responseJson);

					auditLog.setResponsePayload(responseJson);
					auditLog.setApiStatus("SUCCESS");
					auditLog.setStatus("COMPLETED");
					auditLog.setResTs(new Timestamp(System.currentTimeMillis()));
					auditLog.setUpdateTs(new Timestamp(System.currentTimeMillis()));
					auditLogsRepository.save(auditLog);

					responseHeader.setResponseCode("0");
					responseHeader.setResponseMessage("SUCCESS");
					responseBody.setResponseObj(responseJson);

				} catch (Exception e) {

					logger.error("Error parsing response", e);

					auditLog.setApiStatus("FAILED");
					auditLog.setStatus("INPROGRESS");
					auditLog.setResponsePayload(e.getMessage());
					auditLog.setResTs(new Timestamp(System.currentTimeMillis()));
					auditLog.setUpdateTs(new Timestamp(System.currentTimeMillis()));
					auditLogsRepository.save(auditLog);

					CommonUtils.generateHeaderForGenericError(responseHeader);
				}

				return Response.builder()
						.responseHeader(responseHeader)
						.responseBody(responseBody)
						.build();

			}).onErrorResume(ex -> {

				logger.error("Exception occurred", ex);

				auditLog.setApiStatus("FAILED");
				auditLog.setStatus("INPROGRESS");
				auditLog.setResponsePayload(ex.getMessage());
				auditLog.setResTs(new Timestamp(System.currentTimeMillis()));
				auditLog.setUpdateTs(new Timestamp(System.currentTimeMillis()));
				auditLogsRepository.save(auditLog);

				CommonUtils.generateHeaderForGenericError(responseHeader);

				return Mono.just(Response.builder()
						.responseHeader(responseHeader)
						.responseBody(null)
						.build());
			});

		} catch (Exception ex) {

			logger.error("Exception occurred", ex);
			auditLog.setApiStatus("FAILED");
			auditLog.setStatus("INPROGRESS");
			auditLog.setResponsePayload(ex.getMessage());
			auditLog.setResTs(new Timestamp(System.currentTimeMillis()));
			auditLog.setUpdateTs(new Timestamp(System.currentTimeMillis()));
			auditLogsRepository.save(auditLog);

			CommonUtils.generateHeaderForGenericError(responseHeader);
			return Mono.just(Response.builder()
					.responseHeader(responseHeader)
					.responseBody(null)
					.build());
		}
	}
	public Mono<Response> maitriUpiCollection(MaitriUPIRequest request, Header header) {

		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();

		TbUaobAuditLogs auditLog = new TbUaobAuditLogs();

		try {
			logger.debug("Request to Maitri UPI Collection :: {}", request);

			Timestamp now = new Timestamp(System.currentTimeMillis());
			auditLog.setSeqId(UUID.randomUUID().toString());
			auditLog.setApiName("MaitriUpiCollection");
			auditLog.setAppId(request.getAppId());

			if (request.getRequestObj() != null
					&& request.getRequestObj().getCustomerDetails() != null
					&& !request.getRequestObj().getCustomerDetails().isEmpty()) {

				auditLog.setCustDtlId(
						request.getRequestObj()
								.getCustomerDetails()
								.get(0)
								.getCustomerId());
			}
			auditLog.setApplicationId(
					request.getRequestObj().getUniqueIdentifier());

			auditLog.setReqTs(now);
			auditLog.setCreateTs(now);
			auditLog.setStatus("INITIATED");
			auditLog.setApiStatus("REQUEST_SENT");

			ObjectMapper mapper = new ObjectMapper();
			auditLog.setRequestPayload(
					mapper.writeValueAsString(request));

			auditLogsRepository.save(auditLog);
			header.setInterfaceId(MAITRI_UPI_COLLECTION_INTF);

			Mono<Object> responseMono = interfaceAdapter.callExternalService(header,
					request, MAITRI_UPI_COLLECTION_INTF, true);
			return responseMono.map(val -> {
				try {
					String responseJson = mapper.writeValueAsString(val);

					auditLog.setResponsePayload(responseJson);
					auditLog.setApiStatus("SUCCESS");
					auditLog.setStatus("COMPLETED");
					auditLog.setResTs(new Timestamp(System.currentTimeMillis()));
					auditLog.setUpdateTs(new Timestamp(System.currentTimeMillis()));
					auditLogsRepository.save(auditLog);

					responseHeader.setResponseCode("0");
					responseHeader.setResponseMessage("SUCCESS");

					responseBody.setResponseObj(responseJson);

				} catch (Exception e) {

					logger.error("Error parsing response", e);

					auditLog.setApiStatus("FAILED");
					auditLog.setStatus("INPROGRESS");
					auditLog.setResponsePayload(e.getMessage());
					auditLog.setResTs(new Timestamp(System.currentTimeMillis()));
					auditLog.setUpdateTs(new Timestamp(System.currentTimeMillis()));
					auditLogsRepository.save(auditLog);

					CommonUtils.generateHeaderForGenericError(responseHeader);
				}
				return Response.builder()
						.responseHeader(responseHeader).responseBody(responseBody)
						.build();
			}).onErrorResume(ex -> {

				logger.error(CommonConstants.EXCEP_OCCURED, ex);

				auditLog.setApiStatus("FAILED");
				auditLog.setStatus("INPROGRESS");
				auditLog.setResponsePayload(ex.getMessage());
				auditLog.setResTs(new Timestamp(System.currentTimeMillis()));
				auditLog.setUpdateTs(new Timestamp(System.currentTimeMillis()));
				auditLogsRepository.save(auditLog);

				CommonUtils.generateHeaderForGenericError(responseHeader);

				response.setResponseHeader(responseHeader);
				response.setResponseBody(null);

				return Mono.just(response);
			});

		} catch (Exception ex) {

			logger.error(CommonConstants.EXCEP_OCCURED, ex);

			auditLog.setApiStatus("FAILED");
			auditLog.setStatus("INPROGRESS");
			auditLog.setResponsePayload(ex.getMessage());
			auditLog.setResTs(new Timestamp(System.currentTimeMillis()));
			auditLog.setUpdateTs(new Timestamp(System.currentTimeMillis()));
			auditLogsRepository.save(auditLog);

			CommonUtils.generateHeaderForGenericError(responseHeader);
			response.setResponseHeader(responseHeader);
			response.setResponseBody(null);
			return Mono.just(response);
		}
	}

	public Response fetchQRDetails(FetchQRDetailsRequest request) {
		ResponseHeader responseHeader = new ResponseHeader();
		try {
			logger.debug("Request to Fetch QR Details Collection :: {}", request);
			List<TbUacoQRDtls> tbUacoQRDtls = new ArrayList<>();
			if(request.getRequestObj().getKendraIds() != null && !request.getRequestObj().getKendraIds().isEmpty()) {
				tbUacoQRDtls = tbUacoQRDtlsRepo.findByKendraIdsAndMeetingDate(request.getRequestObj().getKendraIds()
						, request.getRequestObj().getMeetingDate());
			} else {
				tbUacoQRDtls = tbUacoQRDtlsRepo.findByBranchIdAndMeetingDate(request.getRequestObj().getBranchId()
						, request.getRequestObj().getMeetingDate());
			}
			ResponseBody responseBody = new ResponseBody();
			String qrDetailsStr = new ObjectMapper().writeValueAsString(tbUacoQRDtls);
			responseHeader.setResponseCode("0");
			responseHeader.setResponseMessage("SUCCESS");
			responseBody.setResponseObj(qrDetailsStr);
			return Response.builder()
					.responseHeader(responseHeader)
					.responseBody(responseBody)
					.build();
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
			CommonUtils.generateHeaderForGenericError(responseHeader);
			return Response.builder()
					.responseHeader(responseHeader)
					.responseBody(null)
					.build();
		}
	}

	public Mono<Response> fetchCustomerTxnDetails(FetchCustomerTxnDetailsRequest request, Header header) {

		ResponseHeader responseHeader = new ResponseHeader();
		ObjectMapper mapper = new ObjectMapper();

		try {

			List<TbUacoQRDtls> qrList =
					tbUacoQRDtlsRepo.findByCustomerIdInAndStatus(
							request.getRequestObj().getCustomerIds(), "INITIATED"
					);

			if (qrList == null || qrList.isEmpty()) {
				logger.warn("QR List is empty");
				return Mono.just(buildSuccessResponse("NO QR DATA", null));
			}

			List<String> merchantTranIds =
					qrList.stream()
							.map(TbUacoQRDtls::getMasterTxnId)
							.filter(Objects::nonNull)
							.toList();

			if (merchantTranIds.isEmpty()) {
				logger.warn("merchantTranIds empty");
				return Mono.just(buildSuccessResponse("NO VALID TXNS", null));
			}

			logger.debug("merchantTranIds = {}", merchantTranIds);

			BulkStatusRequest bulkRequest = BulkStatusRequest.builder()
					.appId(request.getAppId())
					.interfaceName(request.getInterfaceName())
					.userId(request.getAppId())
					.requestObj(BulkStatusRequestFields.builder()
							.superMerchantId(759L)
							.merchantTranIds(merchantTranIds)
							.date(request.getRequestObj().getDate())
							.build())
					.build();

			return bulkStatusCheck(bulkRequest, header)
					.flatMap(response -> {

						try {

							Object obj = response.getResponseBody() != null
									? response.getResponseBody().getResponseObj()
									: null;

							if (obj == null) {
								logger.warn("ResponseObj null");
								return Mono.just(response);
							}

							String json = String.valueOf(obj);
							if (json.isBlank() || json.equals("\"\"")) {
								logger.warn("Empty API response");
								return Mono.just(response);
							}

							BulkStatusApiResponse apiResponse =
									mapper.readValue(json, BulkStatusApiResponse.class);

							if (apiResponse == null || apiResponse.getData() == null) {
								logger.warn("API data null");
								return Mono.just(response);
							}

							Map<String, String> statusMap =
									apiResponse.getData().stream()
											.filter(t -> t.getMerchantTranId() != null)
											.collect(Collectors.toMap(
													TxnData::getMerchantTranId,
													TxnData::getStatus,
													(a, b) -> a
											));

							logger.debug("StatusMap :: {}", statusMap);

							List<TbUacoQRDtls> updatedList = new ArrayList<>();

							for (TbUacoQRDtls qr : qrList) {

								if (qr == null) continue;

								String status = statusMap.get(qr.getMasterTxnId());

								if (!"SUCCESS".equalsIgnoreCase(status)) {
									logger.info("Skipping non-success txn: {} status: {}",
											qr.getCustomerId(), status);
									continue;
								}

								String customerId = qr.getCustomerId();

								logger.info("Processing SUCCESS txn for customerId: {}", customerId);

								JsonNode payloadNode;
								try {
									payloadNode = mapper.readTree(qr.getPayload());
								} catch (Exception e) {
									logger.error("Invalid payload for customer: {}", customerId, e);
									continue;
								}

								String userId = payloadNode.has("userId")
										? payloadNode.get("userId").asText("SYSTEM")
										: "SYSTEM";

								List<LoanDetails> loanDetailsList = new ArrayList<>();

								if (payloadNode.isArray()) {
									for (JsonNode node : payloadNode) {

										if (node == null) continue;

										String loanId = node.hasNonNull("id")
												? node.get("id").asText()
												: "";

										double due = node.hasNonNull("dueAmt")
												? node.get("dueAmt").asDouble()
												: 0.0;

										double cashAmt = node.hasNonNull("cashAmt")
												? node.get("cashAmt").asDouble()
												: 0.0;

										double upiAmt = node.hasNonNull("upiAmt")
												? node.get("upiAmt").asDouble()
												: 0.0;

										double parAmt = node.hasNonNull("parAmt")
												? node.get("parAmt").asDouble()
												: 0.0;

										double prevParAmt = node.hasNonNull("prevParAmt")
												? node.get("prevParAmt").asDouble()
												: 0.0;

										double collectionAmt =
												cashAmt + upiAmt + parAmt + prevParAmt;

										LoanDetails details = LoanDetails.builder()
												.loanId(loanId)
												.loanDue(String.valueOf(due))
												.loanCollectionAmt(String.valueOf(collectionAmt))
												.build();

										loanDetailsList.add(details);
									}
								}

								CusDetails cusDetails = CusDetails.builder()
										.customerId(customerId)
										.cusCollectionAmt(qr.getAmount() != null ? qr.getAmount() : "0")
										.cusFlag(getCusFlag(qr.getPayload()))
										.upiFlag("MAITRI")
										.loanDetails(loanDetailsList)
										.build();

								MaitriUPIRequestFields requestFields =
										MaitriUPIRequestFields.builder()
												.uniqueIdentifier(generateUniqueIdentifier(customerId))
												.branchCode(qr.getBranchId())
												.customerDetails(Collections.singletonList(cusDetails))
												.build();

								MaitriUPIRequest maitriRequest = MaitriUPIRequest.builder()
										.appId("APZCBO")
										.userId(userId)
										.interfaceName(UPIPaymentService.MAITRI_UPI_COLLECTION_INTF)
										.requestObj(requestFields)
										.build();

								logger.debug("MAITRI Request :: {}", maitriRequest);

								try {
									interfaceAdapter.callExternalService(
											header,
											maitriRequest,
											MAITRI_UPI_COLLECTION_INTF,
											true
									);
								} catch (Exception ex) {
									logger.error("MAITRI call failed for customer: {}", customerId, ex);
								}

								qr.setStatus(status);
								updatedList.add(qr);
							}

							return Mono.fromCallable(() -> {
										tbUacoQRDtlsRepo.saveAll(updatedList);
										return response;
									})
									.subscribeOn(Schedulers.boundedElastic());

						} catch (Exception e) {
							logger.error("Processing error", e);
							return Mono.just(response);
						}
					});

		} catch (Exception ex) {
			logger.error("Exception occurred", ex);

			CommonUtils.generateHeaderForGenericError(responseHeader);

			return Mono.just(Response.builder()
					.responseHeader(responseHeader)
					.responseBody(null)
					.build());
		}
	}

	private String getCusFlag(String payload) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode payloadArray = mapper.readTree(payload);

			String totalDueAmt = "0";
			String totalUpiAmt = "0";

			for (JsonNode loanNode : payloadArray) {
				String dueAmt =
						loanNode.has("dueAmt")
								? loanNode.get("dueAmt").asText()
								: "0";
				String upiAmt =
						loanNode.has("upiAmt")
								? loanNode.get("upiAmt").asText()
								: "0";
				totalDueAmt = String.valueOf(
						Double.parseDouble(totalDueAmt)
								+ Double.parseDouble(dueAmt));
				totalUpiAmt = String.valueOf(
						Double.parseDouble(totalUpiAmt)
								+ Double.parseDouble(upiAmt));
			}
			logger.debug("Total Due Amount :: {}", totalDueAmt);
			logger.debug("Total UPI Amount :: {}", totalUpiAmt);

			if (Double.parseDouble(totalUpiAmt)
					>= Double.parseDouble(totalDueAmt)) {
				return "FULL";
			}
			return "PARTIAL";
		} catch (Exception e) {
			logger.error("Error while calculating cusFlag", e);
			return "PARTIAL";
		}
	}

	private String generateUniqueIdentifier(String customerId) {

		LocalDate today = LocalDate.now();
		String currentDate =
				today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		Timestamp startDate =
				Timestamp.valueOf(today.atStartOfDay());
		Timestamp endDate =
				Timestamp.valueOf(today.plusDays(1).atStartOfDay());
		long todayCount =
				tbUacoQRDtlsRepo.countByCustomerIdAndCreateTsBetween(
						customerId,
						startDate,
						endDate);

		long sequenceNumber = todayCount + 1;
		return "C-"
				+ customerId
				+ "-"
				+ sequenceNumber
				+ "-"
				+ currentDate;
	}

	private Response buildSuccessResponse(String message, Object body) {

		ResponseHeader header = new ResponseHeader();
		header.setResponseCode("0");
		header.setResponseMessage(message);

		ResponseBody responseBody = new ResponseBody();

		try {
			if (body != null) {
				ObjectMapper mapper = new ObjectMapper();

				String json;

				// If already String, use directly
				if (body instanceof String) {
					json = (String) body;
				} else {
					json = mapper.writeValueAsString(body);
				}

				responseBody.setResponseObj(json);
			} else {
				responseBody.setResponseObj(null);
			}
		} catch (Exception e) {
			// fallback if serialization fails
			responseBody.setResponseObj(String.valueOf(body));
		}

		return Response.builder()
				.responseHeader(header)
				.responseBody(responseBody)
				.build();
	}
}
