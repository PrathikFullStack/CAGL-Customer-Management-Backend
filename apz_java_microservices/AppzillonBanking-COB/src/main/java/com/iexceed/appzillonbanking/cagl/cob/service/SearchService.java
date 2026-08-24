package com.iexceed.appzillonbanking.cagl.cob.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.spec.OnboardingSpecification;
import com.iexceed.appzillonbanking.cagl.cob.mapper.ApplicationSummaryMapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;

import com.iexceed.appzillonbanking.cagl.cob.repository.cus.OnboardingRepository;
import com.iexceed.appzillonbanking.cagl.cob.utils.PaginationUtil;
import com.iexceed.appzillonbanking.core.payload.*;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private static final DashboardListResponse EMPTY_RESPONSE = new DashboardListResponse(new DashboardListResponseObj(Page.empty(), new ArrayList<>()));

    private final OnboardingRepository onboardingRepository;
    private final AuditService auditService;
    private final ApplicationSummaryMapper applicationSummaryMapper;

    public ResponseWrapper search(SearchRequest request, Header header) {
        SearchRequestPayload fields = request.getReqObj();
        if (fields == null) {
            ResponseWrapper responseWrapper = new ResponseWrapper();

            DashboardListResponseObj emptyResponseObj =
                    new DashboardListResponseObj(
                            Page.empty(),
                            new ArrayList<>()
                    );
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                ResponseBody responseBody = new ResponseBody();

                responseBody.setResponseObj(
                        objectMapper.writeValueAsString(emptyResponseObj)
                );

                ResponseHeader responseHeader = new ResponseHeader();
                CommonUtils.generateHeaderForSuccess(responseHeader);

                responseWrapper.setApiResponse(
                        Response.builder()
                                .responseHeader(responseHeader)
                                .responseBody(responseBody)
                                .build()
                );

            } catch (Exception e) {
                log.error("Error serializing empty response", e);
            }

            return responseWrapper;
        }

        Pageable pageable = PaginationUtil.createPageable(fields.getPagination());
        Specification<TbObApplicationMaster> spec;

        if ("FILTER".equalsIgnoreCase(fields.getSearchType())) {
            spec = OnboardingSpecification.buildFilterSpecification(fields);
        } else if ("LOCAL".equalsIgnoreCase(fields.getSearchType())) {
            spec = OnboardingSpecification.buildLocalSearchSpecification(fields);
        } else {
            spec = OnboardingSpecification.buildSearchSpecification(fields);
        }

        Page<TbObApplicationMaster> results = onboardingRepository.findAll(spec, pageable);
        Page<ApplicationSummary> summaryPage = results.map(applicationSummaryMapper::toApplicationSummary);

        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            ResponseBody responseBody = new ResponseBody();
            responseBody.setResponseObj(objectMapper.writeValueAsString(summaryPage)
            );
            ResponseHeader responseHeader = new ResponseHeader();
            CommonUtils.generateHeaderForSuccess(responseHeader
            );
            responseWrapper.setApiResponse(Response.builder()
                            .responseHeader(responseHeader)
                            .responseBody(responseBody)
                            .build()
            );

        } catch (Exception e) {

            log.error(
                    "Error serializing search response",
                    e
            );
        }

        return responseWrapper;
    }
}