package com.iexceed.appzillonbanking.cagl.cm.service;

public interface AuditTrailService {

    /**
     * Records a field-level change audit log in tb_cm_cust_audit_trail
     *
     * @param applicationId Application identifier
     * @param customerId    Customer identifier
     * @param userId        Actor user ID
     * @param userName      Actor name
     * @param userRole      Actor role
     * @param stageId       Stage identifier
     * @param subStage      Sub-stage identifier
     * @param wfStatus      Workflow status
     * @param editedDetails Delta fields modified (JSON object/map)
     * @param fullPayload   Complete request payload
     */
    void logAudit(String applicationId, String customerId, String userId, String userName,
                  String userRole, String stageId, String subStage, String wfStatus,
                  Object editedDetails, Object fullPayload);
}
