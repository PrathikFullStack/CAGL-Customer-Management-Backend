package com.iexceed.appzillonbanking.cagl.cm.rest;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cm.constants.CmConstants;
import com.iexceed.appzillonbanking.cagl.cm.constants.ResponseCodeConstants;
import com.iexceed.appzillonbanking.cagl.cm.constants.ResponseMessageConstants;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.CustomerProfileResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.service.CustomerProfileService;

@RestController
@RequestMapping(CmConstants.API_PROFILE)
public class CustomerProfileRestController {

    private static final Logger logger = LogManager.getLogger(CustomerProfileRestController.class);

    private final CustomerProfileService profileService;

    public CustomerProfileRestController(CustomerProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ResponseWrapper<CustomerProfileResponseDto>> getProfile(
            @PathVariable("customerId") String customerId,
            @RequestHeader(value = CmConstants.HEADER_USER_ID, required = false, defaultValue = CmConstants.DEFAULT_USER_ID) String userId) {
        logger.info("Fetching Profile for Customer ID: {} by User: {}", customerId, userId);

        Optional<CustomerProfileResponseDto> profileOpt = profileService.getCustomerProfile(customerId, userId);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ResponseWrapper.error(ResponseCodeConstants.CODE_NOT_FOUND, ResponseMessageConstants.MSG_CUSTOMER_NOT_FOUND));
        }

        return ResponseEntity.ok(ResponseWrapper.success(profileOpt.get(), ResponseMessageConstants.MSG_PROFILE_FETCHED));
    }
}
