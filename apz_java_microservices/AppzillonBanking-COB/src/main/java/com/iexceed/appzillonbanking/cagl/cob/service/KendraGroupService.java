package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import com.iexceed.appzillonbanking.cagl.cob.exception.BlacklistedLocationException;
import com.iexceed.appzillonbanking.cagl.cob.exception.DuplicateApplicationException;
import com.iexceed.appzillonbanking.cagl.cob.exception.ResourceNotFoundException;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGroupRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObKendraRepository;
import com.iexceed.appzillonbanking.core.payload.*;
import com.iexceed.appzillonbanking.interfaceAdapter.service.InterfaceAdapter;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class KendraGroupService {

    private static final Logger logger = LogManager.getLogger(KendraGroupService.class);

    @Autowired
    private TbObKendraRepository kendraRepository;

    @Autowired
    private InterfaceAdapter interfaceAdapter;

    @Autowired
    private TbObGroupRepository groupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kendra.distance.default-threshold-km:25}")
    private BigDecimal distanceThresholdKm;

    @Autowired
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kendra.blacklist.check.url}")
    private String kendraBlacklistCheckUrl;

    @Value("${group.min-member-count-threshold:5}")
    private int minMemberCountThreshold;

    @Value("${group.max-member-count-limit:20}")
    private int maxMemberCountLimit;

    private static final List<String> SPECIAL_PRIVILEGE_ROLES = List.of("AM", "CHT", "IT");

    private static final List<String> VALID_FREQUENCIES = List.of("WEEKLY", "FORTNIGHTLY", "MONTHLY");

    @Transactional
    public Response createKendra(CreateKendraRequest request, Header header) throws JsonProcessingException {
        logger.info("Start : createKendra");
        Response response = new Response();

        if (request == null || request.getRequestObj() == null) {
            throw new IllegalArgumentException("Invalid request : Kendra details are missing.");
        }
        CreateKendraRequestFields req = request.getRequestObj();
        logger.debug("Create Kendra Request : {}", req);
        validateRequest(req);
        validateDuplicateKendra(req);

        performBlacklistValidation(req, header);
        DistanceValidationResult distanceResult = evaluateDistance(req);
        Long generatedKendraId = kendraRepository.getNextKendraId();
        String kendraId = String.valueOf(generatedKendraId);
        logger.debug("Generated kendraId :: {}", kendraId);

        String t24RefNumber = null;
        try {
            KendraCreationRequest t24Request = buildKendraCreationRequest(req, kendraId, header);
            Mono<Object> t24ResponseMono = interfaceAdapter.callExternalService(
                    header, t24Request, "kendraCreation", true);
            Object t24Response = t24ResponseMono.block();
            if (t24Response != null) {
                String t24ResponseJson = objectMapper.writeValueAsString(t24Response);
                JsonNode t24RootNode = objectMapper.readTree(t24ResponseJson);
                t24RefNumber = t24RootNode.path("header").path("id").asText(null);
                logger.debug("T24 reference number received :: {}", t24RefNumber);
            } else {
                logger.warn("T24 KendraCreation call returned an empty response for kendraId:{}", kendraId);
            }
        } catch (Exception ex) {
            logger.error("Error calling T24 KendraCreation for kendraId:{}", kendraId, ex);
        }
        TbObKendra kendra = prepareKendraEntity(req, header, kendraId, t24RefNumber, distanceResult);
        kendra = kendraRepository.save(kendra);

        createDefaultGroupForKendra(kendra, header);

        prepareSuccessResponse(response, kendra);
        logger.info("End : createKendra | kendraId={}", kendra.getKendraId());
        return response;
    }

    private void performBlacklistValidation(CreateKendraRequestFields req, Header header) {
        CheckBlacklistRequestFields checkFields = CheckBlacklistRequestFields.builder()
                .village(req.getVillage())
                .pincode(req.getPincode())
                .build();

        CheckBlacklistRequest checkRequest = CheckBlacklistRequest.builder()
                .interfaceName("CheckKendraBlacklist")
                .appId(header != null ? header.getAppId() : null)
                .userId(header != null ? header.getUserId() : null)
                .requestObj(checkFields)
                .build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (header != null) {
            httpHeaders.set("appId", header.getAppId());
            httpHeaders.set("interfaceId", header.getInterfaceId());
            httpHeaders.set("userId", header.getUserId());
            httpHeaders.set("masterTxnRefNo", header.getMasterTxnRefNo());
            httpHeaders.set("deviceId", header.getDeviceId());
        }
        try {
            HttpEntity<CheckBlacklistRequest> entity = new HttpEntity<>(checkRequest, httpHeaders);
            ResponseEntity<ResponseWrapper> cdhResponse =
                    restTemplate.postForEntity(kendraBlacklistCheckUrl, entity, ResponseWrapper.class);
            ResponseWrapper wrapper = cdhResponse.getBody();
            if (wrapper == null || wrapper.getApiResponse() == null
                    || wrapper.getApiResponse().getResponseBody() == null) {
                logger.warn("Blacklist check returned no usable response — proceeding without blocking, "
                        + "since we can't confirm a match either way. Fail-open — confirm this is acceptable.");
                return;
            }
            String responseObj = wrapper.getApiResponse().getResponseBody().getResponseObj();
            CheckBlacklistResult result = objectMapper.readValue(responseObj, CheckBlacklistResult.class);
            if (result.isBlacklisted()) {
                logger.warn("Kendra creation blocked — village/pincode blacklisted. matchedOn:{}, reason:{}",
                        result.getMatchedOn(), result.getReason());
                throw new BlacklistedLocationException(
                        result.getReason() != null ? result.getReason() : "Selected location is restricted.");
            }
        } catch (BlacklistedLocationException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error calling blacklist check — proceeding without blocking, since we can't confirm a "
                    + "match either way. Fail-open — confirm this is acceptable.", ex);
        }
    }

    private TbObKendra prepareKendraEntity(CreateKendraRequestFields req, Header header, String kendraId,
                                           String t24RefNumber, DistanceValidationResult distanceResult) throws JsonProcessingException {
        LocalDateTime now = LocalDateTime.now();
        String userId = header != null ? header.getUserId() : null;

        String payloadJson = req.getPayload() != null
                ? objectMapper.writeValueAsString(req.getPayload())
                : null;
        return TbObKendra.builder()
                .kendraId(kendraId)
                .t24RefNo(t24RefNumber)
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
                .distanceFromBranch(distanceResult.getDistanceKm())
                .meetingDay(req.getMeetingDay())
                .meetingTime(encodeMeetingTimeRange(req.getMeetingTimeFrom(), req.getMeetingTimeTo()))
                .meetingPlace(req.getMeetingPlace())
                .meetingFrequency(req.getMeetingFrequency())
                .firstMeetingDate(req.getFirstMeetingDate())
                .dmsFolderIdx(req.getDmsFolderIdx())
                .payload(payloadJson)
                .status("PENDING")
                .blacklistStatus("CLEAR")
                .totalGroupCount(0)
                .createdBy(userId)
                .createdTs(now)
                .updatedBy(userId)
                .updatedTs(now)
                .build();
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
        kendraRepository.findByKendraName(req.getKendraName())
                .ifPresent(k -> {
                    throw new DuplicateApplicationException("Kendra already exists : " + req.getKendraName());
                });
    }
    private String generateRecordId(String branchId) {
        String branchSuffix = (branchId != null && branchId.length() >= 4)
                ? branchId.substring(branchId.length() - 4)
                : String.format("%4s", branchId == null ? "" : branchId).replace(' ', '0');
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String timePart = now.format(DateTimeFormatter.ofPattern("HHmmssSSS"));

        return "K" + branchSuffix + datePart + timePart;
    }
    private String generateGroupReferenceCode(String branchId) {
        String branchSuffix = (branchId != null && branchId.length() >= 4)
                ? branchId.substring(branchId.length() - 4)
                : String.format("%4s", branchId == null ? "" : branchId).replace(' ', '0');
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String timePart = now.format(DateTimeFormatter.ofPattern("HHmmssSSS"));

        return "G" + branchSuffix + datePart + timePart;
    }
    private String encodeMeetingTimeRange(String from, String to) {
        String fromDigits = (from != null) ? from.replace(":", "") : "0000";
        String toDigits = (to != null) ? to.replace(":", "") : "0000";
        return fromDigits + toDigits;
    }
    private KendraCreationRequest buildKendraCreationRequest(CreateKendraRequestFields req, String kendraId,
                                                             Header header) {

        DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

        String firstMeetingDateStr = req.getFirstMeetingDate() != null
                ? req.getFirstMeetingDate().format(yyyyMMdd)
                : null;
        String projectionMeetingDateStr = req.getProjectionMeetingDate() != null
                ? req.getProjectionMeetingDate().format(yyyyMMdd)
                : null;
        String surveyDateStr = LocalDate.now().format(yyyyMMdd);
        String gpsLat = req.getGpsLatitude() != null ? req.getGpsLatitude().toPlainString() : "";
        String gpsLong = req.getGpsLongitude() != null ? req.getGpsLongitude().toPlainString() : "";
        Integer distanceInt = req.getDistanceFromBranch() != null
                ? req.getDistanceFromBranch().setScale(0, RoundingMode.HALF_UP).intValue()
                : null;
        Integer pinCodeInt = null;
        if (req.getPincode() != null && !req.getPincode().isBlank()) {
            try {
                pinCodeInt = Integer.valueOf(req.getPincode());
            } catch (NumberFormatException ex) {
                logger.warn("pincode '{}' is not numeric — sending null pinCode to T24", req.getPincode());
            }
        }
        List<KendraAddressItem> addressList = new ArrayList<>();
        for (String line : new String[]{req.getAddressLine1(), req.getAddressLine2(),
                req.getAddressLine3(), req.getAddressLine4()}) {
            if (line != null && !line.isBlank()) {
                addressList.add(KendraAddressItem.builder().address(line).build());
            }
        }
        String userId = header != null ? header.getUserId() : null;
        String kendraManagerId = req.getKmId() != null ? req.getKmId() : userId;
        String recordId = generateRecordId(req.getBranchId());

        KendraCreationRequestFields fields = KendraCreationRequestFields.builder()
                .distance(distanceInt)
                .gpsLatitude(gpsLat)
                .gpsLongitude(gpsLong)
                .kendraName(req.getKendraName())
                .firstMeetingDate(firstMeetingDateStr)
                .projectionMeetingDate(projectionMeetingDateStr)
                .recordId(recordId)
                .searchId("")
                .areaType(req.getAreaType())
                .villageType(req.getVillageType())
                .kendraAddress(addressList)
                .village(req.getVillage())
                .meetingFrequency(req.getMeetingFrequency())
                .branchId(req.getBranchId())
                .kendraId(kendraId)
                .surveyDate(surveyDateStr)
                .taluk(req.getTaluk())
                .recordType("ACTIVE")
                .stateId(req.getState() != null ? String.valueOf(req.getState()) : null)
                .surveyOfficer(userId)
                .meetingTimeTo(req.getMeetingTimeTo())
                .parentId("")
                .referenceId(recordId)
                .meetingTimeFrom(req.getMeetingTimeFrom())
                .kendraManager(kendraManagerId)
                .projectMeetingOfficer(userId)
                .districtId(req.getDistrict())
                .meetingPlace(req.getMeetingPlace())
                .pinCode(pinCodeInt)
                .build();

        return KendraCreationRequest.builder()
                .appId(header != null ? header.getAppId() : null)
                .userId(userId)
                .interfaceName("kendraCreation")
                .requestObj(fields)
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

    private DistanceValidationResult evaluateDistance(CreateKendraRequestFields req) {
        BigDecimal actualDistanceKm = req.getDistanceFromBranch();

        if (actualDistanceKm == null) {
            logger.debug("No distance supplied — skipping distance validation.");
            return DistanceValidationResult.builder()
                    .distanceKm(null)
                    .thresholdKm(distanceThresholdKm)
                    .deviationKm(null)
                    .exceeded(false)
                    .build();
        }
        boolean exceeded = actualDistanceKm.compareTo(distanceThresholdKm) > 0;
        BigDecimal deviationKm = exceeded
                ? actualDistanceKm.subtract(distanceThresholdKm)
                : BigDecimal.ZERO;
        if (exceeded) {
            logger.warn("Kendra distance threshold exceeded for branchId:{} — reported:{}km, threshold:{}km, "
                            + "deviation:{}km. Creation is NOT blocked per FSD, but flagged for BM/AM/monitoring visibility.",
                    req.getBranchId(), actualDistanceKm, distanceThresholdKm, deviationKm);
        }
        return DistanceValidationResult.builder()
                .distanceKm(actualDistanceKm)
                .thresholdKm(distanceThresholdKm)
                .deviationKm(deviationKm)
                .exceeded(exceeded)
                .build();
    }

    //Group Creation
    @Transactional
    public Response createGroup(CreateGroupRequest request, Header header) throws JsonProcessingException {
        logger.info("Start : createGroup");
        Response response = new Response();

        if (request == null || request.getRequestObj() == null) {
            throw new IllegalArgumentException("Invalid request : Group details are missing.");
        }
        CreateGroupRequestFields req = request.getRequestObj();
        logger.debug("Create Group Request : {}", req);
        validateGroupRequest(req);

        TbObKendra kendra = kendraRepository.findById(req.getKendraId())
                .orElseThrow(() -> new ResourceNotFoundException("Kendra not found : " + req.getKendraId()));
        validateKendraEligibleForGroup(kendra);
        validateDuplicateGroup(req);
        validateGroupMemberCountLimits(req.getKendraId());

        boolean specialPrivilege = req.getUserRole() != null
                && SPECIAL_PRIVILEGE_ROLES.contains(req.getUserRole().toUpperCase());
        Long generatedGroupId = groupRepository.getNextGroupId();
        String groupId = String.valueOf(generatedGroupId);
        logger.debug("Generated groupId :: {}", groupId);

        String t24RefNumber = null;
        try {
            GroupCreationRequest t24Request = buildGroupCreationRequest(req, groupId, kendra, header);
            Mono<Object> t24ResponseMono = interfaceAdapter.callExternalService(
                    header, t24Request, "groupCreation", true);
            Object t24Response = t24ResponseMono.block();
            if (t24Response != null) {
                String t24ResponseJson = objectMapper.writeValueAsString(t24Response);
                JsonNode t24RootNode = objectMapper.readTree(t24ResponseJson);
                t24RefNumber = t24RootNode.path("header").path("id").asText(null);
                logger.debug("T24 group reference number received :: {}", t24RefNumber);
            } else {
                logger.warn("T24 GroupCreation call returned an empty response for groupId:{}", groupId);
            }
        } catch (Exception ex) {
            logger.error("Error calling T24 GroupCreation for groupId:{}", groupId, ex);
        }
        TbObGroup group = prepareGroupEntity(req, header, groupId, t24RefNumber, specialPrivilege);
        group = groupRepository.save(group);

        prepareGroupSuccessResponse(response, group);
        logger.info("End : createGroup | groupId={}", group.getGroupId());
        return response;
    }

    private void validateGroupMemberCountLimits(String kendraId) {
        List<TbObGroup> existingGroups = groupRepository.findByKendraId(kendraId);

        int totalMembers = 0;
        for (TbObGroup g : existingGroups) {
            int count = parseMemberCount(g.getTotalMemberCount());
            totalMembers += count;

            if (count < minMemberCountThreshold) {
                throw new IllegalArgumentException(
                        "Cannot add a new group — existing group '" + g.getGroupName()
                                + "' under this Kendra has only " + count
                                + " member(s), below the minimum of " + minMemberCountThreshold + ".");
            }
        }

        if (totalMembers >= maxMemberCountLimit) {
            throw new IllegalArgumentException(
                    "Cannot add a new group — Kendra has reached the maximum member limit of "
                            + maxMemberCountLimit + " (currently " + totalMembers + ").");
        }
    }
    private int parseMemberCount(String value) {
        try {
            return value != null ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException ex) {
            logger.warn("Non-numeric total_member_count '{}' — treating as 0", value);
            return 0;
        }
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

    private GroupCreationRequest buildGroupCreationRequest(CreateGroupRequestFields req, String groupId,
                                                           TbObKendra kendra, Header header) {
        String userId = header != null ? header.getUserId() : null;
        String groupReferenceCode = generateGroupReferenceCode(kendra.getBranchId());

        Long kendraT24Ref = null;
        if (kendra.getT24RefNo() != null) {
            try {
                kendraT24Ref = Long.valueOf(kendra.getT24RefNo());
            } catch (NumberFormatException ex) {
                logger.warn("Kendra t24RefNo '{}' is not numeric — sending null kendraId to T24 for group creation",
                        kendra.getT24RefNo());
            }
        }
        GroupCreationRequestFields fields = GroupCreationRequestFields.builder()
                .recordId(Long.valueOf(groupId))
                .branchId(kendra.getBranchId())
                .grtBy(userId)
                .groupName(groupReferenceCode)
                .groupType("JLG")
                .kendraId(kendraT24Ref)
                .groupId(groupReferenceCode)
                .grtDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .referenceId(groupReferenceCode)
                .build();

        return GroupCreationRequest.builder()
                .appId(header != null ? header.getAppId() : null)
                .userId(userId)
                .interfaceName("groupCreation")
                .requestObj(List.of(fields))
                .build();
    }

    private TbObGroup prepareGroupEntity(CreateGroupRequestFields req, Header header, String groupId,
                                         String t24RefNumber, boolean specialPrivilege) throws JsonProcessingException {
        LocalDateTime now = LocalDateTime.now();
        String userId = header != null ? header.getUserId() : null;

        String payloadJson = req.getPayload() != null
                ? objectMapper.writeValueAsString(req.getPayload())
                : null;
        return TbObGroup.builder()
                .groupId(groupId)
                .groupName(req.getGroupName())
                .kendraId(String.valueOf(req.getKendraId()))
                .t24RefNo(t24RefNumber)
                .totalMemberCount(String.valueOf(0))
                .activatedCount(String.valueOf(0))
                .inactiveCount(String.valueOf(0))
                .inprogressCount(String.valueOf(0))
                .releasedCount(String.valueOf(0))
                .cgtStatus("PENDING")
                .status("ACTIVE")
                .dmsFolderIdx(req.getDmsFolderIdx())
                .payload(payloadJson)
                .createdBy(userId)
                .createdTs(now)
                .updatedBy(userId)
                .updatedTs(now)
                .build();
    }

    private void createDefaultGroupForKendra(TbObKendra kendra, Header header) throws JsonProcessingException {
        Long generatedGroupId = groupRepository.getNextGroupId();
        String groupId = String.valueOf(generatedGroupId);

        CreateGroupRequestFields defaultGroupReq = CreateGroupRequestFields.builder()
                .groupName(kendra.getKendraName() + " - Group 1")
                .kendraId(kendra.getKendraId())
                .build();
        String t24RefNumber = null;
        try {
            GroupCreationRequest t24Request = buildGroupCreationRequest(defaultGroupReq, groupId, kendra, header);
            Mono<Object> t24ResponseMono = interfaceAdapter.callExternalService(
                    header, t24Request, "groupCreation", true);
            Object t24Response = t24ResponseMono.block();
            if (t24Response != null) {
                String t24ResponseJson = objectMapper.writeValueAsString(t24Response);
                JsonNode t24RootNode = objectMapper.readTree(t24ResponseJson);
                t24RefNumber = t24RootNode.path("header").path("id").asText(null);
            }
        } catch (Exception ex) {
            logger.error("Error calling T24 GroupCreation for default group, kendraId:{}", kendra.getKendraId(), ex);
        }
        TbObGroup defaultGroup = prepareGroupEntity(defaultGroupReq, header, groupId, t24RefNumber, false);
        groupRepository.save(defaultGroup);
        logger.info("Default group created for new Kendra :: kendraId={}, groupId={}",
                kendra.getKendraId(), defaultGroup.getGroupId());
    }

    private void prepareGroupSuccessResponse(Response response, TbObGroup group) throws JsonProcessingException {
        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setHttpStatus(HttpStatus.OK);
        responseHeader.setResponseCode(String.valueOf(HttpStatus.OK.value()));

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

    public Response fetchGroupMemberCount(FetchGroupMemberCountRequest request, Header header) throws JsonProcessingException {
        String userRole = request.requestObj().userRole();
        String userId = request.requestObj().userId();
        String branchId = request.requestObj().branchId();

        List<TbObKendra> kendras = resolveKendras(userRole, userId, branchId);

        List<FetchKendraGroupCountResponse> response = kendras.isEmpty()
                ? List.of()
                : buildKendraGroupCountResponses(kendras);

        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setHttpStatus(HttpStatus.OK);
        responseHeader.setResponseCode(String.valueOf(HttpStatus.OK.value()));
        ResponseBody responseBody = new ResponseBody();
        responseBody.setResponseObj(objectMapper.writeValueAsString(response));

        return Response.builder()
                .responseHeader(responseHeader)
                .responseBody(responseBody)
                .build();
    }

    private List<TbObKendra> resolveKendras(String userRole, String userId, String branchId) {
        return switch (userRole) {
            case "KM" -> kendraRepository.findByKmId(userId);
            case "BM" -> kendraRepository.findByBranchId(branchId);
            default -> List.of();
        };
    }

    private List<FetchKendraGroupCountResponse> buildKendraGroupCountResponses(List<TbObKendra> kendras) {
        List<String> kendraIds = kendras.stream()
                .map(TbObKendra::getKendraId)
                .toList();

        List<TbObGroup> groups = groupRepository.findByKendraIdIn(kendraIds);

        Map<String, List<TbObGroup>> groupsByKendraId = groups.stream()
                .collect(Collectors.groupingBy(TbObGroup::getKendraId));

        return kendras.stream()
                .map(kendra -> toKendraGroupCountResponse(
                        kendra,
                        groupsByKendraId.getOrDefault(kendra.getKendraId(), List.of())))
                .toList();
    }

    private FetchKendraGroupCountResponse toKendraGroupCountResponse(TbObKendra kendra, List<TbObGroup> groups) {
        List<FetchGroupMemberCountResponse> groupCounts = groups.stream()
                .map(this::toGroupCountResponse)
                .toList();

        return FetchKendraGroupCountResponse.builder()
                .kendraId(kendra.getKendraId())
                .kendraName(kendra.getKendraName())
                .groups(groupCounts)
                .build();
    }

    private FetchGroupMemberCountResponse toGroupCountResponse(TbObGroup group) {
        return FetchGroupMemberCountResponse.builder()
                .groupId(group.getGroupId())
                .totalMemberCount(group.getTotalMemberCount())
                .activatedCount(group.getActivatedCount())
                .inactiveCount(group.getInactiveCount())
                .inprogessCount(group.getInprogressCount())
                .releasedCount(group.getReleasedCount())
                .build();
    }
}
