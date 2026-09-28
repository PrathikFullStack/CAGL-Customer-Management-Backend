package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObLoan;
import com.iexceed.appzillonbanking.cagl.cob.payload.CbRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.CbRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.EditLoanDetailsRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.EditLoanRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.InsuranceDetails;
import com.iexceed.appzillonbanking.cagl.cob.payload.LoanDtls;
import com.iexceed.appzillonbanking.cagl.cob.payload.SaveLoanDetailsRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.SaveLoanRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.SaveLoanResponseFields;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCustomerRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObLoanRepository;
import com.iexceed.appzillonbanking.cagl.cob.utils.SequenceUtil;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_LOAN_STATUS = "INITIATE";
    private static final String DEFAULT_SCHEDULER_FLAG = "N";
    private static final String SAVE_CB_RECHECK_FLAG = "N";
    private static final String EDIT_CB_RECHECK_FLAG = "Y";
    private static final String CB_CHECK_INTERFACE_NAME = "OnboardingCBCheck";
    private static final String DEFAULT_VERSION_NO = "1";
    private static final String DEFAULT_CRT_FLAG = "N";

    private static final String SUCCESS_MESSAGE = "Loan and Charge details saved successfully!!";
    private static final String UPDATE_SUCCESS_MESSAGE = "Loan and Charge details updated successfully!!";
    private static final String LOAN_NOT_FOUND_MESSAGE = "No loan application found for the given loanId.";
    private static final String EXCEPTION_MSG = "Something went wrong. Please try again.";
    private static final String EXCEPTION_OCCURRED = "EXCEPTION_OCCURRED";

    @Autowired
    private TbObLoanRepository loanRepository;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private TbObCustomerRepository tbObCustomerRepository;

    @Autowired
    private SequenceUtil sequenceUtil;

    // ==================== SAVE LOAN ====================

    public Mono<Response> saveLoanDetails(SaveLoanDetailsRequest apiRequest, Header header) {

        logger.debug("Inside saveLoanDetails");

        return Mono.fromCallable(() -> persistNewLoan(apiRequest))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(savedLoan -> triggerCbCheckAfterSave(savedLoan, apiRequest, header))
                .onErrorResume(IllegalArgumentException.class, exception -> {
                    logger.error("Validation failed in saveLoanDetails: {}", exception.getMessage());
                    return Mono.just(buildFailureResponse(exception.getMessage()));
                })
                .onErrorResume(exception -> {
                    logger.error("Exception occurred in saveLoanDetails", exception);
                    return Mono.just(buildFailureResponse(EXCEPTION_MSG));
                });
    }

    @Transactional
    protected TbObLoan persistNewLoan(SaveLoanDetailsRequest apiRequest) {

        validateSaveRequest(apiRequest);

        SaveLoanRequestFields requestFields = apiRequest.getRequestObj();
        LoanDtls loanDetails = requestFields.getLoanDtls();
        String applicationId = requestFields.getApplicationId();

        TbObLoan loanEntity = buildLoanEntity(loanDetails, applicationId);
        TbObLoan savedLoan = loanRepository.save(loanEntity);

        logger.debug(
                "Loan details saved successfully. loanSeqId={}, loanId={}, applicationId={}",
                savedLoan.getLoanSeqId(),
                savedLoan.getLoanId(),
                savedLoan.getApplicationId()
        );

        return savedLoan;
    }

    private Mono<Response> triggerCbCheckAfterSave(
            TbObLoan savedLoan,
            SaveLoanDetailsRequest apiRequest,
            Header header) {

        SaveLoanRequestFields requestFields = apiRequest.getRequestObj();

        CbRequest cbRequest = buildCbRequest(
                apiRequest.getAppId(),
                savedLoan.getApplicationId(),
                SAVE_CB_RECHECK_FLAG,
                apiRequest.getUserId(),
                requestFields.getUserRole(),
                requestFields.getUserName(),
                requestFields.getAppVersion(),
                requestFields.getRemarks()
        );

        logger.debug("Triggering OnboardingCBCheck for save. applicationId={}", savedLoan.getApplicationId());

        return onboardingService.onboardingCbCheck(cbRequest, header, DEFAULT_SCHEDULER_FLAG)
                .map(cbResponse -> buildSaveSuccessResponse(savedLoan, cbResponse))
                .onErrorResume(cbException -> {
                    logger.error(
                            "OnboardingCBCheck failed for save. applicationId={}",
                            savedLoan.getApplicationId(),
                            cbException
                    );
                    return Mono.just(buildSaveSuccessResponse(savedLoan, null));
                });
    }

    private Response buildSaveSuccessResponse(TbObLoan savedLoan, Object cbResponse) {

        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        SaveLoanResponseFields saveLoanResponse = SaveLoanResponseFields.builder()
                .message(SUCCESS_MESSAGE)
                .loanSeqId(savedLoan.getLoanSeqId())
                .loanId(savedLoan.getLoanId())
                .applicationId(savedLoan.getApplicationId())
                .customerId(savedLoan.getCustomerId())
                .build();

        try {
            responseBody.setResponseObj(objectMapper.writeValueAsString(saveLoanResponse));
        } catch (JsonProcessingException exception) {
            logger.error("Failed to serialize save loan response", exception);
            responseBody.setResponseObj(SUCCESS_MESSAGE);
        }

        if (isFallbackResponse(cbResponse)) {
            logger.warn("CB check returned fallback for applicationId={}", savedLoan.getApplicationId());
        }

        CommonUtils.generateHeaderForSuccess(responseHeader);
        response.setResponseBody(responseBody);
        response.setResponseHeader(responseHeader);
        return response;
    }

    private TbObLoan buildLoanEntity(LoanDtls loanDetails, String applicationId) {

        LocalDateTime currentTimestamp = LocalDateTime.now();
        String loanSeqId = sequenceUtil.nextValueAsString("seq_ob_loan_id");
        String customerId = resolveCustomerId(applicationId);
        BigDecimal loanAmount = extractLoanAmount(loanDetails);
        String repaymentFrequency = extractRepaymentFrequency(loanDetails);

        logger.debug(
                "Building loan entity. applicationId={}, customerId={}, loanSeqId={}, amount={}, frequency={}",
                applicationId,
                customerId,
                loanSeqId,
                loanAmount,
                repaymentFrequency
        );

        return TbObLoan.builder()
                .loanSeqId(loanSeqId)
                .applicationId(applicationId)
                .customerId(customerId)
                .loanId(loanSeqId)
                .amount(loanAmount)
                .loanStatus(resolveLoanStatus(loanDetails))
                .product(loanDetails.getProduct())
                .term(loanDetails.getTerm())
                .freq(repaymentFrequency)
                .productDetails(buildLoanAndNomineeDetails(loanDetails))
                .charges(buildInsuranceAndChargeDetails(loanDetails))
                .createdTs(currentTimestamp)
                .updatedTs(currentTimestamp)
                .build();
    }

    // ==================== EDIT LOAN ====================

    public Mono<Response> editLoanDetails(EditLoanDetailsRequest apiRequest, Header header) {

        logger.debug("Inside editLoanDetails");

        return Mono.fromCallable(() -> persistLoanEdit(apiRequest))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(editResult -> resolveEditResponse(editResult, apiRequest, header))
                .onErrorResume(IllegalArgumentException.class, exception -> {
                    logger.error("Validation failed in editLoanDetails: {}", exception.getMessage());
                    return Mono.just(buildFailureResponse(exception.getMessage()));
                })
                .onErrorResume(exception -> {
                    logger.error("Exception occurred in editLoanDetails", exception);
                    return Mono.just(buildFailureResponse(EXCEPTION_MSG));
                });
    }

    private Mono<Response> resolveEditResponse(
            EditLoanResult editResult,
            EditLoanDetailsRequest apiRequest,
            Header header) {

        if (editResult.getUpdatedLoan() == null) {
            return Mono.just(editResult.getNotFoundResponse());
        }

        if (!editResult.isBreRetriggerNeeded()) {
            logger.debug(
                    "No BRE-relevant fields changed. Skipping CB check. loanId={}",
                    editResult.getUpdatedLoan().getLoanId()
            );
            return Mono.just(buildEditSuccessResponse(null));
        }

        return triggerCbCheckAfterEdit(editResult.getUpdatedLoan(), apiRequest, header);
    }

    @Transactional
    protected EditLoanResult persistLoanEdit(EditLoanDetailsRequest apiRequest) {

        validateEditRequest(apiRequest);

        EditLoanRequestFields requestFields = apiRequest.getRequestObj();
        String loanId = requestFields.getLoanId();
        LoanDtls loanDetails = requestFields.getLoanDtls();

        Optional<TbObLoan> existingLoanOptional = loanRepository.findByLoanId(loanId);

        if (existingLoanOptional.isEmpty()) {
            logger.debug("Loan not found for loanId={}", loanId);
            return EditLoanResult.notFound(buildFailureResponse(LOAN_NOT_FOUND_MESSAGE));
        }

        TbObLoan existingLoan = existingLoanOptional.get();

        // Snapshot OLD values BEFORE mutation — required for BRE-retrigger comparison.
        BigDecimal oldAmount = existingLoan.getAmount();
        String oldTerm = existingLoan.getTerm();
        String oldFreq = existingLoan.getFreq();
        String oldProduct = existingLoan.getProduct();
        Map<String, Object> oldCharges = existingLoan.getCharges();

        updateLoanEntity(existingLoan, loanDetails);
        TbObLoan updatedLoan = loanRepository.save(existingLoan);

        logger.debug(
                "Loan details updated successfully. loanId={}, applicationId={}",
                updatedLoan.getLoanId(),
                updatedLoan.getApplicationId()
        );

        boolean breRetriggerNeeded = hasBreRetriggerFields(
                oldAmount,
                oldTerm,
                oldFreq,
                oldProduct,
                oldCharges,
                loanDetails
        );

        return EditLoanResult.success(updatedLoan, breRetriggerNeeded);
    }

    private Mono<Response> triggerCbCheckAfterEdit(
            TbObLoan updatedLoan,
            EditLoanDetailsRequest apiRequest,
            Header header) {

        EditLoanRequestFields requestFields = apiRequest.getRequestObj();

        CbRequest cbRequest = buildCbRequest(
                apiRequest.getAppId(),
                updatedLoan.getApplicationId(),
                EDIT_CB_RECHECK_FLAG,
                apiRequest.getUserId(),
                requestFields.getUserRole(),
                requestFields.getUserName(),
                requestFields.getAppVersion(),
                requestFields.getRemarks()
        );

        logger.debug("Triggering OnboardingCBCheck for edit. applicationId={}", updatedLoan.getApplicationId());

        return onboardingService.onboardingCbCheck(cbRequest, header, DEFAULT_SCHEDULER_FLAG)
                .map(this::buildEditSuccessResponse)
                .onErrorResume(cbException -> {
                    logger.error(
                            "OnboardingCBCheck failed for edit. applicationId={}",
                            updatedLoan.getApplicationId(),
                            cbException
                    );
                    return Mono.just(buildEditSuccessResponse(null));
                });
    }

    private Response buildEditSuccessResponse(Object cbResponse) {

        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        if (isFallbackResponse(cbResponse)) {
            logger.warn("CB check returned fallback during edit.");
        }

        responseBody.setResponseObj(UPDATE_SUCCESS_MESSAGE);
        CommonUtils.generateHeaderForSuccess(responseHeader);

        response.setResponseBody(responseBody);
        response.setResponseHeader(responseHeader);
        return response;
    }

    private void updateLoanEntity(TbObLoan existingLoan, LoanDtls loanDetails) {

        existingLoan.setAmount(extractLoanAmount(loanDetails));
        existingLoan.setProduct(loanDetails.getProduct());
        existingLoan.setTerm(loanDetails.getTerm());
        existingLoan.setFreq(extractRepaymentFrequency(loanDetails));
        existingLoan.setProductDetails(buildLoanAndNomineeDetails(loanDetails));
        existingLoan.setCharges(buildInsuranceAndChargeDetails(loanDetails));
        existingLoan.setUpdatedTs(LocalDateTime.now());
    }

    // ==================== BRE RETRIGGER COMPARISON ====================

    private boolean hasBreRetriggerFields(
            BigDecimal oldAmount,
            String oldTerm,
            String oldFreq,
            String oldProduct,
            Map<String, Object> oldCharges,
            LoanDtls incoming) {

        boolean amountChanged = !amountsEqual(oldAmount, extractLoanAmount(incoming));
        boolean termChanged = !Objects.equals(oldTerm, incoming.getTerm());
        boolean freqChanged = !Objects.equals(oldFreq, extractRepaymentFrequency(incoming));
        boolean productChanged = !Objects.equals(oldProduct, incoming.getProduct());
        boolean insuranceChanged = hasInsuranceChanged(oldCharges, incoming.getInsurDtls());

        logger.debug(
                "BRE retrigger check — amount:{}, term:{}, freq:{}, product:{}, insurance:{}",
                amountChanged,
                termChanged,
                freqChanged,
                productChanged,
                insuranceChanged
        );

        return amountChanged || termChanged || freqChanged || productChanged || insuranceChanged;
    }

    private boolean amountsEqual(BigDecimal oldAmount, BigDecimal newAmount) {

        if (oldAmount == null || newAmount == null) {
            return Objects.equals(oldAmount, newAmount);
        }
        return oldAmount.compareTo(newAmount) == 0;
    }

    private boolean hasInsuranceChanged(Map<String, Object> oldCharges, InsuranceDetails incomingInsurance) {

        if (oldCharges == null || incomingInsurance == null) {
            return oldCharges != null || incomingInsurance != null;
        }

        // TbObLoan.getCharges() returns Map<String,Object> (jsonb column) — no manual JSON parsing needed.
        Object oldInsurDtlsObj = oldCharges.get("insurDtls");
        InsuranceDetails oldInsurance = objectMapper.convertValue(oldInsurDtlsObj, InsuranceDetails.class);

        if (oldInsurance == null) {
            return true;
        }

        return !Objects.equals(oldInsurance.getMember(), incomingInsurance.getMember())
                || !Objects.equals(oldInsurance.getSpouse(), incomingInsurance.getSpouse());
    }

    // ==================== CB REQUEST BUILDER ====================

    private CbRequest buildCbRequest(
            String appId,
            String applicationId,
            String cbRecheckFlag,
            String userId,
            String userRole,
            String userName,
            String appVersion,
            String remarks) {

        CbRequestFields requestFields = CbRequestFields.builder()
                .applicationId(applicationId)
                .versionNo(DEFAULT_VERSION_NO)
                .schedulerEnabled(DEFAULT_SCHEDULER_FLAG)
                .custDtlId("")
                .cbRecheck(cbRecheckFlag)
                .crtFlag(DEFAULT_CRT_FLAG)
                .userRole(userRole)
                .userName(userName)
                .appVersion(appVersion)
                .remarks(remarks)
                .userId(userId)
                .build();

        return CbRequest.builder()
                .appId(appId)
                .interfaceName(CB_CHECK_INTERFACE_NAME)
                .userId(userId)
                .requestObj(requestFields)
                .build();
    }

    private boolean isFallbackResponse(Object cbResponse) {
        return cbResponse instanceof Response;
    }

    // ==================== SHARED HELPERS ====================

    private String resolveCustomerId(String applicationId) {

        Optional<TbObCustomer> customerOptional = tbObCustomerRepository.findByApplicationId(applicationId);

        if (customerOptional.isEmpty()) {
            throw new IllegalArgumentException("Customer details not found for applicationId: " + applicationId);
        }

        TbObCustomer customer = customerOptional.get();
        String customerId = customer.getCustomerId();

        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID not found for applicationId: " + applicationId);
        }

        logger.debug("Resolved customerId={} for applicationId={}", customerId, applicationId);
        return customerId;
    }

    private BigDecimal extractLoanAmount(LoanDtls loanDetails) {

        if (loanDetails.getChargeAndBreakupDtls() == null) {
            return null;
        }

        String loanAmount = loanDetails.getChargeAndBreakupDtls().getLoanAmt();

        if (loanAmount == null || loanAmount.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(loanAmount);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid loan amount: " + loanAmount);
        }
    }

    private String extractRepaymentFrequency(LoanDtls loanDetails) {
        return loanDetails.getRepayFrequency() != null
                ? loanDetails.getRepayFrequency().getIdDesc()
                : null;
    }

    private String resolveLoanStatus(LoanDtls loanDetails) {
        return loanDetails.getLoanStatus() == null || loanDetails.getLoanStatus().isBlank()
                ? DEFAULT_LOAN_STATUS
                : loanDetails.getLoanStatus();
    }

    private Map<String, Object> buildLoanAndNomineeDetails(LoanDtls loanDetails) {

        Map<String, Object> details = new HashMap<>();
        details.put("product", loanDetails.getProduct());
        details.put("productId", loanDetails.getProductId());
        details.put("productType", loanDetails.getProductType());
        details.put("shortDesc", loanDetails.getShortDesc());
        details.put("term", loanDetails.getTerm());
        details.put("loanMode", loanDetails.getLoanMode());
        details.put("purpose", loanDetails.getPurpose());
        details.put("disburseMode", loanDetails.getDisburseMode());
        details.put("repayFrequency", loanDetails.getRepayFrequency());
        details.put("nomineeDtls", loanDetails.getNomineeDtls());
        return details;
    }

    private Map<String, Object> buildInsuranceAndChargeDetails(LoanDtls loanDetails) {

        Map<String, Object> details = new HashMap<>();
        details.put("insurDtls", loanDetails.getInsurDtls());
        details.put("spouseInsurance", loanDetails.getSpouseInsurance());
        details.put("insurancePercentage", loanDetails.getInsurancePercentage());
        details.put("chargeAndBreakupDtls", loanDetails.getChargeAndBreakupDtls());
        details.put("caglAmt", loanDetails.getCaglAmt());
        details.put("cbAmt", loanDetails.getCbAmt());
        details.put("custVintageInterestRate", loanDetails.getCustVintageInterestRate());
        return details;
    }

    private Response buildFailureResponse(String message) {

        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        responseBody.setResponseObj(message);
        CommonUtils.generateHeaderForFailure(responseHeader, EXCEPTION_OCCURRED);

        response.setResponseBody(responseBody);
        response.setResponseHeader(responseHeader);
        return response;
    }

    private void validateSaveRequest(SaveLoanDetailsRequest apiRequest) {

        if (apiRequest == null) {
            throw new IllegalArgumentException("Save loan request cannot be null.");
        }
        if (apiRequest.getRequestObj() == null) {
            throw new IllegalArgumentException("Request object cannot be null.");
        }
        if (apiRequest.getRequestObj().getApplicationId() == null
                || apiRequest.getRequestObj().getApplicationId().isBlank()) {
            throw new IllegalArgumentException("Application ID cannot be null or empty.");
        }
        if (apiRequest.getRequestObj().getLoanDtls() == null) {
            throw new IllegalArgumentException("Loan details cannot be null.");
        }
    }

    private void validateEditRequest(EditLoanDetailsRequest apiRequest) {

        if (apiRequest == null) {
            throw new IllegalArgumentException("Edit loan request cannot be null.");
        }
        if (apiRequest.getRequestObj() == null) {
            throw new IllegalArgumentException("Request object cannot be null.");
        }
        if (apiRequest.getRequestObj().getLoanId() == null
                || apiRequest.getRequestObj().getLoanId().isBlank()) {
            throw new IllegalArgumentException("Loan ID cannot be null or empty.");
        }
        if (apiRequest.getRequestObj().getLoanDtls() == null) {
            throw new IllegalArgumentException("Loan details cannot be null.");
        }
    }

    // ==================== INTERNAL HELPER ====================

    private static class EditLoanResult {

        private final TbObLoan updatedLoan;
        private final boolean breRetriggerNeeded;
        private final Response notFoundResponse;

        private EditLoanResult(TbObLoan updatedLoan, boolean breRetriggerNeeded, Response notFoundResponse) {
            this.updatedLoan = updatedLoan;
            this.breRetriggerNeeded = breRetriggerNeeded;
            this.notFoundResponse = notFoundResponse;
        }

        static EditLoanResult success(TbObLoan updatedLoan, boolean breRetriggerNeeded) {
            return new EditLoanResult(updatedLoan, breRetriggerNeeded, null);
        }

        static EditLoanResult notFound(Response notFoundResponse) {
            return new EditLoanResult(null, false, notFoundResponse);
        }

        TbObLoan getUpdatedLoan() {
            return updatedLoan;
        }

        boolean isBreRetriggerNeeded() {
            return breRetriggerNeeded;
        }

        Response getNotFoundResponse() {
            return notFoundResponse;
        }
    }
}