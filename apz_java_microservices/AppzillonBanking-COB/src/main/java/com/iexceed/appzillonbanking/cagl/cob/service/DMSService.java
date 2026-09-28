package com.iexceed.appzillonbanking.cagl.cob.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObDMSSessionDataRepo;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGroupRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObKendraRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObOtherDocumentRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObPhotoThumbnailRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObDocumentRepository;
import com.iexceed.appzillonbanking.cagl.cob.utils.AESDecryptionUtil;
import com.iexceed.appzillonbanking.cagl.cob.utils.Utils;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import com.iexceed.appzillonbanking.interfaceAdapter.service.InterfaceAdapter;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.AdapterUtil;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;

import net.coobird.thumbnailator.Thumbnails;
import reactor.core.publisher.Mono;

@Service
public class DMSService {

	private static final Logger LOG = LoggerFactory.getLogger(DMSService.class);

	@Autowired
	private TbObDMSSessionDataRepo tbObDMSSessionDataRepo;

	@Autowired
	private InterfaceAdapter interfaceAdapter;

	@Autowired
	private AdapterUtil adapterUtil;

	@Autowired
	private TbObKendraRepository kendradetails;

	@Autowired
	private TbObGroupRepository groupDetails;

	@Autowired
	private TbObOtherDocumentRepository tbObOtherDocumentRepository;

	@Autowired
	private TbObDocumentRepository tbObDocumentRepository;

	@Autowired
	private TbObPhotoThumbnailRepository tbObPhotoThumbnailRepo;

	@Autowired
	private TbObApplicationMasterRepository tbObApplicationMasterRepo;

	@Autowired
	@Lazy
	private DMSUploadService dmsUploadService;

	public static final String DMS_SESSION_INTERFACEID = "dmsSession";
	public static final String DMS_FOLDER_INDEX_INTERFACEID = "dmsFolderIndex";
	public static final String EXCEPTION_MSG = "Something went wrong, Please try again!!";
	public static final String INVALIDAPP_MSG = "Invalid application details, Please try again!!";
	public static final String EXCEPTION_OCCURED = "Exception occurred";

	public static final String DMS_DOCUMENT_UPLOAD = "dmsdocumentupload";
	// TODO: replace these with the real configured interface IDs for fetch/delete.
	public static final String DMS_DOCUMENT_FETCH = "dmsdocumentfetch";

	// Operation types coming in on the request (operationType field).
	public static final String OP_UPLOAD = "upload";
	public static final String OP_FETCH = "fetch";
	public static final String OP_DELETE = "delete";
	public static final String OP_FETCHALL = "fetchall";

	// Entity types (type field).
	public static final String TYPE_CUSTOMER = "customer";
	public static final String TYPE_KENDRA = "kendra";
	public static final String TYPE_GROUP = "group";

	// subType whitelist config. Naya subType aaye to sirf properties me add karo.
	public static final String SUBTYPE_ALLOWED_KEY = "PHOTO_THUMBNAIL_ALLOWED_SUBTYPES";
	public static final int SUBTYPE_MAX_LEN = 50;
	public static final String INVALID_SESSION_STATUS = "-50146";

	private static final Map<String, String> lDMSProperties = Utils.getPropertiesFromClassPath("dmsAPI.properties");

	// Empty set => koi restriction nahi (sab allowed). Non-empty => sirf listed allowed.
	private static final Set<String> ALLOWED_SUBTYPES = Arrays
			.stream(lDMSProperties.getOrDefault(SUBTYPE_ALLOWED_KEY, "").split(",")).map(String::trim)
			.filter(s -> !s.isEmpty()).map(String::toUpperCase).collect(Collectors.toSet());

	public Mono<ResponseWrapper> processDMSDoc(DmsDocumentRequest apiRequest, Header header) {
		try {
			if (apiRequest == null || apiRequest.getApiRequest() == null
					|| apiRequest.getApiRequest().getRequestObj() == null
					|| apiRequest.getApiRequest().getRequestObj().getDocList() == null
					|| apiRequest.getApiRequest().getRequestObj().getDocList().isEmpty()) {
				LOG.error("Invalid request: apiRequest or requestObj/docList is null/empty");
				return Mono.just(buildFailureWrapper(INVALIDAPP_MSG));
			}

			String operationType = resolveOperationType(apiRequest);
			LOG.info("Processing DMS document operation: [{}]", operationType);

			switch (operationType) {
				case OP_UPLOAD:
					List<DmsDocumenRequestFields> docList = apiRequest.getApiRequest().getRequestObj().getDocList();
					return handleUploadOperation(apiRequest, header)
							.doOnNext(responses -> dmsUploadService.captureFailedUploads(docList, responses, apiRequest, header))
						.map(this::buildUploadListWrapper);
			case OP_FETCH:
				return handleFetchOperation(apiRequest, header);
			case OP_FETCHALL:
				return handleFetchAllOperation(apiRequest, header);
			default:
				LOG.error("Unsupported operationType: [{}]", operationType);
				return Mono.just(buildFailureWrapper("Unsupported operation type: " + operationType));
			}
		} catch (Exception e) {
			LOG.error("Exception in processDMSDoc dispatcher", e);
			return Mono.just(buildFailureWrapper());
		}
	}

	public Map<String, String> getThumbnails(List<String> applicationIds, String docuType) {
	    Map<String, String> result = new LinkedHashMap<>();

	    if (applicationIds == null || applicationIds.isEmpty() || docuType == null || docuType.isBlank()) {
	        return result;
	    }

	    List<TbObPhotoThumbnail> thumbnails =
	            tbObPhotoThumbnailRepo.findByApplicationIdInAndDocuType(applicationIds, docuType);

	    for (TbObPhotoThumbnail thumb : thumbnails) {
	        byte[] bytes = thumb.getThumbnail();
	        if (bytes == null || bytes.length == 0) {
	            LOG.warn("Skipping thumbnail with empty bytes for applicationId={} docuId={} docuType={}",
	                    thumb.getApplicationId(), thumb.getDocuId(), thumb.getDocuType());
	            continue;
	        }
	        String b64 = Base64.getEncoder().encodeToString(bytes);
	        String uri = "data:" + thumb.getMimeType() + ";base64," + b64;
	        String key = thumb.getApplicationId() + "|" + thumb.getDocuId() + "|" + thumb.getDocuType();
	        result.put(key, uri);
	    }

	    return result;
	}

