package com.iexceed.appzillonbanking.cagl.cob.rest;

import com.iexceed.appzillonbanking.cagl.cob.payload.PhotoDedupeFetchRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.PhotoDedupeFetchRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.payload.PhotoDedupeUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.PhotoDedupeUpdateRequestWrapper;
import com.iexceed.appzillonbanking.cagl.cob.service.PhotoDedupeService;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Tag(description = "Customer Onboarding", name = "Customer Onboarding")
@RequestMapping("application/cob")
public class PhotoDedupeController {

    private static final Logger logger = LogManager.getLogger(PhotoDedupeController.class);

    @Autowired
    private PhotoDedupeService photoDedupeService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PhotoDedupe status updated successfully"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    @Operation(summary = "Update PhotoDedupeStatus",
            description = "Updates photo_dedupe_status on tb_ob_application_master for one or more application_id(s) in a single call.")
    @PostMapping(value = "/photodedupestatus/submit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> updatePhotoDedupeStatus(
            @RequestBody PhotoDedupeUpdateRequestWrapper requestWrapper,
            @RequestHeader String appId,
            @RequestHeader String interfaceId,
            @RequestHeader String userId,
            @RequestHeader String masterTxnRefNo,
            @RequestHeader String deviceId) {

        logger.info("Received Photo Dedupe Status Update Request.");
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("Header :: {}", header);
        PhotoDedupeUpdateRequest request = requestWrapper == null ? null : requestWrapper.getApiRequest();
        Mono<Response> responseMono = photoDedupeService.updatePhotoDedupeStatus(request, header);
        return responseMono.map(response -> {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setApiResponse(response);
            logger.debug("Photo Dedupe Status Update Response :: {}", response);
            return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        });
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PhotoDedupe pending records fetched successfully"),
            @ApiResponse(responseCode = "408", description = "Service Timed Out"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "Resource Not Found")
    })
    @Operation(summary = "Fetch Pending PhotoDedupeStatus",
            description = "Fetches every application with photo_dedupe_status = PENDING along with each member's photo_doc_id. " +
                    "Pagination is applied only when pageNumber/pageSize are supplied in the request; otherwise the full list is returned.")
    @PostMapping(value = "/photodedupe/pendingstatus/fetch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> fetchPendingPhotoDedupeStatus(
            @RequestBody PhotoDedupeFetchRequestWrapper requestWrapper,
            @RequestHeader String appId,
            @RequestHeader String interfaceId,
            @RequestHeader String userId,
            @RequestHeader String masterTxnRefNo,
            @RequestHeader String deviceId) {

        logger.info("Received Photo Dedupe Pending Status Fetch Request.");
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        logger.debug("Header :: {}", header);
        PhotoDedupeFetchRequest request = requestWrapper == null ? null : requestWrapper.getApiRequest();
        Mono<Response> responseMono = photoDedupeService.fetchPendingPhotoDedupeDetails(request, header);
        return responseMono.map(response -> {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setApiResponse(response);
            logger.debug("Photo Dedupe Pending Status Fetch Response :: {}", response);
            return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        });
    }
}
