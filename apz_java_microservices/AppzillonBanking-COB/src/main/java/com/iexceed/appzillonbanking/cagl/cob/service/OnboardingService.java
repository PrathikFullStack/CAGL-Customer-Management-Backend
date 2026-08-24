package com.iexceed.appzillonbanking.cagl.cob.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplnWorkflow;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.*;
import com.iexceed.appzillonbanking.core.payload.*;
import com.iexceed.appzillonbanking.core.utils.FallbackUtils;
import com.iexceed.appzillonbanking.interfaceAdapter.service.InterfaceAdapter;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.AdapterUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCbResponse;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCbResponseRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObUserAuditTrailRepository;


import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObRecordLockRepo;
import reactor.core.publisher.Mono;

@Service
public class OnboardingService {

    private static final Logger logger = LogManager.getLogger(OnboardingService.class);

    @Autowired
    TbObApplicationMasterRepository applicationMasterRepository;

    @Autowired
    TbObCustomerRepository customerRepository;

    @Autowired
    TbObAddressRepository addressRepository;

    @Autowired
    TbObLoanRepository loanRepository;

    @Autowired
    TbObFamilyMemberRepository familyMemberRepository;

    @Autowired
    TbObCustOthersRepository tbObCustOthersRepository;

    @Autowired
    private TbObKendraRepository kendraRepository;

    @Autowired
    TbObRecordLockRepo tbObRecordLockRepo;

    @Autowired
    TbObGroupMappingHistoryRepository groupMappingHistoryRepository;

    @Autowired
    TbObGroupRepository groupRepository;

    @Autowired
    TbObCbResponseRepository cbResponseRepository;

    @Autowired
    TbObApplnWorkflowRepository applnWfRepository;

    @Autowired
    private TbObUserAuditTrailRepository tbObUserAuditTrailRepo;


    @Value("${STATIC_LOAN_AMOUNT}")
    private String staticLoanAmount;

    @Value("${STATIC_LOAN_FREQUENCY}")
    private String staticLoanFrequency;

    @Value("${STATIC_LOAN_TERM}")
    private String staticLoanTerm;

    @Value("${STATIC_LOAN_PRODUCT_ID}")
    private String staticLoanProductId;

    @Value("${STATIC_LOAN_MEMBER_INS}")
    private String staticLoanMemberIns;

    @Value("${STATIC_LOAN_SPOUSE_INS}")
    private String staticLoanSpouseIns;

    @Value("${STATIC_LOAN_MEMBER_INS_AMOUNT}")
    private String staticLoanMemberInsAmount;

    @Value("${STATIC_LOAN_SPOUSE_INS_AMOUNT}")
    private String staticLoanSpouseInsAmount;

    @Value("${record.lock.rpc.role:RPC}")
    private String recordLockRpcRole;

    @Value("${record.lock.rpc.inactivity-minutes:60}")
    private long recordLockRpcInactivityMinutes;

    @Autowired
    private InterfaceAdapter interfaceAdapter;

    @Autowired
    private AdapterUtil adapterUtil;

    CbRequest cbRequest;
    Header header;

    private static final String MAPPING_TYPE_TRANSFER = "TRANSFER";
    private static final int GROUP_MAX_MEMBERS = 10;

    private static final String LOCK_STATUS_ACTIVE = "ACTIVE";
    private static final String LOCK_STATUS_EXPIRED = "EXPIRED";
    private static final String LOCK_STATUS_RELEASED = "RELEASED";
    private static final String LOCK_TYPE_PERMANENT = "PERMANENT";




    public Response createKendra(CreateKendraRequest request, Header header) {
        logger.info("Start : createKendra");
        Response response = new Response();
        try {
            CreateKendraRequestFields requestFields = request.getRequestObj();
            logger.debug("Create Kendra Request : {}", requestFields);
            validateDuplicateKendra(requestFields);
            performBlacklistValidation(requestFields);
            TbObKendra tbObKendra = prepareKendraEntity(requestFields, header);
            tbObKendra = kendraRepository.save(tbObKendra);
            prepareSuccessResponse(response, tbObKendra);
        } catch (Exception ex) {
            logger.error("Exception occurred while creating Kendra", ex);
            throw ex;
        }
        logger.info("End : createKendra");
        return response;
    }

    private void validateDuplicateKendra(CreateKendraRequestFields request) {
        Optional<TbObKendra> existingKendra =
                kendraRepository.findByKendraNameAndBranchId(request.getKendraName(), request.getBranchId());
        if (existingKendra.isPresent()) {
            throw new IllegalArgumentException("Kendra already exists.");
        }
    }
    private TbObKendra prepareKendraEntity(CreateKendraRequestFields request, Header header) {
        return TbObKendra.builder()
                .kendraName(request.getKendraName())
                .branchId(request.getBranchId())
                .kmId(request.getKmId())
                .addressLine1(request.getAddressLine1())
                .state(request.getState())
                .district(request.getDistrict())
                .village(request.getVillage())
                .pincode(request.getPincode())
                .gpsLatitude(request.getGpsLatitude())
                .gpsLongitude(request.getGpsLongitude())
                .distanceFromBranch(request.getDistanceFromBranch())
                .meetingDay(request.getMeetingDay())
                .meetingTime(request.getMeetingTime())
                .meetingPlace(request.getMeetingPlace())
                .meetingFrequency(request.getMeetingFrequency())
                .firstMeetingDate(request.getFirstMeetingDate())
                .photoDocId(request.getPhotoDocId())
                .payload(request.getPayload().toString())
                .status("PENDING")
                .blacklistStatus("CLEAR")
                .createdBy(header.getUserId())
                .createdTs(LocalDateTime.now())
                .updatedBy(header.getUserId())
                .updatedTs(LocalDateTime.now())
                .build();
    }
    private void performBlacklistValidation(CreateKendraRequestFields request) {
        logger.debug("Performing blacklist validation.");
        /*
         * TODO
         * Call Blacklist Interface
         *
         * If blacklisted
         * throw exception
         */
    }
    private void prepareSuccessResponse(Response response, TbObKendra tbObKendra) {
        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setHttpStatus(HttpStatus.OK);
        responseHeader.setResponseMessage("Kendra created successfully.");
        ResponseBody responseBody = new ResponseBody();
        CreateKendraResponse createKendraResponse = CreateKendraResponse.builder()
                .kendraId(tbObKendra.getKendraId())
                .status(tbObKendra.getStatus())
                .build();
        responseBody.setResponseObj(String.valueOf(createKendraResponse));
        response.setResponseHeader(responseHeader);
        response.setResponseBody(responseBody);
    }


