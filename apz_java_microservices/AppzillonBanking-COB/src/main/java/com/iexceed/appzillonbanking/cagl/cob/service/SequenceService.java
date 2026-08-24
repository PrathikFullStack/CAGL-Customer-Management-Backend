package com.iexceed.appzillonbanking.cagl.cob.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

/**
 * Central point for pulling values from DB sequences. Every ID in this schema
 * is explicitly fetched here rather than via JPA @GeneratedValue, because
 * several of these identifiers are components of a composite @IdClass key —
 * portable @GeneratedValue support for a single field inside a composite id
 * is inconsistent across providers, so an explicit nextval() is used instead.
 */
@Service
public class SequenceService {

    private static final String SEQ_CUSTOMER_ID = "seq_ob_customer_id";
    private static final String SEQ_AUDIT_TRAIL_ID = "seq_ob_audit_trail_id";
    private static final String SEQ_APPLN_WORKFLOW_VERSION = "seq_ob_appln_workflow_version";

    @PersistenceContext
    private EntityManager entityManager;

    /** tb_ob_application_master.customer_id / tb_ob_customer.customer_id (shared value). */
    public long nextCustomerId() {
        return nextValue(SEQ_CUSTOMER_ID);
    }

    /** tb_ob_cust_audit_trail.id. */
    public long nextAuditTrailId() {
        return nextValue(SEQ_AUDIT_TRAIL_ID);
    }

    /** tb_ob__appln_workflow.version_id. */
    public long nextApplnWorkflowVersionId() {
        return nextValue(SEQ_APPLN_WORKFLOW_VERSION);
    }

    /** Generic escape hatch for any other sequence in the schema. */
    public long nextValue(String sequenceName) {
        Object result = entityManager
                .createNativeQuery("SELECT nextval('" + sequenceName + "')")
                .getSingleResult();
        return ((Number) result).longValue();
    }
}