	private String resolveOperationType(DmsDocumentRequest apiRequest) {
		if (apiRequest == null
				|| apiRequest.getApiRequest() == null
				|| apiRequest.getApiRequest().getOperationType() == null) {
			return "";
		}
		return apiRequest.getApiRequest().getOperationType().trim().toLowerCase();
	}

	private String resolveType(DmsRequestObj requestObj) {
		String type = (requestObj == null) ? null : requestObj.getType();
		return (type == null) ? "" : type.trim().toLowerCase();
	}

	/**
	 * subType ko validate + normalize karta hai. null/blank/too-long ya whitelist
	 * ke bahar ho to Optional.empty(). Allowed list config-driven hai
	 * (dmsAPI.properties), isliye naya subType aane pe code touch nahi karna.
	 */
	private Optional<String> resolveValidSubType(String rawSubType) {
		if (rawSubType == null || rawSubType.isBlank()) {
			return Optional.empty();
		}
		String normalized = rawSubType.trim().toUpperCase();
		if (normalized.length() > SUBTYPE_MAX_LEN) {
			LOG.warn("subType exceeds max length, skipping thumbnail persist");
			return Optional.empty();
		}
		if (!ALLOWED_SUBTYPES.isEmpty() && !ALLOWED_SUBTYPES.contains(normalized)) {
			LOG.warn("subType '{}' not in allowed list, skipping thumbnail persist", normalized);
			return Optional.empty();
		}
		return Optional.of(normalized);
	}

	public Mono<List<ResponseWrapper>> handleUploadOperation(
			DmsDocumentRequest apiRequest,
			Header header) {

		List<DmsDocumenRequestFields> requestObjList =
				apiRequest.getApiRequest().getRequestObj().getDocList();

		if (requestObjList == null || requestObjList.isEmpty()) {
			LOG.error("No request objects received for DMS upload");
			return Mono.just(new ArrayList<>(List.of(buildFailureWrapper())));
		}

		Mono<List<ResponseWrapper>> chain = Mono.just(new ArrayList<>());

		for (DmsDocumenRequestFields requestObj : requestObjList) {

			Header perItemHeader = copyHeader(header);

			chain = chain.flatMap(acc ->
					handleSingleUpload(
							requestObj,
							perItemHeader,
							apiRequest,
							apiRequest.getApiRequest().getRequestObj()
					).map(resp -> {
						acc.add(resp);
						return acc;
					})
			);
		}

		return chain;
	}

