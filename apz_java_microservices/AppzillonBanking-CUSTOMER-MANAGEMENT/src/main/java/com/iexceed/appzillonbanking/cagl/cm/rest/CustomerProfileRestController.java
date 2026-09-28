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

import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.profile.CustomerProfileResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.service.CustomerProfileService;

@RestController
@RequestMapping("/api/v1/cm/profile")
public class CustomerProfileRestController {

    private static final Logger logger = LogManager.getLogger(CustomerProfileRestController.class);

    private final CustomerProfileService profileService;

    public CustomerProfileRestController(CustomerProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ResponseWrapper<CustomerProfileResponseDto>> getProfile(
            @PathVariable("customerId") String customerId,
            @RequestHeader(value = "userId", required = false, defaultValue = "SYSTEM") String userId) {
        logger.info("Fetching Profile for Customer ID: {} by User: {}", customerId, userId);

        Optional<CustomerProfileResponseDto> profileOpt = profileService.getCustomerProfile(customerId, userId);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ResponseWrapper.error("404", "Customer not found"));
        }

        return ResponseEntity.ok(ResponseWrapper.success(profileOpt.get(), "Profile fetched successfully"));
    }
}
