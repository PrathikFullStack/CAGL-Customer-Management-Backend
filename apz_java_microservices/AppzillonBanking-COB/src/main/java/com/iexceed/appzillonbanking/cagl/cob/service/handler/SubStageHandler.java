package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;

@FunctionalInterface
public interface SubStageHandler {
    void handle(SubStageHandlerContext context) throws JsonProcessingException;
}