    @CircuitBreaker(name = "fallback", fallbackMethod = "cbCheckFallback")
    public Mono<Object> onboardingCbCheck(CbRequest cbRequest, Header header, String schedulerFlag) {
        logger.debug("Printing Onboarding cbRequest: {}", cbRequest);
        this.cbRequest = cbRequest;
        this.header = header;
        logger.debug("calling Onboarding cbCheck external service");
        try {
            List<String> statuses = Arrays.asList("REJECTED", "DISBURSED", "CANCELLED");
            List<TbObApplicationMaster> appMasterDetails = applicationMasterRepository
                    .findApplicationBasedOnApplicationId(cbRequest.getRequestObj().getApplicationId(), statuses);
            logger.debug("Printing Onboarding appMasterDetails: {}", appMasterDetails);
            if (!appMasterDetails.isEmpty()) {
                logger.debug("Loan application {} has already been processed with status: {}",
                        cbRequest.getRequestObj().getApplicationId(), appMasterDetails.get(0).getStatus());
                Throwable error = new Throwable("Loan application has already been processed");
                return cbCheckFallback(cbRequest, header, schedulerFlag, error);
            }
        //    WorkFlow not present for edit flow.

           // CREATE OR CHECK WITH OTHERS
            if ("N".equalsIgnoreCase(cbRequest.getRequestObj().getSchedulerEnabled())
                    && "Y".equalsIgnoreCase(cbRequest.getRequestObj().getCbRecheck())) {
                logger.debug("Printing Onboarding schedularEnabled: {}", cbRequest.getRequestObj().getSchedulerEnabled());
                logger.debug("Printing Onboarding CbRecheck: {}", cbRequest.getRequestObj().getCbRecheck());

                Optional<TbObApplnWorkflow> applicationWorkflow = applnWfRepository
                        .findTopByApplicationIdOrderByCreatedTsDesc(cbRequest.getRequestObj().getApplicationId());
                logger.debug("Printing applicationWorkflow: {}", applicationWorkflow);
                if (applicationWorkflow == null || applicationWorkflow.isEmpty()) {
                    logger.debug("WorkFlow not present for edit flow.");
                    Throwable error = new Throwable("WorkFlow not present for edit flow.");
                    return cbCheckFallback(cbRequest, header, schedulerFlag, error);
                }
            }
            // if mobile number is not there ,that handle it
            Optional<TbObCustomer> custDtls = customerRepository
                    .findByApplicationId(cbRequest.getRequestObj().getApplicationId());
            logger.debug("Printing Onboarding custDtls: {}", custDtls);
            if (custDtls.isPresent()) {
                JSONObject kycJson = new JSONObject(custDtls.get().getKycDetails());
                logger.debug("Printing kycJson: {}", kycJson);
                if (!kycJson.has("mobileNum") ||
                        kycJson.isNull("mobileNum") ||
                        kycJson.getString("mobileNum").trim().isEmpty()){
                    logger.debug("Mobile number is not present.");
                    Throwable error = new Throwable("Mobile number is not present");
                    return cbCheckFallback(cbRequest, header, schedulerFlag, error);
                }
            }
        } catch (Exception e) {
            logger.error("Exception occurred while checking application status for ID {}",
                    cbRequest.getRequestObj().getApplicationId(), e);
        }
        ResponseBody responseBody = new ResponseBody();
        Response response = new Response();
        ResponseWrapper responseWrapper = new ResponseWrapper();

        return Mono.fromCallable(() -> onboardingCbCheckRequest(cbRequest))
                .flatMap(cbCheckRequestResponse -> cbCheckRequestResponse).map(val -> {
                    ObjectMapper objectMapper =new ObjectMapper();
                    OnboardingBRERequest cbCheckReq =objectMapper.convertValue(val, OnboardingBRERequest.class);
                    logger.debug("ONBOARDING cbCheck request before calling external service::\"+  cbCheckReq");
                    return  cbCheckReq;
                }).flatMap(cbCheckReq -> {
                    Timestamp reqTs = new Timestamp(System.currentTimeMillis());
                    logger.debug("Header  ::::"+header);
                    logger.debug("cbCheckReq::::"+cbCheckReq);
                    logger.debug("cbRequest.getInterfaceName()  ::::"+cbRequest.getInterfaceName());
                    Mono<Object> cbRes = this.interfaceAdapter.callExternalService(header, cbCheckReq,
                            cbRequest.getInterfaceName(), true);
                    return saveOnboardingCbCheckData(cbRes, cbCheckReq, header, cbRequest, reqTs, schedulerFlag).flatMap(val2 -> {
                        logger.debug("CB CHECK RESPONSE STR : {}", val2);
                        responseBody.setResponseObj(new Gson().toJson(val2));
                        response.setResponseBody(responseBody);
                        responseWrapper.setApiResponse(response);
                        return Mono.just(val2);
                    });
                }).onErrorResume(ex -> {
                    logger.error("Exception in cbCheck reactive chain", ex);
                    return cbCheckFallback(cbRequest, header, schedulerFlag, ex);
                });
    }

