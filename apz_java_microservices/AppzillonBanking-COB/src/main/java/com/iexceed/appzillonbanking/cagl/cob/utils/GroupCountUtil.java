package com.iexceed.appzillonbanking.cagl.cob.utils;

/**
 * Shared helpers for the group-level member-count columns on {@code TbObGroup}
 * (totalMemberCount, activatedCount, inactiveCount, inprogressCount, releasedCount).
 * These are plain integers persisted as strings, mutated in place whenever a
 * member moves between groups/stages - previously duplicated in
 * {@code KendraSelectionHandler} and {@code OnboardingService}.
 */
public final class GroupCountUtil {

    private GroupCountUtil() {
    }

    /** Parses a group count column, treating null/blank/unparseable values as zero. */
    public static int parseIntOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * Applies {@code delta} to a group count column, clamping the result at zero
     * so counts never go negative, and returns it re-stringified for the entity.
     */
    public static String adjust(String currentValue, int delta) {
        int updated = parseIntOrZero(currentValue) + delta;
        return String.valueOf(Math.max(updated, 0));
    }
}
