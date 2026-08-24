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
import com.iexceed.appzillonbanking.cagl.cob.constants.AuditConstants;
import com.iexceed.appzillonbanking.cagl.cob.service.ApplicationDetailsMapper;
import com.iexceed.appzillonbanking.cagl.cob.service.ApplicationService;
import com.iexceed.appzillonbanking.cagl.cob.service.AuditService;
import com.iexceed.appzillonbanking.cagl.cob.service.RecordLockService;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.FetchApplicationHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.resolver.FetchApplicationHandlerResolver;
import com.iexceed.appzillonbanking.cagl.cob.utils.IdGeneratorUtil;
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
    private final TbObAddressRepository addressRepository;
    private final TbObFamilyMemberRepository familyMemberRepository;
    private final TbObDocumentRepository documentRepository;
    private final TbObApplnWorkflowRepository workflowRepository;
    private final TbObRecordLockRepository recordLockRepository;
    private final TbObOtherDocumentRepository otherDocumentRepository;
    private final TbObLoanRepository loanRepository;
    private final TbObCustAuditTrailRepository custAuditTrailRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationDetailsMapper mapper;
    private final TbObCustOthersRepository custOthersRepository;
    private final RecordLockService recordLockService;
    private static final int AUDIT_SNAPSHOT_LIMIT = 20;
    private final FetchApplicationHandlerResolver fetchApplicationHandlerResolver;
    private final AuditService auditService;

    // =========================================================================================
    // CREATE
    // =========================================================================================

    @Override
    @Transactional
    public ResponseWrapper createApplication(CreateApplicationRequest request) throws JsonProcessingException {
        ApiResponse lastResponse = null;

        for (ApplicationCreateDtls dtls : request.getRequestObj().getApplicationdtls()) {
            String applicationId = IdGeneratorUtil.generateApplicationId(request.getBranchId(), request.getUserId());
            LocalDateTime now = LocalDateTime.now();

            KycDetailsCreate kyc = Optional.ofNullable(dtls.getCustomerDtls())
                    .map(CustomerCreateDtls::getKycDetails)
                    .orElse(null);

            // 2. tb_ob_customer - shares the same customer_id; NOT-NULL columns that aren't known
            //    yet (dob, marital_status, primary_kyc_type/id) get a PENDING placeholder and are
            //    overwritten once sub_stage 1.2 (member KYC) is submitted.
            Map<String, Object> kycMap = new HashMap<>();
            if (kyc != null) {
                kycMap.put("mobileNum", kyc.getMobileNum());
                kycMap.put("altMobileNum", kyc.getAltMobileNum());
                kycMap.put("deviceType", kyc.getDeviceType());
                kycMap.put("lang", kyc.getLang());
                kycMap.put("gpsLat", kyc.getGpsLat());
                kycMap.put("gpsLong", kyc.getGpsLong());
            }

            TbObCustomer customer = TbObCustomer.builder()
                    .applicationId(applicationId)
                    .livePhotoStatus(ApplicationConstants.PLACEHOLDER_NOT_CAPTURED)
                    .kycStatus(ApplicationConstants.PLACEHOLDER_NOT_CAPTURED)
                    .kycDetails(kycMap)
                    .createdBy(request.getUserId())
                    .createdTs(now)
                    .build();
            customerRepository.save(customer);

            // 1. tb_ob_application_master - customer_id is DB-sequence generated, leave null so
            //    Hibernate pulls the next value from seq_ob_customer_id via the SequenceGenerator.
            TbObApplicationMaster master = TbObApplicationMaster.builder()
                    .applicationId(applicationId)
                    .customerId(customer.getCustomerId())
                    .version("1")
                    .mobileNumber(kyc != null ? kyc.getMobileNum() : null)
                    .kendraId(dtls.getKendraId())
                    .kendraName(dtls.getKendraName())
                    .groupId(dtls.getGroupId())
                    .branchId(dtls.getBranchId())
                    .kmName(dtls.getKmName())
                    .stage(ApplicationConstants.DEFAULT_STAGE)
                    .subStage(ApplicationConstants.INITIAL_SUB_STAGE)
                    .wfStage(ApplicationConstants.WFSTAGE_DRAFT)
                    .status(ApplicationConstants.STATUS_DRAFT)
                    .recordType(ApplicationConstants.RECORD_TYPE_NEW)
                    .leadId(dtls.getLeadId())
                    .remarks(dtls.getRemarks())
                    .createdByRole(request.getUserRole())
                    .createdBy(request.getUserId())
                    .loanEligible("NA")
                    .createdTs(now)
                    .custLabel("NA")
                    .build();
            master = applicationMasterRepository.save(master);

            // 3. initial workflow snapshot (version 1 / seq 1)
            workflowRepository.save(TbObApplnWorkflow.builder()
                    .appId(request.getAppId())
                    .applicationId(applicationId)
                    .versionNo(1)
                    .workflowSeqNo(1)
                    .applicationStatus(ApplicationConstants.OTP_VERIFIED)
                    .createdTs(now)
                    .createdBy(request.getUserId())
                    .presentRole(request.getUserRole())
                    .nextWorkflowStage(ApplicationConstants.WFSTAGE_DRAFT)

                    .remarks(dtls.getRemarks())
                    .createdUsername(request.getUserName())
                    .build());


            // 4. audit trail entry (application level)
            auditService.saveApplicationAudit(master, customer, request.getUserId(), request.getUserName(),
                    request.getUserRole(), request.getAppId(), request.getAppVersion(),
                    ApplicationConstants.DEFAULT_STAGE, ApplicationConstants.INITIAL_SUB_STAGE,
                    ApplicationConstants.WFSTAGE_DRAFT, false, dtls, null);

            // audit trail entry (user level - who created the application)
            auditService.saveUserAudit(AuditConstants.APPLICATION_CREATED, applicationId,
                    String.valueOf(customer.getCustomerId()), request.getUserId(), request.getUserName(),
                    request.getUserRole(), master.getBranchId(), master.getKendraId(), master.getGroupId(), dtls);

            lastResponse = ApiResponse.success("Application created successfully", applicationId,
                    master.getCustomerId(), ApplicationConstants.DEFAULT_STAGE, ApplicationConstants.INITIAL_SUB_STAGE, 1);
        }

        ResponseWrapper responseWrapper = new ResponseWrapper();
        ResponseHeader responseHeader = new ResponseHeader();
        CommonUtils.generateHeaderForSuccess(responseHeader);
        ResponseBody responseBody = new ResponseBody();
        responseBody.setResponseObj(objectMapper.writeValueAsString(Objects.requireNonNull(lastResponse)));
        responseWrapper.setApiResponse(Response.builder().responseBody(responseBody).responseHeader(responseHeader).build());
        return responseWrapper;
    }

    // =========================================================================================
    // UPDATE
    // =========================================================================================

    @Override
    @Transactional
    public ResponseWrapper updateApplication(UpdateApplicationRequest request) throws JsonProcessingException {
        ApiResponse lastResponse = null;

        for (ApplicationUpdateDtls dtls : request.getRequestObj().getApplicationdtls()) {
            String applicationId = dtls.getApplicationId();

            TbObApplicationMaster master = applicationMasterRepository.findByApplicationId(applicationId)
                    .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

            assertNotLockedByAnotherUser(applicationId, request.getUserId());

            TbObCustomer customer = customerRepository.findByApplicationId(applicationId)
                    .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

            LocalDateTime now = LocalDateTime.now();
            CustomerUpdateDtls cd = dtls.getCustomerDtls();

            // route the payload to the right table(s) based on sub_stage
            String subStage = dtls.getSubStage();
            System.out.println("sub_stage: " + subStage);
            if (cd != null) {
                switch (subStage) {
                    case ApplicationConstants.SUB_STAGE_MEMBER_KYC -> handleMemberKyc(cd, applicationId, customer, request.getUserId(), now);
                    case ApplicationConstants.SUB_STAGE_ADDRESS -> handleAddress(cd, applicationId, customer, request.getUserId(), now);
                    case ApplicationConstants.SUB_STAGE_FAMILY -> handleFamily(cd, applicationId, customer, request.getUserId(), now);
                    case ApplicationConstants.SUB_STAGE_INCOME -> handleIncome(cd, applicationId, customer, request.getUserId(), now);
                    case ApplicationConstants.SUB_STAGE_KENDRA_SELECTION -> handleKendraSelection(cd, master, customer);
                    case ApplicationConstants.SUB_STAGE_BANK -> handleBank(cd, applicationId, customer, request.getUserId(), now);
                    case ApplicationConstants.SUB_STAGE_ADDITIONAL_DOCS, ApplicationConstants.SUB_STAGE_ADDITIONAL_DOCS_1 -> handleAdditionalDocs(cd, applicationId, customer, request.getUserId(), now);
                    default -> throw new InvalidSubStageException(subStage);
                }
                if(cd.getKycDetails() != null) {
                    customer.setKycDetails(cd.getKycDetails());
                }
                mergeVerification(cd, customer);
            }

            // common application_master fields present on every update call
            if(dtls.getCustomerName() != null) {
                master.setCustomerName(dtls.getCustomerName());
                customer.setCustomerName(dtls.getCustomerName());
            }
            master.setStage(dtls.getStage() != null ? dtls.getStage() : master.getStage());
            master.advanceSubStage(subStage);
            master.setWfStage(dtls.getWfstage());
            master.setKmName(dtls.getKmName() != null ? dtls.getKmName() : master.getKmName());
            master.setChannelType(dtls.getChannelType());
            master.setDmsFolderIdx(dtls.getDmsFolderIdx() != null ? dtls.getDmsFolderIdx() : master.getDmsFolderIdx());
            master.setRemarks(dtls.getRemarks());
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
                    dtls.getWfstage(), true, dtls, dtls.getAddInfo());

            // audit trail entry (user level - who updated the application, and what sub-stage)
            auditService.saveUserAudit(AuditConstants.APPLICATION_UPDATED, applicationId,
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

    // =========================================================================================
    // Sub-stage 1.2 - member KYC
    // =========================================================================================

    private void handleMemberKyc(CustomerUpdateDtls cd, String applicationId, TbObCustomer customer,
                                 String uploadedBy, LocalDateTime now) throws JsonProcessingException {
        Map<String, Object> kycMap = customer.getKycDetails() != null ? customer.getKycDetails() : new HashMap<>();
        if (cd.getKycDetails() != null) {
            kycMap.putAll(cd.getKycDetails());
        }
        customer.setPhotoDocId(cd.getPhotoDocId());
        customer.setKycDetails(kycMap);
        customer.setLivePhotoStatus(cd.getMemberPhoto().getStatus());

        Object primaryType = kycMap.get("primaryType");
        Object primaryId = kycMap.get("primaryId");
        if (primaryType != null) customer.setPrimaryKycType(primaryType.toString());
        if (primaryId != null) customer.setPrimaryKycId(primaryId.toString());

        // member live photo
        Optional.ofNullable(cd.getMemberPhoto())
                .map(MemberPhotoDet::getDocumentList)
                .ifPresent(documentList ->
                        documentList.stream()
                                .map(DocumentListItem::getDocumentDetails)
                                .filter(Objects::nonNull)
                                .forEach(documentDetails ->
                                {
                                    try {
                                        saveDocument(
                                                documentDetails,
                                                applicationId,
                                                customer.getCustomerId(),
                                                uploadedBy,
                                                now
                                        );
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException(e);
                                    }
                                }));

        // member KYC documents (Voter ID / Aadhaar / PAN ...)
        if (cd.getMemberKycDetails() != null && !CollectionUtils.isEmpty(cd.getMemberKycDetails().getDocumentList())) {
            for (DocumentListItem item : cd.getMemberKycDetails().getDocumentList()) {
                if (item.getDocumentDetails() != null) {
                    saveDocument(item.getDocumentDetails(), applicationId, customer.getCustomerId(), uploadedBy, now);
                }
            }
        }
    }

    // =========================================================================================
    // Sub-stage 1.3 - address
    // =========================================================================================

    private void handleAddress(CustomerUpdateDtls cd, String applicationId, TbObCustomer customer,
                               String uploadedBy, LocalDateTime now) throws JsonProcessingException {
        if (cd.getCustomerName() != null) {
            customer.setCustomerName(cd.getCustomerName());
        }
        Map<String, Object> kycMap = customer.getKycDetails() != null ? customer.getKycDetails() : new HashMap<>();
        if (cd.getKycDetails() != null) {
            kycMap.putAll(cd.getKycDetails());
        }
        customer.setKycDetails(kycMap);
        Object dob = kycMap.get("dob");
        if (dob != null) {
            customer.setDob(dob.toString());
        }

        PersonalAddressDet pad = cd.getPersonalAddressDet();
        if (pad == null || CollectionUtils.isEmpty(pad.getDocumentList())) {
            return;
        }

        for (DocumentListItem document : pad.getDocumentList()) {
            DocumentDetail doc = document.getDocumentDetails();
            String addressType = "ADDC".equalsIgnoreCase(doc.getSubCat())
                    ? ApplicationConstants.ADDRESS_TYPE_COMMUNICATION
                    : ApplicationConstants.ADDRESS_TYPE_PERMANENT;

            Map<String, Object> addrPayload = new HashMap<>();
            addrPayload.put("nameSelected", cd.getPersonalAddressDet().getNameSelected());
            addrPayload.put("dobSelected", cd.getPersonalAddressDet().getDobSelected());
            addrPayload.put("PA", cd.getPersonalAddressDet().getPa());
            addrPayload.put("CA", cd.getPersonalAddressDet().getCa());

            TbObAddress address = addressRepository.findByApplicationIdAndAddressType(applicationId, addressType)
                    .orElse(TbObAddress.builder()
                            .customerId(customer.getCustomerId())
                            .applicationId(applicationId)
                            .addressType(addressType)
                            .commSameAsPerm("N")
                            .createdTs(now)
                            .build());
            address.setAddrPayload(addrPayload);
            address.setAddressProofDocId(doc.getPhoto());
            address.setUpdatedTs(now);
            address.setUpdatedBy(uploadedBy);
            addressRepository.save(address);

            saveDocument(doc, applicationId, customer.getCustomerId(), uploadedBy, now,
                    ApplicationConstants.DOCUMENT_CATEGORY_ADDRESS);
        }
    }

    // =========================================================================================
    // Sub-stage 1.4 - family members
    // =========================================================================================

    private void handleFamily(CustomerUpdateDtls cd, String applicationId, TbObCustomer customer,
                              String uploadedBy, LocalDateTime now) throws JsonProcessingException {
        log.info("Enter handle family: " + cd + " : " + applicationId + " : " + customer + " : " + uploadedBy + " : " + now);
        Map<String, Object> kycMap = customer.getKycDetails() != null ? customer.getKycDetails() : new HashMap<>();
        if (cd.getKycDetails() != null) {
            kycMap.putAll(cd.getKycDetails());
        }
        customer.setKycDetails(kycMap);

        FamilyDet fd = cd.getFamilyDetails();
        if (fd == null || CollectionUtils.isEmpty(fd.getMemberList())) {
            return;
        }

        // re-submission of this sub-stage replaces the previously captured family list
//        familyMemberRepository.deleteByApplicationId(applicationId);

        for (FamilyMemberItem item : fd.getMemberList()) {
            String name = null;
            String gender = null;
            LocalDate dob = null;
            String kycType = null;
            String kycDocId = null;
            String kycDocFront = null;
            String kycDocBack = null;
            String photoDocId = null;

            if (!CollectionUtils.isEmpty(item.getDocumentList())) {
                for (DocumentListItem docListItem : item.getDocumentList()) {
                    DocumentDetail doc = docListItem.getDocumentDetails();
                    if (doc == null) continue;

                    Map<String, Object> inputData = doc.getInputData();
                    if (inputData != null) {
                        if (name == null && inputData.get("name") != null) name = inputData.get("name").toString();
                        if (gender == null && inputData.get("gender") != null) gender = inputData.get("gender").toString();
                        if (dob == null && inputData.get("dob") != null) dob = parseDob(inputData.get("dob").toString());
                    }
                    if ("VOTER-ID".equalsIgnoreCase(doc.getLegalDocName()) && doc.getSubCat().contains("FD1")
                            && item.getMemberType().equalsIgnoreCase("P")) {
                        kycType = doc.getLegalDocName();
                        kycDocId = doc.getLegalDocId();
                        kycDocFront = doc.getDocuNoF();
                        kycDocBack = doc.getDocuNoB();
                    } else if(item.getMemberType().equalsIgnoreCase("S")) {
                        kycType = doc.getLegalDocName();
                        kycDocId = doc.getLegalDocId();
                        kycDocFront = doc.getDocuNoF();
                        kycDocBack = doc.getDocuNoB();
                    }
                    if (doc.getPhoto() != null && !doc.getPhoto().isBlank() && doc.getDocuNoF() == null) {
                        photoDocId = doc.getPhoto();
                    }
                }
            }
            customer.setMaritalStatus(cd.getFamilyDetails().getMaritalStatus());
            TbObFamilyMember familyMember = familyMemberRepository
                    .findByApplicationIdAndRelationAndMemberType(
                            applicationId,
                            item.getRelationType(),
                            item.getMemberType())
                    .orElseGet(TbObFamilyMember::new);

            // populate fields
            familyMember.setCustomerId(customer.getCustomerId());
            familyMember.setApplicationId(applicationId);
            familyMember.setMemberType(item.getMemberType());
            familyMember.setRelation(item.getRelationType());
            familyMember.setName(name != null ? name : ApplicationConstants.PLACEHOLDER_NOT_CAPTURED);
            familyMember.setDob(dob);
            familyMember.setGender(gender);
            familyMember.setMobileNum(item.getMobileNum());
            familyMember.setKycType(kycType);
            familyMember.setKycDocId(kycDocId);
            familyMember.setKycDocFront(kycDocFront);
            familyMember.setKycDocBack(kycDocBack);
            familyMember.setPhotoDocId(photoDocId);
            familyMember.setIsNominee(Boolean.TRUE.equals(item.getIsNominee()));
            familyMember.setIsEarningMember(Boolean.TRUE.equals(item.getIsEarning()));

            if (familyMember.getFamilyMemId() == null) {
                familyMember.setCreatedTs(now);
            }

            familyMemberRepository.save(familyMember);
            System.out.println("Family Member Id: " + familyMember.getFamilyMemId());
            if (!CollectionUtils.isEmpty(item.getDocumentList())) {
                for (DocumentListItem docListItem : item.getDocumentList()) {
                    if (docListItem.getDocumentDetails() != null) {
                        // For mapping the family document details
                        docListItem.getDocumentDetails().setMappingId(familyMember.getFamilyMemId());
                        saveDocument(docListItem.getDocumentDetails(), applicationId, customer.getCustomerId(), uploadedBy, now);
                    }
                }
            }
        }
    }

    // =========================================================================================
    // Sub-stage 1.5 - income
    // =========================================================================================

    private void handleIncome(CustomerUpdateDtls cd, String applicationId, TbObCustomer customer,
                              String uploadedBy, LocalDateTime now) throws JsonProcessingException {
        Map<String, Object> payload = customer.getPayload() != null ? customer.getPayload() : new HashMap<>();
        if (cd.getPayload() != null) {
            payload.putAll(cd.getPayload());
        }

        TbObCustOthers custOthers = custOthersRepository.findByApplicationId(applicationId)
                .orElseGet(() -> TbObCustOthers.builder()
                        .applicationId(applicationId)
                        .customerId(customer.getCustomerId())
                        .createdTs(now)
                        .build());
        custOthers.setCustomerId(customer.getCustomerId());
        String incomeDet = objectMapper.convertValue(cd.getIncomeDet(), new TypeReference<String>() {});
        custOthers.setIncomedet(incomeDet);
        IncomeDet inc = cd.getIncomeDet();
        if (inc != null) {
            custOthers.setQuestionnaire(objectMapper.writeValueAsString(inc.questPayload()));
//            if (inc.incomePayload() != null) payload.put("incomePayload", inc.incomePayload());
//            if (inc.questPayload() != null) payload.put("questPayload", inc.questPayload());
        }
        customer.setPayload(payload);
        custOthersRepository.save(custOthers);
        if (inc != null && !CollectionUtils.isEmpty(inc.documentList())) {
            for (DocumentListItem document : inc.documentList()) {
                saveDocument(document.getDocumentDetails(), applicationId, customer.getCustomerId(), uploadedBy, now);
            }
        }
    }

    // =========================================================================================
    // Sub-stage 1.6 - kendra selection
    // =========================================================================================

    private void handleKendraSelection(CustomerUpdateDtls cd, TbObApplicationMaster master, TbObCustomer customer) {
        KendraSelectionDetails ks = cd.getKendraSelectionDetails();
        if (ks == null) {
            return;
        }
        master.setKendraId(ks.getKendraId());
        master.setKendraName(ks.getKendraName());
        master.setGroupId(ks.getGroupId());
        customer.setDistanceFromKendra(ks.getDistanceFromKendra());

        if (cd.getPayload() != null) {
            Map<String, Object> payload = customer.getPayload() != null ? customer.getPayload() : new HashMap<>();
            payload.putAll(cd.getPayload());
            customer.setPayload(payload);
        }
    }

    // =========================================================================================
    // Sub-stage 1.7 - bank details
    // =========================================================================================

    private void handleBank(CustomerUpdateDtls cd, String applicationId, TbObCustomer customer,
                            String uploadedBy, LocalDateTime now) throws JsonProcessingException {
        BankDet bd = cd.getBankDet();
        if (bd == null) {
            return;
        }

        Map<String, Object> bankMap = new HashMap<>();
        bankMap.put("bankAccNo", bd.getBankAccNo());
        bankMap.put("bankAccName", bd.getBankAccName());
        bankMap.put("bankBranchName", bd.getBankBranchName());
        bankMap.put("bankName", bd.getBankName());
        bankMap.put("bankIfscCode", bd.getBankIfscCode());
        bankMap.put("status", bd.getStatus());
        bankMap.put("pennyRes", bd.getPennyRes());
        customer.setBankDetails(bankMap);

        if (cd.getPayload() != null) {
            Map<String, Object> payload = customer.getPayload() != null ? customer.getPayload() : new HashMap<>();
            payload.putAll(cd.getPayload());
            customer.setPayload(payload);
        }

        if (!CollectionUtils.isEmpty(bd.getDocumentList())) {
            for (DocumentListItem document : bd.getDocumentList()) {
                saveDocument(document.getDocumentDetails(), applicationId, customer.getCustomerId(), uploadedBy, now);
            }
        }
    }

    // =========================================================================================
    // Sub-stage 1.8 - additional / supporting documents (home, business, other)
    // =========================================================================================

    private void handleAdditionalDocs(CustomerUpdateDtls cd, String applicationId, TbObCustomer customer,
                                      String uploadedBy, LocalDateTime now) throws JsonProcessingException {
        AdditionalDocuDet ad = cd.getAdditionalDocuDet();

        if (ad == null || CollectionUtils.isEmpty(ad.getDocumentList())) {
            return;
        }
        if (cd.getPayload() != null) {
            Map<String, Object> payload = customer.getPayload() != null ? customer.getPayload() : new HashMap<>();
            payload.putAll(cd.getPayload());
            customer.setPayload(payload);
        }
        for (DocumentListItem document : ad.getDocumentList()) {
            saveDocument(document.getDocumentDetails(), applicationId, customer.getCustomerId(), uploadedBy, now);
        }
    }

    // =========================================================================================
    // shared helpers
    // =========================================================================================

    private void mergeVerification(CustomerUpdateDtls cd, TbObCustomer customer) {
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

    /** Overload for document types whose category comes straight from the payload (default). */
    private void saveDocument(DocumentDetail doc, String applicationId, String customerId, String uploadedBy, LocalDateTime now) throws JsonProcessingException {
        saveDocument(doc, applicationId, customerId, uploadedBy, now, doc.getCategory());
    }

    /**
     * Maps one wire-level {@link DocumentDetail} onto a {@code tb_ob_document} row and persists it.
     * docu_id is issued by the client and is stable/unique per application, so this is an upsert
     * (save acts as insert-or-update against the composite PK).
     * <p>
     * NOTE: legal_doc_name and kyc_type are NOT NULL on tb_ob_document, but a few document types
     * (e.g. the live member photo, plain home/business photos) don't carry either field in the
     * payload. Defensive defaults are applied below; confirm with the product/schema owner whether
     * these columns should be made nullable for non-KYC document categories instead.
     */
    private void saveDocument(DocumentDetail doc, String applicationId, String customerId, String uploadedBy,
                              LocalDateTime now, String categoryOverride) throws JsonProcessingException {
        TbObDocument document = documentRepository.findById(
                        new TbObDocumentId(applicationId, doc.getDocuId()))
                .orElse(TbObDocument.builder()
                        .applicationId(applicationId)
                        .docuId(doc.getDocuId())
                        .createdTs(now)
                        .docVersion(1)
                        .build());

        boolean isUpdate = document.getUploadedAt() != null;

        document.setDocuId(doc.getDocuId());
        document.setCustomerId(customerId);
        document.setCategory(categoryOverride != null ? categoryOverride : "OTHER");
        document.setSubCat(doc.getSubCat() != null ? doc.getSubCat() : "NA");
        document.setKycType(doc.getKycType() != null ? doc.getKycType() : "NON-KYC");
        document.setAuthMode(doc.getAuthMode());
        document.setIdType(doc.getIdType());
        document.setMemRelation(doc.getMemRelation());
        document.setMappingId(doc.getMappingId() != null ? doc.getMappingId() : null);
        document.setLegalDocName(doc.getLegalDocName() != null ? doc.getLegalDocName() : document.getSubCat());
        document.setLegalDocId(doc.getLegalDocId());
        document.setDmsDocIdFront(doc.getDocuNoF());
        document.setDmsDocIdBack(doc.getDocuNoB());
        document.setPhoto(doc.getPhoto());
        document.setPayload(objectMapper.writeValueAsString(doc.getPayload()));
        document.setStatus(doc.getStatus() != null ? doc.getStatus() : "uncaptured");
        document.setScore(doc.getLivePhotoScore());
//        document.setOcrData(doc.getOcrData());
//        document.setInputData(doc.getInputData());
        document.setIsEdited(Boolean.TRUE.equals(doc.getIsEdited()));
        document.setEditedBy(doc.getEditedBy());
        document.setEditedFields(doc.getEditedFields());
        document.setReuploadedBy(doc.getReUploadedBy());
        document.setReason(doc.getReason());
        document.setClarityScore(doc.getClarityScore());
        document.setClarityPass(doc.getClarityPass());
        document.setDedupeStatus(doc.getDedupeStatus());
        document.setValidationStatus("pending");
        document.setUploadedBy(uploadedBy);
        document.setUploadedAt(now);
        document.setUpdatedTs(now);
        if (isUpdate) {
            document.setDocVersion(document.getDocVersion() + 1);
        }

        documentRepository.save(document);
    }

    private void assertNotLockedByAnotherUser(String applicationId, String userId) {
        Optional<TbObRecordLock> activeLock = recordLockRepository.findByApplicationIdAndStatus(applicationId, "ACTIVE");
        activeLock.ifPresent(lock -> {
            if (!lock.getLockedBy().equals(userId)) {
                throw new RecordLockedException(applicationId, lock.getLockedBy());
            }
        });
    }

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

    private LocalDate parseDob(String dob) {
        try {
            return LocalDate.parse(dob, DOB_FORMAT);
        } catch (Exception ex) {
            log.warn("Could not parse dob '{}', expected dd/MM/yyyy", dob);
            return null;
        }
    }

    /**
     * @param lockDurationMinutes configured TIMED lock duration (onboarding.record-lock.timed-lock-duration-minutes)
     */
    @Transactional
    public ResponseWrapper getApplicationDetails(FetchApplicationDetailsRequest req, long lockDurationMinutes) throws JsonProcessingException {
        FetchApplicationHandler fetchApplicationHandler = fetchApplicationHandlerResolver.resolve("KM");
        try {
            return fetchApplicationHandler.handleFetchApplicationDetails(req, lockDurationMinutes);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes an audit row for the given application. Used here for the
     * APPLICATION_VIEWED event mandated by API 9; other APIs (create/update)
     * will call this with their own event codes.
     */
    public void recordEvent(TbObApplicationMaster app, AuditEventType eventType,
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
    }
}