	private Mono<ResponseWrapper> handleSingleUpload(
			DmsDocumenRequestFields requestObj,
			Header header,
			DmsDocumentRequest apiRequest,
			DmsRequestObj dmsRequestObj) {
		try {
			Date today = new Date();
			DmsRequest sessionRequest = buildDmsRequest(apiRequest);
			UploadDmsDocumentRequest dmsDocumentRequest = new UploadDmsDocumentRequest();
			UploadDmsDocumenRequestFields requestFields = new UploadDmsDocumenRequestFields();

			String type = dmsRequestObj.getType();
			String id = null;

			requestFields.setFileData(requestObj.getFileData());
			requestFields.setDocumentName(requestObj.getDocumentName());
			requestFields.setFileType(requestObj.getFileType());

			dmsDocumentRequest.setRequestObj(requestFields);

			String applicationId = dmsRequestObj.getApplicationId();

			// Validate and check existing folderIndex based on type
			Optional<String> existingFolderIndex = Optional.empty();

			switch (type == null ? "" : type.toLowerCase()) {
				case "customer": {
					if (applicationId == null || applicationId.isEmpty()) {
						LOG.error("Empty or null applicationId in the request");
						return Mono.just(buildFailureWrapper("Invalid applicationId"));
					}
					Optional<TbObApplicationMaster> appMasterOpt = tbObApplicationMasterRepo.findByApplicationId(applicationId);
					if (appMasterOpt.isEmpty()) {
						LOG.error("ApplicationId not found in the database: {}", applicationId);
						return Mono.just(buildFailureWrapper("Invalid applicationId"));
					}
					TbObApplicationMaster appMaster = appMasterOpt.get();
					existingFolderIndex = Optional.ofNullable(appMaster.getDmsFolderIdx())
							.filter(idx -> !idx.trim().isEmpty());
					// customerId needed for folder generation if folderIndex absent
					id = appMaster.getCustomerId(); // <-- pick customerId from applicationMaster
					break;
				}

				case "kendra": {
					if (applicationId == null || applicationId.isEmpty()) {
						LOG.error("Empty or null kendraId in the request");
						return Mono.just(buildFailureWrapper("Invalid kendraId"));
					}
					String kendraId = applicationId; // frontend now sends kendraId in applicationId
					Optional<TbObKendra> kendraOpt = kendradetails.findByKendraId(kendraId);
					if (kendraOpt.isEmpty()) {
						LOG.error("KendraId not found in kendra table: {}", kendraId);
						return Mono.just(buildFailureWrapper("Invalid kendraId"));
					}
					existingFolderIndex = kendraOpt
							.map(TbObKendra::getDmsFolderIdx)
							.filter(idx -> idx != null && !idx.trim().isEmpty());
					id = kendraId;
					break;
				}

				case "group": {
					if (applicationId == null || applicationId.isEmpty()) {
						LOG.error("Empty or null groupId in the request");
						return Mono.just(buildFailureWrapper("Invalid groupId"));
					}
					String groupId = applicationId; // frontend now sends groupId in applicationId
					Optional<TbObGroup> groupOpt = groupDetails.findByGroupId(groupId);
					if (groupOpt.isEmpty()) {
						LOG.error("GroupId not found in group table: {}", groupId);
						return Mono.just(buildFailureWrapper("Invalid groupId"));
					}
					existingFolderIndex = groupOpt
							.map(TbObGroup::getDmsFolderIdx)
							.filter(idx -> idx != null && !idx.trim().isEmpty());
					id = groupId;
					break;
				}
				default:
					LOG.error("Invalid type: {}", type);
					return Mono.just(buildFailureWrapper("Invalid type"));
			}

			final String finalId = id;
			final String finalApplicationId = applicationId;

			// If folderIndex already exists, reuse it directly
			if (existingFolderIndex.isPresent()) {
				String folderIndex = existingFolderIndex.get();
				LOG.info("Using existing folder index for type: {}, id: {}, index: {}", type, id, folderIndex);
				dmsDocumentRequest.getRequestObj().setFolderIndex(folderIndex);

				header.setInterfaceId(DMS_DOCUMENT_UPLOAD);
				Mono<Object> apiResponse = interfaceAdapter.callExternalService(header, dmsDocumentRequest,
						DMS_DOCUMENT_UPLOAD, true);
				return adapterUtil.generateRespWrapper(apiResponse, DMS_DOCUMENT_UPLOAD, header, true)
						.map(uploadResponse -> {
							persistUploadResponse(uploadResponse, requestObj, header, type, applicationId, dmsRequestObj);
							return uploadResponse;
						}).onErrorResume(e -> {
							LOG.error("exception at upload DMS Document Api", e);
							return Mono.just(buildFailureWrapper("DMS document upload failed, please re-trigger the upload"));
						});
			}
			Optional<String> recheckFolder = getExistingFolderIndex(type.toLowerCase(),
					"customer".equalsIgnoreCase(type) ? finalApplicationId : finalId);
			if (recheckFolder.isPresent()) {
				LOG.warn("Duplicate avoided — folderIndex found on re-check for type={}, id={}, index={}",
						type, "customer".equalsIgnoreCase(type) ? finalApplicationId : finalId, recheckFolder.get());
				dmsDocumentRequest.getRequestObj().setFolderIndex(recheckFolder.get());
				header.setInterfaceId(DMS_DOCUMENT_UPLOAD);
				Mono<Object> apiResponse = interfaceAdapter.callExternalService(header, dmsDocumentRequest,
						DMS_DOCUMENT_UPLOAD, true);
				return adapterUtil.generateRespWrapper(apiResponse, DMS_DOCUMENT_UPLOAD, header, true)
						.map(uploadResponse -> {
							persistUploadResponse(uploadResponse, requestObj, header, type, applicationId, dmsRequestObj);
							return uploadResponse;
						}).onErrorResume(e -> {
							LOG.error("exception at upload DMS Document Api", e);
							return Mono.just(buildFailureWrapper("DMS document upload failed, please re-trigger the upload"));
						});
			}
			LOG.info("No folderIndex found even on re-check, proceeding to generate new folder for type={}, id={}", type, finalId);
			return getDMSSessionId(today, header, sessionRequest).flatMap(sessionWrapper -> {

				String userDBId = extractResponseObj(sessionWrapper);
				if (userDBId == null || userDBId.isBlank()) {
					LOG.error("Unable to fetch DMS session id / userDBId");
					return Mono.just(buildFailureWrapper("DMS SessionId generation failed, please re-trigger the upload"));
				}
				LOG.debug("userDBId resolved : {}", userDBId);

				return generateFolderIndex(header, type, finalId, userDBId, lDMSProperties, apiRequest).flatMap(folderIndex -> {

					if (folderIndex == null || folderIndex.isBlank()) {
						LOG.error("Unable to generate folder index");
						return Mono.just(buildFailureWrapper("DMS FolderIndex generation failed, please re-trigger the upload"));
					}
					LOG.debug("Generated folderIndex : {}", folderIndex);
					LOG.info("Persisting newly generated folderIndex={} for type={}, id={}", folderIndex, type,
							"customer".equalsIgnoreCase(type) ? finalApplicationId : finalId);
					updateDmsFolderIndex(
							type,
							"customer".equalsIgnoreCase(type) ? finalApplicationId : finalId,
							folderIndex);
					dmsDocumentRequest.getRequestObj().setFolderIndex(folderIndex);

					header.setInterfaceId(DMS_DOCUMENT_UPLOAD);
					Mono<Object> apiResponse = interfaceAdapter.callExternalService(header, dmsDocumentRequest,
							DMS_DOCUMENT_UPLOAD, true);
					return adapterUtil.generateRespWrapper(apiResponse, DMS_DOCUMENT_UPLOAD, header, true)
							.map(uploadResponse -> {
								persistUploadResponse(uploadResponse, requestObj, header, type, applicationId, dmsRequestObj);
								return uploadResponse;
							});
				});
			}).onErrorResume(e -> {
				LOG.error("exception at upload DMS Document Api", e);
				String msg = e.getMessage() != null && e.getMessage().contains("Timeout")
						? "DMS server timeout, please re-trigger the upload"
						: "DMS FolderIndex/SessionId issue, please re-trigger the upload";
				return Mono.just(buildFailureWrapper(msg));
			});

		} catch (Exception e) {
			LOG.error("exception at upload DMS Document Api", e);
			return Mono.just(buildFailureWrapper("DMS upload failed, please re-trigger the upload"));
		}
	}

	public Mono<ResponseWrapper> uploadDocumentForRetry(
			DmsDocumenRequestFields requestObj,
			Header header,
			DmsDocumentRequest apiRequest,
			DmsRequestObj dmsRequestObj) {

		return handleSingleUpload(requestObj, header, apiRequest, dmsRequestObj
		);
	}

	private Mono<ResponseWrapper> handleFetchOperation(DmsDocumentRequest requFields, Header header) {
		try {
			FetchDmsDocumentRequest apiRequest = new FetchDmsDocumentRequest();
			FetchDmsDocumenRequestFields requestObj = new FetchDmsDocumenRequestFields();
			apiRequest.setAppId(requFields.getApiRequest().getAppId());
			apiRequest.setInterfaceName(requFields.getApiRequest().getInterfaceName());
			apiRequest.setUserId(requFields.getApiRequest().getUserId());
			requestObj.setDocIndex(requFields.getApiRequest().getRequestObj().getDocList().get(0).getDocIndex());
			apiRequest.setRequestObj(requestObj);
			return fetchDocumentByType(apiRequest, header);
		} catch (Exception e) {
			LOG.error("Exception in handleFetchOperation", e);
			return Mono.just(buildFailureWrapper());
		}
	}



	private Mono<ResponseWrapper> fetchDocumentByType(FetchDmsDocumentRequest apiRequest, Header header) {
		LOG.debug("Printing FetchDmsDocumentRequest: {}", apiRequest);
		try {
			header.setInterfaceId(DMS_DOCUMENT_FETCH);
			Mono<Object> apiResponse = interfaceAdapter.callExternalService(header, apiRequest, DMS_DOCUMENT_FETCH,
					true);
			return adapterUtil.generateRespWrapper(apiResponse, DMS_DOCUMENT_FETCH, header, true);
		} catch (Exception e) {
			LOG.error("exception at Fetch DMD Document Api", e);
			Response response = new Response();
			ResponseHeader respHeader = new ResponseHeader();
			ResponseBody respBody = new ResponseBody();
			respBody.setResponseObj(EXCEPTION_MSG);
			CommonUtils.generateHeaderForFailure(respHeader, EXCEPTION_OCCURED);
			response.setResponseBody(respBody);
			response.setResponseHeader(respHeader);
			ResponseWrapper resWrapper = new ResponseWrapper();
			resWrapper.setApiResponse(response);
			return Mono.just(resWrapper);
		}
	}