    private Mono<Object> onboardingCbCheckRequest(CbRequest cbRequest) {


        OnboardingBRERequest cbCheckReq = new OnboardingBRERequest();
        OnboardingBRERequestFields reqFields = new OnboardingBRERequestFields();
        Applicant applicant = new Applicant();
        List<HouseholdMemberPayload> householdMembersList = new ArrayList<>();
        List<AddressPayload> addressPayloadList = new ArrayList<>();
        List<DocumentPayload> docPayloadList = new ArrayList<>();

        Optional<TbObApplicationMaster> appMasterList = applicationMasterRepository
                .findByApplicationId(cbRequest.getRequestObj().getApplicationId());
        Optional<TbObCustomer> custDtls = customerRepository
                .findByApplicationId(cbRequest.getRequestObj().getApplicationId());  //CHECK
        List<TbObAddress> addressData = addressRepository
                .findByApplicationId(cbRequest.getRequestObj().getApplicationId());
        Optional<TbObLoan> loanData = loanRepository
                .findByApplicationId(cbRequest.getRequestObj().getApplicationId());
        List<TbObFamilyMember> familyMembers =familyMemberRepository
                .findByApplicationId(cbRequest.getRequestObj().getApplicationId());
        Optional<TbObCustOthers> tbObCustOthers=  tbObCustOthersRepository
                .findByApplicationId(cbRequest.getRequestObj().getApplicationId());


        //APPLICANT ADDRESS
        if (!addressData.isEmpty()) {
            String type1 = addressData.get(0).getAddressType();
            String type2 = addressData.get(1).getAddressType();

            if (type1.equalsIgnoreCase(type2)) {
                TbObAddress addDomain = addressData.get(0);
                JSONObject payloadObj = null;
                try {
                    payloadObj = new JSONObject(new ObjectMapper().writeValueAsString(addDomain.getAddrPayload()));
                } catch (JsonProcessingException e) {
                    logger.debug("Exception", e.getStackTrace());
                }
                AddressPayload addPayload = new AddressPayload();

                if (payloadObj.has(CommonConstants.ADDLINE1)) {
                    String addLine1 = payloadObj.optString(CommonConstants.ADDLINE1, "");
                    String addLine2 = payloadObj.optString(CommonConstants.ADDLINE2, "");
                    String addLine3 = payloadObj.optString(CommonConstants.ADDLINE3, "");

                    if (!addLine1.isEmpty()) {
                        addLine1 = addLine1.replaceAll("[^a-zA-Z0-9]", " ");
                    }
                    if (!addLine2.isEmpty()) {
                        addLine2 = addLine2.replaceAll("[^a-zA-Z0-9]", " ");
                    }
                    if (!addLine3.isEmpty()) {
                        addLine3 = addLine3.replaceAll("[^a-zA-Z0-9]", " ");
                    }

                    String address = (addLine1 + " " + addLine2 + " " + addLine3).trim();
                    addPayload.setAddrLine1(address);
                }
                if (payloadObj.has(CommonConstants.VILLAGELOCALITY)) {
                    addPayload.setCity(payloadObj.getString(CommonConstants.VILLAGELOCALITY));
                    applicant.setVillageName(payloadObj.getString(CommonConstants.VILLAGELOCALITY));
                }
                if (payloadObj.has(CommonConstants.DISTRICT)) {
                    addPayload.setDistrict(payloadObj.getString(CommonConstants.DISTRICT));
                }
                if (payloadObj.has(CommonConstants.STATE)) {
                    addPayload.setState(payloadObj.getString(CommonConstants.STATE));
                    applicant.setStateBranch(payloadObj.getString(CommonConstants.STATE));
                }
                if (payloadObj.has(CommonConstants.PINCODE)) {
                    addPayload.setPinCode(payloadObj.getString(CommonConstants.PINCODE));
                }

                // Use whatever addrType is stored in DB
                String givenAddrType = addDomain.getAddressType();
                if(givenAddrType.equalsIgnoreCase("P")) addPayload.setAddrType("1");
                else if(givenAddrType.equalsIgnoreCase("C")) addPayload.setAddrType("2");

                addressPayloadList.add(addPayload);

                // Second entry gets the opposite addrType
                String otherAddrType = "P".equalsIgnoreCase(givenAddrType) ? "C" : "P";  //  1 OR 2
                AddressPayload addPayload2 = new AddressPayload();
                addPayload2.setAddrLine1(addPayload.getAddrLine1());
                addPayload2.setCity(addPayload.getCity());
                addPayload2.setDistrict(addPayload.getDistrict());
                addPayload2.setState(addPayload.getState());
                addPayload2.setPinCode(addPayload.getPinCode());
                if(otherAddrType.equalsIgnoreCase("P")) addPayload2.setAddrType("1");
                else if(otherAddrType.equalsIgnoreCase("C")) addPayload2.setAddrType("2");
                addressPayloadList.add(addPayload2);
            }
            else{
                // Two addresses — set as-is
                for (TbObAddress addDomain : addressData) {
                    AddressPayload addPayload = new AddressPayload();
                    logger.debug("addPayload :"+addPayload);
                    JSONObject payloadObj = new JSONObject(addDomain.getAddrPayload());

                    logger.debug("payloadObj :"+payloadObj);
                    String addType=addDomain.getAddressType();
                    if(addType.equalsIgnoreCase("P")) addPayload.setAddrType("1");
                    else if(addType.equalsIgnoreCase("C")) addPayload.setAddrType("2");

                    if (payloadObj.has(CommonConstants.ADDLINE1)) {
                        String addLine1 = payloadObj.getString(CommonConstants.ADDLINE1);
                        if (StringUtils.isNotBlank(addLine1)) {
                            addLine1 = addLine1.replaceAll("[^a-zA-Z0-9]", " ");
                        }
                        addPayload.setAddrLine1(addLine1);
                    }
                    if (payloadObj.has(CommonConstants.VILLAGELOCALITY)) {
                        addPayload.setCity(payloadObj.getString(CommonConstants.VILLAGELOCALITY));
                        applicant.setVillageName(payloadObj.getString(CommonConstants.VILLAGELOCALITY));
                    }
                    if (payloadObj.has(CommonConstants.DISTRICT)) {
                        addPayload.setDistrict(payloadObj.getString(CommonConstants.DISTRICT));
                    }
                    try{
                    if (payloadObj.has(CommonConstants.STATE)) {
                        addPayload.setState(String.valueOf(payloadObj.get(CommonConstants.STATE)));
                    }}
                    catch (Exception e)
                    {
                        logger.debug("Exception for state ", e);
                    }
                    if (payloadObj.has(CommonConstants.PINCODE)) {
                        addPayload.setPinCode(payloadObj.getString(CommonConstants.PINCODE));
                    }
                    addressPayloadList.add(addPayload);
                }
            }
        }
        applicant.setAddress(addressPayloadList);

        //HOUSEHOLD MEMBER
        if( !familyMembers.isEmpty())
        {
            for(TbObFamilyMember familyMember: familyMembers) {
                logger.debug("familyMember "+familyMember);
                if (familyMember.getIsEarningMember() != null && Boolean.TRUE.equals(familyMember.getIsEarningMember())) {
                    logger.debug("familyMember inside "+familyMember);
                    HouseholdMemberPayload householdMember = new HouseholdMemberPayload();
                    householdMember.setApplicantType("H");
                    householdMember.setRelationtype(familyMember.getRelation());
                    householdMember.setDob(String.valueOf(familyMember.getDob()));
                    householdMember.setGender(familyMember.getGender());
                    householdMember.setPhone(familyMember.getMobileNum());
                    householdMember.setCustName(familyMember.getName());
                    householdMember.setEarningFlag(String.valueOf(familyMember.getIsEarningMember()));

                    if(familyMember.getMemberType().equalsIgnoreCase("P"))
                    {
                        applicant.setDepName(familyMember.getName());
                        applicant.setDepType(familyMember.getRelation());
                    }
                    // Document
                    List<DocumentPayload> docList = new ArrayList<>();
                    if (familyMember.getKycType() != null && familyMember.getKycDocId() != null) {
                        DocumentPayload docPayload = new DocumentPayload();
                        docPayload.setDocType(familyMember.getKycType());
                        docPayload.setDocId(familyMember.getKycDocId());
                        docList.add(docPayload);
                    }
                    householdMember.setDocument(docList);
                    householdMember.setAddress(new ArrayList<>(addressPayloadList)); // SETTING SAME AS APPLICANT ADDRESS
                    householdMembersList.add(householdMember);
                }
            }

        }
        reqFields.setHousehold_member(householdMembersList);

        //APPLICANT REMAINING FIELDS
        if (custDtls.isPresent()) {
            try {
                applicant.setDob(custDtls.get().getDob());
                String docTypeStr = custDtls.get().getPrimaryKycType();
                String docIdStr = custDtls.get().getPrimaryKycId();
                if (StringUtils.isNotBlank(docTypeStr) && StringUtils.isNotBlank(docIdStr)) {
                    List<String> docTypList = Arrays.asList(docTypeStr.split(",")).stream()
                            .filter(a -> StringUtils.isNotBlank(a)).collect(Collectors.toList());

                    List<String> docIdList = Arrays.asList(docIdStr.split(",")).stream()
                            .filter(a -> StringUtils.isNotBlank(a)).collect(Collectors.toList());
                    for (int i = 0; i < docTypList.size(); i++) {
                        DocumentPayload docPayload = new DocumentPayload();
                        docPayload.setDocType(docTypList.get(i));
                        docPayload.setDocId(docIdList.get(i).replaceAll("[^a-zA-Z0-9]", ""));
                        docPayloadList.add(docPayload);
                    }
                }
            JSONObject kycJson = new JSONObject(custDtls.get().getKycDetails());
            if (kycJson.has("mobileNum")) {
               applicant.setPhoneNumber(kycJson.getString("mobileNum"));
            }
                applicant.setDocument(docPayloadList);
            } catch (Exception e) {
                logger.debug("Exception", e.getStackTrace());

            }
        }
        applicant.setActivationDate("");
        applicant.setCustName(custDtls.get().getCustomerName());
        applicant.setEmail("");  // HARDCODE
        applicant.setSlNo("1");   // HARDCODE
        applicant.setCategoryId("1");  // HARDCODE
        applicant.setProductCode("CCR"); // HARDCODE
        applicant.setDurationOfAgreement("10");  //  HARDCODE MUST ASK PRABHA
        applicant.setBankProductId("01"); // HARDCODE
        applicant.setGender("2");//  HARDCODE
        applicant.setLoanType("2");  //  HARDCODE MUST ASK PRABHA
        applicant.setAppId(cbRequest.getRequestObj().getApplicationId());
        applicant.setLosIndex("LOS"); //  HARDCODE
        applicant.setLosIndicator("1"); //  HARDCODE MUST ASK PRABHA

        String maritalStatus = "0";
        if (custDtls.isPresent()) {
            JSONObject kycJson = new JSONObject(custDtls.get().getKycDetails());
            if (kycJson.has("maritalStatus")) {
                maritalStatus = kycJson.getString("maritalStatus").trim().toUpperCase();
            }
        }
        String maritalCode;
        switch (maritalStatus) {
            case "COHABITATING":
                maritalCode = "1";
                break;
            case "DIVORCED":
                maritalCode = "2";
                break;
            case "MARRIED":
                maritalCode = "3";
                break;
            case "NOTASKED":
                maritalCode = "4";
                break;
            case "NOTGIVEN":
                maritalCode = "5";
                break;
            case "OTHER":
                maritalCode = "6";
                break;
            case "SEPARATED":
                maritalCode = "7";
                break;
            case "UNMARRIED":
                maritalCode = "8";
                break;
            case "TOBEMARRIED":
                maritalCode = "9";
                break;
            case "WIDOW":
                maritalCode = "10";
                break;
            case "SINGLE":
                maritalCode = "11";
                break;
            case "0":
                maritalCode = "5";
                break;
            default:
                maritalCode = "5";
        }
        applicant.setMaritalStatus(maritalCode);

        if (appMasterList.isPresent()) {
            applicant.setKendra(appMasterList.get().getKendraId());
            applicant.setBranch(appMasterList.get().getBranchId());
            applicant.setCustId(appMasterList.get().getCustomerId().toString());
            applicant.setPhoneNumber(appMasterList.get().getMobileNumber());
        }

        // CHECK IN MAITRI, UNNATI CB
        applicant.setDigiAgilDFAFlag("MAITRI");
        applicant.setEarningFlag("ABC");   //  HARDCODE MUST ASK PRABHA
        applicant.setEnquiryType("A");  //  HARDCODE MUST ASK PRABHA

        if(loanData.isPresent())
        {
            applicant.setLoanId(loanData.get().getLoanId());
            applicant.setLoanProductType(loanData.get().getProduct());
            applicant.setProduct_code(loanData.get().getProduct());
            applicant.setLoanAmount(loanData.get().getAmount().toString());
            JSONObject freqPayload = new JSONObject(loanData.get().getFreq());
            if (freqPayload.has("idDesc")) {
                applicant.setAppliedFrequency(freqPayload.getString("idDesc"));
            }
            if (StringUtils.isNotBlank(String.valueOf(loanData.get().getCharges()))) {
                JSONObject insurance =  new JSONObject(loanData.get().getCharges());
                if(!insurance.isEmpty())
                {
                    if(insurance.has("sp_insu")){
                        applicant.setSpouseInsurance(insurance.getString("sp_insu"));}
                    if(insurance.has("mem_insu")){
                        applicant.setApplicantInsurance(String.valueOf(insurance.has("mem_insu")));}
                   if(insurance.has("mem_insu")){
                       applicant.setApplicantInsurance(String.valueOf(insurance.has("mem_insu")));}
                   if(insurance.has("mem_insu")){
                        applicant.setApplicantInsurance(String.valueOf(insurance.has("mem_insu")));}

                }

//               applicant.setApplicantInsuranceAmt(insDtl.getApplicant_insurance_amt());
//                applicant.setSpouseInsuranceAmt(insDtl.getSpouse_insurance_amt());

                String term = loanData.get().getTerm();
                String termValue = term.replaceAll("\\D", "");
                applicant.setAppliedTermWeeks(termValue);
            }
        }
        else{
            applicant.setLoanProductType(staticLoanProductId);
            applicant.setProduct_code(staticLoanProductId);
            applicant.setLoanAmount(staticLoanAmount);
            applicant.setAppliedFrequency(staticLoanFrequency);
            applicant.setAppliedTermWeeks(staticLoanTerm);
            applicant.setApplicantInsurance(staticLoanMemberIns);
            applicant.setSpouseInsurance(staticLoanSpouseIns);
            applicant.setApplicantInsuranceAmt(staticLoanAmount);
            applicant.setSpouseInsuranceAmt(staticLoanSpouseInsAmount);

        }

        applicant.setReplacementMemberFlag("No");
        applicant.setSource("OTHERS");
        applicant.setApplicantType("P");
        applicant.setLosIndicator("1");
        applicant.setSpouseInsurance("");

        if(tbObCustOthers.isPresent()) {
            JSONObject incomeDetails= new JSONObject(tbObCustOthers.get().getIncomedet());
            if(!incomeDetails.isEmpty()){
                if(incomeDetails.has("income"))
                {
                    applicant.setHhAnnualIncome(incomeDetails.get("income").toString());
                }
            }
        }

        reqFields.setApplicant(applicant);
        cbCheckReq.setAppId(cbRequest.getAppId());
        cbCheckReq.setUserId(cbRequest.getUserId());
        cbCheckReq.setInterfaceName(cbRequest.getInterfaceName());
        cbCheckReq.setRequestObj(reqFields);
        logger.debug("CB CHECCK "+cbCheckReq);
        System.out.println(" CB CHECK 2"+cbCheckReq);
        return Mono.just(cbCheckReq);

    }

