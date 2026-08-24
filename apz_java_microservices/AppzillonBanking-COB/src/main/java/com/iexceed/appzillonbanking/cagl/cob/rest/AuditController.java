package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.AuditTrailRecord;
import com.iexceed.appzillonbanking.cagl.cob.payload.AuditTrailResponse;
import com.iexceed.appzillonbanking.cagl.cob.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(description = "Audit Trail Services", name = "Audit")
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "Fetch Application and User Audit Trail", description = "Fetches a combined and chronologically sorted list of all audit events for a given application or customer.")
    @GetMapping("/fetch")
    public ResponseEntity<AuditTrailResponse> fetchAuditTrail(
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) String customerId,
            @RequestHeader(value = "userId", required = false) String userId,
            @RequestHeader(value = "userRole", required = false) String userRole) {

    
        log.info("Start: Fetching audit trail for applicationId: {} or customerId: {}", applicationId, customerId);

        // Input validation
        if (!StringUtils.hasText(applicationId) && !StringUtils.hasText(customerId)) {
            log.warn("Either ApplicationId or CustomerId is mandatory.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        List<AuditTrailRecord> records =
                auditService.fetchCombinedAuditTrail(
                        applicationId,
                        customerId,
                        userId,
                        userRole);

        log.info("End: Found {} audit trail records.", records.size());
        AuditTrailResponse response = new AuditTrailResponse(records);
        return ResponseEntity.ok(response);
    }
}