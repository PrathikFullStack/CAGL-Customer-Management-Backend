package com.iexceed.appzillonbanking.cagl.cob.exception;

import com.iexceed.appzillonbanking.cagl.cob.payload.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateApplicationException(
            DuplicateApplicationException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", HttpStatus.CONFLICT.getReasonPhrase());
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);
        });
        response.put("message", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ApplicationNotFoundException ex
                                                                                ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message(ex.getMessage())
                        //.path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(ApplicationLockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(ApplicationLockedException ex
                                                      ) {
        Map<String, Object> details = new HashMap<>();
        details.put("applicationId", ex.getApplicationId());
        details.put("lockedBy", ex.getLockedBy());
        details.put("lockedByRole", ex.getLockedByRole());
        details.put("lockType", ex.getLockType());
        details.put("lockExpiryTs", ex.getLockExpiryTs());

        return ResponseEntity.status(HttpStatus.LOCKED).body(
                ErrorResponse.builder()
                        .status(HttpStatus.LOCKED.value())
                        .error(HttpStatus.LOCKED.getReasonPhrase())
                        .message(ex.getMessage())
                        //.path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .details(details)
                        .build());
    }

    @ExceptionHandler(TransferValidationException.class)
    public ResponseEntity<ErrorResponse> handleTransferValidation(
            TransferValidationException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex
                                                          ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(ex.getMessage())
                        //.path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception while processing request {}", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .message("An unexpected error occurred. Please contact support if this persists.")
                        //.path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(RecordLockedException.class)
    public ResponseEntity<ErrorResponse> handleRecordLockException(RecordLockedException ex) {
        log.warn("Record lock exception while processing request.", ex);

        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.LOCKED.value())
                        .error(HttpStatus.LOCKED.getReasonPhrase())
                        .message("The requested record is currently locked by another user. Please try again after some time.")
                        .timestamp(Instant.now())
                        .build());
    }
}