	private Mono<ResponseWrapper> handleFetchAllOperation(DmsDocumentRequest apiRequest, Header header) {
		try {
			DmsDocumenRequestFields reqFields =
					apiRequest.getApiRequest().getRequestObj().getDocList().get(0);

			String type = resolveType(apiRequest.getApiRequest().getRequestObj());
			String applicationId = apiRequest.getApiRequest().getRequestObj().getApplicationId();

			if (applicationId == null || applicationId.isEmpty()) {
				LOG.error("Empty or null applicationId in fetchAll request");
				return Mono.just(buildFailureWrapper("Invalid applicationId"));
			}

			String id;
			if (TYPE_CUSTOMER.equalsIgnoreCase(type)) {
				id = applicationId; // fetchAllDocumentByType uses applicationId for customer
			} else if (TYPE_KENDRA.equalsIgnoreCase(type) || TYPE_GROUP.equalsIgnoreCase(type)) {
				id = applicationId; // frontend now sends kendraId/groupId directly
				if (id == null || id.isEmpty()) {
					LOG.error("Empty or null id for type: {}", type);
					return Mono.just(buildFailureWrapper("Invalid " + type + " id"));
				}
			} else {
				LOG.error("Invalid type: {}", type);
				return Mono.just(buildFailureWrapper(INVALIDAPP_MSG));
			}

			return fetchAllDocumentByType(type, id, reqFields, header);

		} catch (Exception e) {
			LOG.error("Exception in handleFetchAllOperation", e);
			return Mono.just(buildFailureWrapper());
		}
	}

	private Mono<ResponseWrapper> fetchAllDocumentByType(String type, String id, DmsDocumenRequestFields reqFields,
			Header header) {
		try {
			LOG.info("Fetching all documents for type: {}, id: {}", type, id);
			List<?> documentList;
			if (TYPE_CUSTOMER.equalsIgnoreCase(type)) {
				documentList = tbObDocumentRepository.findByApplicationIdOrderByDocuId(id);
			} else if (TYPE_KENDRA.equalsIgnoreCase(type) || TYPE_GROUP.equalsIgnoreCase(type)) {
				documentList = tbObOtherDocumentRepository.findByOtherId(id);
			} else {
				LOG.error("Invalid type: {}", type);
				return Mono.just(buildFailureWrapper(INVALIDAPP_MSG));
			}
			if (documentList == null || documentList.isEmpty()) {
				return Mono.just(buildSuccessWrapper("[]"));
			}
			List<String> docIndices = extractDocumentIndices(documentList);
			if (docIndices.isEmpty()) {
				return Mono.just(buildSuccessWrapper("[]"));
			}
			return fetchAllDocumentsFromDMS(docIndices, header).map(this::buildSuccessWrapperWithList)
					.onErrorResume(e -> {
						LOG.error("Error fetching documents from DMS: {}", e.getMessage(), e);
						return Mono.just(buildFailureWrapper("Error fetching documents: " + e.getMessage()));
					});
		} catch (Exception e) {
			LOG.error("Exception in fetchAllDocumentByType for type: {}, id: {}", type, id, e);
			return Mono.just(buildFailureWrapper());
		}
	}

	private List<String> extractDocumentIndices(List<?> documentList) {
		return documentList.stream().flatMap(doc -> {
			List<String> indices = new ArrayList<>();
			if (doc instanceof TbObDocument) {
				TbObDocument tbDoc = (TbObDocument) doc;
				if (tbDoc.getDmsDocIdFront() != null && !tbDoc.getDmsDocIdFront().isBlank()) {
					indices.add(tbDoc.getDmsDocIdFront());
				}
				if (tbDoc.getDmsDocIdBack() != null && !tbDoc.getDmsDocIdBack().isBlank()) {
					indices.add(tbDoc.getDmsDocIdBack());
				}
			} else if (doc instanceof TbObOtherDocument) {
				TbObOtherDocument otherDoc = (TbObOtherDocument) doc;
				if (otherDoc.getDmsDocIdFront() != null && !otherDoc.getDmsDocIdFront().isBlank()) {
					indices.add(otherDoc.getDmsDocIdFront());
				}
				if (otherDoc.getDmsDocIdBack() != null && !otherDoc.getDmsDocIdBack().isBlank()) {
					indices.add(otherDoc.getDmsDocIdBack());
				}
			}
			return indices.stream();
		}).collect(Collectors.toList());
	}

	private Mono<List<Object>> fetchAllDocumentsFromDMS(List<String> docIndices, Header header) {

		List<Mono<ResponseWrapper>> fetchMonos = docIndices.stream().map(docIndex -> {
			FetchDmsDocumentRequest apiRequest = new FetchDmsDocumentRequest();
			FetchDmsDocumenRequestFields requestObj = new FetchDmsDocumenRequestFields();
			requestObj.setDocIndex(docIndex);
			apiRequest.setRequestObj(requestObj);

			return fetchDocumentByType(apiRequest, copyHeader(header)).onErrorResume(e -> {
				LOG.warn("Error fetching document with index: {}", docIndex);
				return Mono.just(buildFailureWrapper("Error fetching document: " + docIndex));
			});
		}).collect(Collectors.toList());

		return Mono.zip(fetchMonos, results -> Arrays.stream(results).map(result -> {
			if (result instanceof ResponseWrapper) {
				ResponseWrapper wrapper = (ResponseWrapper) result;
				return (Object) wrapper.getApiResponse().getResponseBody().getResponseObj();
			}
			return null;
		}).collect(Collectors.toList())).onErrorResume(e -> Mono.just(new ArrayList<Object>()));
	}

	// ------------------------------------------------------------------
	// Response wrapper builders for lists
	// ------------------------------------------------------------------

