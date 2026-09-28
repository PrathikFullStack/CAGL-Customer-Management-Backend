package com.iexceed.appzillonbanking.cagl.cob.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.constants.ApplicationConstants;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplnWorkflow;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.enums.AuditEventType;
import com.iexceed.appzillonbanking.cagl.cob.exception.ApplicationNotFoundException;
import com.iexceed.appzillonbanking.cagl.cob.exception.InvalidSubStageException;
import com.iexceed.appzillonbanking.cagl.cob.exception.RecordLockedException;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentDetail;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentListItem;
import com.iexceed.appzillonbanking.cagl.cob.payload.ApplicationCreateDtls;
import com.iexceed.appzillonbanking.cagl.cob.payload.CreateApplicationRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.CustomerCreateDtls;
import com.iexceed.appzillonbanking.cagl.cob.payload.KycDetailsCreate;
import com.iexceed.appzillonbanking.cagl.cob.payload.ApiResponse;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.service.*;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.FetchApplicationHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandlerContext;
import com.iexceed.appzillonbanking.cagl.cob.service.resolver.FetchApplicationHandlerResolver;
import com.iexceed.appzillonbanking.cagl.cob.service.resolver.SubStageHandlerResolver;
import com.iexceed.appzillonbanking.cagl.cob.utils.IdGeneratorUtil;
import com.iexceed.appzillonbanking.cagl.cob.utils.RequestValidationUtils;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private static final DateTimeFormatter DOB_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TbObApplicationMasterRepository applicationMasterRepository;
    private final TbObCustomerRepository customerRepository;
    private final TbObApplnWorkflowRepository workflowRepository;
    private final TbObRecordLockRepository recordLockRepository;
    private final ObjectMapper objectMapper;
    private final FetchApplicationHandlerResolver fetchApplicationHandlerResolver;
    private final SubStageHandlerResolver subStageHandlerResolver;
    private final AuditService auditService;
    private final MisReportService misReportService;
    private final TbAsmiUserRepository tbAsmiUserRepository;
    private final TbObApplicationMasterRepository tbObApplicationMasterRepository;
    private final TbObLeadRepository leadRepository;
    private final DuplicateValidationService duplicateValidationService;

    @Override
    @Transactional
    public ResponseWrapper createApplication(CreateApplicationRequest request) throws JsonProcessingException {
        ApiResponse lastResponse = null;

        List<String> validationErrors = RequestValidationUtils.validateApplicationRequest(request.getRequestObj());
        if (!validationErrors.isEmpty()) {
            log.warn("Application request validation failed: {}", validationErrors);
            return ResponseWrapper.builder().apiResponse(buildValidationFailureResponse(validationErrors)).build();
        }

        Response duplicateCheckResponse = duplicateValidationService.validateDatabaseDuplicate(
                request.getRequestObj().getApplicationdtls(),
                record -> record.getCustomerDtls().getKycDetails().getMobileNum(),
                DuplicateValidationService.DuplicateCheckScope.APPLICATION_CREATION);

        if (duplicateCheckResponse != null) {
            return ResponseWrapper.builder().apiResponse(duplicateCheckResponse).build();
        }

        for (ApplicationCreateDtls dtls : request.getRequestObj().getApplicationdtls()) {
            String applicationId = IdGeneratorUtil.generateApplicationId(request.getBranchId(), request.getUserId());
            LocalDateTime now = LocalDateTime.now();

            KycDetailsCreate kyc = Optional.ofNullable(dtls.getCustomerDtls())
                    .map(CustomerCreateDtls::getKycDetails)
                    .orElse(null);

            Map<String, Object> kycMap = new HashMap<>();
            if (kyc != null) {
                kycMap.put("mobileNum", kyc.getMobileNum());
                kycMap.put("altMobileNum", kyc.getAltMobileNum());
                kycMap.put("deviceType", kyc.getDeviceType());
                kycMap.put("lang", kyc.getLang());
                kycMap.put("gpsLat", kyc.getGpsLat());
                kycMap.put("gpsLong", kyc.getGpsLong());
            }

            Map<String, Object> locationDetailsMap = new HashMap<>();
            if (kyc != null) {
                locationDetailsMap.put("userRole", request.getUserRole());
                locationDetailsMap.put("lat", kyc.getGpsLat());
                locationDetailsMap.put("long", kyc.getGpsLong());
            }

            TbObCustomer customer = TbObCustomer.builder()
                    .applicationId(applicationId)
                    .livePhotoStatus(ApplicationConstants.PLACEHOLDER_NOT_CAPTURED)
                    .kycStatus(ApplicationConstants.PLACEHOLDER_NOT_CAPTURED)
                    .kycDetails(kycMap)
                    .locationDetails(objectMapper.writeValueAsString(locationDetailsMap))
                    .createdBy(request.getUserId())
                    .createdTs(now)
                    .build();
            customerRepository.save(customer);

            TbObApplicationMaster master = TbObApplicationMaster.builder()
                    .applicationId(applicationId)
                    .customerId(customer.getCustomerId())
                    .version("1")
                    .mobileNumber(kyc != null ? kyc.getMobileNum() : null)
                    .kendraId(dtls.getKendraId())
                    .kendraName(dtls.getKendraName())
                    .groupId(dtls.getGroupId())
                    .branchId(dtls.getBranchId())
                    .branchName(dtls.getBranchName())
                    .kmName(dtls.getKmName())
                    .stage(ApplicationConstants.DEFAULT_DRAFT)
                    .subStage(ApplicationConstants.INITIAL_SUB_STAGE)
                    .wfStage(ApplicationConstants.WFSTAGE_DRAFT)
                    .status(ApplicationConstants.STATUS_INITIATE)
                    .recordType(ApplicationConstants.RECORD_TYPE_NEW)
                    .dmsDeleteFlag("N")
                    .leadId(dtls.getLeadId())
                    .remarks(dtls.getRemarks())
                    .createdByRole(request.getUserRole())
                    .createdBy(request.getUserId())
                    .loanEligible("NA")
                    .createdTs(now)
                    .custLabel("NA")
                    .build();
            master = applicationMasterRepository.save(master);

            workflowRepository.save(TbObApplnWorkflow.builder()
                    .appId(request.getAppId())
                    .applicationId(applicationId)
                    .versionNo(1)
                    .workflowSeqNo(1)
                    .applicationStatus(ApplicationConstants.OTPCONSENT)
                    .createdTs(now)
                    .createdBy(request.getUserId())
                    .presentRole(request.getUserRole())
                    .nextWorkflowStage(ApplicationConstants.STAGE_DRAFT)
                    .remarks(dtls.getRemarks())
                    .createdUsername(request.getUserName())
                    .build());


            // 4. audit trail entry (application level)
            auditService.saveApplicationAudit(master, customer, request.getUserId(), request.getUserName(),
                    request.getUserRole(), request.getAppId(), request.getAppVersion(),
                    ApplicationConstants.DEFAULT_DRAFT, ApplicationConstants.INITIAL_SUB_STAGE,
                    ApplicationConstants.WFSTAGE_DRAFT, dtls, null, null);

            // audit trail entry (user level - who created the application)
            auditService.saveUserAudit(request.getInterfaceName(), applicationId,
                    String.valueOf(customer.getCustomerId()), request.getUserId(), request.getUserName(),
                    request.getUserRole(), master.getBranchId(), master.getKendraId(), master.getGroupId(), dtls);

            lastResponse = ApiResponse.success("Application created successfully", applicationId,
                    master.getCustomerId(), ApplicationConstants.DEFAULT_DRAFT, ApplicationConstants.INITIAL_SUB_STAGE, 1);
        }

        ResponseWrapper responseWrapper = new ResponseWrapper();
        ResponseHeader responseHeader = new ResponseHeader();
        CommonUtils.generateHeaderForSuccess(responseHeader);
        ResponseBody responseBody = new ResponseBody();
        responseBody.setResponseObj(objectMapper.writeValueAsString(Objects.requireNonNull(lastResponse)));
        responseWrapper.setApiResponse(Response.builder().responseBody(responseBody).responseHeader(responseHeader).build());
        return responseWrapper;
    }

    public static Response buildValidationFailureResponse(List<String> validationErrors) {
        ResponseHeader responseHeader = new ResponseHeader();
        CommonUtils.generateHeaderForFailure(responseHeader, String.join(" | ", validationErrors));

        Response response = new Response();
        response.setResponseHeader(responseHeader);
        response.setResponseBody(new ResponseBody());
        return response;
    }

    private Response validateDatabaseDuplicate(CreateApplicationRequestFields requestObj) throws JsonProcessingException {
        log.info("Inside validateDatabaseDuplicate");
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        for (ApplicationCreateDtls record : requestObj.getApplicationdtls()) {

            // 1. Duplicate check against tb_ob_lead (existing OPEN leads)
            Optional<TbObLead> duplicateLead =
                    leadRepository.findDuplicate(record.getCustomerDtls().getKycDetails().getMobileNum(), "OPEN");
            if (duplicateLead.isPresent()) {
                responseBody.setResponseObj(
                        objectMapper.writeValueAsString(buildDuplicateResponse(duplicateLead.get())));
                CommonUtils.generateHeaderForFailure(responseHeader, "Lead already exists.");
                response.setResponseHeader(responseHeader);
                response.setResponseBody(responseBody);
                return response;
            }

            // 2. Duplicate check against tb_ob_application_master (already onboarded customers)
            Optional<TbObApplicationMaster> customer =
                    tbObApplicationMasterRepository.findByMobileNumber(record.getCustomerDtls().getKycDetails().getMobileNum());
            if (customer.isPresent()) {
                CommonUtils.generateHeaderForFailure(responseHeader, "Customer already exists.");
                response.setResponseHeader(responseHeader);
                response.setResponseBody(responseBody);
                return response;
            }

            // 3. Duplicate check against tb_asmi_user (already registered app users)
            Optional<TbAsmiUser> asmiUser =
                    tbAsmiUserRepository.findByMobileNumber(record.getCustomerDtls().getKycDetails().getMobileNum());
            if (asmiUser.isPresent()) {
                CommonUtils.generateHeaderForFailure(responseHeader, "User already exists.");
                response.setResponseHeader(responseHeader);
                response.setResponseBody(responseBody);
                return response;
            }
        }
        return null;
    }

    private List<LeadCreationResult> buildDuplicateResponse(TbObLead lead) {
        List<LeadCreationResult> responseList = new ArrayList<>();
        responseList.add(LeadCreationResult.builder()
                .leadId(lead.getLeadId())
                .memberName(lead.getCustomerName())
                .mobileNumber(lead.getMobileNumber())
                .message("Lead already exists. Click 'Recapture Details' to continue.")
                .build());
        return responseList;
    }

    @Override
    @Transactional
    public ResponseWrapper updateApplication(UpdateApplicationRequest request) throws JsonProcessingException {
        ApiResponse lastResponse = null;

        for (ApplicationUpdateDtls dtls : request.getRequestObj().getApplicationdtls()) {
            String applicationId = dtls.getApplicationId();

            TbObApplicationMaster master = applicationMasterRepository.findByApplicationId(applicationId)
                    .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

//            assertNotLockedByAnotherUser(applicationId, request.getUserId());

            TbObCustomer customer = customerRepository.findByApplicationId(applicationId)
                    .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

            LocalDateTime now = LocalDateTime.now();
            CustomerUpdateDtls cd = dtls.getCustomerDtls();

            String subStage = dtls.getSubStage();
            System.out.println("sub stage - 1: " + subStage);
            if (cd != null && cd.getVerficationDet() != null) {
                SubStageHandler handler = subStageHandlerResolver.resolve(subStage);
                SubStageHandlerContext context = SubStageHandlerContext.builder()
                        .customerUpdateDtls(cd)
                        .applicationId(applicationId)
                        .customer(customer)
                        .userId(request.getUserId())
                        .now(now)
                        .master(master)
                        .build();
                handler.handle(context);

                if (cd.getKycDetails() != null) {
                    customer.setKycDetails(cd.getKycDetails());
                }
                mergeVerification(cd, customer);
            }

            if (dtls.getCustomerName() != null) {
                master.setCustomerName(dtls.getCustomerName());
                customer.setCustomerName(dtls.getCustomerName());
            }
            master.setStage(dtls.getStage() != null ? dtls.getStage() : master.getStage());
            master.setSubStage(subStage);
            master.setWfStage(dtls.getWfstage());
            master.setKmName(dtls.getKmName() != null ? dtls.getKmName() : master.getKmName());
            master.setChannelType(dtls.getChannelType());
            master.setDmsFolderIdx(dtls.getDmsFolderIdx() != null ? dtls.getDmsFolderIdx() : master.getDmsFolderIdx());
            master.setRemarks(dtls.getRemarks());
            master.setAddInfo1(objectMapper.writeValueAsString(dtls.getAddInfo()));
            master.setUpdatedBy(request.getUserId());
            master.setUpdatedByRole(request.getUserRole());
            master.setUpdatedTs(now);

            applicationMasterRepository.save(master);
            customer.setUpdatedBy(request.getUserId());
            customer.setUpdatedTs(now);
            customerRepository.save(customer);

            int versionNo = parseInt(dtls.getVersionNum(), 1);
            long nextSeq = workflowRepository.countByApplicationId(applicationId) + 1;
            workflowRepository.save(TbObApplnWorkflow.builder()
                    .appId(request.getAppId())
                    .applicationId(applicationId)
                    .versionNo(versionNo)
                    .workflowSeqNo((int) nextSeq)
                    .applicationStatus(dtls.getWfstage())
                    .createdTs(now)
                    .createdBy(request.getUserId())
                    .presentRole(request.getUserRole())
                    .nextWorkflowStage(subStage)
                    .remarks(dtls.getRemarks())
                    .createdUsername(request.getUserName())
                    .build());

            auditService.saveApplicationAudit(master, customer, request.getUserId(), request.getUserName(),
                    request.getUserRole(), request.getAppId(), request.getAppVersion(), dtls.getStage(), subStage,
                    dtls.getWfstage(), dtls, dtls.getModifiedDetails(), dtls.getAddInfo());

            // offline/MIS report capture - overall (not per-field) modification counter
            misReportService.recordModifiedDetails(master, customer, request.getUserId(), request.getUserRole(),
                    request.getAppVersion(), dtls.getStage(), subStage, dtls.getWfstage(),
                    dtls.getChannelType(), dtls.getRemarks(), dtls.getModifiedDetails());

            // audit trail entry (user level - who updated the application, and what sub-stage)
            auditService.saveUserAudit(request.getInterfaceName(), applicationId,
                    String.valueOf(customer.getCustomerId()), request.getUserId(), request.getUserName(),
                    request.getUserRole(), master.getBranchId(), master.getKendraId(), master.getGroupId(), dtls);

            lastResponse = ApiResponse.success("Application updated successfully", applicationId,
                    customer.getCustomerId(), dtls.getStage(), subStage, versionNo);
        }

        ResponseWrapper responseWrapper = new ResponseWrapper();
        ResponseHeader responseHeader = new ResponseHeader();
        CommonUtils.generateHeaderForSuccess(responseHeader);
        ResponseBody responseBody = new ResponseBody();
        responseBody.setResponseObj(objectMapper.writeValueAsString(Objects.requireNonNull(lastResponse)));
        responseWrapper.setApiResponse(Response.builder().responseBody(responseBody).responseHeader(responseHeader).build());
        return responseWrapper;
    }

    private void mergeVerification(CustomerUpdateDtls cd, TbObCustomer customer) {
        System.out.println("cd: " + cd.getVerficationDet());
        if (CollectionUtils.isEmpty(cd.getVerficationDet())) {
            return;
        }
        Map<String, Object> merged = customer.getVerificationDet() != null
                ? new HashMap<>(customer.getVerificationDet())
                : new HashMap<>();
        for (Map.Entry<String, Object> entry : cd.getVerficationDet().entrySet()) {
            merged.put(entry.getKey(), objectMapper.convertValue(entry.getValue(), Map.class));
        }
        customer.setVerificationDet(merged);
    }

    private void assertNotLockedByAnotherUser(String applicationId, String userId) {
        Optional<TbObRecordLock> activeLock = recordLockRepository.findByApplicationIdAndStatus(applicationId, "ACTIVE");
        activeLock.ifPresent(lock -> {
            if (!lock.getLockedBy().equals(userId)) {
                throw new RecordLockedException(applicationId, lock.getLockedBy());
            }
        });
    }

 /*   private TbObCustAuditTrail buildAuditEntry(Object request, String applicationId, String stage, String subStage,
                                               String wfstatus, TbObApplicationMaster master, TbObCustomer customer,
                                               Map<String, Object> addInfo) {
        String userId, userName, userRole, appId, appVersion;
        if (request instanceof CreateApplicationRequest r) {
            userId = r.getUserId();
            userName = r.getUserName();
            userRole = r.getUserRole();
            appId = r.getAppId();
            appVersion = r.getAppVersion();
        } else {
            UpdateApplicationRequest r = (UpdateApplicationRequest) request;
            userId = r.getUserId();
            userName = r.getUserName();
            userRole = r.getUserRole();
            appId = r.getAppId();
            appVersion = r.getAppVersion();
        }

        return TbObCustAuditTrail.builder()
                .appId(appId)
                .applicationId(applicationId)
                .userId(userId)
                .userName(userName)
                .userRole(userRole)
                .stageId(stage)
                .subStage(subStage)
                .wfStatus(wfstatus)
                .customerId(String.valueOf(customer.getCustomerId()))
                .customerName(customer.getCustomerName())
                .mobileNo(
                        customer.getKycDetails() != null
                                ? String.valueOf(customer.getKycDetails().get("mobileNum"))
                                : master.getMobileNumber()
                )
                .kendraId(master.getKendraId() != null ? master.getKendraId() : null)
                .kendraName(master.getKendraName())
                .groupId(master.getGroupId() != null ? master.getGroupId() : null)
                .branchId(master.getBranchId())
                .addInfo1(addInfo)
                .appVersion(appVersion)
                .createTs(LocalDateTime.now())
                .build();
    }*/

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            log.warn("Could not parse numeric value '{}'", value);
            return null;
        }
    }

    private int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    @Transactional
    public ResponseWrapper getApplicationDetails(FetchApplicationDetailsRequest req, long lockDurationMinutes) throws JsonProcessingException {
        FetchApplicationHandler fetchApplicationHandler = fetchApplicationHandlerResolver.resolve("KM");
        try {
            return fetchApplicationHandler.handleFetchApplicationDetails(req, lockDurationMinutes);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

  /*  public void recordEvent(TbObApplicationMaster app, AuditEventType eventType,
                            String userId, String userName, String userRole) {

        LocalDateTime now = LocalDateTime.now();

        TbObCustAuditTrail audit = TbObCustAuditTrail.builder()
                .appId(app.getApplicationId())
                .applicationId(app.getApplicationId())
                .customerId(String.valueOf(app.getCustomerId()))
                .kendraId(String.valueOf(app.getKendraId()))
                .groupId(String.valueOf(app.getGroupId()))
                .userId(userId)
                .userName(userName)
                .userRole(userRole)
                .stageId(app.getStage())
                .subStage(app.getSubStage())
                .wfStatus(app.getWfStage())
                .customerName(app.getCustomerName())
                .kendraName(app.getKendraName())
                .branchId(app.getBranchId())
                .payload(Map.of(
                        "event", eventType.name(),
                        "applicationId", app.getApplicationId(),
                        "status", app.getStatus() == null ? "" : app.getStatus()
                ))
                .createTs(now)
                .build();

        custAuditTrailRepository.save(audit);
    }*/
}