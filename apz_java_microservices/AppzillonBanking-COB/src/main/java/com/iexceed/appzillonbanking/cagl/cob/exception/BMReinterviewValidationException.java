package com.iexceed.appzillonbanking.cagl.cob.exception;

/**
 * Deliberate, caller-facing validation failure for the BM Reinterview flow (bad request state,
 * "not found" lookups, an invalid decision, ...)
 * <p>
 * {@link com.iexceed.appzillonbanking.cagl.cob.service.BMReinterviewService} catches this type
 * separately from a plain {@link RuntimeException} specifically so its message is safe to echo
 * back in the API response -- an unexpected exception (NPE, DB constraint violation, ...) is never
 * one of these, and gets a generic message instead of leaking internals to the caller.
 */
public class BMReinterviewValidationException extends RuntimeException {
    public BMReinterviewValidationException(String message) {
        super(message);
    }
}
