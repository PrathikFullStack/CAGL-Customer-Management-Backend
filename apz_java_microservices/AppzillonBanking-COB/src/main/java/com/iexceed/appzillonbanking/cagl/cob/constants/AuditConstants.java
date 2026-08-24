package com.iexceed.appzillonbanking.cagl.cob.constants;

public final class AuditConstants {

    private AuditConstants() {
        // Private constructor to hide the implicit public one
    }

    // User Audit Event Types
    public static final String DASHBOARD_VIEWED = "DASHBOARD_VIEWED"; // User viewed the dashboard
    public static final String GLOBAL_SEARCH = "GLOBAL_SEARCH"; // User performed a global search
    public static final String AUDIT_TRAIL_VIEWED = "AUDIT_TRAIL_VIEWED"; // User viewed the audit trail
    public static final String APPLICATION_VIEWED = "APPLICATION_VIEWED"; // User opened/viewed an application
    public static final String DOCUMENT_VIEWED = "DOCUMENT_VIEWED"; // User viewed a specific document
    public static final String RECORD_LOCK_ACQUIRED = "RECORD_LOCK_ACQUIRED"; // User locked a record for editing
    public static final String EMBDF_DOWNLOADED = "EMBDF_DOWNLOADED"; // User downloaded the eMBDF
    public static final String OTP_SENT = "OTP_SENT"; // An OTP was sent to a user or customer
    public static final String OTP_VERIFIED = "OTP_VERIFIED"; // An OTP was successfully verified

    // Application Audit Event Types
    public static final String APPLICATION_CREATED = "APPLICATION_CREATED"; // A new application was initiated
    public static final String APPLICATION_UPDATED = "APPLICATION_UPDATED"; // A field in the application was updated
    public static final String KM_SUBMITTED = "KM_SUBMITTED"; // KM submitted the application
    public static final String BRE_TRIGGERED = "BRE_TRIGGERED"; // Business Rule Engine was triggered
    public static final String RPC_APPROVED = "RPC_APPROVED"; // RPC approved the application
    public static final String RPC_REJECTED = "RPC_REJECTED"; // RPC rejected the application
    public static final String CGT_EVENT = "CGT_EVENT"; // A CGT-related event occurred
    public static final String BM_REINTERVIEW_SUBMITTED = "BM_REINTERVIEW_SUBMITTED"; // BM submitted a re-interview
    public static final String GRT_SUBMITTED = "GRT_SUBMITTED"; // GRT was submitted
    public static final String T24_ACTIVATED = "T24_ACTIVATED"; // Application was activated in T24
    public static final String MBDF_TRANSFERRED = "MBDF_TRANSFERRED"; // MBDF data was transferred
    public static final String DOCUMENT_REUPLOADED = "DOCUMENT_REUPLOADED"; // A document was re-uploaded
    public static final String STAGE_CHANGED = "STAGE_CHANGED"; // The application moved to a new stage
}