    public Mono<Object> cbCheckFallback(CbRequest cbRequest, Header header, String flag, Throwable ex) {
        logger.error("cbCheck fallback error {}: , request is:{} and header is : {} ", ex, cbRequest, header);
        return FallbackUtils.genericFallbackMonoObject();
    }

    private Mono<Object> saveOnboardingCbCheckData(Mono<Object> response, OnboardingBRERequest cbCheckRequest, Header header, CbRequest cbRequest, Timestamp reqTs, String schedulerFlag) {
        logger.debug("Onboarding cbRequest::" + cbRequest);
        logger.debug("Onboarding cb request ::" + cbCheckRequest);
        return response.flatMap(val -> {
            logger.debug("ONBOARDING RAW EXTERNAL RESPONSE>>>: {}", val);
            ResponseWrapper responseWrapper = adapterUtil.getResponseMapper(val, cbCheckRequest.getInterfaceName(), header, true);
            String respObj = responseWrapper.getApiResponse().getResponseBody().getResponseObj();
            logger.debug("ONBOARDING CB RESPONSE IN CB SAVE METHOD>>>>::" + respObj);
            String applicationId = cbRequest.getRequestObj().getApplicationId();
            JSONObject cbResJson = new JSONObject(respObj);
            boolean isSuccess = false;
            if (cbResJson.has("IRIS_message") && cbResJson.get("IRIS_message") != null
                    && (cbResJson.get("IRIS_message").toString().equalsIgnoreCase("SUCCESS")
                    || cbResJson.get("IRIS_message").toString().equalsIgnoreCase("bureau_data_issue_spouse_node_corrected_success"))) {
                isSuccess = true;
            }
            String finalDecision = cbResJson.optString("Final_Decision", "");
            String rejectionReason = cbResJson.optString("Rejection_reason", "");
            String cbCheckStatus = "FAILURE";
            if (isSuccess) {
                if (cbResJson.isNull("Rejection_reason") || rejectionReason.isEmpty()) {
                    cbCheckStatus = "SUCCESS";
                }
                if ("REJECT".equalsIgnoreCase(finalDecision) || "Deviation".equalsIgnoreCase(finalDecision)) {
                    cbCheckStatus = "FAILURE";
                }
            }
            Optional<TbObCbResponse> cbRes = cbResponseRepository.findByApplicationId(applicationId);
            if (cbRes.isPresent()) {
                logger.debug("Onboarding cbRes is present");
                TbObCbResponse cbResObj = cbRes.get();
                cbResObj.setCustomerId(cbCheckRequest.getRequestObj().getApplicant().getCustId());
                cbResObj.setApplicationId(applicationId);
                cbResObj.setApiName(cbRequest.getInterfaceName());
                cbResObj.setApiReqTs(LocalDateTime.now());
                cbResObj.setApiResTs(LocalDateTime.now());
                cbResObj.setUserRole(cbRequest.getRequestObj().getUserRole());
                cbResObj.setAppVer(cbRequest.getRequestObj().getAppVersion());
                cbResObj.setSchedulerStatus(schedulerFlag);

                try {
                    cbResObj.setRequestPayload(new ObjectMapper().writeValueAsString(cbCheckRequest));
                    cbResObj.setResponsePayload(respObj);
                } catch (JsonProcessingException e) {
                    logger.error("Error while serializing cbCheck request/response", e);
                }

                int currentRetry = 0;
                if (cbResObj.getRetryCount() != null) {
                    try {
                        currentRetry = Integer.parseInt(cbResObj.getRetryCount());
                    } catch (NumberFormatException e) {
                        currentRetry = 0;
                    }
                }
                if ("Y".equalsIgnoreCase(schedulerFlag)) {
                    cbResObj.setRetryCount(String.valueOf(currentRetry + 1));
                }

                cbResObj.setStatus(isSuccess ? "SUCCESS" : "FAILURE");
                cbResObj.setApiStatus(cbCheckStatus);

                cbResponseRepository.save(cbResObj);
            } else {
                logger.debug("Onboarding cbRes not present, creating new record");
                TbObCbResponse cbResponse = new TbObCbResponse();
                cbResponse.setApplicationId(applicationId);
                cbResponse.setCustomerId(cbCheckRequest.getRequestObj().getApplicant().getCustId());
                cbResponse.setApiName(cbRequest.getInterfaceName());
                cbResponse.setApiReqTs(LocalDateTime.now());
                cbResponse.setApiResTs(LocalDateTime.now());
                cbResponse.setUserRole(cbRequest.getRequestObj().getUserRole());
                cbResponse.setAppVer(cbRequest.getRequestObj().getAppVersion());
                cbResponse.setSchedulerStatus(schedulerFlag);
                cbResponse.setRetryCount("0");
                try {
                    cbResponse.setRequestPayload(new ObjectMapper().writeValueAsString(cbCheckRequest));
                    cbResponse.setResponsePayload(respObj);
                } catch (JsonProcessingException e) {
                    logger.error("Error while serializing cbCheck request/response", e);
                }

                // Status logic from response
                cbResponse.setStatus(isSuccess ? "SUCCESS" : "FAILURE");
                cbResponse.setApiStatus(cbCheckStatus);

                cbResponse.setCreateTs(new Timestamp(System.currentTimeMillis()));
                cbResponseRepository.save(cbResponse);
            }
            try {
                Optional<TbObApplicationMaster> appMasterOpt = applicationMasterRepository.findByApplicationId(applicationId);
                if (appMasterOpt.isPresent()) {
                    TbObApplicationMaster appMaster = appMasterOpt.get();

                    if (isSuccess) {
                        if ("REJECT".equalsIgnoreCase(finalDecision) || "Deviation".equalsIgnoreCase(finalDecision)) {
                            appMaster.setStatus("REJECTED");
                            logger.debug("Onboarding CB: Final Decision is REJECT/Deviation, setting status to REJECTED");
                        }
                    } else {
                        logger.debug("Onboarding CB: IRIS_message not SUCCESS, CB pending");
                    }

                    JSONObject addInfoJson;
                    try {
                        addInfoJson = (appMaster.getAddInfo1() != null && !appMaster.getAddInfo1().isEmpty())
                                ? new JSONObject(appMaster.getAddInfo1())
                                : new JSONObject();
                    } catch (Exception e) {
                        addInfoJson = new JSONObject();
                    }
                    addInfoJson.put("cbCheckStatus", cbCheckStatus);
                    addInfoJson.put("finalDecision", finalDecision);
                    addInfoJson.put("eir", cbResJson.optString("eir", "0"));
                    addInfoJson.put("FOIR", cbResJson.optString("final_foir_obligation", "0"));
                    appMaster.setAddInfo1(addInfoJson.toString());
                    appMaster.setUpdatedTs(LocalDateTime.now());
                    applicationMasterRepository.save(appMaster);
                    logger.debug("Onboarding ApplicationMaster updated for applicationId: {}", applicationId);
                }
            } catch (Exception e) {
                logger.error("Error while updating ApplicationMaster for applicationId: {}", applicationId, e);
            }

            return Mono.just(respObj);
        });
    }

