package com.iexceed.appzillonbanking.cagl.cm.service;

import java.util.Optional;

import com.iexceed.appzillonbanking.cagl.cm.payload.profile.CustomerProfileResponseDto;

public interface CustomerProfileService {

    /**
     * Loads full 360-degree consolidated profile response DTO for UI screens
     *
     * @param customerOrAppId Customer ID or Application ID
     * @param userId          Logged-in user ID
     * @return Full customer profile DTO
     */
    Optional<CustomerProfileResponseDto> getCustomerProfile(String customerOrAppId, String userId);
}
