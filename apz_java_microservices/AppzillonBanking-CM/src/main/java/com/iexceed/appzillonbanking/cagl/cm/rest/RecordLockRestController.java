package com.iexceed.appzillonbanking.cagl.cm.rest;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.service.RecordLockService;

@RestController
@RequestMapping("/api/v1/cm/lock")
public class RecordLockRestController {

    private static final Logger logger = LogManager.getLogger(RecordLockRestController.class);

    private final RecordLockService lockService;

    public RecordLockRestController(RecordLockService lockService) {
        this.lockService = lockService;
    }

    @PostMapping("/acquire")
    public ResponseEntity<ResponseWrapper<Boolean>> acquireLock(@RequestBody RequestWrapper<Map<String, String>> request) {
        String appId = request.getBody().get("applicationId");
        String userId = request.getHeader() != null ? request.getHeader().getUserId() : "SYSTEM";
        String role = request.getHeader() != null ? request.getHeader().getUserRole() : "KM";

        boolean locked = lockService.acquireLock(appId, userId, role);
        if (!locked) {
            return ResponseEntity.status(409).body(ResponseWrapper.error("409", "Application is already locked by another user"));
        }
        return ResponseEntity.ok(ResponseWrapper.success(true, "Lock acquired successfully"));
    }

    @PostMapping("/release")
    public ResponseEntity<ResponseWrapper<Boolean>> releaseLock(@RequestBody RequestWrapper<Map<String, String>> request) {
        String appId = request.getBody().get("applicationId");
        String userId = request.getHeader() != null ? request.getHeader().getUserId() : "SYSTEM";

        boolean released = lockService.releaseLock(appId, userId);
        return ResponseEntity.ok(ResponseWrapper.success(released, "Lock release processed"));
    }
}
