package com.iexceed.appzillonbanking.cagl.cob.service;

import com.google.gson.Gson;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObDmsFailedUpload;
import com.iexceed.appzillonbanking.cagl.cob.payload.DmsApiRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.DmsDocumenRequestFields;
import com.iexceed.appzillonbanking.cagl.cob.payload.DmsDocumentRequest;
import com.iexceed.appzillonbanking.cagl.cob.payload.DmsRequestObj;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplicationMasterRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObDmsFailedUploadRepo;
import com.iexceed.appzillonbanking.core.payload.Header;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DMSUploadService {

    @Autowired
    private DMSService dmsService;

    @Autowired
    private TbObDmsFailedUploadRepo dmsFailedUploadRepo;

    @Autowired
    private TbObApplicationMasterRepository tbObApplicationMasterRepo;

    private final Gson gson = new Gson();

    /**
     * Context required to reconstruct a failed DMS upload.
     */
    private static class FailedUploadContext {
        DmsDocumentRequest apiRequest;
        Header header;
        DmsDocumenRequestFields requestObj;
    }

    // =========================================================
    // MAIN SCHEDULER FLOW
    // =========================================================

    public void retryFailedUploads() {
        try {
            log.info("Starting DMS upload scheduler");

            /*
             * Step 1:
             * Process application master records.
             * - Generate folder index if missing.
             * - Ignore applications having dms_upload_flag = Y.
             * - Identify applications having dms_upload_flag = N.
             */
            processMasterTableRecords();

            /*
             * Step 2:
             * Retry already failed DMS uploads (sequentially, one at a time,
             * so this scheduler run fully completes before the next trigger).
             */
            processFailedUploadRecords();

            log.info("Completed DMS upload scheduler");

        } catch (Exception e) {
            log.error("Exception in DMS upload scheduler", e);
        }
    }

    // =========================================================
    // MASTER TABLE PROCESSING
    // =========================================================

    private void processMasterTableRecords() {

        List<TbObApplicationMaster> applications = tbObApplicationMasterRepo.findAll();

        if (applications == null || applications.isEmpty()) {
            log.info("No application master records found");
            return;
        }

        log.info("Processing {} application master records for DMS", applications.size());

        for (TbObApplicationMaster application : applications) {
            try {
                processApplication(application);
            } catch (Exception e) {
                log.error("Error processing DMS applicationId={}", application.getApplicationId(), e);
            }
        }
    }

    private void processApplication(TbObApplicationMaster application) {

        String applicationId = application.getApplicationId();

        if (applicationId == null || applicationId.isBlank()) {
            log.warn("Skipping application because applicationId is missing");
            return;
        }

        /*
         * Requirement:
         * dms_folder_idx = null --> generate folder index --> update master
         *
         * Note: if folder-index generation itself fails, dmsFolderIdx stays
         * null and dms_upload_flag stays N (its existing/default value) --
         * the next scheduler run will simply retry folder generation again.
         * No row is created in tb_ob_dms_failed_upload for this case since
         * there is no specific document/file involved yet.
         */
        if (application.getDmsFolderIdx() == null || application.getDmsFolderIdx().isBlank()) {
            ensureFolderIndex(application);
        }

        /*
         * Requirement: dms_upload_flag = Y --> ignore
         */
        if ("Y".equalsIgnoreCase(application.getDmsUploadFlag())) {
            log.debug("DMS upload already completed. applicationId={}", applicationId);
            return;
        }

        /*
         * Requirement: dms_upload_flag = N --> identify for upload/retry
         *
         * Do not create a duplicate failed row here. Actual failed documents
         * are captured by captureFailedUploads() at real-time upload-failure
         * time. Existing failed rows are handled by processFailedUploadRecords().
         */
        if ("N".equalsIgnoreCase(application.getDmsUploadFlag())) {
            log.info("Pending DMS upload identified. applicationId={}", applicationId);
            return;
        }

        log.debug("DMS upload flag is not Y/N. applicationId={}, flag={}", applicationId, application.getDmsUploadFlag());
    }

    // =========================================================
    // FOLDER INDEX
    // =========================================================

    private void ensureFolderIndex(TbObApplicationMaster application) {

        try {
            String applicationId = application.getApplicationId();

            if (application.getCustomerId() == null || application.getCustomerId().isBlank()) {
                log.error("customerId missing, cannot generate DMS folder index. applicationId={}", applicationId);
                return;
            }

            log.info("DMS folder index missing. Generating folder index for applicationId={}", applicationId);

            // TODO: confirm appId/interfaceName/userId values below are correct
            // for a system/batch-triggered DMS call (vs. real user-request header).
            DmsDocumentRequest request = DmsDocumentRequest.builder()
                    .apiRequest(
                            DmsApiRequest.builder()
                                    .appId("COB")
                                    .interfaceName("DMSServices")
                                    .userId("SYSTEM")
                                    .build()
                    )
                    .build();

            Header header = new Header();

            String userDBId = dmsService.getDMSSessionId(
                            new java.util.Date(),
                            header,
                            dmsService.buildDmsRequest(request)
                    )
                    .map(dmsService::extractResponseObj)
                    .subscribeOn(Schedulers.boundedElastic())
                    .block();

            if (userDBId == null || userDBId.isBlank()) {
                log.error("Unable to obtain DMS session for applicationId={}", applicationId);
                return;
            }

            String folderIndex = dmsService.generateFolderIndex(
                            header,
                            DMSService.TYPE_CUSTOMER,
                            application.getCustomerId(),
                            userDBId,
                            DMSService.getDmsProperties(),
                            request
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .block();

            if (folderIndex == null || folderIndex.isBlank()) {
                log.error("Unable to generate DMS folder index for applicationId={}", applicationId);
                return;
            }

            dmsService.updateDmsFolderIndex(DMSService.TYPE_CUSTOMER, applicationId, folderIndex);

            log.info("DMS folder index generated successfully. applicationId={}, folderIndex={}", applicationId, folderIndex);

        } catch (Exception e) {
            log.error("Error generating DMS folder index for applicationId={}", application.getApplicationId(), e);
        }
    }

    // =========================================================
    // CAPTURE FAILED UPLOAD (called from real-time upload flow, not scheduler)
    // =========================================================

    public void captureFailedUploads(
            List<DmsDocumenRequestFields> docList,
            List<ResponseWrapper> responses,
            DmsDocumentRequest apiRequest,
            Header header) {

        try {
            if (docList == null || responses == null) {
                log.warn("Document list or response list is null");
                return;
            }

            if (apiRequest == null
                    || apiRequest.getApiRequest() == null
                    || apiRequest.getApiRequest().getRequestObj() == null) {
                log.error("Invalid DMS API request while capturing failed uploads");
                return;
            }

            String applicationId =
                    apiRequest.getApiRequest().getRequestObj().getApplicationId();

            if (applicationId == null || applicationId.isBlank()) {
                log.error("ApplicationId missing while capturing failed uploads");
                return;
            }

            if (responses.size() != docList.size()) {
                log.error(
                        "DMS upload response count mismatch. applicationId={}, documents={}, responses={}",
                        applicationId,
                        docList.size(),
                        responses.size());
            }

            int count = Math.min(responses.size(), docList.size());
            boolean failurePersistenceFailed = false;

            for (int i = 0; i < count; i++) {

                ResponseWrapper wrapper = responses.get(i);

                if (isFailure(wrapper)) {

                    boolean saved = persistFailedUpload(
                            docList.get(i),
                            header,
                            applicationId,
                            apiRequest,
                            wrapper);

                    if (!saved) {
                        failurePersistenceFailed = true;
                    }
                }
            }

            /*
             * Do not change dms_upload_flag when any failed document
             * could not be persisted into the failed-upload table.
             */
            if (!failurePersistenceFailed) {
                refreshDmsUploadFlag(applicationId);
            } else {
                log.error(
                        "One or more DMS upload failures could not be persisted. "
                                + "dms_upload_flag will not be changed. applicationId={}",
                        applicationId);
            }

        } catch (Exception e) {
            log.error(
                    "Exception while capturing failed DMS uploads. applicationId={}",
                    apiRequest != null
                            && apiRequest.getApiRequest() != null
                            && apiRequest.getApiRequest().getRequestObj() != null
                            ? apiRequest.getApiRequest().getRequestObj().getApplicationId()
                            : null,
                    e);
        }
    }

    private boolean isFailure(ResponseWrapper wrapper) {
        if (wrapper == null
                || wrapper.getApiResponse() == null
                || wrapper.getApiResponse().getResponseHeader() == null) {
            return true;
        }
        String responseCode = wrapper.getApiResponse().getResponseHeader().getResponseCode();
        return !"0".equals(responseCode);
    }

    // =========================================================
    // SAVE FAILED UPLOAD
    // =========================================================

    private boolean persistFailedUpload(
            DmsDocumenRequestFields requestObj,
            Header header,
            String applicationId,
            DmsDocumentRequest apiRequest,
            ResponseWrapper failedResponse) {

        try {
            if (requestObj == null) {
                log.error("DMS failed upload request object is null. applicationId={}",
                        applicationId);
                return false;
            }

            if (requestObj.getDocumentName() == null
                    || requestObj.getDocumentName().isBlank()) {
                log.error("DMS failed upload document name is missing. applicationId={}",
                        applicationId);
                return false;
            }

            if (requestObj.getFileData() == null
                    || requestObj.getFileData().isBlank()) {
                log.error("DMS failed upload file data is missing. applicationId={}, documentId={}",
                        applicationId, requestObj.getDocumentName());
                return false;
            }

            FailedUploadContext context = new FailedUploadContext();
            context.apiRequest = apiRequest;
            context.header = header;
            context.requestObj = requestObj;

            String payloadJson = gson.toJson(context);
            String responseJson = gson.toJson(failedResponse);

            TbObDmsFailedUpload entity =
                    dmsFailedUploadRepo.findByApplicationIdAndDocumentId(
                                    applicationId,
                                    requestObj.getDocumentName())
                            .orElse(new TbObDmsFailedUpload());

            entity.setApplicationId(applicationId);
            entity.setDocumentId(requestObj.getDocumentName());
            entity.setBase64(
                    Base64.getDecoder().decode(requestObj.getFileData()));
            entity.setPayload(payloadJson);
            entity.setResponse(responseJson);
            entity.setUpdatedTs(LocalDateTime.now());

            if (entity.getSeqId() == null) {
                entity.setCreatedTs(LocalDateTime.now());
                entity.setRetryCount(0);
            }

            dmsFailedUploadRepo.save(entity);

            return true;

        } catch (Exception e) {
            log.error(
                    "Failed to persist DMS upload failure. applicationId={}, documentId={}",
                    applicationId,
                    requestObj != null ? requestObj.getDocumentName() : null,
                    e);

            return false;
        }
    }

    // =========================================================
    // DMS UPLOAD FLAG
    // =========================================================

    private void refreshDmsUploadFlag(String applicationId) {
        try {
            Optional<TbObApplicationMaster> masterOpt =
                    tbObApplicationMasterRepo.findByApplicationId(applicationId);

            if (masterOpt.isEmpty()) {
                log.warn(
                        "Application master not found while refreshing DMS upload flag. applicationId={}",
                        applicationId);
                return;
            }

            TbObApplicationMaster master = masterOpt.get();

            if (master.getDmsFolderIdx() == null
                    || master.getDmsFolderIdx().isBlank()) {

                log.warn(
                        "DMS folder index is not available. Keeping dms_upload_flag as N. applicationId={}",
                        applicationId);

                master.setDmsUploadFlag("N");
                tbObApplicationMasterRepo.save(master);
                return;
            }

            boolean anyPending =
                    !dmsFailedUploadRepo.findByApplicationId(applicationId).isEmpty();

            master.setDmsUploadFlag(anyPending ? "N" : "Y");

            tbObApplicationMasterRepo.save(master);

        } catch (Exception e) {
            log.error(
                    "Failed to refresh DMS upload flag. applicationId={}",
                    applicationId,
                    e);
        }
    }
    // =========================================================
    // FAILED RECORD RETRY
    // =========================================================

    private void processFailedUploadRecords() {

        List<TbObDmsFailedUpload> failedRows = dmsFailedUploadRepo.findAll();

        if (failedRows == null || failedRows.isEmpty()) {
            log.info("No pending failed DMS uploads to retry");
            return;
        }

        log.info("Retrying {} failed DMS uploads", failedRows.size());

        // Sequential processing (no fire-and-forget): each row's retry fully
        // completes (success or failure persisted) before the next row starts,
        // and before this scheduler run is considered complete.
        for (TbObDmsFailedUpload row : failedRows) {
            try {
                retrySingleFailedRow(row);
            } catch (Exception e) {
                log.error("Error retrying failed DMS upload. seqId={}", row.getSeqId(), e);
                handleRetryFailure(row, e.getMessage());
            }
        }
    }

    private void retrySingleFailedRow(TbObDmsFailedUpload row) {

        if (row == null) {
            return;
        }

        log.info("Retrying failed DMS upload. seqId={}, applicationId={}, documentId={}, retryCount={}",
                row.getSeqId(), row.getApplicationId(), row.getDocumentId(), row.getRetryCount());

        if (row.getPayload() == null || row.getPayload().isBlank()) {
            handleRetryFailure(row, "Payload is missing in failed upload table");
            return;
        }

        FailedUploadContext context = gson.fromJson(row.getPayload(), FailedUploadContext.class);

        if (context == null || context.apiRequest == null || context.header == null || context.requestObj == null) {
            handleRetryFailure(row, "Invalid/corrupt payload in failed upload table");
            return;
        }

        /*
         * base_64 column is the source of truth for retry.
         * DB byte[] -> Base64 String
         */
        if (row.getBase64() == null || row.getBase64().length == 0) {
            handleRetryFailure(row, "File data is missing in base_64 column");
            return;
        }

        String fileDataBase64 = Base64.getEncoder().encodeToString(row.getBase64());
        context.requestObj.setFileData(fileDataBase64);

        DmsRequestObj dmsRequestObj = context.apiRequest.getApiRequest().getRequestObj();

        /*
         * Reuse existing DMSService upload implementation.
         * Blocking call (not fire-and-forget) so this row's outcome is known
         * and persisted before the loop moves to the next row.
         */
        ResponseWrapper response;
        try {
            response = dmsService.uploadDocumentForRetry(
                    context.requestObj,
                    context.header,
                    context.apiRequest,
                    dmsRequestObj
            )
                    .subscribeOn(Schedulers.boundedElastic())
                    .block();
        } catch (Exception e) {
            log.error("Exception while retrying failed DMS upload. applicationId={}, documentId={}",
                    row.getApplicationId(), row.getDocumentId(), e);
            handleRetryFailure(row, e.getMessage());
            return;
        }

        handleRetryResult(row, response);
    }

    // =========================================================
    // RETRY RESULT
    // =========================================================

    private void handleRetryResult(TbObDmsFailedUpload row, ResponseWrapper response) {

        try {
            if (!isFailure(response)) {
                /*
                 * Upload successful.
                 * 1. Delete failed row.
                 * 2. Refresh application flag (-> Y if no more pending rows).
                 */
                dmsFailedUploadRepo.delete(row);
                refreshDmsUploadFlag(row.getApplicationId());

                log.info("DMS retry successful. applicationId={}, documentId={}", row.getApplicationId(), row.getDocumentId());
                return;
            }

            handleRetryFailure(row, gson.toJson(response));

        } catch (Exception e) {
            log.error("Exception while handling DMS retry result. seqId={}", row.getSeqId(), e);
            handleRetryFailure(row, e.getMessage());
        }
    }

    // =========================================================
    // RETRY FAILURE
    // =========================================================

    private void handleRetryFailure(TbObDmsFailedUpload row, String errorMessage) {

        if (row == null) {
            return;
        }

        int retryCount = row.getRetryCount() == null ? 0 : row.getRetryCount();

        // Retry-till-success: no cap on retryCount, no dead-letter/skip logic.
        row.setRetryCount(retryCount + 1);
        row.setResponse(errorMessage);
        row.setUpdatedTs(LocalDateTime.now());

        dmsFailedUploadRepo.save(row);

        log.error("DMS retry failed. applicationId={}, documentId={}, retryCount={}",
                row.getApplicationId(), row.getDocumentId(), row.getRetryCount());
    }
}