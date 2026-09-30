package com.iexceed.appzillonbanking.cagl.cm.constants;

/**
 * General application-wide constants for Customer Management microservice.
 */
public final class CmConstants {

    private CmConstants() {
        // Prevent instantiation
    }

    // Default User & Roles
    public static final String DEFAULT_USER_ID = "SYSTEM";
    public static final String ROLE_KM = "KM";
    public static final String ROLE_BM = "BM";
    public static final String ROLE_AM = "AM";
    public static final String ROLE_RPC_MAKER = "RPCMAKER";
    public static final String ROLE_RPC_CHECKER = "RPCCHECKER";
    public static final String ROLE_AML = "AML";
    public static final String ROLE_CRT = "CRT";

    // Source Systems
    public static final String SOURCE_CDH = "CDH";
    public static final String SOURCE_CM = "CM";
    public static final String SOURCE_APZILLON = "APZILLON";

    // Numeric & Boolean String Flags
    public static final String FLAG_TRUE = "1";
    public static final String FLAG_FALSE = "0";
    public static final String FLAG_YES = "Y";
    public static final String FLAG_NO = "N";

    // API Base Endpoints
    public static final String API_V1_PREFIX = "/api/v1/cm";
    public static final String API_PROFILE = API_V1_PREFIX + "/profile";
    public static final String API_SEARCH = API_V1_PREFIX + "/search";
    public static final String API_UPDATE = API_V1_PREFIX + "/update";
    public static final String API_DASHBOARD = API_V1_PREFIX + "/dashboard";
    public static final String API_WORKFLOW = API_V1_PREFIX + "/workflow";
    public static final String API_LOCK = API_V1_PREFIX + "/lock";

    // Request Headers
    public static final String HEADER_USER_ID = "userId";
    public static final String HEADER_USER_ROLE = "userRole";
    public static final String HEADER_BRANCH_ID = "branchId";
    public static final String HEADER_APP_ID = "appId";
    public static final String HEADER_CHANNEL = "channel";
}
