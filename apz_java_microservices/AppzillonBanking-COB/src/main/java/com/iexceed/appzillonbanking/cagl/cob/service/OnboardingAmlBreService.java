package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.Gson;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;

import com.iexceed.appzillonbanking.cagl.cob.repository.ab.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.*;

import com.iexceed.appzillonbanking.core.payload.*;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import com.iexceed.appzillonbanking.core.utils.FallbackUtils;
import com.iexceed.appzillonbanking.interfaceAdapter.service.InterfaceAdapter;
import com.iexceed.appzillonbanking.interfaceAdapter.utils.AdapterUtil;
import io.micrometer.common.util.StringUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;
@Service
public class OnboardingAmlBreService {

    private static final Logger logger = LogManager.getLogger(OnboardingAmlBreService.class);

    private static final String AML_INTERFACE_ID = "amlCheck";
    private static final String BRE_INTERFACE_ID = "OnboardingCBCheck";
    private static final String EXCEPTION_MSG = "Exception occurred";
    private static final String EXCEPTION_OCCURED = "EXCEPTION_OCCURED";

    @Value("${onboarding.aml.product:Micro Loan}")
    private String staticAmlProduct;

    @Value("${onboarding.aml.customerType:GL}")
    private String staticAmlCustomerType;

    @Value("${onboarding.aml.reason:AML Check}")
    private String staticAmlReason;

    @Autowired
    private TbObApplicationMasterRepository applicationMasterRepository;

    @Autowired
    private TbObCustomerRepository customerRepository;

    @Autowired
    private TbObAddressRepository addressRepository;

    @Autowired
    private TbObLoanRepository loanRepository;

    @Autowired
    private TbObFamilyMemberRepository familyMemberRepository;

    @Autowired
    private TbObCbResponseRepository cbResponseRepository;

    @Autowired
    private InterfaceAdapter interfaceAdapter;

    @Autowired
    private AdapterUtil adapterUtil;

    @Value("${STATIC_LOAN_PRODUCT_ID:GL.IGL.PRAGATI.PLUS}")
    private String staticLoanProductId;

    @Value("${STATIC_LOAN_PRODUCT_TYPE:GL IGL PRAGATI PLUS}")
    private String staticLoanProductType;

    @Value("${STATIC_LOAN_AMOUNT:40000}")
    private String staticLoanAmount;

    @Value("${STATIC_LOAN_FREQUENCY:Weekly}")
    private String staticLoanFrequency;

    @Value("${STATIC_LOAN_TERM:104}")
    private String staticLoanTerm;

    @Value("${STATIC_LOAN_MEMBER_INS:Y}")
    private String staticLoanMemberIns;

    @Value("${STATIC_LOAN_SPOUSE_INS:Y}")
    private String staticLoanSpouseIns;

    @Value("${STATIC_LOAN_SPOUSE_INS_AMOUNT:1040}")
    private String staticLoanSpouseInsAmount;

