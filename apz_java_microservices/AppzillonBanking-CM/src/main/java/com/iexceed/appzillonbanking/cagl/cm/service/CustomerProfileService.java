package com.iexceed.appzillonbanking.cagl.cm.service;

import java.util.Optional;

import com.iexceed.appzillonbanking.cagl.cm.payload.profile.CustomerProfileResponseDto;

public interface CustomerProfileService {


    Optional<CustomerProfileResponseDto> getCustomerProfile(String customerOrAppId, String userId);
}
