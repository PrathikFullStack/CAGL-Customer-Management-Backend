package com.iexceed.appzillonbanking.cagl.cob.utils;

import com.iexceed.appzillonbanking.cagl.cob.payload.SequenceSyncProperties;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SequenceInitializer {

    private static final Logger log = LoggerFactory.getLogger(SequenceInitializer.class);

    // Matches the numeric tail of an ID like "AD604001" -> "604001"
    private static final String DEFAULT_NUMERIC_SUFFIX_PATTERN = "[0-9]+$";

    @PersistenceContext
    private EntityManager entityManager;

    private final SequenceSyncProperties properties;

    public SequenceInitializer(SequenceSyncProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initializeSequences() {
        if (properties.getItems() == null || properties.getItems().isEmpty()) {
            log.warn("No sequences configured under app.sequences.items");
            return;
        }
        properties.getItems().forEach(cfg ->
                syncSequence(cfg.getSequenceName(), cfg.getTable(), cfg.getColumn(), cfg.getFloor()));
    }

    @Transactional
    public void syncSequence(String sequenceName, String table, String column, long floor) {
        requireSafeIdentifier(sequenceName);
        requireSafeIdentifier(table);
        requireSafeIdentifier(column);

        acquireLock(sequenceName);
        try {
            // Extract the trailing numeric portion of alphanumeric IDs (e.g. AD604001 -> 604001)
            // before casting to BIGINT, and only consider rows that actually match that shape.
            Number newValue = (Number) entityManager.createNativeQuery("""
                SELECT setval(
                    :seq,
                    COALESCE(
                        (SELECT MAX((substring(%1$s FROM :numPattern))::BIGINT)
                         FROM %2$s
                         WHERE %1$s ~ :numPattern),
                        :floor
                    ),
                    true
                )
                """.formatted(column, table))
                    .setParameter("seq", sequenceName)
                    .setParameter("numPattern", DEFAULT_NUMERIC_SUFFIX_PATTERN)
                    .setParameter("floor", floor)
                    .getSingleResult();

            log.info("Synced sequence [{}] -> {}", sequenceName, newValue);
        } finally {
            releaseLock(sequenceName);
        }
    }

    private void acquireLock(String sequenceName) {
        entityManager.createNativeQuery("SELECT pg_advisory_lock(hashtext(:name))")
                .setParameter("name", sequenceName)
                .getSingleResult();
    }

    private void releaseLock(String sequenceName) {
        entityManager.createNativeQuery("SELECT pg_advisory_unlock(hashtext(:name))")
                .setParameter("name", sequenceName)
                .getSingleResult();
    }

    private void requireSafeIdentifier(String identifier) {
        if (!identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Unsafe SQL identifier in sequence config: " + identifier);
        }
    }
}