    public Mono<Object> processAmlBreCheck(AmlBreCheckRequest request, Header header) {
        String applicationId = request.getRequestObj().getApplicationId();
        logger.debug("Processing AML+BRE check for applicationId: {}", applicationId);

        try {
            // Pre-validation: check if application already processed
            List<String> statuses = Arrays.asList("REJECTED", "DISBURSED", "CANCELLED");
            List<TbObApplicationMaster> appMasterDetails = applicationMasterRepository
                    .findApplicationBasedOnApplicationId(applicationId, statuses);
            if (!appMasterDetails.isEmpty()) {
                String status = appMasterDetails.get(0).getStatus();
                logger.debug("Application {} already processed with status: {}", applicationId, status);
                AmlBreCombinedResponse errorResponse = new AmlBreCombinedResponse();
                errorResponse.setAmlStatus("ALREADY_PROCESSED");
                errorResponse.setAmlResponse("Application has already been processed with status: " + status);
                errorResponse.setBreResponse(null);
                return Mono.just(errorResponse);
            }

            // Pre-validation: check mobile number
            Optional<TbObCustomer> custDtls = customerRepository.findByApplicationId(applicationId);
            if (custDtls.isPresent()) {
                JSONObject kycJson = new JSONObject(custDtls.get().getKycDetails());
                if (!kycJson.has("mobileNum") ||
                        kycJson.isNull("mobileNum") ||
                        kycJson.getString("mobileNum").trim().isEmpty()) {
                    logger.debug("Mobile number is not present for applicationId: {}", applicationId);
                    return buildErrorResponse("Mobile number is not present");
                }
            }
        } catch (Exception e) {
            logger.error("Exception during pre-validation for applicationId: {}", applicationId, e);
        }

        // Step 1: Call AML
        return callAmlExternalService(request, header, applicationId)
                .flatMap(amlResponseWrapper -> {
                    String amlRespObj = amlResponseWrapper.getApiResponse().getResponseBody().getResponseObj();
                    String amlStatus = extractAmlStatus(amlRespObj);
                    logger.debug("AML status for applicationId {}: {}", applicationId, amlStatus);

                    if ("MATCHED".equalsIgnoreCase(amlStatus)) {
                        // AML MATCHED — do NOT call BRE, return only AML response
                        logger.debug("AML MATCHED. Skipping BRE for applicationId: {}", applicationId);
                        JSONObject amlOnlyResponse = new JSONObject();
                        amlOnlyResponse.put("amlResponse", new JSONObject(amlRespObj));
                        return Mono.just((Object) amlOnlyResponse.toString());
                    }

                    // AML NOT MATCHED — proceed with BRE
                    logger.debug("AML NOT MATCHED. Proceeding with BRE for applicationId: {}", applicationId);
                    return callBreExternalService(request, header, applicationId)
                            .map(breRespObj -> {
                                JSONObject combined = new JSONObject();
                                combined.put("amlResponse", new JSONObject(amlRespObj));
                                combined.put("breResponse", new JSONObject(breRespObj.toString()));
                                return (Object) combined.toString();
                            });
                })
                .onErrorResume(ex -> {
                    logger.error("Exception in AML+BRE reactive chain for applicationId: {}", applicationId, ex);
                    return FallbackUtils.genericFallbackMonoObject();
                });
    }

    // AML EXTERNAL CALL

