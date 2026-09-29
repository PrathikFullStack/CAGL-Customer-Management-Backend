package com.iexceed.appzillonbanking.cagl.cm.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.lock.RecordLockRequestDto;
import com.iexceed.appzillonbanking.cagl.cm.payload.lock.RecordLockResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.service.RecordLockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/cm/lock")
@Tag(name = "6. Record Concurrency & Locks", description = "Endpoints for pessimistic concurrency locks to prevent simultaneous editing")
public class RecordLockRestController {

    private static final Logger logger = LogManager.getLogger(RecordLockRestController.class);

    private final RecordLockService lockService;

    public RecordLockRestController(RecordLockService lockService) {
        this.lockService = lockService;
    }

    @PostMapping("/acquire")
    @Operation(summary = "Acquire Application Record Lock", description = "Acquires lock for an application when a user opens it for edit/approval")
    public ResponseEntity<ResponseWrapper<RecordLockResponseDto>> acquireLock(
            @RequestBody RequestWrapper<RecordLockRequestDto> request) {
        String appId = request.getBody().getApplicationId();
        String userId = request.getHeader() != null && request.getHeader().getUserId() != null 
                ? request.getHeader().getUserId() : "SYSTEM";
        String role = request.getHeader() != null && request.getHeader().getUserRole() != null 
                ? request.getHeader().getUserRole() : "KM";

        boolean locked = lockService.acquireLock(appId, userId, role);
        if (!locked) {
            RecordLockResponseDto response = RecordLockResponseDto.builder()
                    .applicationId(appId)
                    .lockStatus("LOCK_FAILED")
                    .success(false)
                    .message("Application is already locked by another user")
                    .build();
            return ResponseEntity.status(409).body(ResponseWrapper.error("409", "Application is already locked by another user"));
        }

        RecordLockResponseDto response = RecordLockResponseDto.builder()
                .applicationId(appId)
                .lockStatus("LOCKED")
                .lockedBy(userId)
                .success(true)
                .message("Lock acquired successfully")
                .build();
        return ResponseEntity.ok(ResponseWrapper.success(response, "Lock acquired successfully"));
    }

    @PostMapping("/release")
    @Operation(summary = "Release Application Record Lock", description = "Releases lock when user leaves the screen or completes the action")
    public ResponseEntity<ResponseWrapper<RecordLockResponseDto>> releaseLock(
            @RequestBody RequestWrapper<RecordLockRequestDto> request) {
        String appId = request.getBody().getApplicationId();
        String userId = request.getHeader() != null && request.getHeader().getUserId() != null 
                ? request.getHeader().getUserId() : "SYSTEM";

        boolean released = lockService.releaseLock(appId, userId);
        RecordLockResponseDto response = RecordLockResponseDto.builder()
                .applicationId(appId)
                .lockStatus(released ? "RELEASED" : "NOT_LOCKED")
                .success(released)
                .message(released ? "Lock released successfully" : "No active lock found to release")
                .build();
        return ResponseEntity.ok(ResponseWrapper.success(response, response.getMessage()));
    }
}
