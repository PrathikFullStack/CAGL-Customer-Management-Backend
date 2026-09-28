//package com.iexceed.appzillonbanking.cagl.cob.scheduler;
//
//import com.iexceed.appzillonbanking.cagl.cob.service.DMSUploadService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//public class DmsUploadRetryScheduler {
//
//    private static final Logger LOG = LoggerFactory.getLogger(DmsUploadRetryScheduler.class);
//
//    @Autowired
//    private DMSUploadService dmsUploadService;
//
//    @Value("${spring.dmsRetry.url}")
//    private String dmsRetryUrl;
//
//    @Value("${spring.dmsFailedRetry.url}")
//    private String dmsFailedRetryUrl;
//
//    @Value("${ab.common.dmsUploadRetrySchedulerCron}")
//    private String dmsUploadRetrySchedulerCron;
//
//    // Scheduler 1 — tb_ob_dms_failed_upload table retry
//    @Scheduled(cron = "${ab.common.dmsUploadRetrySchedulerCron}")
//    public void retryFailedDmsUploads() {
//        LOG.info("DMS upload retry scheduler (Scheduler 1) triggered");
//        dmsUploadService.retryFailedUploads();
//    }
//}