	/** Multiple upload responses ko ek success wrapper (JSON array) me convert karta hai. */
	private ResponseWrapper buildUploadListWrapper(List<ResponseWrapper> wrappers) {
		boolean anyFailure = wrappers.stream().anyMatch(w -> {
			if (w == null || w.getApiResponse() == null
					|| w.getApiResponse().getResponseHeader() == null) return true;
			String code = w.getApiResponse().getResponseHeader().getResponseCode();
			return !"0".equals(code);
		});

		List<Object> responseObjs = wrappers.stream()
				.map(w -> (Object) extractResponseObj(w))
				.collect(Collectors.toList());
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		String jsonArray = new Gson().toJson(responseObjs);
		responseBody.setResponseObj(jsonArray);

		if (anyFailure) {
			CommonUtils.generateHeaderForFailure(responseHeader, EXCEPTION_OCCURED);
		} else {
			CommonUtils.generateHeaderForSuccess(responseHeader);
		}

		response.setResponseBody(responseBody);
		response.setResponseHeader(responseHeader);
		ResponseWrapper responseWrapper = new ResponseWrapper();
		responseWrapper.setApiResponse(response);
		return responseWrapper;
	}

	private ResponseWrapper buildSuccessWrapperWithList(List<Object> responseList) {
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		String jsonArray = new Gson().toJson(responseList);
		responseBody.setResponseObj(jsonArray);
		CommonUtils.generateHeaderForSuccess(responseHeader);
		response.setResponseBody(responseBody);
		response.setResponseHeader(responseHeader);
		ResponseWrapper responseWrapper = new ResponseWrapper();
		responseWrapper.setApiResponse(response);
		return responseWrapper;
	}

	public Mono<ResponseWrapper> getDMSSessionId(Date today, Header header, DmsRequest requestWrapper) {
		Optional<TbObDMSSessionDataEntity> sessionData = tbObDMSSessionDataRepo.getSessionData(today);
		if (sessionData.isPresent()) {
			String sessionId = sessionData.get().getSessionId();
			LOG.info("Session Id found in DB : {} (picked latest for date={})", sessionId, today);
			return Mono.just(buildSuccessWrapper(sessionId));
		}
	    LOG.debug("Session not found in DB. Calling DMS_SESSION API");
	    return dmsSessionAPIReactive(header, requestWrapper).map(dmsSessionResponse -> {
	        LOG.debug("DMS_SESSION API execution completed");
	        String sessionId = "";
	        if (dmsSessionResponse != null) {
	            JSONObject connectCabinetOutput = Optional.ofNullable(dmsSessionResponse.optJSONObject("NGOExecuteAPIResponseBDO"))
	                    .map(obj -> obj.optJSONObject("outputData"))
	                    .map(obj -> obj.optJSONObject("NGOConnectCabinet_Output"))
	                    .orElse(null);
	            if (connectCabinetOutput != null && "0".equals(connectCabinetOutput.optString("Status"))) {
	                sessionId = connectCabinetOutput.optString("UserDBId", "");
	            }
	            LOG.debug("Session Id received from API : {}", sessionId);
	        }
	        if (!sessionId.isBlank()) {
	            try {
	                TbObDMSSessionDataEntity sessionEntity = new TbObDMSSessionDataEntity(today, sessionId);
	                tbObDMSSessionDataRepo.save(sessionEntity);
	                LOG.debug("Session Id saved in DB successfully");
	            } catch (Exception e) {
	                LOG.error("Failed to save session id in DB", e);
	            }
	        } else {
	            LOG.debug("Session Id is not present");
	        }
	        return buildSuccessWrapper(sessionId);
	    }).onErrorResume(e -> {
	        LOG.error("Exception while calling DMS_SESSION API", e);
	        ResponseWrapper responseWrapper = new ResponseWrapper();
	        Response response = new Response();
	        ResponseHeader responseHeader = new ResponseHeader();
	        ResponseBody responseBody = new ResponseBody();
	        CommonUtils.generateHeaderForFailure(responseHeader, EXCEPTION_OCCURED);
	        responseBody.setResponseObj("");
	        response.setResponseBody(responseBody);
	        response.setResponseHeader(responseHeader);
	        responseWrapper.setApiResponse(response);
	        return Mono.just(responseWrapper);
	    });
	}

	private Mono<JSONObject> dmsSessionAPIReactive(Header header, DmsRequest requestWrapper) {
		try {
			String decryptedPassword;
			try {
				decryptedPassword = AESDecryptionUtil.decrypt(lDMSProperties.get("CONNECT_CABINET_USER_PASSWORD"),
						lDMSProperties.get("AES_SECRET_KEY"));
			} catch (Exception e) {
				LOG.error("Password decryption failed", e);
				return Mono.error(new RuntimeException("Unable to decrypt password", e));
			}
			NGOConnectCabinetInput connectCabinetInput = NGOConnectCabinetInput.builder()
					.option(lDMSProperties.get("CONNECT_CABINET_OPTION"))
					.userExist(lDMSProperties.get("CONNECT_CABINET_IS_USER_EXIST"))
					.cabinetName(lDMSProperties.get("CONNECT_CABINET_CABINET_NAME"))
					.userName(lDMSProperties.get("CONNECT_CABINET_USER_NAME")).userPassword(decryptedPassword)
					.locale(lDMSProperties.get("CONNECT_CABINET_LOCALE")).build();
			InputData inputData = InputData.builder().ngoConnectCabinetInput(connectCabinetInput).build();
			NGOExecuteAPIBDO ngoExecuteAPIBDO = NGOExecuteAPIBDO.builder().inputData(inputData).base64Encoded("N")
					.locale(lDMSProperties.get("CONNECT_CABINET_LOCALE")).build();
			NGOExecuteAPIBDORequest requestObj = NGOExecuteAPIBDORequest.builder().ngoExecuteAPIBDO(ngoExecuteAPIBDO)
					.build();
			requestWrapper.setRequestObj(requestObj);
			LOG.debug("DMS Session API Request : {}", requestWrapper);
			return callExternalServiceAsJson(header, requestWrapper, DMS_SESSION_INTERFACEID, true);
		} catch (Exception e) {
			LOG.error("Exception while preparing DMS Session request", e);
			return Mono.error(e);
		}
	}

	private Header copyHeader(Header header) {
		if (header == null) {
			return new Header();
		}
		return new Gson().fromJson(new Gson().toJson(header), Header.class);
	}

