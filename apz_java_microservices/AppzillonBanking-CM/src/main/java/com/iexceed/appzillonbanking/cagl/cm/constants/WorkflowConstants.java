package com.iexceed.appzillonbanking.cagl.cm.constants;

/**
 * Constants representing workflow stages, actions, queues, and statuses.
 */
public final class WorkflowConstants {

    private WorkflowConstants() {
        // Prevent instantiation
    }

    // Workflow Actions
    public static final String ACTION_SUBMIT = "SUBMIT";
    public static final String ACTION_APPROVE = "APPROVE";
    public static final String ACTION_PUSHBACK = "PUSHBACK";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_RESPOND = "RESPOND";
    public static final String ACTION_VERIFY = "VERIFY";
    public static final String ACTION_RETRIGGER = "RETRIGGER";

    // Workflow Stages
    public static final String STAGE_DRAFT = "DRAFT";
    public static final String STAGE_BM_REVIEW = "BM_REVIEW";
    public static final String STAGE_AM_REVIEW = "AM_REVIEW";
    public static final String STAGE_RPC_MAKER = "RPC_MAKER";
    public static final String STAGE_RPC_CHECKER = "RPC_CHECKER";
    public static final String STAGE_AML_CHECK = "AML_CHECK";
    public static final String STAGE_CRT_CHECK = "CRT_CHECK";
    public static final String STAGE_APPROVED = "APPROVED";
    public static final String STAGE_REJECTED = "REJECTED";

    // Queue Names
    public static final String QUEUE_BM = "BMQUEUE";
    public static final String QUEUE_AM = "AMQUEUE";
    public static final String QUEUE_RPC_MAKER = "RPCMAKERQUEUE";
    public static final String QUEUE_RPC_CHECKER = "RPCCHECKERQUEUE";
    public static final String QUEUE_AML = "AMLQUEUE";
    public static final String QUEUE_CRT = "CRTQUEUE";
}
