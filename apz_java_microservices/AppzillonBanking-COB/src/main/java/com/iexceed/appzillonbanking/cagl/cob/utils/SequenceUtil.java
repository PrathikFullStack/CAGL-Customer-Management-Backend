package com.iexceed.appzillonbanking.cagl.cob.utils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Thin, shared wrapper around Postgres {@code nextval()} so any service needing a sequence-backed
 * id (e.g. {@code cgt_id} on {@code tb_ob_cgt_details}) can draw one without holding its own
 * {@link JdbcTemplate} field or repeating the query string.
 *
 * <p>{@code sequenceName} is only ever expected to be a hardcoded constant from calling code
 * (never raw user input) since it's concatenated into the SQL rather than bound as a parameter --
 * JDBC has no placeholder syntax for identifiers. {@link #requireSafeIdentifier} is a defense-in-depth
 * guard against that being misused, not a substitute for keeping it that way.
 */
@Component
public class SequenceUtil {

    private final JdbcTemplate jdbcTemplate;

    public SequenceUtil(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Draws the next value of {@code sequenceName}, as a String -- for String-typed id columns. */
    public String nextValueAsString(String sequenceName) {
        return String.valueOf(nextValue(sequenceName));
    }

    /** Draws the next value of {@code sequenceName}, as a long. */
    public long nextValue(String sequenceName) {
        requireSafeIdentifier(sequenceName);
        Long value = jdbcTemplate.queryForObject(
                "SELECT nextval('" + sequenceName + "')", Long.class);
        return value != null ? value : 0L;
    }

    private void requireSafeIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Unsafe/invalid sequence name: " + identifier);
        }
    }
}