	private Mono<ResponseWrapper> callExternalServiceAsResponseWrapper(Header header, Object requestObj,
			String interfaceId, boolean isReactive) {
		try {
			Header headerCopy = copyHeader(header);
			headerCopy.setInterfaceId(interfaceId);
			LOG.debug("Calling external service {} with header: {}", interfaceId, headerCopy);

			Mono<Object> responseMono = interfaceAdapter.callExternalService(headerCopy, requestObj, interfaceId,
					isReactive);

			return adapterUtil.generateRespWrapper(responseMono, interfaceId, headerCopy, isReactive).doOnError(e -> LOG
					.error("Error while generating response wrapper for {}: {}", interfaceId, e.getMessage(), e));
		} catch (Exception e) {
			LOG.error("Exception while calling {} API: {}", interfaceId, e.getMessage(), e);
			return Mono.just(buildFailureWrapper());
		}
	}

	private Mono<JSONObject> callExternalServiceAsJson(Header header, Object requestObj, String interfaceId,
	        boolean isReactive) {
	    return callExternalServiceAsResponseWrapper(header, requestObj, interfaceId, isReactive)
	            .map(responseWrapper -> {
	                try {
	                    Object respObj = Optional.ofNullable(responseWrapper).map(ResponseWrapper::getApiResponse)
	                            .map(Response::getResponseBody).map(ResponseBody::getResponseObj).orElse(null);
	                    if (respObj == null) {
	                        return new JSONObject();
	                    }
	                    String respString = (respObj instanceof String)
	                            ? (String) respObj
	                            : new Gson().toJson(respObj);
	                    return new JSONObject(respString);
	                } catch (Exception ex) {
	                    LOG.error("Failed to convert response to JSONObject for interface {}: {}", interfaceId,
	                            ex.getMessage(), ex);
	                    return new JSONObject();
	                }
	            });
	}

	public Mono<String> generateFolderIndex(Header header, String type, String t24Id, String userDBId,
			Map<String, String> lDMSProperties, DmsDocumentRequest apiRequest) {

		DmsRequest folderRequest =  buildDmsRequest(apiRequest);
		String parentFolderKey;
		switch (type == null ? "" : type.toLowerCase()) {
		case "customer":
			parentFolderKey = "ADD_FOLDER_PARENT_FOLDER_INDEX_CUSTOMER";
			break;
		case "kendra":
			parentFolderKey = "ADD_FOLDER_PARENT_FOLDER_INDEX_KENDRA";
			break;
		case "group":
			parentFolderKey = "ADD_FOLDER_PARENT_FOLDER_INDEX_GROUP";
			break;
		default:
			parentFolderKey = "";
		}

		String parentFolderIndex = lDMSProperties.get(parentFolderKey);

		String creationDateTime = lDMSProperties.get("ADD_FOLDER_CREATION_DATE_TIME");
		if (creationDateTime == null || creationDateTime.trim().isEmpty()) {
			creationDateTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
		}

		Folder folder = Folder.builder().parentFolderIndex(parentFolderIndex).folderName(t24Id)
				.creationDateTime(creationDateTime).accessType(lDMSProperties.get("ADD_FOLDER_ACCESS_TYPE"))
				.imageVolumeIndex(lDMSProperties.get("ADD_FOLDER_IMAGE_VOLUME_INDEX"))
				.folderType(lDMSProperties.get("ADD_FOLDER_FOLDER_TYPE"))
				.location(lDMSProperties.get("ADD_FOLDER_LOCATION")).comment(lDMSProperties.get("ADD_FOLDER_COMMENT"))
				.owner(lDMSProperties.get("ADD_FOLDER_OWNER")).build();

		NGOAddFolderInput addFolderInput = NGOAddFolderInput.builder().option(lDMSProperties.get("ADD_FOLDER_OPTION"))
				.cabinetName(lDMSProperties.get("ADD_FOLDER_CABINET_NAME")).userDBId(userDBId).folder(folder).build();

		InputData inputData = InputData.builder().ngoAddFolderInput(addFolderInput).build();
		NGOExecuteAPIBDO ngoExecute = NGOExecuteAPIBDO.builder().inputData(inputData).build();
		NGOExecuteAPIBDORequest requestObj = NGOExecuteAPIBDORequest.builder().ngoExecuteAPIBDO(ngoExecute).build();

		folderRequest.setRequestObj(requestObj);

		return generateFolderIndex(folderRequest, header);
	}

	private Mono<String> generateFolderIndex(DmsRequest folderRequest, Header header) {
	    return addFolderAPI(folderRequest, header).flatMap(responseWrapper -> {
	        JSONObject responseJson = new JSONObject(
	                responseWrapper.getApiResponse().getResponseBody().getResponseObj().toString());

	        JSONObject folderOutput = Optional.ofNullable(responseJson.optJSONObject("NGOExecuteAPIResponseBDO"))
	                .map(obj -> obj.optJSONObject("outputData")).map(obj -> obj.optJSONObject("NGOAddFolder_Output"))
	                .orElse(null);

	        if (folderOutput == null) {
	            LOG.error("No NGOAddFolder_Output in response: {}", responseJson);
	            return Mono.just("");
	        }

	        String status = folderOutput.optString("Status");

	        if ("0".equals(status)) {
	            return Mono.just(Optional.ofNullable(folderOutput.optJSONObject("Folder"))
	                    .map(obj -> obj.optString("FolderIndex", "")).orElse(""));
	        }

	        if (INVALID_SESSION_STATUS.equals(status)) {
	            LOG.warn("DMS session invalid (Status {}). Regenerating session and retrying add-folder once.", status);
	            return refreshDmsSessionAndRetryAddFolder(folderRequest, header);
	        }

	        LOG.error("Add Folder API failed. Status: {}, Error: {}", status, folderOutput.optString("Error"));
	        return Mono.just("");
	    });
	}

