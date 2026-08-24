package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iexceed.appzillonbanking.cagl.cob.payload.FetchApplicationDetailsRequest;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;

import java.util.concurrent.ExecutionException;

public interface FetchApplicationHandler {
    ResponseWrapper handleFetchApplicationDetails(FetchApplicationDetailsRequest req, long lockDurationMinutes) throws JsonProcessingException, ExecutionException, InterruptedException;
}