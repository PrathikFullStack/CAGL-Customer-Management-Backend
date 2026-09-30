package com.iexceed.appzillonbanking.cagl.cm.rest;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cm.constants.CmConstants;
import com.iexceed.appzillonbanking.cagl.cm.constants.ResponseCodeConstants;
import com.iexceed.appzillonbanking.cagl.cm.constants.ResponseMessageConstants;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.service.RecordLockService;

@RestController
@RequestMapping(CmConstants.API_LOCK)
public class RecordLockRestController {

    private static final Logger logger = LogManager.getLogger(RecordLockRestController.class);

    private final RecordLockService lockService;

    public RecordLockRestController(RecordLockService lockService) {
        this.lockService = lockService;
    }

    @PostMapping("/acquire")
    public ResponseEntity<ResponseWrapper<Boolean>> acquireLock(@RequestBody RequestWrapper<Map<String, String>> request) {
        String appId = request.getBody().get("applicationId");
        String userId = request.getHeader() != null ? request.getHeader().getUserId() : CmConstants.DEFAULT_USER_ID;
        String role = request.getHeader() != null ? request.getHeader().getUserRole() : CmConstants.ROLE_KM;

        boolean locked = lockService.acquireLock(appId, userId, role);
        if (!locked) {
            return ResponseEntity.status(409).body(ResponseWrapper.error(ResponseCodeConstants.CODE_CONFLICT, ResponseMessageConstants.MSG_APP_ALREADY_LOCKED));
        }
        return ResponseEntity.ok(ResponseWrapper.success(true, ResponseMessageConstants.MSG_LOCK_ACQUIRED));
    }

    @PostMapping("/release")
    public ResponseEntity<ResponseWrapper<Boolean>> releaseLock(@RequestBody RequestWrapper<Map<String, String>> request) {
        String appId = request.getBody().get("applicationId");
        String userId = request.getHeader() != null ? request.getHeader().getUserId() : CmConstants.DEFAULT_USER_ID;

        boolean released = lockService.releaseLock(appId, userId);
        return ResponseEntity.ok(ResponseWrapper.success(released, ResponseMessageConstants.MSG_LOCK_RELEASED));
    }
}
