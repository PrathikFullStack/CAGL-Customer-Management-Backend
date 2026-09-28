package com.iexceed.appzillonbanking.cagl.cob.constants;

/**
 * Central place for the sub-stage codes that drive routing in
 * {@code ApplicationServiceImpl#updateApplication}. Kept as plain strings
 * (not an enum) because sub_stage is persisted as varchar(10) and new
 * stages get added by config/product teams without code changes to the enum.
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
    }

    // interfaceName values coming from the client
    public static final String INTERFACE_CREATE_CUSTOMER = "CreateCustomer";
    public static final String INTERFACE_UPDATE_CUSTOMER = "UpdateCustomer";

    // requestType values
    public static final String REQUEST_TYPE_CREATE = "create";
    public static final String REQUEST_TYPE_ADD = "add";

    // next_workflow_stage
    public static final String OTPCONSENT = "OTPCONSENT";

    // sub_stage codes (stage "1" = member onboarding journey)
    public static final String SUB_STAGE_MEMBER_KYC = "1.2";
    public static final String SUB_STAGE_ADDRESS = "1.3";
    public static final String SUB_STAGE_FAMILY = "1.4";
    public static final String SUB_STAGE_INCOME = "1.5";
    public static final String SUB_STAGE_KENDRA_SELECTION = "1.6";
    public static final String SUB_STAGE_BANK = "1.7";
    public static final String SUB_STAGE_ADDITIONAL_DOCS = "1.8";
    public static final String SUB_STAGE_ADDITIONAL_DOCS_1 = "1.9";

    public static final String RPC_SUB_STAGE_MEMBER_KYC = "2.2";
    public static final String RPC_SUB_STAGE_ADDRESS = "2.3";
    public static final String RPC_SUB_STAGE_FAMILY = "2.4";
    public static final String RPC_SUB_STAGE_INCOME = "2.5";
    public static final String RPC_SUB_STAGE_KENDRA_SELECTION = "2.6";
    public static final String RPC_SUB_STAGE_BANK = "2.7";
    public static final String RPC_SUB_STAGE_ADDITIONAL_DOCS = "2.8";
    public static final String RPC_SUB_STAGE_ADDITIONAL_DOCS_1 = "2.9";

    // record_type / status defaults used at creation time
    public static final String RECORD_TYPE_NEW = "NEW";
    public static final String STATUS_INITIATE = "INITIATE";
    public static final String WFSTAGE_DRAFT = "DRAFT";
    public static final String STAGE_DRAFT = "DRAFT";
    public static final String DEFAULT_DRAFT = "DRAFT";
    public static final String INITIAL_SUB_STAGE = "1.1";

    // address_type codes used in tb_ob_address
    public static final String ADDRESS_TYPE_PERMANENT = "P";
    public static final String ADDRESS_TYPE_COMMUNICATION = "C";

    // placeholders for NOT NULL columns that are not yet known at creation time
    public static final String PLACEHOLDER_NOT_CAPTURED = "PENDING";

    public static final String APPLICATION_ID_PREFIX = "CO";
    public static final String DOCUMENT_CATEGORY_ADDRESS = "ADDRESS";

    //for MBDF transfer
    public static final String MAPPING_TYPE_TRANSFER = "TRANSFER";
    public static final int GROUP_MAX_MEMBERS = 10;
}
