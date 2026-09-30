package com.iexceed.appzillonbanking.cagl.cm.constants;

/**
 * Standard API response status codes and business error codes.
 */
public final class ResponseCodeConstants {

    private ResponseCodeConstants() {
        // Prevent instantiation
    }

    // HTTP Status Code Strings
    public static final String CODE_SUCCESS = "200";
    public static final String CODE_CREATED = "201";
    public static final String CODE_ACCEPTED = "202";
    public static final String CODE_BAD_REQUEST = "400";
    public static final String CODE_UNAUTHORIZED = "401";
    public static final String CODE_FORBIDDEN = "403";
    public static final String CODE_NOT_FOUND = "404";
    public static final String CODE_CONFLICT = "409";
    public static final String CODE_INTERNAL_SERVER_ERROR = "500";
    public static final String CODE_SERVICE_UNAVAILABLE = "503";

    // Business Status Codes
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    public static final String STATUS_ERROR = "ERROR";
}