   // lock method for RPC,KM and AM userrole.

    private boolean isRpcRole(String role) {
        return role != null && recordLockRpcRole.equalsIgnoreCase(role);
    }

    private long getRpcInactivityThresholdMillis() {
        return recordLockRpcInactivityMinutes * 60_000L;
    }

    @Transactional
    public void lockApplication(String applicationId, String userId, String userRole) throws Exception {

        if (applicationId == null || applicationId.isBlank()) {
            throw new Exception("Application ID is required.");
        }

        if (userId == null || userId.isBlank()) {
            throw new Exception("User ID is required.");
        }

        if (userRole == null || userRole.isBlank()) {
            throw new Exception("User role is required.");
        }

        userRole = userRole.trim().toUpperCase();

        // Only RPC can acquire an application lock.
        if (!isRpcRole(userRole)) {
            throw new Exception(
                    "Only RPC user is allowed to edit the application.");
        }

        long currentTime = System.currentTimeMillis();

        Optional<TbObRecordLockEntity> lockOpt = tbObRecordLockRepo.findActiveByApplicationId(applicationId);
        if (lockOpt.isEmpty()) {
            createRpcLock(
                    applicationId,
                    userId,
                    userRole,
                    currentTime
            );
            return;
        }
        TbObRecordLockEntity existingLock = lockOpt.get();
        String existingUserId = existingLock.getLockedBy();
        String existingUserRole = existingLock.getLockedByRole();

        if (userId.equalsIgnoreCase(existingUserId) && userRole.equalsIgnoreCase(existingUserRole)) {
            validateRpcActivity(
                    applicationId,
                    userId,
                    currentTime
            );
            existingLock.setLockedAt(currentTime);
            existingLock.setLockType(LOCK_TYPE_PERMANENT);
            existingLock.setLockExpiryTs(null);
            existingLock.setStatus(LOCK_STATUS_ACTIVE);
            tbObRecordLockRepo.save(existingLock);
            return;
        }

        if (isRpcRole(existingUserRole)) {
            boolean existingRpcActive = isRpcActive(
                    applicationId,
                    existingUserId,
                    currentTime
            );

            if (existingRpcActive) {
                throw new Exception(
                        "Application is currently being accessed by RPC User ID: "
                                + existingUserId
                                + ". RPC has priority. Please try again later."
                );
            }
            existingLock.setStatus(LOCK_STATUS_EXPIRED);
            existingLock.setReleasedAt(currentTime);
            tbObRecordLockRepo.save(existingLock);
            createRpcLock(
                    applicationId,
                    userId,
                    userRole,
                    currentTime
            );

            return;
        }

        existingLock.setStatus(LOCK_STATUS_EXPIRED);
        existingLock.setReleasedAt(currentTime);
        tbObRecordLockRepo.save(existingLock);
        createRpcLock(
                applicationId,
                userId,
                userRole,
                currentTime
        );
    }
    private void createRpcLock(
            String applicationId,
            String userId,
            String userRole,
            long currentTime) {
        TbObRecordLockEntity lock = TbObRecordLockEntity.builder()
                .applicationId(applicationId)
                .lockedBy(userId)
                .lockedByRole(userRole)
                .lockType(LOCK_TYPE_PERMANENT)
                .lockedAt(currentTime)
                .lockExpiryTs(null)
                .status(LOCK_STATUS_ACTIVE)
                .releasedAt(null)
                .build();
        tbObRecordLockRepo.save(lock);
    }
    private boolean isRpcActive(
            String applicationId,
            String userId,
            long currentTime) {
        Optional<LocalDateTime> lastActivityOpt =
                tbObUserAuditTrailRepo
                        .findLastActivityByApplicationIdAndUserId(
                                applicationId,
                                userId
                        );
        long lastActivityEpoch;
        if (lastActivityOpt.isPresent()) {
            lastActivityEpoch = lastActivityOpt.get()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } else {
            return false;
        }
        long inactivityThresholdMillis = getRpcInactivityThresholdMillis();
        return currentTime - lastActivityEpoch
                <= inactivityThresholdMillis;
    }

