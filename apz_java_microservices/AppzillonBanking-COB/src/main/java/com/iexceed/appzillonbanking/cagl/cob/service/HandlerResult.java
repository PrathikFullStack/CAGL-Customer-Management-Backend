package com.iexceed.appzillonbanking.cagl.cob.service;
import java.util.List;

/**
 * Outcome of a single {@code UpdateHandler} invocation, reported back to
 * the orchestrator so it can decide on channel reclassification, sub-stage
 * advancement and audit logging - without each handler needing to know
 * about those cross-cutting concerns.
 */
public record HandlerResult(
        boolean changed,
        List<String> changedFields,
        String nextSubStage,
        String nextStatus
) {
    public static HandlerResult unchanged() {
        return new HandlerResult(false, List.of(), null, null);
    }

    public static HandlerResult of(List<String> changedFields, String nextSubStage) {
        return new HandlerResult(!changedFields.isEmpty(), changedFields, nextSubStage, null);
    }

    public static HandlerResult of(List<String> changedFields, String nextSubStage, String nextStatus) {
        return new HandlerResult(!changedFields.isEmpty(), changedFields, nextSubStage, nextStatus);
    }
}