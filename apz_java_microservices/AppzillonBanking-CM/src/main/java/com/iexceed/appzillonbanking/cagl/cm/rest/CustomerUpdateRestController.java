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

import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.CustomerUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.UpdateResponseDto;
import com.iexceed.appzillonbanking.cagl.cm.service.handler.UpdateHandler;
import com.iexceed.appzillonbanking.cagl.cm.service.handler.UpdateHandlerRegistry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/cm/update")
@Tag(name = "4. Customer Section Updates", description = "Endpoints for modular data modifications (KYC, Location, Family, Bank, Income, Additional Details)")
public class CustomerUpdateRestController {

    private static final Logger logger = LogManager.getLogger(CustomerUpdateRestController.class);

    private final UpdateHandlerRegistry handlerRegistry;

    public CustomerUpdateRestController(UpdateHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    @PostMapping("/{section}")
    @Operation(summary = "Update Specific Customer Section", description = "Executes section update for: kyc, location, family, bank, income, or additional details")
    public ResponseEntity<ResponseWrapper<UpdateResponseDto>> updateSection(
            @Parameter(description = "Section to update (kyc | location | family | bank | income | additional)", required = true)
            @PathVariable("section") String section,
            @RequestBody RequestWrapper<CustomerUpdateRequest> request) {
        logger.info("Handling section update for Section: {}, Customer ID: {}",
                section, request.getBody().getCustomerId());

        Optional<UpdateHandler> handlerOpt = handlerRegistry.getHandler(section);
        if (handlerOpt.isEmpty()) {
            logger.error("No handler registered for section: {}", section);
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("400", "Unsupported update section: " + section));
        }

        UpdateResponseDto response = handlerOpt.get().handleUpdate(request.getBody(), request.getHeader());
        return ResponseEntity.ok(ResponseWrapper.success(response, "Section update processed"));
    }
}