    private void validateRpcActivity(
            String applicationId,
            String userId,
            long currentTime) throws Exception {
        if (!isRpcActive(applicationId, userId, currentTime)) {
            throw new Exception(
                    "RPC user session has been inactive for more than "
                            + recordLockRpcInactivityMinutes
                            + " minutes. Please login again."
            );
        }
    }

    @Transactional
    public void unlockApplication(
            String applicationId,
            String userId,
            String userRole) throws Exception {

        if (applicationId == null || applicationId.isBlank()) {
            throw new Exception("Application ID is required.");
        }

        if (userId == null || userId.isBlank()) {
            throw new Exception("User ID is required.");
        }

        if (userRole == null || userRole.isBlank()) {
            throw new Exception("User role is required.");
        }

        userRole = userRole.trim().toUpperCase();

        if (!isRpcRole(userRole)) {
            throw new Exception(
                    "Only RPC user is allowed to unlock the application.");
        }

        Optional<TbObRecordLockEntity> lockOpt = tbObRecordLockRepo.findActiveByApplicationId(applicationId);
        if (lockOpt.isEmpty()) {
            return;
        }
        TbObRecordLockEntity lock = lockOpt.get();
        if (!userId.equalsIgnoreCase(lock.getLockedBy())) {
            throw new Exception(
                    "Application is locked by another RPC user. "
                            + "Only the user who acquired the lock can unlock it."
            );
        }
        if (!isRpcRole(lock.getLockedByRole())) {
            throw new Exception(
                    "Invalid lock owner role for RPC unlock operation."
            );
        }
        lock.setStatus(LOCK_STATUS_RELEASED);
        lock.setReleasedAt(System.currentTimeMillis());
        tbObRecordLockRepo.save(lock);
    }
    //for MBDF transfer
@Transactional
public Response transferApplication(TransferApplicationRequest request) {
    logger.info("Start : transferApplication :: {}", request);

    if (request == null || request.getRequestObj() == null) {
        throw new IllegalArgumentException("Transfer request object is missing.");
    }
    TransferApplicationRequestObj requestObj = request.getRequestObj();

    String fromGroupId = requireValue(requestObj.getFromGroupId(), "fromGroupId");
    String toGroupId = requireValue(requestObj.getToGroupId(), "toGroupId");
    String fromKendraId = requireValue(requestObj.getFromKendraId(), "fromKendraId");
    String toKendraId = requireValue(requestObj.getToKendraId(), "toKendraId");
    String userId = request.getUserId();
    logger.info(
            "Transfer Request -> fromGroup={}, toGroup={}, fromKendra={}, toKendra={}, applications={}",
            fromGroupId,
            toGroupId,
            fromKendraId,
            toKendraId,
            requestObj.getApplicationDtls()
    );

    if (fromGroupId.equals(toGroupId)) {
        throw new IllegalArgumentException("Source and destination groups cannot be the same.");
    }

    boolean groupTransfer = (requestObj.getApplicationDtls() == null || requestObj.getApplicationDtls().isEmpty());

    List<TbObGroupMappingHistory> latestRows = new ArrayList<>();
    if (groupTransfer) {
        latestRows = groupMappingHistoryRepository.findCurrentApplicationsByGroup(fromGroupId);
        if (latestRows == null || latestRows.isEmpty()) {
            throw new IllegalArgumentException("No applications found in source group " + fromGroupId + " to transfer.");
        }
    } else {
        Set<String> seen = new HashSet<>();
        for (TransferApplicationDetails details : requestObj.getApplicationDtls()) {
            if (details == null || details.getApplicationId() == null || details.getApplicationId().trim().isEmpty()) {
                throw new IllegalArgumentException("applicationId is missing in applicationDtls.");
            }
            String applicationId = details.getApplicationId().trim();
            if (!seen.add(applicationId)) {
                continue;
            }

            TbObGroupMappingHistory latest = groupMappingHistoryRepository.findLatestApplication(applicationId);
            logger.info(
                    "Latest Mapping -> applicationId={}, fromGroup={}, toGroup={}, fromKendra={}, toKendra={}, createdTs={}",
                    applicationId,
                    latest != null ? latest.getFromGroupId() : null,
                    latest != null ? latest.getToGroupId() : null,
                    latest != null ? latest.getFromKendraId() : null,
                    latest != null ? latest.getToKendraId() : null,
                    latest != null ? latest.getCreatedTs() : null
            );
            if (latest == null) {
                throw new IllegalArgumentException("No mapping history found for applicationId: " + applicationId + ".");
            }
            String currentGroupId = latest.getToGroupId() != null
                    ? latest.getToGroupId()
                    : latest.getFromGroupId();

            logger.info(
                    "Validation -> applicationId={}, requestFromGroup={}, calculatedCurrentGroup={}, fromGroupInDB={}, toGroupInDB={}",
                    applicationId,
                    fromGroupId,
                    currentGroupId,
                    latest.getFromGroupId(),
                    latest.getToGroupId()
            );
            if (!fromGroupId.equals(currentGroupId)) {
                throw new IllegalArgumentException(
                        "Application " + applicationId
                                + " does not currently belong to source group " + fromGroupId + ".");
            }
            latestRows.add(latest);
        }
    }

    int movingCount = latestRows.size();
    int destinationCount = safeCount(groupMappingHistoryRepository.getCurrentGroupCount(toGroupId));
    logger.info(
            "Transfer Count Check -> movingCount={}, destinationCount={}, totalAfterTransfer={}, destinationGroup={}",
            movingCount,
            destinationCount,
            destinationCount + movingCount,
            toGroupId
    );
    if (destinationCount + movingCount > GROUP_MAX_MEMBERS) {
        throw new IllegalArgumentException("Destination group cannot exceed maximum limit of " + GROUP_MAX_MEMBERS + " members.");
    }

    LocalDateTime createdTs = LocalDateTime.now();
    LocalDateTime updatedTs = LocalDateTime.now();
    LocalDate mappingDate = LocalDate.now();
    String mappingReason = (requestObj.getMappingReason() != null && !requestObj.getMappingReason().trim().isEmpty())
            ? requestObj.getMappingReason() : request.getRemarks();

    int preMemberCount = destinationCount;
    int newMemberCount = destinationCount + movingCount;

    for (TbObGroupMappingHistory latest : latestRows) {
        String applicationId = latest.getApplicationId();

        TbObApplicationMaster master = applicationMasterRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application master record not found for applicationId: " + applicationId + "."));

        TbObGroupMappingHistory row = TbObGroupMappingHistory.builder()
                .applicationId(applicationId)
                .fromGroupId(fromGroupId)
                .fromKendraId(fromKendraId)
                .toGroupId(toGroupId)
                .toKendraId(toKendraId)
                .customerCurrentStatus(latest.getCustomerCurrentStatus())
                .mappingType(MAPPING_TYPE_TRANSFER)
                .preMemberCount(String.valueOf(preMemberCount))
                .currentMemberCount(String.valueOf(newMemberCount))
                .relatedGroupId(toGroupId)
                .affectedCustomerId(master.getCustomerId())
                .mappingReason(mappingReason)
                .mappingDate(mappingDate)
                .dmsFolderIdx(latest.getDmsFolderIdx())
                .photoDocId(latest.getPhotoDocId())
                .createdBy(userId)
                .createdTs(createdTs)
                .build();

        logger.info(
                "Saving Mapping -> applicationId={}, fromGroup={}, toGroup={}, fromKendra={}, toKendra={}",
                row.getApplicationId(),
                row.getFromGroupId(),
                row.getToGroupId(),
                row.getFromKendraId(),
                row.getToKendraId()
        );
        groupMappingHistoryRepository.save(row);

        master.setGroupId(toGroupId);
        master.setKendraId(toKendraId);
        master.setUpdatedBy(userId);
        master.setUpdatedTs(updatedTs);
        applicationMasterRepository.save(master);
    }

    updateGroupCountsAfterTransfer(fromGroupId, toGroupId, movingCount, userId, updatedTs);
    if (groupTransfer && !fromKendraId.equals(toKendraId)) {
        updateKendraGroupCountsAfterTransfer(fromKendraId, toKendraId, userId, updatedTs);
    }

    logger.info("End : transferApplication :: moved {} application(s) from group {} to group {}", movingCount, fromGroupId, toGroupId);

    Response response = new Response();
    ResponseHeader responseHeader = new ResponseHeader();
    responseHeader.setHttpStatus(HttpStatus.OK);
    responseHeader.setResponseMessage(movingCount + " application(s) transferred successfully from group "
            + fromGroupId + " to group " + toGroupId + ".");
    ResponseBody responseBody = new ResponseBody();
    responseBody.setResponseObj("{\"transferredCount\":" + movingCount
            + ",\"fromGroupId\":" + fromGroupId + ",\"toGroupId\":" + toGroupId + "}");
    response.setResponseHeader(responseHeader);
    response.setResponseBody(responseBody);
    return response;
}

