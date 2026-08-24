package com.iexceed.appzillonbanking.cagl.cob.enums;

/**
 * Audit event codes written to tb_ob_cust_audit_trail / tb_ob_user_audit_trail.
 * Only the events relevant to this module (API 9) are listed here; other APIs
 * (API 1, API 10, API 17 ...) will extend this enum as they are implemented.
 */
public enum AuditEventType {
    APPLICATION_VIEWED,
    APPLICATION_CREATED,
    FIELD_UPDATED,
    EMBDF_DOWNLOADED
}
