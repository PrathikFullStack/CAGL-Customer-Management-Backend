package com.iexceed.appzillonbanking.cagl.cm.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.iexceed.appzillonbanking.cagl.cm.payload.common.ErrorResponse;
import com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseWrapper;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ResponseWrapper<ErrorResponse>> handleApplicationNotFound(ApplicationNotFoundException ex) {
        log.warn("Application not found: {}", ex.getMessage());
        ErrorResponse err = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error("404", ex.getMessage()));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ResponseWrapper<ErrorResponse>> handleCustomerNotFound(CustomerNotFoundException ex) {
        log.warn("Customer not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error("404", ex.getMessage()));
    }

    @ExceptionHandler(RecordLockedException.class)
    public ResponseEntity<ResponseWrapper<ErrorResponse>> handleRecordLocked(RecordLockedException ex) {
        log.warn("Record lock conflict: {}", ex.getMessage());
        Map<String, Object> details = new HashMap<>();
        details.put("applicationId", ex.getApplicationId());
        details.put("lockedBy", ex.getLockedBy());
        details.put("lockedByRole", ex.getLockedByRole());

        ErrorResponse err = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Locked")
                .message(ex.getMessage())
                .details(details)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseWrapper.error("409", ex.getMessage()));
    }

    @ExceptionHandler(InvalidWorkflowTransitionException.class)
    public ResponseEntity<ResponseWrapper<ErrorResponse>> handleInvalidWorkflowTransition(InvalidWorkflowTransitionException ex) {
        log.warn("Invalid workflow transition: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error("400", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<List<String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        log.warn("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.<List<String>>builder()
                        .header(com.iexceed.appzillonbanking.cagl.cm.payload.common.ResponseHeader.builder()
                                .status("FAILURE")
                                .responseCode("400")
                                .responseMessage("Validation failed")
                                .build())
                        .body(errors)
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseWrapper<ErrorResponse>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error("400", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<ErrorResponse>> handleGenericException(Exception ex) {
        log.error("Unhandled exception occurred during request processing", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("500", "An unexpected error occurred. Please contact support."));
    }
}
