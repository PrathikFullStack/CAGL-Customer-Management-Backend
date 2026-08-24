package com.iexceed.appzillonbanking.cagl.cob.constants;

import java.util.Set;

public final class DashboardConstants {

    private DashboardConstants() {
        // Private constructor to prevent instantiation
    }

    // Tile Types for API requests and responses
    public static final String TILE_DRAFT = "DRAFT";
    public static final String TILE_CB_QUEUE = "CB Queue";
    public static final String TILE_BRE_FAIL = "BRE Fail";
    public static final String TILE_PENDING_WITH_RPC = "Pending with RPC";
    public static final String TILE_GREEN_SUBMITTED = "Green Submitted";
    public static final String TILE_RPC_QUEUE = "RPC Queue";
    public static final String TILE_CGT_IN_PROGRESS = "CGT";
    public static final String TILE_BM_QUEUE = "BM Queue";
    public static final String TILE_GRT_QUEUE = "GRT Queue";
    public static final String TILE_REINTERVIEW = "Reinterview";
    public static final String TILE_GRT_APPROVED = "GRT Approved";
    public static final String TILE_ACTIVATED = "Activated";
    public static final String TILE_CRT_QUEUE = "CRT Queue";
    public static final String TILE_REJECTED = "Rejected";
    public static final String TILE_REPLACEMENT = "Replacement";
    public static final String TILE_REACTIVATION = "Reactivation";
    public static final String TILE_ACTIVATED_REJECTED_TODAY = "ACTIVATED/REJECTED";
    public static final String TILE_INPUT_LOAN_DETAILS = "Input Loan Details";
    public static final String TILE_ONHOLD = "Onhold";
    public static final String TILE_NEW_KENDRA_AND_GROUP = "New Kendra and Group";
    public static final String TILE_PENDING_FOR_ACTIVATION = "PENDING_FOR_ACTIVATION";
    public static final String TILE_PENDING_FOR_GRT = "Pending for GRT";
    public static final String TILE_CRT_PENDING = "CBQUEUE";
    
    // Workflow Stages for RPC
    public static final String WF_STAGE_RPC_MAKER_PENDING = "RPC_QUEUE";
    public static final String WF_STAGE_RPC_CHECKER_PENDING = "RPCQUEUE"; // Assuming this is for Checker pending
    public static final String WF_STAGE_ONHOLD = "ONHOLD";


    // Stage values used in the database
    public static final String STAGE_CGT = "4";
    public static final String STAGE_BM = "5";
    public static final String STAGE_GRT = "6";

    // Record Type values used in the database
    public static final String RECORD_TYPE_REPLACEMENT = "REPLACEMENT";
    public static final String RECORD_TYPE_REACTIVATION = "REACTIVATION";

    // Channel Type values used in the database
    public static final String CHANNEL_TYPE_GREEN = "GREEN";

    // All userRole values that should be treated as an RPC user for dashboard routing.
    // Add new RPC sub-roles here only — no dispatch logic elsewhere needs to change.
    public static final Set<String> RPC_ROLES = Set.of("RPC", "RPC TL", "RPC IN", "RPC HO", "RPC CHT", "RPC BST");

}