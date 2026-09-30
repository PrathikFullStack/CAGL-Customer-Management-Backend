package com.iexceed.appzillonbanking.cagl.cm.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.iexceed.appzillonbanking.cagl.cm.constants.ResponseCodeConstants;
import com.iexceed.appzillonbanking.cagl.cm.constants.ResponseMessageConstants;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Object>> handleGenericException(Exception ex) {
        logger.error("Unhandled Exception caught in GlobalExceptionHandler: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error(
                        ResponseCodeConstants.CODE_INTERNAL_SERVER_ERROR,
                        ResponseMessageConstants.MSG_INTERNAL_SERVER_ERROR + ": " + ex.getMessage()));
    }
}
