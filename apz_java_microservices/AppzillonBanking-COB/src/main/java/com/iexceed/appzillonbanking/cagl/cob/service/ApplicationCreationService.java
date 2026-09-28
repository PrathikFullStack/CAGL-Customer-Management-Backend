package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iexceed.appzillonbanking.cagl.cob.payload.CreateApplicationRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.utils.RequestValidationUtils;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationCreationService {

    private final DuplicateValidationService duplicateValidationService;

    public Response createApplication(CreateApplicationRequestFields requestObj) throws JsonProcessingException {

        List<String> validationErrors = RequestValidationUtils.validateApplicationRequest(requestObj);
        if (!validationErrors.isEmpty()) {
            log.warn("Application request validation failed: {}", validationErrors);
            return buildValidationFailureResponse(validationErrors);
        }

        Response duplicateCheckResponse = duplicateValidationService.validateDatabaseDuplicate(
                requestObj.getApplicationdtls(),
                record -> record.getCustomerDtls().getKycDetails().getMobileNum(),
                DuplicateValidationService.DuplicateCheckScope.APPLICATION_CREATION);

        if (duplicateCheckResponse != null) {
            return duplicateCheckResponse;
        }

        return null;
    }

    public static Response buildValidationFailureResponse(List<String> validationErrors) {
        ResponseHeader responseHeader = new ResponseHeader();
        CommonUtils.generateHeaderForFailure(responseHeader, String.join(" | ", validationErrors));

        Response response = new Response();
        response.setResponseHeader(responseHeader);
        response.setResponseBody(new ResponseBody());
        return response;
    }
}