package com.iexceed.appzillonbanking.cagl.cm.service;

public interface AuditTrailService {

    void logAudit(String applicationId, String customerId, String userId, String userName,
                  String userRole, String stageId, String subStage, String wfStatus,
                  Object editedDetails, Object fullPayload);
}
