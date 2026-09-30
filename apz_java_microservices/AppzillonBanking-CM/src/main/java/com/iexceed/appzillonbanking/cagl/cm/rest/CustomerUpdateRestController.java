package com.iexceed.appzillonbanking.cagl.cm.rest;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cagl.cm.constants.CmConstants;
import com.iexceed.appzillonbanking.cagl.cm.constants.ResponseCodeConstants;
import com.iexceed.appzillonbanking.cagl.cm.constants.ResponseMessageConstants;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.CustomerUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.UpdateResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.service.handler.UpdateHandler;
import com.iexceed.appzillonbanking.cagl.cm.service.handler.UpdateHandlerRegistry;

@RestController
@RequestMapping(CmConstants.API_UPDATE)
public class CustomerUpdateRestController {

    private static final Logger logger = LogManager.getLogger(CustomerUpdateRestController.class);

    private final UpdateHandlerRegistry handlerRegistry;

    public CustomerUpdateRestController(UpdateHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    @PostMapping("/{section}")
    public ResponseEntity<ResponseWrapper<UpdateResponseDto>> updateSection(
            @PathVariable("section") String section,
            @RequestBody RequestWrapper<CustomerUpdateRequest> request) {
        logger.info("Handling section update for Section: {}, Customer ID: {}",
                 section, request.getBody().getCustomerId());

        Optional<UpdateHandler> handlerOpt = handlerRegistry.getHandler(section);
        if (handlerOpt.isEmpty()) {
            logger.error("No handler registered for section: {}", section);
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(ResponseCodeConstants.CODE_BAD_REQUEST, ResponseMessageConstants.MSG_UNSUPPORTED_SECTION + section));
        }

        UpdateResponseDto response = handlerOpt.get().handleUpdate(request.getBody(), request.getHeader());
        return ResponseEntity.ok(ResponseWrapper.success(response, ResponseMessageConstants.MSG_SECTION_UPDATED));
    }
}
