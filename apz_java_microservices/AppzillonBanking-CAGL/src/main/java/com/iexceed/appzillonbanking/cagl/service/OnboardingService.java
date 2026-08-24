package com.iexceed.appzillonbanking.cagl.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.entity.GkUnifiedLeadData;
import com.iexceed.appzillonbanking.cagl.payload.CreateLeadRequest;
import com.iexceed.appzillonbanking.cagl.payload.CreateLeadRequestFields;
import com.iexceed.appzillonbanking.cagl.payload.LeadReferenceResponse;
import com.iexceed.appzillonbanking.cagl.repository.cus.GkUnifiedLeadRepository;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OnboardingService {


    @Autowired
    private GkUnifiedLeadRepository gkUnifiedLeadRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LogManager.getLogger(OnboardingService.class);

    @Transactional
    public Response createLead(CreateLeadRequest request, Header header) {
        logger.info("Inside createLead");
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();
        try {
            List<LeadReferenceResponse> referenceList = new ArrayList<>();
            CreateLeadRequestFields requestObj = request.getRequestObj();
            for (CreateLeadRequestFields.LeadRecord record : requestObj.getRecords()) {
                GkUnifiedLeadData lead = buildLead(record, requestObj, header);
                // Save into CDH
                lead = gkUnifiedLeadRepository.save(lead);
                referenceList.add(LeadReferenceResponse.builder()
                        .leadId(lead.getLeadId())
                        .memberName(record.getMemberName())
                        .mobileNumber(record.getMemberMobile())
                        .message("Lead captured successfully in CDH.")
                        .uId(String.valueOf(lead.getId()))
                        .build()
                );
            }
            responseBody.setResponseObj(
                    objectMapper.writeValueAsString(referenceList));
            CommonUtils.generateHeaderForSuccess(responseHeader);
        } catch (Exception ex) {
            logger.error("Exception while creating lead in CDH", ex);
            CommonUtils.generateHeaderForFailure(
                    responseHeader, "Unable to create Lead in CDH");
            responseBody.setResponseObj("Lead creation failed.");
        }
        response.setResponseHeader(responseHeader);
        response.setResponseBody(responseBody);

        return response;
    }

    private GkUnifiedLeadData buildLead(CreateLeadRequestFields.LeadRecord record,
                                        CreateLeadRequestFields requestObj, Header header) {
        LocalDateTime now = LocalDateTime.now();

        GkUnifiedLeadData lead = new GkUnifiedLeadData();
        lead.setLeadId(record.getLeadId());
        lead.setLeadName(record.getMemberName());
        lead.setLeadMobileNumber(record.getMemberMobile());
        lead.setLeadSource(StringUtils.defaultIfBlank(record.getLeadSource(), "MAITRI"));
        lead.setLeadType("MAITRI");
        lead.setLeadStatus("OPEN");
        lead.setBranchId(requestObj.getBranchId());
        lead.setKmId(header.getUserId());
        lead.setUserId(header.getUserId());
        lead.setReferringCustomerName(record.getReferredBy());
        lead.setReferringCustomerMobileNumber(record.getReferenceMobile());
        lead.setAddInfo(buildAddInfo(record));
        lead.setCreatedBy(header.getUserId());
        lead.setCreatedTs(now);
        lead.setUpdatedBy(header.getUserId());
        lead.setUpdatedTs(now);
        return lead;
    }

    private Map<String, Object> buildAddInfo(CreateLeadRequestFields.LeadRecord record) {
        Map<String, Object> addInfo = new LinkedHashMap<>();
        addIfPresent(addInfo, "villageAreaName", record.getVillageAreaName());
        addIfPresent(addInfo, "landmark", record.getLandmark());
        addIfPresent(addInfo, "kycType", record.getKycType());
        addIfPresent(addInfo, "kycId", record.getKycId());
        return addInfo;
    }

    private void addIfPresent(Map<String, Object> addInfo, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            addInfo.put(key, value.trim());
        }
    }
}
