package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import com.iexceed.appzillonbanking.cagl.cob.exception.DuplicateApplicationException;
import com.iexceed.appzillonbanking.cagl.cob.exception.ResourceNotFoundException;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGroupRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObKendraRepository;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class KendraGroupService {

    private static final Logger logger = LogManager.getLogger(OnboardingService.class);

    @Autowired
    private TbObKendraRepository kendraRepository;

    @Autowired
    private TbObGroupRepository groupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final List<String> VALID_FREQUENCIES = List.of("WEEKLY", "FORTNIGHTLY", "MONTHLY");
    private static final List<String> LOCATION_EXEMPT_ROLES = List.of("CHT", "IT_HELPDESK");
    private static final BigDecimal DEFAULT_MAX_DISTANCE_KM = BigDecimal.valueOf(25);
    private static final int GROUP_MEMBER_HARD_MAX = 30;
    private static final List<String> GROUP_CREATE_WITHOUT_CUSTOMER_ROLES = List.of("AM", "CHT", "IT_HELPDESK");

    @Transactional
    public Response createKendra(CreateKendraRequest request, Header header) throws JsonProcessingException {
        logger.info("Start : createKendra");
        Response response = new Response();

        if (request == null || request.getRequestObj() == null) {
            throw new IllegalArgumentException("Invalid request : Kendra details are missing.");
        }

        CreateKendraRequestFields req = request.getRequestObj();
        logger.debug("Create Kendra Request : {} | role={}", req);
        validateRequest(req);
        validateDuplicateKendra(req);
        // 3. Village / pincode blacklist — BLOCKS creation on HIT (per FSD)
        // performBlacklistValidation(req);

        // 4. Distance deviation — flag only, never blocks
        // boolean distanceExceeds = evaluateDistance(req, header, role);

        // 5. Build + persist (dms_folder_idx taken straight from request)
        TbObKendra kendra = prepareKendraEntity(req, header);
        kendra = kendraRepository.save(kendra);

        // 8. Success response
        prepareSuccessResponse(response, kendra);

        logger.info("End : createKendra | kendraId={}", kendra.getKendraId());
        return response;
    }

    private void validateRequest(CreateKendraRequestFields req) {
        if (isBlank(req.getKendraName())) {
            throw new IllegalArgumentException("Kendra name is required.");
        }

        String freq = req.getMeetingFrequency();
        if (freq != null && !VALID_FREQUENCIES.contains(freq.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid meeting frequency. Allowed : WEEKLY / FORTNIGHTLY / MONTHLY.");
        }
    }

    private void validateDuplicateKendra(CreateKendraRequestFields req) {
        kendraRepository.findByKendraNameAndBranchId(req.getKendraName(), req.getBranchId())
                .ifPresent(k -> {
                    throw new DuplicateApplicationException("Kendra already exists : " + req.getKendraName());
                });
    }
    /**
     * Village / pincode blacklist. Per FSD this BLOCKS creation.
     * TODO: replace stub once CAGL provides the blacklist config / API.
     */

    private TbObKendra prepareKendraEntity(CreateKendraRequestFields req, Header header)
            throws JsonProcessingException {
        LocalDateTime now = LocalDateTime.now();
        String userId = header != null ? header.getUserId() : null;

        String payloadJson = req.getPayload() != null
                ? objectMapper.writeValueAsString(req.getPayload())
                : null;

        return TbObKendra.builder()
                .kendraName(req.getKendraName())
                .branchId(req.getBranchId())
                .kmId(userId)
                .addressLine1(req.getAddressLine1())
                .state(req.getState())
                .district(req.getDistrict())
                .village(req.getVillage())
                .pincode(req.getPincode())
                .gpsLatitude(req.getGpsLatitude())
                .gpsLongitude(req.getGpsLongitude())
                .distanceFromBranch(req.getDistanceFromBranch())
                .meetingDay(req.getMeetingDay())
                .meetingTime(req.getMeetingTime())
                .meetingPlace(req.getMeetingPlace())
                .meetingFrequency(req.getMeetingFrequency())
                .firstMeetingDate(req.getFirstMeetingDate())
                .dmsFolderIdx(req.getDmsFolderIdx())
                .photoDocId(req.getPhotoDocId())
                .payload(payloadJson)
                .status("PENDING")
                .blacklistStatus("PENDING")
                .createdBy(userId)
                .createdTs(now)
                .updatedBy(userId)
                .updatedTs(now)
                .build();
    }

    private void prepareSuccessResponse(Response response, TbObKendra kendra)
            throws JsonProcessingException {
        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setHttpStatus(HttpStatus.OK);
        responseHeader.setResponseCode(String.valueOf(HttpStatus.OK.value()));
        responseHeader.setResponseMessage("Kendra created successfully.");

        CreateKendraResponse body = CreateKendraResponse.builder()
                .kendraId(kendra.getKendraId())
                .status(kendra.getStatus())
                .build();
        ResponseBody responseBody = new ResponseBody();
        responseBody.setResponseObj(objectMapper.writeValueAsString(body));
        response.setResponseHeader(responseHeader);
        response.setResponseBody(responseBody);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Transactional
    public Response createGroup(CreateGroupRequest request, Header header) throws JsonProcessingException {
        logger.info("Start : createGroup");
        Response response = new Response();

        if (request == null || request.getRequestObj() == null) {
            throw new IllegalArgumentException("Invalid request : Group details are missing.");
        }
        CreateGroupRequestFields req = request.getRequestObj();
        logger.debug("Create Group Request : {} | role={}", req);
        validateGroupRequest(req);
        TbObKendra kendra = kendraRepository.findById(req.getKendraId())
                .orElseThrow(() -> new ResourceNotFoundException("Kendra not found : " + req.getKendraId()));
        validateKendraEligibleForGroup(kendra);
        validateDuplicateGroup(req);
        TbObGroup group = prepareGroupEntity(req, header);
        group = groupRepository.save(group);
        //  writeGroupAudit(group, kendra, header);
        prepareGroupSuccessResponse(response, group);
        logger.info("End : createGroup | groupId={}", group.getGroupId());
        return response;
    }

    private void validateGroupRequest(CreateGroupRequestFields req) {
        if (isBlank(req.getGroupName())) {
            throw new IllegalArgumentException("Group name is required.");
        }
        if (req.getKendraId() == null) {
            throw new IllegalArgumentException("Kendra ID is required to create a group.");
        }
    }

    private void validateKendraEligibleForGroup(TbObKendra kendra) {
        String kendraStatus = kendra.getStatus();
        if ("DISSOLVED".equalsIgnoreCase(kendraStatus) || "INACTIVE".equalsIgnoreCase(kendraStatus)
                || "REJECTED".equalsIgnoreCase(kendraStatus)) {
            throw new IllegalArgumentException(
                    "Groups cannot be added to a " + kendraStatus + " Kendra.");
        }
    }
    private void validateDuplicateGroup(CreateGroupRequestFields req) {
        groupRepository.findByGroupNameAndKendraId(req.getGroupName(), req.getKendraId())
                .ifPresent(g -> {
                    throw new DuplicateApplicationException(
                            "Group already exists under this Kendra : " + req.getGroupName());
                });
    }

    private TbObGroup prepareGroupEntity(CreateGroupRequestFields req, Header header)
            throws JsonProcessingException {
        LocalDateTime now = LocalDateTime.now();
        String userId = header != null ? header.getUserId() : null;

        String payloadJson = req.getPayload() != null
                ? objectMapper.writeValueAsString(req.getPayload())
                : null;
        return TbObGroup.builder()
                .groupName(req.getGroupName())
                .kendraId(String.valueOf(req.getKendraId()))
                .totalMemberCount(String.valueOf(0))
                .cgtStatus("PENDING")
                .status("ACTIVE")
                .dmsFolderIdx(req.getDmsFolderIdx())
                .payload(payloadJson)
                //.lastActivityTs(now)
                .createdBy(userId)
                .createdTs(now)
                .updatedBy(userId)
                .updatedTs(now)
                .build();
    }


    private void prepareGroupSuccessResponse(Response response, TbObGroup group) throws JsonProcessingException {
        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setHttpStatus(HttpStatus.OK);
        responseHeader.setResponseCode(String.valueOf(HttpStatus.OK.value()));
        responseHeader.setResponseMessage("Group created successfully.");

        CreateGroupResponse body = CreateGroupResponse.builder()
                .groupId(group.getGroupId())
                .kendraId(group.getKendraId())
                .status(group.getStatus())
                .build();
        ResponseBody responseBody = new ResponseBody();
        responseBody.setResponseObj(objectMapper.writeValueAsString(body));
        response.setResponseHeader(responseHeader);
        response.setResponseBody(responseBody);
    }
}
