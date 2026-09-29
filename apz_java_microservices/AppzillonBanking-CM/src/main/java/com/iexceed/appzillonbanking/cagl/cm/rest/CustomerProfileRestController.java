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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/cm/profile")
@Tag(name = "3. Customer 360° Profile", description = "Endpoints for retrieving full 360-degree customer profile view")
public class CustomerProfileRestController {

    private static final Logger logger = LogManager.getLogger(CustomerProfileRestController.class);

    private final CustomerProfileService profileService;

    public CustomerProfileRestController(CustomerProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get Customer 360° Profile", description = "Returns full profile details including Demographics, Addresses, Family Members, Bank accounts, Active Loans, and Profile Progress")
    public ResponseEntity<ResponseWrapper<CustomerProfileResponseDto>> getProfile(
            @Parameter(description = "Customer Identifier / Member ID", required = true)
            @PathVariable("customerId") String customerId,
            @Parameter(description = "Logged in User ID")
            @RequestHeader(value = "userId", required = false, defaultValue = "SYSTEM") String userId) {
        logger.info("Fetching Profile for Customer ID: {} by User: {}", customerId, userId);

        Optional<CustomerProfileResponseDto> profileOpt = profileService.getCustomerProfile(customerId, userId);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ResponseWrapper.error("404", "Customer not found"));
        }

        return ResponseEntity.ok(ResponseWrapper.success(profileOpt.get(), "Profile fetched successfully"));
    }
}
