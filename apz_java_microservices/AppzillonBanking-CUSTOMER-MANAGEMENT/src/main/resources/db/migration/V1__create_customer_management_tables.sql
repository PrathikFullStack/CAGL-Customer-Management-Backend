-- ================================================================================
-- V1__create_customer_management_tables.sql
-- Database: PostgreSQL (Primary DataSource)
-- System: CAGL Customer Management Architecture
-- Purpose: Complete DDL for all 11 tb_cm_* relational tables
-- ================================================================================

-- 1. Master configuration for all workflow states and transitions
CREATE TABLE IF NOT EXISTS tb_ob_workflow_master (
    app_id VARCHAR(20) NOT NULL,
    workflow_id VARCHAR(30) NOT NULL,
    workflow_desc VARCHAR(200),
    workflow_status VARCHAR(20) DEFAULT 'ACTIVE',
    PRIMARY KEY (app_id, workflow_id)
);

-- 2. Defines workflow stage transitions, actions, roles and routing rules
CREATE TABLE IF NOT EXISTS tb_ob_workflow_definition (
    app_id VARCHAR(50) NOT NULL,
    workflow_id VARCHAR(50) NOT NULL,
    stage_seq_no INTEGER NOT NULL,
    from_stage_id VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    next_stage_id VARCHAR(50),
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rule_id VARCHAR(50),
    curr_role VARCHAR(50),
    next_role VARCHAR(50),
    next_workflow_status VARCHAR(50),
    present_role VARCHAR(255),
    remarks TEXT,
    PRIMARY KEY (app_id, workflow_id, from_stage_id, action)
);

-- 3. Core Customer Master record with KYC, photo, and compliance statuses
CREATE TABLE IF NOT EXISTS tb_cm_customer (
    customer_id VARCHAR(20) PRIMARY KEY,
    application_id VARCHAR(30) NOT NULL,
    customer_name VARCHAR(100),
    dob VARCHAR(10),
    marital_status VARCHAR(15),
    live_photo_status VARCHAR(15) DEFAULT 'PENDING',
    kyc_status VARCHAR(15) DEFAULT 'PENDING',
    primary_kyc_type VARCHAR(20),
    primary_kyc_id VARCHAR(50),
    aml_status VARCHAR(10) DEFAULT 'PENDING',
    bre_status VARCHAR(15) DEFAULT 'PENDING',
    cgt_status VARCHAR(20),
    grt_status VARCHAR(20),
    kyc_details JSONB,
    bank_details JSONB,
    created_by VARCHAR(20) NOT NULL,
    created_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(20),
    updated_ts TIMESTAMP
);

-- 4. Customer Address details (Permanent and Communication)
CREATE TABLE IF NOT EXISTS tb_cm_address (
    address_id VARCHAR(20) PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL REFERENCES tb_cm_customer(customer_id),
    application_id VARCHAR(30) NOT NULL,
    address_type CHAR(1) NOT NULL, -- P=Permanent, C=Communication
    comm_same_as_perm CHAR(1) DEFAULT 'N',
    addr_payload JSONB NOT NULL,
    address_proof_type VARCHAR(30),
    sub_type VARCHAR(30),
    address_proof_doc_id VARCHAR(20),
    distance_from_branch VARCHAR(10),
    created_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_ts TIMESTAMP,
    updated_by VARCHAR(20)
);

