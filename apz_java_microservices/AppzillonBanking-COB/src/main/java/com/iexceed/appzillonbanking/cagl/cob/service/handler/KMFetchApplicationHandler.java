package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.FetchApplicationDetailsRequest;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.*;
import com.iexceed.appzillonbanking.cagl.cob.service.ApplicationDetailsMapper;
import com.iexceed.appzillonbanking.cagl.cob.service.AuditTrailService;
import com.iexceed.appzillonbanking.cagl.cob.service.RecordLockService;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonInliningUtil;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class KMFetchApplicationHandler extends AbstractFetchApplicationHandler
        implements FetchApplicationHandler {

    public KMFetchApplicationHandler(TbObApplicationMasterRepository applicationMasterRepository,
                                     RecordLockService recordLockService,
                                     TbObCustomerRepository customerRepository,
                                     TbObCustOthersRepository custOthersRepository,
                                     TbObAddressRepository addressRepository,
                                     TbObFamilyMemberRepository familyMemberRepository,
                                     TbObDocumentRepository documentRepository,
                                     TbObOtherDocumentRepository otherDocumentRepository,
                                     TbObLoanRepository loanRepository,
                                     TbObCustAuditTrailRepository custAuditTrailRepository,
                                     TbObApplnWorkflowRepository workflowRepository,
                                     AuditTrailService auditTrailService,
                                     JsonInliningUtil jsonInliningUtil,
                                     ObjectMapper objectMapper,
                                     ApplicationDetailsMapper mapper) {
        super(applicationMasterRepository, recordLockService, customerRepository, custOthersRepository, addressRepository, familyMemberRepository, documentRepository, otherDocumentRepository, loanRepository, custAuditTrailRepository, workflowRepository, auditTrailService, jsonInliningUtil, objectMapper, mapper);
    }

    // No override of fetchAdditionalData() — pure common flow.

    @Override
    public ResponseWrapper handleFetchApplicationDetails(
            FetchApplicationDetailsRequest req, long lockDurationMinutes) throws JsonProcessingException, ExecutionException, InterruptedException {
        return buildResponseWrapper(handle(req, lockDurationMinutes));
    }
}