package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iexceed.appzillonbanking.cagl.cob.payload.CreateApplicationRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.FetchApplicationDetailsRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.UpdateApplicationRequest;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;

import java.util.concurrent.ExecutionException;

public interface ApplicationService {

    ResponseWrapper createApplication(CreateApplicationRequest request) throws JsonProcessingException;

    ResponseWrapper updateApplication(UpdateApplicationRequest request) throws JsonProcessingException;

    ResponseWrapper getApplicationDetails(FetchApplicationDetailsRequest request, long lockDurationMinutes) throws NoSuchFieldException, IllegalAccessException, JsonProcessingException, ExecutionException, InterruptedException;
}