-- 5. Family members, co-applicants, nominees, and earning members
CREATE TABLE IF NOT EXISTS tb_cm_family_member (
    family_mem_id VARCHAR(20) PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL REFERENCES tb_cm_customer(customer_id),
    application_id VARCHAR(30) NOT NULL,
    member_type VARCHAR(3) NOT NULL, -- SP=Spouse, CO=Co-applicant, EM=Earning Member
    relation VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    dob DATE,
    gender VARCHAR(10),
    mobile_num VARCHAR(15),
    kyc_type VARCHAR(20),
    kyc_doc_id VARCHAR(50),
    is_nominee BOOLEAN DEFAULT FALSE,
    is_earning_member BOOLEAN DEFAULT FALSE,
    nominee_bank_details JSONB,
    created_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Main Application record tracking loans, branch, and center assignment
CREATE TABLE IF NOT EXISTS tb_cm_application_master (
    application_id VARCHAR(30) PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL REFERENCES tb_cm_customer(customer_id),
    version VARCHAR(3) NOT NULL DEFAULT '1',
    customer_name VARCHAR(100),
    mobile_number VARCHAR(12),
    branch_id VARCHAR(20) NOT NULL,
    branch_name VARCHAR(50) NOT NULL,
    kendra_id VARCHAR(20),
    kendra_name VARCHAR(100),
    group_id VARCHAR(20),
    km_name VARCHAR(100) NOT NULL,
    leader VARCHAR(20),
    stage VARCHAR(30),
    sub_stage VARCHAR(30),
    wfstage VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    record_type VARCHAR(15) NOT NULL DEFAULT 'NEW',
    channel_type VARCHAR(10),
    loan_eligible VARCHAR(50) NOT NULL DEFAULT 'N',
    loan_id VARCHAR(30),
    created_by VARCHAR(20) NOT NULL,
    created_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(20),
    updated_ts TIMESTAMP
);

-- 7. Customer Documents, DMS image IDs, OCR results, and clarity scores
CREATE TABLE IF NOT EXISTS tb_cm_document (
    docu_id VARCHAR(20) PRIMARY KEY,
    application_id VARCHAR(30) NOT NULL REFERENCES tb_cm_application_master(application_id),
    customer_id VARCHAR(20) NOT NULL REFERENCES tb_cm_customer(customer_id),
    category VARCHAR(20) NOT NULL,
    sub_cat VARCHAR(20) NOT NULL,
    kyc_type VARCHAR(10) NOT NULL,
    legal_doc_name VARCHAR(30) NOT NULL,
    legal_doc_id VARCHAR(50),
    dms_doc_id_front VARCHAR(50),
    dms_doc_id_back VARCHAR(50),
    status VARCHAR(10) DEFAULT 'PENDING',
    clarity_score NUMERIC(5,2),
    clarity_pass CHAR(1),
    dedupe_status VARCHAR(10),
    validation_status VARCHAR(10),
    doc_version INTEGER DEFAULT 1,
    uploaded_by VARCHAR(20) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_ts TIMESTAMP
);

-- 8. Concurrency / Pessimistic record locking table
CREATE TABLE IF NOT EXISTS tb_cm_record_lock (
    lock_id VARCHAR(20) PRIMARY KEY,
    application_id VARCHAR(30) NOT NULL REFERENCES tb_cm_application_master(application_id),
    locked_by VARCHAR(20) NOT NULL,
    locked_by_role VARCHAR(20) NOT NULL,
    lock_type VARCHAR(15) NOT NULL DEFAULT 'EDIT',
    locked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lock_expiry_ts TIMESTAMP,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    released_at TIMESTAMP
);

-- 9. Customer modification audit trail table
CREATE TABLE IF NOT EXISTS tb_cm_cust_audit_trail (
    id VARCHAR(20) PRIMARY KEY,
    application_id VARCHAR(30) NOT NULL REFERENCES tb_cm_application_master(application_id),
    customer_id VARCHAR(20) REFERENCES tb_cm_customer(customer_id),
    user_id VARCHAR(20),
    user_name VARCHAR(100),
    user_role VARCHAR(50),
    stage_id VARCHAR(20),
    sub_stage VARCHAR(20),
    wfstatus VARCHAR(20),
    isedited VARCHAR(10),
    editeddetails JSONB,
    payload JSONB,
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. Configurable application data table
CREATE TABLE IF NOT EXISTS tb_cm_configurable_data (
    app_id VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    json_payload JSONB NOT NULL,
    PRIMARY KEY (app_id, type)
);

-- 11. Workflow movement execution history
CREATE TABLE IF NOT EXISTS tb_cm_appln_workflow (
    app_id VARCHAR(255) NOT NULL,
    application_id VARCHAR(255) NOT NULL REFERENCES tb_cm_application_master(application_id),
    version_no INTEGER NOT NULL,
    workflow_seq_no INTEGER NOT NULL,
    application_status VARCHAR(255),
    created_ts TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    present_role VARCHAR(255),
    next_workflow_stage VARCHAR(255),
    remarks VARCHAR(255),
    created_username VARCHAR(500),
    PRIMARY KEY (app_id, application_id, version_no, workflow_seq_no)
);

-- Indexes for optimal search and dedupe queries
CREATE INDEX IF NOT EXISTS idx_cm_cust_kyc ON tb_cm_customer(primary_kyc_type, primary_kyc_id);
CREATE INDEX IF NOT EXISTS idx_cm_app_cust ON tb_cm_application_master(customer_id);
CREATE INDEX IF NOT EXISTS idx_cm_app_kendra ON tb_cm_application_master(kendra_id);
CREATE INDEX IF NOT EXISTS idx_cm_app_branch ON tb_cm_application_master(branch_id);
CREATE INDEX IF NOT EXISTS idx_cm_addr_cust ON tb_cm_address(customer_id, address_type);
CREATE INDEX IF NOT EXISTS idx_cm_lock_app ON tb_cm_record_lock(application_id, status);
CREATE INDEX IF NOT EXISTS idx_cm_audit_app ON tb_cm_cust_audit_trail(application_id);
