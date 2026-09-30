package com.iexceed.appzillonbanking.cagl.cm.constants;

/**
 * Standard API response messages.
 */
public final class ResponseMessageConstants {

    private ResponseMessageConstants() {
        // Prevent instantiation
    }

    // Success Messages
    public static final String MSG_SUCCESS = "Operation completed successfully";
    public static final String MSG_PROFILE_FETCHED = "Profile fetched successfully";
    public static final String MSG_SEARCH_COMPLETED = "Search completed successfully";
    public static final String MSG_SECTION_UPDATED = "Section update processed";
    public static final String MSG_LOCK_ACQUIRED = "Lock acquired successfully";
    public static final String MSG_LOCK_RELEASED = "Lock release processed";
    public static final String MSG_DASHBOARD_FETCHED = "Dashboard summary and all records fetched successfully";
    public static final String MSG_QUEUE_ITEMS_FETCHED = "Queue items fetched successfully";
    public static final String MSG_WORKFLOW_HISTORY_FETCHED = "Workflow history fetched successfully";

    // Error Messages
    public static final String MSG_CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String MSG_APP_ALREADY_LOCKED = "Application is already locked by another user";
    public static final String MSG_UNSUPPORTED_SECTION = "Unsupported update section: ";
    public static final String MSG_INTERNAL_SERVER_ERROR = "An internal server error occurred";
}