	private Mono<String> refreshDmsSessionAndRetryAddFolder(DmsRequest folderRequest, Header header) {
	    DmsRequest sessionRequest = DmsRequest.builder()
	            .interfaceName(folderRequest.getInterfaceName())
	            .appId(folderRequest.getAppId())
	            .userId(folderRequest.getUserId())
	            .build();

	    return dmsSessionAPIReactive(header, sessionRequest).flatMap(dmsSessionResponse -> {
	        String newUserDBId = "";
	        if (dmsSessionResponse != null) {
	            JSONObject connectCabinetOutput = Optional.ofNullable(dmsSessionResponse.optJSONObject("NGOExecuteAPIResponseBDO"))
	                    .map(obj -> obj.optJSONObject("outputData"))
	                    .map(obj -> obj.optJSONObject("NGOConnectCabinet_Output"))
	                    .orElse(null);
	            if (connectCabinetOutput != null && "0".equals(connectCabinetOutput.optString("Status"))) {
	                newUserDBId = connectCabinetOutput.optString("UserDBId", "");
	            }
	        }

	        if (newUserDBId.isBlank()) {
	            LOG.error("Unable to regenerate DMS session after invalid-session error");
	            return Mono.just("");
	        }

	        persistRefreshedSession(newUserDBId);

	        // patch the stale userDBId inside the already-built folder request, then retry once
	        folderRequest.getRequestObj().getNgoExecuteAPIBDO().getInputData().getNgoAddFolderInput()
	                .setUserDBId(newUserDBId);

	        return addFolderAPI(folderRequest, header).map(retryWrapper -> {
	            JSONObject retryJson = new JSONObject(
	                    retryWrapper.getApiResponse().getResponseBody().getResponseObj().toString());
	            JSONObject retryFolderOutput = Optional.ofNullable(retryJson.optJSONObject("NGOExecuteAPIResponseBDO"))
	                    .map(obj -> obj.optJSONObject("outputData")).map(obj -> obj.optJSONObject("NGOAddFolder_Output"))
	                    .orElse(null);

	            if (retryFolderOutput != null && "0".equals(retryFolderOutput.optString("Status"))) {
	                return Optional.ofNullable(retryFolderOutput.optJSONObject("Folder"))
	                        .map(obj -> obj.optString("FolderIndex", "")).orElse("");
	            }
	            LOG.error("Add Folder API failed again even after session refresh: {}",
	                    retryFolderOutput != null ? retryFolderOutput.optString("Error") : "no response");
	            return "";
	        });
	    }).onErrorResume(e -> {
	        LOG.error("Exception while regenerating DMS session after invalid-session error", e);
	        return Mono.just("");
	    });
	}

	private void persistRefreshedSession(String sessionId) {
	    try {
	        Date today = new Date();
	        TbObDMSSessionDataEntity sessionEntity = tbObDMSSessionDataRepo.getSessionData(today)
	                .orElse(new TbObDMSSessionDataEntity(today, sessionId));
	        sessionEntity.setSessionId(sessionId);
	        tbObDMSSessionDataRepo.save(sessionEntity);
	        LOG.debug("Refreshed DMS session id persisted in DB: {}", sessionId);
	    } catch (Exception e) {
	        LOG.error("Failed to persist refreshed DMS session id in DB", e);
	    }
	}

	private Mono<ResponseWrapper> addFolderAPI(DmsRequest dmsRequest, Header header) {
		LOG.debug("ADD Folder API :: Request object : {}", dmsRequest);
		return callExternalServiceAsResponseWrapper(header, dmsRequest, DMS_FOLDER_INDEX_INTERFACEID, true)
				.doOnNext(response -> LOG.debug("ADD Folder API :: Response : {}", response));
	}

	// ------------------------------------------------------------------
	// Persistence
	// ------------------------------------------------------------------

	private void persistUploadResponse(
			ResponseWrapper uploadResponse,
			DmsDocumenRequestFields reqFields,
			Header header,
			String type,
			String applicationId,
			DmsRequestObj dmsRequestObj){
		try {
			type = resolveType(dmsRequestObj);
			if (applicationId == null || applicationId.isEmpty()) {
				LOG.error("Empty or null applicationId, skipping thumbnail persist");
				return;
			}
			if (!TYPE_CUSTOMER.equalsIgnoreCase(type)) {
				LOG.info("Skipping thumbnail persist, type is not customer: {}", type);
				return;
			}

			Optional<String> subTypeOpt = resolveValidSubType(reqFields.getSubType());

			if (subTypeOpt.isEmpty()) {
				LOG.info("Skipping thumbnail persist, invalid/unsupported subType: {}", reqFields.getSubType());
				return;
			}

			String responseObj = String.valueOf(
					uploadResponse.getApiResponse()
							.getResponseBody()
							.getResponseObj());

			JSONObject responseJson = new JSONObject(responseObj);

			String documentIndex = extractDocumentIndex(responseJson);

			if (documentIndex == null || documentIndex.isBlank()) {
				return;
			}

			persistPhotoThumbnail(
					documentIndex,
					subTypeOpt.get(),
					reqFields,
					header,
					applicationId);

		} catch (Exception e) {
			LOG.error("Error while persisting photo thumbnail", e);
		}
	}

	/** Safely pulls documentIndex out of the upload response JSON. */
	private String extractDocumentIndex(JSONObject responseJson) {
		String documentIndex = "";
		try {
			if (responseJson != null && responseJson.has("NGOAddDocumentResponseBDO")) {
				JSONObject addResp = responseJson.optJSONObject("NGOAddDocumentResponseBDO");
				if (addResp != null && addResp.has("NGOGetDocListDocDataBDO")) {
					JSONObject docData = addResp.optJSONObject("NGOGetDocListDocDataBDO");
					if (docData != null) {
						documentIndex = docData.optString("documentIndex", "");
					}
				}
			}

			if (documentIndex == null || documentIndex.isBlank()) {
				LOG.error("documentIndex is blank or missing in upload response JSON: {}", responseJson);
			}
		} catch (Exception e) {
			LOG.error("Error while extracting documentIndex from response JSON: {}", responseJson, e);
		}
		return documentIndex;
	}

	private void persistPhotoThumbnail(
			String documentIndex,
			String subType,
			DmsDocumenRequestFields reqFields,
			Header header,
			String applicationId)  {
		try {

			byte[] thumbBytes = generateThumbnail(reqFields.getFileData());

			TbObPhotoThumbnailId id = TbObPhotoThumbnailId.builder().applicationId(applicationId).docuId(documentIndex)
					.docuType(subType).build();

			TbObPhotoThumbnail thumbnail = tbObPhotoThumbnailRepo.findById(id).orElse(new TbObPhotoThumbnail());

			thumbnail.setApplicationId(applicationId);
			thumbnail.setDocuId(documentIndex);
			thumbnail.setDocuType(subType);
			thumbnail.setCustomerId(parseLongSafe(
					tbObApplicationMasterRepo.findByApplicationId(applicationId)
							.map(TbObApplicationMaster::getCustomerId)
							.orElse(null)));
			thumbnail.setMimeType(reqFields.getFileType());
			thumbnail.setWidth("96");
			thumbnail.setHeight("96");
			thumbnail.setThumbnail(thumbBytes);
			thumbnail.setFileSize(thumbBytes != null ? thumbBytes.length : null);

			tbObPhotoThumbnailRepo.save(thumbnail);
			LOG.info("Saved TbObPhotoThumbnail applicationId={} docuId={}", applicationId, documentIndex);
		} catch (Exception ex) {
			LOG.error("Error saving TbObPhotoThumbnail", ex);
		}
	}

