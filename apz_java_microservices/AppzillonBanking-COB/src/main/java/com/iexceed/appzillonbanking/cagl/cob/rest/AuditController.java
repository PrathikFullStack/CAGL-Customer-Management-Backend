package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.AuditFetchRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.AuditFetchRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.AuditFetchRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.AuditTrailRecord;
import com.iexceed.appzillonbanking.cagl.cob.payload.AuditTrailResponse;
import com.iexceed.appzillonbanking.cagl.cob.service.AuditService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("application/cob/audit")
@RequiredArgsConstructor
@Tag(description = "Audit Trail Services", name = "Audit")
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "Fetch Application Audit Trail", description = "Fetches a chronologically sorted list of all audit events for a given application.")
    @PostMapping(value = "/fetch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> fetchAuditTrail(
            @RequestBody AuditFetchRequestWrapper requestWrapper,
            @RequestHeader(required = false) String appId,
            @RequestHeader(required = false) String interfaceId,
            @RequestHeader(required = false) String userId,
            @RequestHeader(required = false) String masterTxnRefNo,
            @RequestHeader(required = false) String deviceId) {

        log.info("Start : fetchAuditTrail :: {}", requestWrapper);
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        log.debug("Header :: {}", header);

        AuditFetchRequest request = requestWrapper.getApiRequest();
        AuditFetchRequestFields fields = request != null ? request.getReqObj() : null;

        ResponseWrapper responseWrapper = new ResponseWrapper();
        if (fields == null || !StringUtils.hasText(fields.getApplicationId())) {
            log.warn("ApplicationId is mandatory.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<AuditTrailRecord> records = auditService.fetchApplicationAuditTrail(
                fields.getApplicationId(), fields.getUserId(), fields.getUserRole());
        log.info("Found {} audit trail records for applicationId: {}", records.size(), fields.getApplicationId());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            ResponseBody responseBody = new ResponseBody();
            responseBody.setResponseObj(objectMapper.writeValueAsString(new AuditTrailResponse(records)));
            ResponseHeader responseHeader = new ResponseHeader();
            CommonUtils.generateHeaderForSuccess(responseHeader);
            responseWrapper.setApiResponse(
                    Response.builder()
                            .responseHeader(responseHeader)
                            .responseBody(responseBody)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error serializing audit trail response", e);
        }

        log.info("End : fetchAuditTrail :: {}", responseWrapper);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }
}
