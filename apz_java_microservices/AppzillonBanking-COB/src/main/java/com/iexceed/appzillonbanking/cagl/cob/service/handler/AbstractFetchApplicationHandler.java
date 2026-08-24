package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplnWorkflow;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.enums.AuditEventType;
import com.iexceed.appzillonbanking.cagl.cob.exception.ApplicationNotFoundException;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.*;
import com.iexceed.appzillonbanking.cagl.cob.service.ApplicationDetailsMapper;
import com.iexceed.appzillonbanking.cagl.cob.service.AuditTrailService;
import com.iexceed.appzillonbanking.cagl.cob.service.RecordLockService;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonInliningUtil;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public abstract class AbstractFetchApplicationHandler {

    private final TbObApplicationMasterRepository applicationMasterRepository;
    private final RecordLockService recordLockService;
    private final TbObCustomerRepository customerRepository;
    private final TbObCustOthersRepository custOthersRepository;
    private final TbObAddressRepository addressRepository;
    private final TbObFamilyMemberRepository familyMemberRepository;
    private final TbObDocumentRepository documentRepository;
    private final TbObOtherDocumentRepository otherDocumentRepository;
    private final TbObLoanRepository loanRepository;
    private final TbObCustAuditTrailRepository custAuditTrailRepository;
    private final TbObApplnWorkflowRepository workflowRepository;
    private final AuditTrailService auditTrailService;
    private final JsonInliningUtil jsonInliningUtil;
    private final ObjectMapper objectMapper;          // <-- injected, Spring Boot-managed bean
    private final ApplicationDetailsMapper mapper;
    private static final int AUDIT_SNAPSHOT_LIMIT = 20;

    @Transactional
    public ApplicationDetailsResponse handle(FetchApplicationDetailsRequest req, long lockDurationMinutes)
            throws ExecutionException, InterruptedException, JsonProcessingException {
        // no local `new ObjectMapper()` — deleted entirely
        FetchApplicationDetailsRequestFields request = req.getRequestObj();
        String applicationId = request.getApplicationId();

        TbObApplicationMaster app = applicationMasterRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        LockInfoDto lockInfo = recordLockService.evaluateAndMaybeExtend(
                applicationId, request.getUserId(), request.getUserRole(), lockDurationMinutes);

        CompletableFuture<TbObCustomer> customer = CompletableFuture.supplyAsync(() -> customerRepository.findByApplicationId(applicationId).orElse(null));
        CompletableFuture<TbObCustOthers> customerOthers = CompletableFuture.supplyAsync(() -> custOthersRepository.findByApplicationId(applicationId).orElse(null));
        CompletableFuture<List<TbObAddress>> addresses = CompletableFuture.supplyAsync(() -> addressRepository.findByApplicationIdOrderByAddressType(applicationId));
        CompletableFuture<List<TbObFamilyMember>> familyMembers = CompletableFuture.supplyAsync(() -> familyMemberRepository.findByApplicationIdOrderByFamilyMemId(applicationId));
        CompletableFuture<List<TbObDocument>> documents = CompletableFuture.supplyAsync(() -> documentRepository.findByApplicationIdOrderByDocuId(applicationId));
        CompletableFuture<List<TbObOtherDocument>> otherDocuments = CompletableFuture.supplyAsync(() -> otherDocumentRepository.findByApplicationIdOrderByDocuIdAsc(applicationId));
        CompletableFuture<List<TbObLoan>> loans = CompletableFuture.supplyAsync(() -> loanRepository.findByApplicationIdOrderByCreatedTsDesc(applicationId));
        CompletableFuture.allOf(customer, customerOthers, addresses, familyMembers, documents, otherDocuments, loans).join();

        AdditionalApplicationData additionalData = fetchAdditionalData(applicationId, request);

        auditTrailService.recordEvent(app, AuditEventType.APPLICATION_VIEWED,
                request.getUserId(), request.getUserName(), request.getUserRole());

        return mapper.toResponse(app, customer.get(), customerOthers.get(), addresses.get(), familyMembers.get(),
                documents.get(), otherDocuments.get(), loans.get(), additionalData);
    }

    protected AdditionalApplicationData fetchAdditionalData(
            String applicationId, FetchApplicationDetailsRequestFields request) {
        return AdditionalApplicationData.empty();
    }

    // objectMapper parameter removed — always uses the injected, correctly configured bean
    protected ResponseWrapper buildResponseWrapper(ApplicationDetailsResponse dto)
            throws JsonProcessingException {
        Map<String, Object> responseMap = objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {});
        jsonInliningUtil.inlineStoredJsonStrings(responseMap);
        String responseObj = objectMapper.writeValueAsString(responseMap);

        Response response = new Response();
        response.setResponseHeader(ResponseHeader.builder().responseCode("0").responseMessage("SUCCESS").build());
        response.setResponseBody(ResponseBody.builder().responseObj(responseObj).build());
        return ResponseWrapper.builder().apiResponse(response).build();
    }
}