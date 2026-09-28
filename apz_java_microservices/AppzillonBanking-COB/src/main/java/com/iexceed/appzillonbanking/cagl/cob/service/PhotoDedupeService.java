package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.exception.PhotoDedupeValidationException;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;

import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObCustomerRepository;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles the photo-dedupe workflow for onboarding applications:
 *  - bulk update of tb_ob_application_master.photo_dedupe_status, keyed by application_id.
 *  - paginated fetch of applications still awaiting a dedupe outcome (photo_dedupe_status = PENDING),
 *    enriched with every member's photo_doc_id (tb_ob_customer) so the caller doesn't need a second
 *    round trip to act on each record.
 */
@Service
public class PhotoDedupeService {

    private static final Logger logger = LogManager.getLogger(PhotoDedupeService.class);

    public static final String EXCEPTION_MSG = "Something went wrong, Please try again!!";
    private static final String STATUS_PENDING = "PENDING";

    // Guards against a single request trying to update an unbounded number of rows in one go.
    private static final int MAX_UPDATE_BATCH_SIZE = 500;

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "applicationId");

    @Autowired
    private TbObApplicationMasterRepository applicationMasterRepository;

    @Autowired
    private TbObCustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Updates photo_dedupe_status for one or many application_ids in a single transaction.
     * Duplicate application_ids within the same request are resolved on a last-one-wins basis, and
     * application_ids that don't exist are reported back rather than failing the whole batch, so a
     * partially-valid batch still updates every record it legitimately can -- one round trip to fetch
     * the candidates and one batched {@code saveAll} to persist them, instead of a save-per-record loop.
     */
    @Transactional
    public Mono<Response> updatePhotoDedupeStatus(PhotoDedupeUpdateRequest request, Header header) {

        logger.info("Photo Dedupe Status Update API Started.");
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {
            Map<String, String> statusByApplicationId = validateAndDedupe(request);

            List<TbObApplicationMaster> existingApplications =
                    applicationMasterRepository.findByApplicationIdIn(new ArrayList<>(statusByApplicationId.keySet()));

            Map<String, TbObApplicationMaster> existingByApplicationId = existingApplications.stream()
                    .collect(Collectors.toMap(TbObApplicationMaster::getApplicationId, application -> application));

            List<TbObApplicationMaster> toUpdate = new ArrayList<>();
            List<String> notFoundApplicationIds = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (Map.Entry<String, String> entry : statusByApplicationId.entrySet()) {

                TbObApplicationMaster application = existingByApplicationId.get(entry.getKey());
                if (application == null) {
                    notFoundApplicationIds.add(entry.getKey());
                    continue;
                }
                application.setPhotoDedupeStatus(entry.getValue());
                application.setUpdatedBy(request.getUserId());
                application.setUpdatedTs(now);
                toUpdate.add(application);
            }

            if (!toUpdate.isEmpty()) {
                applicationMasterRepository.saveAll(toUpdate);
            }

            logger.info("Photo Dedupe Status Update : requested={}, updated={}, notFound={}",
                    statusByApplicationId.size(), toUpdate.size(), notFoundApplicationIds.size());

            responseBody.setResponseObj(buildUpdateResponse(statusByApplicationId.size(), toUpdate, notFoundApplicationIds));

            if (toUpdate.isEmpty()) {
                CommonUtils.generateHeaderForFailure(responseHeader,
                        "No matching application(s) found for the given application_id(s).");
            } else {
                CommonUtils.generateHeaderForSuccess(responseHeader);
            }

        } catch (PhotoDedupeValidationException ex) {
            logger.error("Photo Dedupe Status Update Validation Failed.", ex);
            responseBody.setResponseObj(ex.getMessage());
            CommonUtils.generateHeaderForFailure(responseHeader, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Exception while updating Photo Dedupe Status.", ex);
            responseBody.setResponseObj(EXCEPTION_MSG);
            CommonUtils.generateHeaderForFailure(responseHeader, EXCEPTION_MSG);
        }

        return Mono.just(Response.builder()
                .responseHeader(responseHeader)
                .responseBody(responseBody)
                .build());
    }

    /**
     * Validates the incoming batch and collapses it into an applicationId -> photoDedupeStatus map.
     * When the same application_id is repeated, the last occurrence wins -- this keeps the API
     * idempotent/forgiving instead of rejecting the whole batch over a duplicate.
     */
    private Map<String, String> validateAndDedupe(PhotoDedupeUpdateRequest request) {

        if (request == null) {
            throw new PhotoDedupeValidationException("Request field is mandatory.");
        }

        PhotoDedupeUpdateRequestFields requestObj = request.getRequestObj();

        if (requestObj == null || requestObj.getPhotoDedupeDetails() == null || requestObj.getPhotoDedupeDetails().isEmpty()) {
            throw new PhotoDedupeValidationException("photoDedupeDetails is mandatory and cannot be empty.");
        }

        List<PhotoDedupeStatusItem> items = requestObj.getPhotoDedupeDetails();

        if (items.size() > MAX_UPDATE_BATCH_SIZE) {
            throw new PhotoDedupeValidationException(
                    "A maximum of " + MAX_UPDATE_BATCH_SIZE + " application_id(s) can be updated in a single request.");
        }

        Map<String, String> statusByApplicationId = new LinkedHashMap<>();

        for (PhotoDedupeStatusItem item : items) {

            if (item == null || isBlank(item.getApplicationId())) {
                throw new PhotoDedupeValidationException("applicationId is mandatory for every record.");
            }
            if (isBlank(item.getPhotoDedupeStatus())) {
                throw new PhotoDedupeValidationException(
                        "photoDedupeStatus is mandatory for applicationId : " + item.getApplicationId());
            }
            statusByApplicationId.put(item.getApplicationId().trim(), item.getPhotoDedupeStatus().trim().toUpperCase());
        }
        return statusByApplicationId;
    }

    private String buildUpdateResponse(int totalRequested, List<TbObApplicationMaster> updated, List<String> notFoundApplicationIds) {

        try {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", notFoundApplicationIds.isEmpty()
                    ? "Photo dedupe status updated successfully."
                    : "Photo dedupe status updated for the matching application(s); some application_id(s) were not found.");
            response.put("totalRequested", totalRequested);
            response.put("totalUpdated", updated.size());
            response.put("updatedApplicationIds", updated.stream()
                    .map(TbObApplicationMaster::getApplicationId)
                    .collect(Collectors.toList()));
            response.put("totalNotFound", notFoundApplicationIds.size());
            response.put("notFoundApplicationIds", notFoundApplicationIds);
            return objectMapper.writeValueAsString(response);
        } catch (Exception ex) {
            logger.error("Error while preparing update response.", ex);
            throw new PhotoDedupeValidationException("Unable to prepare response.");
        }
    }

    /**
     * Fetch of every application still awaiting a photo-dedupe outcome (photo_dedupe_status = PENDING),
     * enriched with that application's photo_doc_id from tb_ob_customer -- looked up via
     * tb_ob_application_master.customer_id, the one customer each application row actually points at,
     * so each record carries a single photoDocId rather than a collection.
     * <p>
     * Pagination is opt-in: it's applied only when the caller explicitly sends pageNumber and/or
     * pageSize; otherwise the entire pending backlog is fetched and returned in one response.
     */
    public Mono<Response> fetchPendingPhotoDedupeDetails(PhotoDedupeFetchRequest request, Header header) {

        logger.info("Photo Dedupe Pending Status Fetch API Started.");
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();

        try {
            PhotoDedupeFetchRequestFields requestObj = request == null ? null : request.getRequestObj();
            Pageable pageable = isPaginationRequested(requestObj) ? resolvePageable(requestObj) : Pageable.unpaged(DEFAULT_SORT);

            Page<TbObApplicationMaster> pendingApplications =
                    applicationMasterRepository.findByPhotoDedupeStatus(STATUS_PENDING, pageable);

            if (pendingApplications.isEmpty()) {
                logger.info("No application(s) found with photo_dedupe_status = PENDING.");
                responseBody.setResponseObj(buildFetchResponse(pendingApplications, Collections.emptyMap()));
                CommonUtils.generateHeaderForNoResult(responseHeader);
            } else {

                List<String> customerIds = pendingApplications.getContent().stream()
                        .map(TbObApplicationMaster::getCustomerId)
                        .filter(customerId -> !isBlank(customerId))
                        .collect(Collectors.toList());

                Map<String, String> photoDocIdByCustomerId = customerRepository.findByCustomerIdIn(customerIds)
                        .stream()
                        .filter(customer -> !isBlank(customer.getPhotoDocId()))
                        .collect(Collectors.toMap(TbObCustomer::getCustomerId, TbObCustomer::getPhotoDocId));

                responseBody.setResponseObj(buildFetchResponse(pendingApplications, photoDocIdByCustomerId));
                CommonUtils.generateHeaderForSuccess(responseHeader);
            }

        } catch (PhotoDedupeValidationException ex) {
            logger.error("Photo Dedupe Pending Status Fetch Validation Failed.", ex);
            responseBody.setResponseObj(ex.getMessage());
            CommonUtils.generateHeaderForFailure(responseHeader, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Exception while fetching Photo Dedupe Pending Status.", ex);
            responseBody.setResponseObj(EXCEPTION_MSG);
            CommonUtils.generateHeaderForFailure(responseHeader, EXCEPTION_MSG);
        }

        return Mono.just(Response.builder()
                .responseHeader(responseHeader)
                .responseBody(responseBody)
                .build());
    }

    /**
     * True only when the caller explicitly sent pageNumber and/or pageSize -- that's the sole trigger
     * for paginating the result. A bare/absent requestObj means "give me everything", not "give me
     * the default page".
     */
    private boolean isPaginationRequested(PhotoDedupeFetchRequestFields requestObj) {
        return requestObj != null && (requestObj.getPageNumber() != null || requestObj.getPageSize() != null);
    }

    /**
     * Only called once pagination has been requested. Whichever of pageNumber/pageSize wasn't supplied
     * falls back to a default (0 / 10) so a partially-specified request (e.g. pageSize only) still
     * works; pageSize is capped at MAX_PAGE_SIZE so a caller can't turn "paginated" into "everything"
     * by just passing a huge size. Sorted by application_id (the primary key) so the page order is
     * stable and repeatable across calls.
     */
    private Pageable resolvePageable(PhotoDedupeFetchRequestFields requestObj) {

        int pageNumber = requestObj.getPageNumber() == null ? DEFAULT_PAGE_NUMBER : requestObj.getPageNumber();
        int pageSize = requestObj.getPageSize() == null ? DEFAULT_PAGE_SIZE : requestObj.getPageSize();

        if (pageNumber < 0) {
            throw new PhotoDedupeValidationException("pageNumber cannot be negative.");
        }
        if (pageSize <= 0) {
            throw new PhotoDedupeValidationException("pageSize must be greater than 0.");
        }
        if (pageSize > MAX_PAGE_SIZE) {
            throw new PhotoDedupeValidationException("pageSize cannot exceed " + MAX_PAGE_SIZE + ".");
        }
        return PageRequest.of(pageNumber, pageSize, DEFAULT_SORT);
    }

    private String buildFetchResponse(Page<TbObApplicationMaster> page, Map<String, String> photoDocIdByCustomerId) {

        try {
            List<Map<String, Object>> records = page.getContent().stream()
                    .map(application -> {
                        Map<String, Object> record = new LinkedHashMap<>();
                        record.put("applicationId", application.getApplicationId());
                        record.put("photoDocId", photoDocIdByCustomerId.get(application.getCustomerId()));
                        record.put("photoDedupeStatus", application.getPhotoDedupeStatus());
                        return record;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("pageNumber", page.getNumber());
            response.put("pageSize", page.getSize());
            response.put("totalElements", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("hasNext", page.hasNext());
            response.put("records", records);
            return objectMapper.writeValueAsString(response);
        } catch (Exception ex) {
            logger.error("Error while preparing fetch response.", ex);
            throw new PhotoDedupeValidationException("Unable to prepare response.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
