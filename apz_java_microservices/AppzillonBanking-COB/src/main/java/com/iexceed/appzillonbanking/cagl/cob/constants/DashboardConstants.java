package com.iexceed.appzillonbanking.cagl.cob.constants;

import java.util.List;
import java.util.Set;

public final class DashboardConstants {

    private DashboardConstants() {
        // Private constructor to prevent instantiation
    }

    public static final String TILE_LEADS = "LEADS";
    public static final String TILE_NEW_KENDRAS= "NEW_KENDRAS";
    public static final String TILE_NEW_KENDRAS_GROUPS = "NEW_KENDRAS_GROUPS";
    public static final String TILE_NEW_GROUPS = "NEW_GROUPS";
    public static final String TILE_DRAFTS_MBDF = "DRAFTS_MBDF";
    public static final String TILE_PENDING_WITH_RPC = "PENDING_WITH_RPC";
    public static final String TILE_INPUT_LOAN_DETAILS = "INPUT_LOAN_DETAILS";
    public static final String TILE_ONHOLD = "ONHOLD";
    public static final String TILE_CGT = "CGT";
    public static final String TILE_PENDING_FOR_GRT = "PENDING_FOR_GRT";
    public static final String TILE_ACTIVATED_REJECTED = "ACTIVATED_REJECTED";
    public static final String TILE_PENDING_FOR_ACTIVATION = "PENDING_FOR_ACTIVATION";
    public static final String TILE_TRANSFER_MBDF = "TRANSFER_MBDF";
    public static final String TILE_ASSIGN_ALLOCATE_KENDRAS = "ASSIGN_ALLOCATE_KENDRAS";
    public static final String TILE_EXCEPTIONAL_APPROVAL = "EXCEPTIONAL_APPROVAL";
    public static final String TILE_PENDING_T24_KENDRAS = "PENDING_FOR_T24_ACTIVATION_KENDRAS";
    public static final String TILE_PENDING_T24_GROUPS = "PENDING_FOR_T24_ACTIVATION_GROUPS";
    public static final String TILE_PENDING_T24_MEMBER = "PENDING_FOR_T24_ACTIVATION_MEMBER";
    public static final String TILE_MAKERS_POOL = "MAKERS_POOL";
    public static final String TILE_CHECKERS_POOL = "CHECKERS_POOL";
    public static final String TILE_CLEARED_BY_USER = "CLEARED_BY_USER";
    public static final String TILE_CASE_AGING = "CASE_AGING";
    public static final String TILE_GREEN_CHANNEL = "GREEN_CHANNEL";
    public static final String TILE_CLEARED_CASES = "CLEARED_CASES";
    public static final String TILE_AML_HO_POOL = "AML_HO_POOL";
    public static final String TILE_CRT = "CRT";

    // Count-only display tile names for the /dashboard/count response (not switch keys).
    public static final String TILE_ACTIVATED = "ACTIVATED";
    public static final String TILE_REJECTED = "REJECTED";
    public static final String TILE_REACTIVATION = "PENDING_FOR_ACTIVATION";

    // Record Type values used in the database
    public static final String RECORD_TYPE_REACTIVATION = "REACTIVATION";

    // Channel Type values used in the database
    public static final String CHANNEL_TYPE_GREEN = "GREEN";


    // Sent as requestObj.flow by CRT to request tile/ageing counts without a list payload.
    public static final String FLOW_DEFAULT = "DEFAULT";

    public static final List<String> CRT_PENDING_STAGES = List.of("CRTQUEUE");
    public static final List<String> CRT_APPROVED_STAGES = List.of("CGT", "BMREPLACE", "RPCQUEUE");
    public static final List<String> CRT_REJECTED_STAGES = List.of("CRTREJECTED");

    // "CRT User Pool" == Pending + Approved + Rejected combined; subCategory=ALL (or unset) spans all three.
    public static final List<String> CRT_ALL_STAGES = java.util.stream.Stream.of(
                    CRT_PENDING_STAGES, CRT_APPROVED_STAGES, CRT_REJECTED_STAGES)
            .flatMap(List::stream)
            .toList();

    // Buckets 0-5 are exact day ages; anything older collapses into bucket 6.
    public static final int CASE_AGEING_ABOVE_BUCKET = 6;

    public static final List<String> REJECT_STAGES = List.of(
            "OTPREJECT", "REJECT", "AMLREJECTED", "DRAFTREJECTED", "QUEUEREJECTED", "BREREJECTED",
            "CRTREJECTED", "RPCREJECTED", "CGTREJECTED", "BMREJECTED", "GRTREJECTED",
            "REPLACEMENTREJECTED", "CUSTREJECTED", "LOANREJECT"
    );


    public static final List<String> RPC_CLEARED_STAGES = List.of(
            "CGT", "BMQUEUE", "GRT", "BMREPLACE", "GRTAPPROVED", "ACTIVATED"
    );

     public static final Set<String> RPC_ROLES = Set.of("RPC", "RPCTL", "RPCIN", "RPCHO", "RPCCHT", "RPCBST", "RPCAMLHO");

    private static String normalizeRole(String userRole) {
        return userRole == null ? null : userRole.trim().toUpperCase().replaceAll("\\s+", "");
    }

    public static boolean isRpcRole(String userRole) {
        String normalized = normalizeRole(userRole);
        return normalized != null && RPC_ROLES.contains(normalized);
    }

   public static boolean isRpcTlRole(String userRole) {
        String normalized = normalizeRole(userRole);
        return "RPCTL".equals(normalized) || "RPCIN".equals(normalized) || "RPCHO".equals(normalized);
    }

    public static boolean isRpcAmlHoRole(String userRole) {
        return "RPCAMLHO".equals(normalizeRole(userRole));
    }

}
