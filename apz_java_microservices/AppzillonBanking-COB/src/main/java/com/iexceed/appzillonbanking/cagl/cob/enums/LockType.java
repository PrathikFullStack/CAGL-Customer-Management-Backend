package com.iexceed.appzillonbanking.cagl.cob.enums;

/**
 * tb_ob_record_lock.lock_type
 * TIMED      = 30-minute auto-expiring lock (RPC Maker working on application).
 * PERMANENT  = held until RPC Checker takes final action (approve/reject). No expiry.
 */
public enum LockType {
    TIMED,
    PERMANENT
}
