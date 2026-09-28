package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustMisReport;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.CustMisReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Offline/MIS report capture (see FSD note: exact parameters to be confirmed by CAGL IT team).
 * Plain service, called directly by other services in-process - no REST controller needed.
 *
 * One row per (applicationId, stageId, subStage): first submission inserts it, every
 * resubmission updates it in place and bumps {@code modifiedCount} by 1 - an overall
 * "how many times has this stage/sub-stage been edited" counter, not a per-field tally.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MisReportService {

    private final CustMisReportRepository custMisReportRepository;
    private final ObjectMapper objectMapper;

    public void recordModifiedDetails(TbObApplicationMaster master,
                                      TbObCustomer customer,
                                      String userId,
                                      String userRole,
                                      String appVersion,
                                      String stageId,
                                      String subStage,
                                      String wfStatus,
                                      String channelType,
                                      String remarks,
                                      Map<String, Object> modifiedDetails) {

        if (modifiedDetails == null || modifiedDetails.isEmpty()) {
            return;
        }

        try {
            String applicationId = master.getApplicationId();
            TbObCustMisReport.MisReportId id =
                    new TbObCustMisReport.MisReportId(applicationId, stageId, subStage);

            String editedDetailsJson = objectMapper.writeValueAsString(flattenModifiedDetails(modifiedDetails));
            LocalDateTime now = LocalDateTime.now();
            String nowText = now.toString();

            TbObCustMisReport report = custMisReportRepository.findById(id).orElse(null);

            if (report == null) {
                report = TbObCustMisReport.builder()
                        .id(id)
                        .mobileNo(customer.getKycDetails() != null
                                ? String.valueOf(customer.getKycDetails().get("mobileNum")) : null)
                        .customerName(customer.getCustomerName())
                        .appVersion(appVersion)
                        .wfStatus(wfStatus)
                        .editedDetails(editedDetailsJson)
                        .isEdited("Y")
                        .createDate(nowText)
                        .updateDate(nowText)
                        .userRole(userRole)
                        .createdBy(userId)
                        .modifyBy(userId)
                        .kendraId(master.getKendraId())
                        .groupId(master.getGroupId())
                        .branchId(master.getBranchId())
                        .remarks(remarks)
                        .reportSnapshotTs(now)
                        .recordType(master.getRecordType())
                        .channelType(channelType)
                        .modifiedCount(1)
                        .build();
            } else {
                report.setEditedDetails(editedDetailsJson);
                report.setIsEdited("Y");
                report.setUpdateDate(nowText);
                report.setModifyBy(userId);
                report.setWfStatus(wfStatus);
                report.setChannelType(channelType);
                report.setRemarks(remarks);
                report.setReportSnapshotTs(now);
                report.setModifiedCount((report.getModifiedCount() == null ? 0 : report.getModifiedCount()) + 1);
            }

            custMisReportRepository.save(report);
            log.info("MIS report recorded :: ApplicationId={}, stage={}, subStage={}, modifiedCount={}",
                    applicationId, stageId, subStage, report.getModifiedCount());

        } catch (Exception e) {
            log.error("Error while recording MIS report", e);
        }
    }

    private Map<String, Object> flattenModifiedDetails(Map<String, Object> modifiedDetails) {
        Map<String, Object> flat = new LinkedHashMap<>();
        flatten("", modifiedDetails, flat);
        return flat;
    }

    @SuppressWarnings("unchecked")
    private void flatten(String pathPrefix, Object node, Map<String, Object> out) {
        if (node instanceof Map) {
            ((Map<String, Object>) node).forEach((key, value) -> {
                String path = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
                flatten(path, value, out);
            });
        } else if (node instanceof List) {
            List<Object> list = (List<Object>) node;
            for (int i = 0; i < list.size(); i++) {
                flatten(pathPrefix + "[" + i + "]", list.get(i), out);
            }
        } else if (node != null) {
            out.put(pathPrefix, node);
        }
    }
}