	/** id numeric na ho to crash ke bajaye null return karta hai. */
	private Long parseLongSafe(String value) {
		try {
			return (value == null || value.isBlank()) ? null : Long.valueOf(value);
		} catch (NumberFormatException e) {
			LOG.error("Invalid numeric id format: {}", value);
			return null;
		}
	}

	private byte[] generateThumbnail(String bigImageBase64) throws IOException {
		if (bigImageBase64 == null || bigImageBase64.isBlank()) {
			return null;
		}
		byte[] fullBytes = Base64.getDecoder().decode(bigImageBase64);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Thumbnails.of(new ByteArrayInputStream(fullBytes)).size(96, 96).outputFormat("jpg").outputQuality(0.7)
				.toOutputStream(out);

		return out.toByteArray();
	}

	// ------------------------------------------------------------------
	// Common helpers
	// ------------------------------------------------------------------

	public String extractResponseObj(ResponseWrapper wrapper) {
		if (wrapper != null && wrapper.getApiResponse() != null && wrapper.getApiResponse().getResponseBody() != null
				&& wrapper.getApiResponse().getResponseBody().getResponseObj() != null) {
			return String.valueOf(wrapper.getApiResponse().getResponseBody().getResponseObj());
		}
		return "";
	}

	private ResponseWrapper buildSuccessWrapper(String responseObj) {
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		responseBody.setResponseObj(responseObj);
		CommonUtils.generateHeaderForSuccess(responseHeader);
		response.setResponseBody(responseBody);
		response.setResponseHeader(responseHeader);
		ResponseWrapper responseWrapper = new ResponseWrapper();
		responseWrapper.setApiResponse(response);
		return responseWrapper;
	}

	private ResponseWrapper buildFailureWrapper() {
		return buildFailureWrapper(EXCEPTION_MSG);
	}

	private ResponseWrapper buildFailureWrapper(String message) {
		Response response = new Response();
		ResponseHeader respHeader = new ResponseHeader();
		ResponseBody respBody = new ResponseBody();
		respBody.setResponseObj(message);
		CommonUtils.generateHeaderForFailure(respHeader, EXCEPTION_OCCURED);
		response.setResponseBody(respBody);
		response.setResponseHeader(respHeader);
		ResponseWrapper resWrapper = new ResponseWrapper();
		resWrapper.setApiResponse(response);
		return resWrapper;
	}

	public void updateDmsFolderIndex(String type, String id, String index) {
		try {
			if (type == null || type.trim().isEmpty() || id == null || id.trim().isEmpty() || index == null
					|| index.trim().isEmpty()) {
				LOG.error("Invalid input parameters: type={}, id={}, index={}", type, id, index);
				return;
			}

			switch (type.toLowerCase()) {
			case "customer":
				Optional<TbObApplicationMaster> customerOpt = tbObApplicationMasterRepo.findById(id);
				if (customerOpt.isPresent()) {
					TbObApplicationMaster customer = customerOpt.get();
					customer.setDmsFolderIdx(index);
					tbObApplicationMasterRepo.save(customer);
					LOG.info("DMS folder index updated successfully for customer ID: {}", id);
				} else {
					LOG.error("Customer not found with ID: {}", id);
				}
				break;

			case "kendra":
				try {
					Optional<TbObKendra> kendraOpt = kendradetails.findById(id);
					if (kendraOpt.isPresent()) {
						TbObKendra kendra = kendraOpt.get();
						kendra.setDmsFolderIdx(index);
						kendradetails.save(kendra);
						LOG.info("DMS folder index updated successfully for kendra ID: {}", id);
					} else {
						LOG.error("Kendra not found with ID: {}", id);
					}
				} catch (NumberFormatException e) {
					LOG.error("Invalid kendra ID format: {}", id);
				}
				break;

			case "group":
				try {
					Optional<TbObGroup> groupOpt = groupDetails.findById(id);
					if (groupOpt.isPresent()) {
						TbObGroup group = groupOpt.get();
						group.setDmsFolderIdx(index);
						groupDetails.save(group);
						LOG.info("DMS folder index updated successfully for group ID: {}", id);
					} else {
						LOG.error("Group not found with ID: {}", id);
					}
				} catch (NumberFormatException e) {
					LOG.error("Invalid group ID format: {}", id);
				}
				break;

			default:
				LOG.error("Invalid entity type: {}", type);
			}

		} catch (Exception e) {
			LOG.error("Exception occurred while updating DMS folder index for type: {}, id: {}, index: {}", type, id,
					index, e);
		}
	}

	private Optional<String> getExistingFolderIndex(String type, String id) {
		try {
			switch (type == null ? "" : type.toLowerCase()) {
			case "customer":
				return tbObApplicationMasterRepo.findById(id)
						.map(TbObApplicationMaster::getDmsFolderIdx)
						.filter(index -> index != null && !index.trim().isEmpty());

			case "kendra":
				try {
					return kendradetails.findById(id).map(TbObKendra::getDmsFolderIdx)
							.filter(index -> index != null && !index.trim().isEmpty());
				} catch (NumberFormatException e) {
					LOG.error("Invalid kendra ID format: {}", id);
					return Optional.empty();
				}

			case "group":
				try {
					return groupDetails.findById(id).map(TbObGroup::getDmsFolderIdx)
							.filter(index -> index != null && !index.trim().isEmpty());
				} catch (NumberFormatException e) {
					LOG.error("Invalid group ID format: {}", id);
					return Optional.empty();
				}

			default:
				LOG.error("Invalid entity type: {}", type);
				return Optional.empty();
			}
		} catch (Exception e) {
			LOG.error("Exception occurred while retrieving existing folder index for type: {}, id: {}", type, id, e);
			return Optional.empty();
		}
	}

	public DmsRequest buildDmsRequest(DmsDocumentRequest apiRequest) {
		return DmsRequest.builder()
				.interfaceName(apiRequest.getApiRequest().getInterfaceName())
				.appId(apiRequest.getApiRequest().getAppId())
				.userId(apiRequest.getApiRequest().getUserId())
				.build();
	}

	public static Map<String, String> getDmsProperties() {
		return lDMSProperties;
	}

}