    private String requireValue(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is mandatory for transfer.");
        }
        return value.trim();
    }
    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private void updateGroupCountsAfterTransfer(String fromGroupId,
                                                String toGroupId,
                                                int movingCount,
                                                String userId,
                                                LocalDateTime updatedTs) {
        TbObGroup fromGroup = groupRepository.findByGroupId(fromGroupId)
                .orElseThrow(() -> new IllegalArgumentException("Source group record not found for groupId: " + fromGroupId + "."));
        int fromTotal = parseIntOrZero(fromGroup.getTotalMemberCount()) - movingCount;          // CHANGED
        fromGroup.setTotalMemberCount(String.valueOf(fromTotal < 0 ? 0 : fromTotal));           // CHANGED
        fromGroup.setReleasedCount(String.valueOf(parseIntOrZero(fromGroup.getReleasedCount()) + movingCount));
        fromGroup.setUpdatedBy(userId);
        fromGroup.setUpdatedTs(updatedTs);
        groupRepository.save(fromGroup);

        TbObGroup toGroup = groupRepository.findByGroupId(toGroupId)
                .orElseThrow(() -> new IllegalArgumentException("Destination group record not found for groupId: " + toGroupId + "."));
        toGroup.setTotalMemberCount(String.valueOf(parseIntOrZero(toGroup.getTotalMemberCount()) + movingCount)); // CHANGED
        toGroup.setUpdatedBy(userId);
        toGroup.setUpdatedTs(updatedTs);
        groupRepository.save(toGroup);
    }

    private void updateKendraGroupCountsAfterTransfer(String fromKendraId,
                                                      String toKendraId,
                                                      String userId,
                                                      LocalDateTime updatedTs) {
        TbObKendra fromKendra = kendraRepository.findByKendraId(fromKendraId)
                .orElseThrow(() -> new IllegalArgumentException("Source kendra record not found for kendraId: " + fromKendraId + "."));
        int fromGroups = safeCount(fromKendra.getTotalGroupCount()) - 1;        // CHANGED
        fromKendra.setTotalGroupCount(fromGroups < 0 ? 0 : fromGroups);         // CHANGED
        fromKendra.setUpdatedBy(userId);
        fromKendra.setUpdatedTs(updatedTs);
        kendraRepository.save(fromKendra);

        TbObKendra toKendra = kendraRepository.findByKendraId(toKendraId)
                .orElseThrow(() -> new IllegalArgumentException("Destination kendra record not found for kendraId: " + toKendraId + "."));
        toKendra.setTotalGroupCount(safeCount(toKendra.getTotalGroupCount()) + 1); // CHANGED
        toKendra.setUpdatedBy(userId);
        toKendra.setUpdatedTs(updatedTs);
        kendraRepository.save(toKendra);
    }

    private int parseIntOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

}