    private Mono<ResponseWrapper> callAmlExternalService(AmlBreCheckRequest request, Header header, String applicationId) {
        logger.debug("Building AML request for applicationId: {}", applicationId);

        try {
            Optional<TbObApplicationMaster> appMasterOpt = applicationMasterRepository.findByApplicationId(applicationId);
            Optional<TbObCustomer> custDtlsOpt = customerRepository.findByApplicationId(applicationId);

            OnboardingAmlRequestFields amlFields = new OnboardingAmlRequestFields();
            amlFields.setApplicantID(applicationId);

            if (custDtlsOpt.isPresent()) {
                TbObCustomer cust = custDtlsOpt.get();
                amlFields.setApplicantName(cust.getCustomerName());
                amlFields.setDob(cust.getDob());
                amlFields.setPrimaryID(cust.getPrimaryKycId());
            }

            if (appMasterOpt.isPresent()) {
                TbObApplicationMaster appMaster = appMasterOpt.get();
                amlFields.setBranchID(appMaster.getBranchId());
                amlFields.setBranchName(appMaster.getBranchName());
                amlFields.setCustomerID(appMaster.getCustomerId());
                amlFields.setGroupID(appMaster.getGroupId());
                amlFields.setKendraID(appMaster.getKendraId());
                amlFields.setKendraName(appMaster.getKendraName());
            }

            // Fields from frontend
            amlFields.setProduct(staticAmlProduct);
            amlFields.setCustomerType(staticAmlCustomerType);
            amlFields.setReason(staticAmlReason);
            amlFields.setUploadedBy(header.getUserId());
            logger.debug("AML request fields: {}", amlFields);

            header.setInterfaceId(request.getAppId() + "__" + AML_INTERFACE_ID);
            logger.debug("AML header after setting interfaceId: {}", header);

            Mono<Object> amlResponse = interfaceAdapter.callExternalService(
                    header, amlFields, AML_INTERFACE_ID, true);

            return adapterUtil.generateRespWrapper(amlResponse, AML_INTERFACE_ID, header, true)
                    .flatMap(responseMono -> Mono.just(responseMono));

        } catch (Exception e) {
            logger.error("Exception building AML request for applicationId: {}", applicationId, e);
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

    private String extractAmlStatus(String amlRespObj) {
        try {
            JSONObject json = new JSONObject(amlRespObj);
            return json.optString("Status", "");
        } catch (Exception e) {
            logger.error("Error parsing AML response status", e);
            return "";
        }
    }

    private Mono<Object> callBreExternalService(AmlBreCheckRequest request, Header header, String applicationId) {
        logger.debug("Building BRE request for applicationId: {}", applicationId);

        OnboardingBRERequest breReq = buildBreRequest(applicationId, request);
        Timestamp reqTs = new Timestamp(System.currentTimeMillis());

        // Build internal CbRequest for save method
        CbRequest cbRequest = new CbRequest();
        cbRequest.setAppId(request.getAppId());
        cbRequest.setInterfaceName(BRE_INTERFACE_ID);
        cbRequest.setUserId(request.getUserId());
        CbRequestFields cbReqFields = new CbRequestFields();
        cbReqFields.setApplicationId(applicationId);
        cbReqFields.setSchedulerEnabled("N");
        cbReqFields.setCbRecheck("N");
        cbReqFields.setUserRole(request.getUserRole());
        cbReqFields.setAppVersion("");
        cbReqFields.setUserId(request.getUserId());
        cbRequest.setRequestObj(cbReqFields);

        logger.debug("BRE request: {}", breReq);
        logger.debug("Header for BRE: {}", header);

        Mono<Object> cbRes = interfaceAdapter.callExternalService(header, breReq, BRE_INTERFACE_ID, true);

        return saveOnboardingCbCheckData(cbRes, breReq, header, cbRequest, reqTs, "N");
    }

    private OnboardingBRERequest buildBreRequest(String applicationId, AmlBreCheckRequest request) {

        OnboardingBRERequest cbCheckReq = new OnboardingBRERequest();
        OnboardingBRERequestFields reqFields = new OnboardingBRERequestFields();
        Applicant applicant = new Applicant();
        List<HouseholdMemberPayload> householdMembersList = new ArrayList<>();
        List<AddressPayload> addressPayloadList = new ArrayList<>();
        List<DocumentPayload> docPayloadList = new ArrayList<>();

        Optional<TbObApplicationMaster> appMasterList = applicationMasterRepository.findByApplicationId(applicationId);
        Optional<TbObCustomer> custDtls = customerRepository.findByApplicationId(applicationId);
        List<TbObAddress> addressData = addressRepository.findByApplicationId(applicationId);
        Optional<TbObLoan> loanData = loanRepository.findByApplicationId(applicationId);
        List<TbObFamilyMember> familyMembers = familyMemberRepository.findByApplicationId(applicationId);
        Optional<TbObCustomer> tbObCustomer = customerRepository.findByApplicationId(applicationId);

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

                String givenAddrType = addDomain.getAddressType();
                if (givenAddrType.equalsIgnoreCase("P")) addPayload.setAddrType("1");
                else if (givenAddrType.equalsIgnoreCase("C")) addPayload.setAddrType("2");

                addressPayloadList.add(addPayload);

                String otherAddrType = "P".equalsIgnoreCase(givenAddrType) ? "C" : "P";
                AddressPayload addPayload2 = new AddressPayload();
                addPayload2.setAddrLine1(addPayload.getAddrLine1());
                addPayload2.setCity(addPayload.getCity());
                addPayload2.setDistrict(addPayload.getDistrict());
                addPayload2.setState(addPayload.getState());
                addPayload2.setPinCode(addPayload.getPinCode());
                if (otherAddrType.equalsIgnoreCase("P")) addPayload2.setAddrType("1");
                else if (otherAddrType.equalsIgnoreCase("C")) addPayload2.setAddrType("2");
                addressPayloadList.add(addPayload2);
            } else {
                for (TbObAddress addDomain : addressData) {
                    AddressPayload addPayload = new AddressPayload();
                    logger.debug("addPayload :" + addPayload);
                    JSONObject payloadObj = new JSONObject(addDomain.getAddrPayload());

                    logger.debug("payloadObj :" + payloadObj);
                    String addType = addDomain.getAddressType();
                    if (addType.equalsIgnoreCase("P")) addPayload.setAddrType("1");
                    else if (addType.equalsIgnoreCase("C")) addPayload.setAddrType("2");

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
                    try {
                        if (payloadObj.has(CommonConstants.STATE)) {
                            addPayload.setState(String.valueOf(payloadObj.get(CommonConstants.STATE)));
                            applicant.setStateBranch(String.valueOf(payloadObj.get(CommonConstants.STATE)));
                        }
                    } catch (Exception e) {
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

        if (!familyMembers.isEmpty()) {
            for (TbObFamilyMember familyMember : familyMembers) {
                logger.debug("familyMember " + familyMember);
                if (familyMember.getIsEarningMember() != null && Boolean.TRUE.equals(familyMember.getIsEarningMember())) {
                    logger.debug("familyMember inside " + familyMember);
                    HouseholdMemberPayload householdMember = new HouseholdMemberPayload();
                    householdMember.setApplicantType("H");
                    householdMember.setRelationtype(familyMember.getRelation());
                    householdMember.setDob(String.valueOf(familyMember.getDob()));
                    householdMember.setGender(familyMember.getGender());
                    householdMember.setPhone(familyMember.getMobileNum());
                    householdMember.setCustName(familyMember.getName());
                    householdMember.setEarningFlag(String.valueOf(familyMember.getIsEarningMember()));

                    if (familyMember.getMemberType().equalsIgnoreCase("P")) {
                        applicant.setDepName(familyMember.getName());
                        applicant.setDepType(familyMember.getRelation());
                    }

                    List<DocumentPayload> docList = new ArrayList<>();
                    if (familyMember.getKycType() != null && familyMember.getKycDocId() != null) {
                        DocumentPayload docPayload = new DocumentPayload();
                        docPayload.setDocType(familyMember.getKycType());
                        docPayload.setDocId(familyMember.getKycDocId());
                        docList.add(docPayload);
                    }
                    householdMember.setDocument(docList);
                    householdMember.setAddress(new ArrayList<>(addressPayloadList));
                    householdMembersList.add(householdMember);
                }
            }
        }
        reqFields.setHousehold_member(householdMembersList);

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
        applicant.setActivationDate(LocalDate.now().toString());
        applicant.setCustName(custDtls.get().getCustomerName());
        applicant.setEmail("");
        applicant.setSlNo("1");
        applicant.setCategoryId("1");
        applicant.setProductCode("CCR");
        applicant.setDurationOfAgreement("10");
        applicant.setBankProductId("01");
        applicant.setGender("2");
        applicant.setLoanType("2");
        applicant.setAppId(applicationId);
        applicant.setLosIndex("LOS");
        applicant.setLosIndicator("1");

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

        applicant.setDigiAgilDFAFlag("DigiAgil");
        applicant.setEarningFlag("ABC");
        applicant.setEnquiryType("A");

        if (loanData.isPresent()) {
            applicant.setLoanId(
                    loanData.get().getLoanId()!=null&&!loanData.get().getLoanId().isBlank()?
                            loanData.get().getLoanId():applicationId
            );
            applicant.setLoanProductType(
                    loanData.get().getProduct()!=null&&!loanData.get().getProduct().isBlank()?
                            loanData.get().getProduct():staticLoanProductType
            );
            applicant.setProduct_code(
                    loanData.get().getProduct()!=null&&!loanData.get().getProduct().isBlank()?
                    loanData.get().getProduct():staticLoanProductId
            );
            applicant.setLoanAmount(
                    loanData.get().getAmount().toString()!=null&&!loanData.get().getAmount().toString().isBlank()?
                            loanData.get().getAmount().toString():staticLoanAmount
            );
            JSONObject freqPayload;
            try {
                freqPayload = new JSONObject(loanData.get().getFreq());
            } catch (Exception e) {
                logger.debug("Exception parsing freq payload", e);
                freqPayload = new JSONObject();
            }
            if (freqPayload.has("idDesc")) {
                String frequency = freqPayload.getString("idDesc");
                applicant.setAppliedFrequency(
                        frequency!=null&&!frequency.isBlank()?frequency:staticLoanFrequency
                );
            }
            if (StringUtils.isNotBlank(String.valueOf(loanData.get().getCharges()))) {
                JSONObject insurance;
                try {
                    insurance = new JSONObject(loanData.get().getCharges());
                } catch (Exception e) {
                    logger.debug("Exception parsing charges payload", e);
                    insurance = new JSONObject();
                }
                if (!insurance.isEmpty()) {
                    if (insurance.has("sp_insu")) {
                        String spInsu = insurance.getString("sp_insu");
                        applicant.setSpouseInsurance(
                                spInsu!=null&&!spInsu.isBlank()?spInsu:staticLoanSpouseIns
                        );
                    }
                    if (insurance.has("mem_insu")) {
                        String memInsu = String.valueOf(insurance.has("mem_insu"));
                        applicant.setApplicantInsurance(
                                !memInsu.isBlank() ?memInsu:staticLoanMemberIns
                        );
                    }
                    if (insurance.has("mem_insu")) {
                        applicant.setApplicantInsurance(String.valueOf(insurance.has("mem_insu")));
                    }
                    if (insurance.has("mem_insu")) {
                        applicant.setApplicantInsurance(String.valueOf(insurance.has("mem_insu")));
                    }
                }
                String term = loanData.get().getTerm();
                String termValue = term.replaceAll("\\D", "");
                applicant.setAppliedTermWeeks(termValue!=null&&!termValue.isBlank()?termValue:staticLoanTerm);
            }
        } else {
            applicant.setLoanId(applicationId);
            applicant.setLoanProductType(staticLoanProductType);
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

        if (tbObCustomer.isPresent()) {
            String custIncomeDet = tbObCustomer.get().getIncomedet();
            if (custIncomeDet != null && !custIncomeDet.isEmpty()) {
                JSONObject incomeDetails = new JSONObject(custIncomeDet);
                if (incomeDetails.has("income")) {
                    applicant.setHhAnnualIncome(incomeDetails.get("income").toString());
                }
            }
        }

        reqFields.setApplicant(applicant);
        cbCheckReq.setAppId(request.getAppId());
        cbCheckReq.setUserId(request.getUserId());
        cbCheckReq.setInterfaceName(BRE_INTERFACE_ID);
        cbCheckReq.setRequestObj(reqFields);

        logger.debug("BRE CHECK request: {}", cbCheckReq);
        return cbCheckReq;
    }

    private Mono<Object> saveOnboardingCbCheckData(Mono<Object> response, OnboardingBRERequest cbCheckRequest,
                                                   Header header, CbRequest cbRequest, Timestamp reqTs, String schedulerFlag) {
        logger.debug("Onboarding AmlBre cbRequest::" + cbRequest);
        logger.debug("Onboarding AmlBre cb request ::" + cbCheckRequest);

        return response.flatMap(val -> {
            logger.debug("ONBOARDING AML-BRE RAW EXTERNAL RESPONSE>>>: {}", val);
            ResponseWrapper responseWrapper = adapterUtil.getResponseMapper(val, cbCheckRequest.getInterfaceName(), header, true);
            String respObj = responseWrapper.getApiResponse().getResponseBody().getResponseObj();
            logger.debug("ONBOARDING AML-BRE CB RESPONSE IN SAVE METHOD>>>>::" + respObj);
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

            // Save to TbObCbResponse
            Optional<TbObCbResponse> cbRes = cbResponseRepository.findByApplicationId(applicationId);
            if (cbRes.isPresent()) {
                logger.debug("Onboarding AmlBre cbRes is present");
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
                logger.debug("Onboarding AmlBre cbRes not present, creating new record");
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

                cbResponse.setStatus(isSuccess ? "SUCCESS" : "FAILURE");
                cbResponse.setApiStatus(cbCheckStatus);
                cbResponse.setCreateTs(new Timestamp(System.currentTimeMillis()));
                cbResponseRepository.save(cbResponse);
            }

            // Update ApplicationMaster
            try {
                Optional<TbObApplicationMaster> appMasterOpt = applicationMasterRepository.findByApplicationId(applicationId);
                if (appMasterOpt.isPresent()) {
                    TbObApplicationMaster appMaster = appMasterOpt.get();

                    if (isSuccess) {
                        if ("REJECT".equalsIgnoreCase(finalDecision) || "Deviation".equalsIgnoreCase(finalDecision)) {
                            appMaster.setStatus("REJECTED");
                            logger.debug("Onboarding AmlBre CB: Final Decision is REJECT/Deviation, setting status to REJECTED");
                        }
                    } else {
                        logger.debug("Onboarding AmlBre CB: IRIS_message not SUCCESS, CB pending");
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
                    logger.debug("Onboarding AmlBre ApplicationMaster updated for applicationId: {}", applicationId);
                }
            } catch (Exception e) {
                logger.error("Error while updating ApplicationMaster for applicationId: {}", applicationId, e);
            }

            return Mono.just(respObj);
        });
    }

    private Mono<Object> buildErrorResponse(String message) {
        AmlBreCombinedResponse errorResponse = new AmlBreCombinedResponse();
        errorResponse.setAmlStatus("ERROR");
        errorResponse.setAmlResponse(message);
        errorResponse.setBreResponse(null);
        return Mono.just(errorResponse);
    }
}