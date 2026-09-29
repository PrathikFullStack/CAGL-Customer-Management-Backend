-- ================================================================================
-- V2__seed_workflow_matrix.sql
-- Database: PostgreSQL (Primary DataSource)
-- Purpose: Master data & complete state transition matrix for Customer Management
-- App ID: APZCBO
-- ================================================================================

-- 1. Insert Workflow Masters
INSERT INTO tb_ob_workflow_master (app_id, workflow_id, workflow_desc, workflow_status) VALUES
('APZCBO', 'CMKMINPUT', 'KM Initiated Customer Management Workflow', 'ACTIVE'),
('APZCBO', 'CMDEOINPUT', 'DEO Initiated Customer Management Workflow', 'ACTIVE'),
('APZCBO', 'CMBMINPUT', 'BM Initiated Customer Management Workflow', 'ACTIVE'),
('APZCBO', 'CMAMINPUT', 'AM Initiated Customer Management Workflow', 'ACTIVE'),
('APZCBO', 'CMRPCMAKER', 'RPC Maker Review Workflow', 'ACTIVE'),
('APZCBO', 'CMRPCCHECKER', 'RPC Checker Review Workflow', 'ACTIVE'),
('APZCBO', 'CMSYSTEM', 'System Automated Processing Workflow (AML/BRE/T24)', 'ACTIVE'),
('APZCBO', 'CMAMLHO', 'AML Head Office Review Workflow', 'ACTIVE'),
('APZCBO', 'CMCRT', 'Credit Risk Team Review Workflow', 'ACTIVE'),
('APZCBO', 'CMINSURANCE', 'Insurance Team Review Workflow', 'ACTIVE'),
('APZCBO', 'CMCHT', 'Central Helpdesk Team / Retry Workflow', 'ACTIVE'),
('APZCBO', 'CMRPCTL', 'RPC Team Lead Post-Audit Workflow', 'ACTIVE'),
('APZCBO', 'CMRPCHO', 'RPC Head Office Post-Audit Workflow', 'ACTIVE'),
('APZCBO', 'CMBST', 'Business Support Team Verification Workflow', 'ACTIVE')
ON CONFLICT (app_id, workflow_id) DO UPDATE 
SET workflow_desc = EXCLUDED.workflow_desc, workflow_status = EXCLUDED.workflow_status;

-- 2. Insert Full Workflow Definition Transition Matrix
INSERT INTO tb_ob_workflow_definition (
    app_id, workflow_id, stage_seq_no, from_stage_id, action, next_stage_id,
    create_ts, rule_id, curr_role, next_role, next_workflow_status, present_role, remarks
) VALUES
-- ==========================================
-- KM INITIATED WORKFLOW (CMKMINPUT)
-- ==========================================
('APZCBO', 'CMKMINPUT', 1, 'INITIATE', 'EDIT', 'DRAFT', CURRENT_TIMESTAMP, NULL, 'KM', 'KM', 'DRAFT_CREATED', NULL, 'KM clicks Edit/Modify Member Details on the Member Profile screen and starts the edit flow. A draft request is created (only one active request per customer).'),
('APZCBO', 'CMKMINPUT', 1, 'DRAFT', 'SAVE', 'DRAFT', CURRENT_TIMESTAMP, NULL, 'KM', 'KM', 'DRAFT_SAVED', NULL, 'KM saves the request. It stays in the Drafts queue.'),
('APZCBO', 'CMKMINPUT', 2, 'DRAFT', 'SUBMIT', 'BMQUEUE', CURRENT_TIMESTAMP, NULL, 'KM', 'BM', 'BMQUEUE_SUBMITTED', NULL, 'KM edits one or more KYC fields and submits (non-STP). Request moves to Pending for BM Review.'),
('APZCBO', 'CMKMINPUT', 3, 'DRAFT', 'STPSUBMIT', 'STPSUBMITTED', CURRENT_TIMESTAMP, NULL, 'KM', 'SYSTEM', 'STP_SUBMITTED', NULL, 'KM submits without editing any KYC field (STP). The request bypasses BM/AM/RPC; request moves to AML (optional) and BRE.'),
('APZCBO', 'CMKMINPUT', 4, 'DRAFT', 'REJECT', 'DRAFTREJECTED', CURRENT_TIMESTAMP, NULL, 'KM', 'KM', 'DRAFT_REJECTED', NULL, 'Request rejected in Draft by KM (for example, the customer declines OTP consent or a dedupe match is found during capture).'),
('APZCBO', 'CMKMINPUT', 1, 'BMONHOLD', 'SUBMIT', 'BMQUEUE', CURRENT_TIMESTAMP, NULL, 'KM', 'BM', 'BM_QUEUE_RESUBMIT', NULL, 'KM resolves the queries raised by BM and resubmits. Request returns to Pending for BM Review.'),
('APZCBO', 'CMKMINPUT', 1, 'RPCONHOLD', 'SUBMIT', 'RPCMAKERQUEUE', CURRENT_TIMESTAMP, NULL, 'KM', 'RPC', 'RPC_REWORK', NULL, 'KM resolves the RPC queries and resubmits. Request moves to the RPC Maker Rework queue.'),

-- ==========================================
-- DEO INITIATED WORKFLOW (CMDEOINPUT)
-- ==========================================
('APZCBO', 'CMDEOINPUT', 1, 'INITIATE', 'EDIT', 'DRAFT', CURRENT_TIMESTAMP, NULL, 'DEO', 'DEO', 'DRAFT_CREATED', NULL, 'DEO clicks Edit/Modify Member Details on the Member Profile screen and starts the edit flow. A draft request is created (only one active request per customer).'),
('APZCBO', 'CMDEOINPUT', 1, 'DRAFT', 'SAVE', 'DRAFT', CURRENT_TIMESTAMP, NULL, 'DEO', 'DEO', 'DRAFT_SAVED', NULL, 'DEO saves the request. It stays in the Drafts queue.'),
('APZCBO', 'CMDEOINPUT', 2, 'DRAFT', 'SUBMIT', 'BMQUEUE', CURRENT_TIMESTAMP, NULL, 'DEO', 'BM', 'BMQUEUE_SUBMITTED', NULL, 'DEO edits one or more KYC fields and submits (non-STP). Request moves to Pending for BM Review.'),
('APZCBO', 'CMDEOINPUT', 3, 'DRAFT', 'STPSUBMIT', 'STPSUBMITTED', CURRENT_TIMESTAMP, NULL, 'DEO', 'SYSTEM', 'STP_SUBMITTED', NULL, 'DEO submits without editing any KYC field (STP). The request bypasses BM/AM/RPC; request moves to AML (optional) and BRE.'),
('APZCBO', 'CMDEOINPUT', 4, 'DRAFT', 'REJECT', 'DRAFTREJECTED', CURRENT_TIMESTAMP, NULL, 'DEO', 'DEO', 'DRAFT_REJECTED', NULL, 'Request rejected in Draft by DEO (for example, the customer declines OTP consent or a dedupe match is found during capture).'),
('APZCBO', 'CMDEOINPUT', 1, 'BMONHOLD', 'SUBMIT', 'BMQUEUE', CURRENT_TIMESTAMP, NULL, 'DEO', 'BM', 'BM_QUEUE_RESUBMIT', NULL, 'DEO resolves the queries raised by BM and resubmits. Request returns to Pending for BM Review.'),
('APZCBO', 'CMDEOINPUT', 1, 'RPCONHOLD', 'SUBMIT', 'RPCMAKERQUEUE', CURRENT_TIMESTAMP, NULL, 'DEO', 'RPC', 'RPC_REWORK', NULL, 'DEO resolves the RPC queries and resubmits. Request moves to the RPC Maker Rework queue.'),

-- ==========================================
-- BM INITIATED & REVIEW WORKFLOW (CMBMINPUT)
-- ==========================================
('APZCBO', 'CMBMINPUT', 1, 'INITIATE', 'EDIT', 'DRAFT', CURRENT_TIMESTAMP, NULL, 'BM', 'BM', 'DRAFT_CREATED', NULL, 'BM clicks Edit/Modify Member Details on the Member Profile screen and starts the edit flow. A draft request is created (only one active request per customer).'),
('APZCBO', 'CMBMINPUT', 1, 'DRAFT', 'SAVE', 'DRAFT', CURRENT_TIMESTAMP, NULL, 'BM', 'BM', 'DRAFT_SAVED', NULL, 'BM saves the request. It stays in the Drafts queue.'),
('APZCBO', 'CMBMINPUT', 2, 'DRAFT', 'SUBMIT', 'AMQUEUE', CURRENT_TIMESTAMP, NULL, 'BM', 'AM', 'AMQUEUE_SUBMITTED', NULL, 'BM-initiated request with KYC fields edited (non-STP). The AM is the reviewer; request moves to Pending for AM Review.'),
('APZCBO', 'CMBMINPUT', 3, 'DRAFT', 'STPSUBMIT', 'STPSUBMITTED', CURRENT_TIMESTAMP, NULL, 'BM', 'SYSTEM', 'STP_SUBMITTED', NULL, 'BM submits without editing any KYC field (STP). The request bypasses BM/AM/RPC; request moves to AML (optional) and BRE.'),
('APZCBO', 'CMBMINPUT', 4, 'DRAFT', 'REJECT', 'DRAFTREJECTED', CURRENT_TIMESTAMP, NULL, 'BM', 'BM', 'DRAFT_REJECTED', NULL, 'Request rejected in Draft by BM (for example, the customer declines OTP consent or a dedupe match is found during capture).'),
('APZCBO', 'CMBMINPUT', 1, 'BMQUEUE', 'APPROVED', 'RPCMAKERQUEUE', CURRENT_TIMESTAMP, NULL, 'BM', 'RPC', 'BM_APPROVED', NULL, 'BM approves the KM/DEO request. Request moves to the RPC Maker''s Pool (New).'),
('APZCBO', 'CMBMINPUT', 2, 'BMQUEUE', 'PUSHBACK', 'BMONHOLD', CURRENT_TIMESTAMP, NULL, 'BM', 'KM', 'BM_PUSHBACK', NULL, 'BM pushes back with section-wise query reasons. Request moves to Onhold (From BM) for KM/DEO action.'),
('APZCBO', 'CMBMINPUT', 3, 'BMQUEUE', 'REJECT', 'BMREJECTED', CURRENT_TIMESTAMP, NULL, 'BM', 'BM', 'BM_REJECTED', NULL, 'BM rejects with a reason and remarks. Updation stops and the member keeps the old details. Request moves to the Rejected queue.'),
('APZCBO', 'CMBMINPUT', 1, 'AMONHOLD', 'SUBMIT', 'AMQUEUE', CURRENT_TIMESTAMP, NULL, 'BM', 'AM', 'AM_QUEUE_RESUBMIT', NULL, 'BM resolves the queries raised by AM and resubmits. Request returns to Pending for AM Review.'),
('APZCBO', 'CMBMINPUT', 1, 'RPCONHOLD', 'SUBMIT', 'RPCMAKERQUEUE', CURRENT_TIMESTAMP, NULL, 'BM', 'RPC', 'RPC_REWORK', NULL, 'BM resolves the RPC queries and resubmits. Request moves to the RPC Maker Rework queue.'),

-- ==========================================
-- AM INITIATED & REVIEW WORKFLOW (CMAMINPUT)
-- ==========================================
('APZCBO', 'CMAMINPUT', 1, 'INITIATE', 'EDIT', 'DRAFT', CURRENT_TIMESTAMP, NULL, 'AM', 'AM', 'DRAFT_CREATED', NULL, 'AM clicks Edit/Modify Member Details on the Member Profile screen and starts the edit flow. A draft request is created (only one active request per customer).'),
('APZCBO', 'CMAMINPUT', 1, 'DRAFT', 'SAVE', 'DRAFT', CURRENT_TIMESTAMP, NULL, 'AM', 'AM', 'DRAFT_SAVED', NULL, 'AM saves the request. It stays in the Drafts queue.'),
('APZCBO', 'CMAMINPUT', 2, 'DRAFT', 'SUBMIT', 'RPCMAKERQUEUE', CURRENT_TIMESTAMP, NULL, 'AM', 'RPC', 'RPC_QUEUE', NULL, 'AM-initiated request with KYC fields edited (non-STP). No BM review; request goes directly to the RPC Maker''s Pool.'),
('APZCBO', 'CMAMINPUT', 3, 'DRAFT', 'STPSUBMIT', 'STPSUBMITTED', CURRENT_TIMESTAMP, NULL, 'AM', 'SYSTEM', 'STP_SUBMITTED', NULL, 'AM submits without editing any KYC field (STP). The request bypasses BM/AM/RPC; request moves to AML (optional) and BRE.'),
('APZCBO', 'CMAMINPUT', 4, 'DRAFT', 'REJECT', 'DRAFTREJECTED', CURRENT_TIMESTAMP, NULL, 'AM', 'AM', 'DRAFT_REJECTED', NULL, 'Request rejected in Draft by AM (for example, the customer declines OTP consent or a dedupe match is found during capture).'),
('APZCBO', 'CMAMINPUT', 1, 'AMQUEUE', 'APPROVED', 'RPCMAKERQUEUE', CURRENT_TIMESTAMP, NULL, 'AM', 'RPC', 'AM_APPROVED', NULL, 'AM approves the BM-initiated request. Request moves to the RPC Maker''s Pool (New).'),
('APZCBO', 'CMAMINPUT', 2, 'AMQUEUE', 'PUSHBACK', 'AMONHOLD', CURRENT_TIMESTAMP, NULL, 'AM', 'BM', 'AM_PUSHBACK', NULL, 'AM pushes back with section-wise query reasons. Request moves to Onhold (From AM) for BM action.'),
('APZCBO', 'CMAMINPUT', 3, 'AMQUEUE', 'REJECT', 'AMREJECTED', CURRENT_TIMESTAMP, NULL, 'AM', 'AM', 'AM_REJECTED', NULL, 'AM rejects with a reason and remarks. Request moves to the Rejected queue.'),
('APZCBO', 'CMAMINPUT', 1, 'RPCONHOLD', 'SUBMIT', 'RPCMAKERQUEUE', CURRENT_TIMESTAMP, NULL, 'AM', 'RPC', 'RPC_REWORK', NULL, 'AM (AM-initiated request) resolves the RPC queries and resubmits. Request moves to the RPC Maker Rework queue.'),

-- ==========================================
-- RPC MAKER WORKFLOW (CMRPCMAKER)
-- ==========================================
('APZCBO', 'CMRPCMAKER', 1, 'RPCMAKERQUEUE', 'APPROVED', 'RPCAPPROVED', CURRENT_TIMESTAMP, NULL, 'RPCMAKER', 'SYSTEM', 'RPC_MAKER_APPROVED', NULL, 'RPC Maker submits without editing (single-level verification). Request moves to RPC Approved (AML optional, then BRE).'),
('APZCBO', 'CMRPCMAKER', 2, 'RPCMAKERQUEUE', 'CHECKER', 'RPCCHECKERQUEUE', CURRENT_TIMESTAMP, NULL, 'RPCMAKER', 'RPCCHECKER', 'RPC_CHECKER_QUEUE', NULL, 'RPC Maker edited one or more fields and submits. Request moves to the Checker''s Pool (a different user must review).'),
('APZCBO', 'CMRPCMAKER', 3, 'RPCMAKERQUEUE', 'ONHOLD', 'RPCONHOLD', CURRENT_TIMESTAMP, NULL, 'RPCMAKER', 'KM', 'RPC_ONHOLD', NULL, 'RPC Maker raises section-wise queries and puts the request On Hold (From RPC). RPC cannot reject; On Hold is the only negative action.'),
('APZCBO', 'CMRPCMAKER', 1, 'RPCONHOLD', 'RESPOND', 'RPCCHECKERQUEUE', CURRENT_TIMESTAMP, NULL, 'RPCMAKER', 'RPCCHECKER', 'RPC_ONHOLD_CLEARED', NULL, 'RPC Maker clears the on-hold queries and responds. Request moves to the Checker''s Pool.'),

-- ==========================================
-- RPC CHECKER WORKFLOW (CMRPCCHECKER)
-- ==========================================
('APZCBO', 'CMRPCCHECKER', 1, 'RPCCHECKERQUEUE', 'APPROVED', 'RPCAPPROVED', CURRENT_TIMESTAMP, NULL, 'RPCCHECKER', 'SYSTEM', 'RPC_CHECKER_APPROVED', NULL, 'RPC Checker approves (the Checker''s edits are final). Request moves to RPC Approved (AML optional, then BRE).'),
('APZCBO', 'CMRPCCHECKER', 2, 'RPCCHECKERQUEUE', 'ONHOLD', 'RPCONHOLD', CURRENT_TIMESTAMP, NULL, 'RPCCHECKER', 'KM', 'RPC_ONHOLD', NULL, 'RPC Checker raises queries and puts the request On Hold (From RPC). RPC cannot reject.'),
('APZCBO', 'CMRPCCHECKER', 1, 'RPCONHOLD', 'RESPOND', 'RPCCHECKERQUEUE', CURRENT_TIMESTAMP, NULL, 'RPCCHECKER', 'RPCCHECKER', 'RPC_ONHOLD_CLEARED', NULL, 'RPC Checker clears the on-hold queries and responds. Request returns to the Checker''s Pool for review by a different user.'),

-- ==========================================
-- SYSTEM AUTOMATED STAGES (CMSYSTEM)
-- ==========================================
('APZCBO', 'CMSYSTEM', 1, 'RPCAPPROVED', 'AML', 'AMLQUEUE', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'AMLHO', 'AML_QUEUE', NULL, 'Post RPC approval: AML check is required (AML-trigger fields changed). Request moves to the AML queue for AML HO.'),
('APZCBO', 'CMSYSTEM', 2, 'RPCAPPROVED', 'BRE', 'BREQUEUE', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM', 'BRE_QUEUE', NULL, 'Post RPC approval: AML is not required (AML is optional). Request moves directly to the BRE queue.'),
('APZCBO', 'CMSYSTEM', 1, 'STPSUBMITTED', 'AML', 'AMLQUEUE', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'AMLHO', 'AML_QUEUE', NULL, 'STP submit: AML check is required (AML-trigger fields changed). Request moves to the AML queue for AML HO.'),
('APZCBO', 'CMSYSTEM', 2, 'STPSUBMITTED', 'BRE', 'BREQUEUE', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM', 'BRE_QUEUE', NULL, 'STP submit: AML is not required (AML is optional). Request moves directly to the BRE queue.'),
('APZCBO', 'CMSYSTEM', 1, 'BREQUEUE', 'PASS', 'T24UPDATE', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM', 'BRE_SUCCESS', NULL, 'BRE/CB API success. Request moves to T24 updation.'),
('APZCBO', 'CMSYSTEM', 2, 'BREQUEUE', 'INSURANCE', 'INSQUEUE', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'INSURANCE', 'INSURANCE_QUEUE', NULL, 'BRE success and the marital status changed from Widowed to Married. Request moves to Insurance Team Review (configurable).'),
('APZCBO', 'CMSYSTEM', 3, 'BREQUEUE', 'FAIL', 'CRTQUEUE', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'CRT', 'CB_FAIL_PENDING', NULL, 'BRE final decision is FAILURE (CB fail). Request moves to the CRT queue.'),
('APZCBO', 'CMSYSTEM', 4, 'BREQUEUE', 'QUEUE', 'BREQUEUE', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM', 'BRE_RETRY', NULL, 'BRE API down or no response (status unknown). Request stays in the BRE queue for scheduler retry.'),
('APZCBO', 'CMSYSTEM', 1, 'T24UPDATE', 'NEXT', 'COMPLETED', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM', 'T24_UPDATED', NULL, 'Data updated in T24/CDH/MAHI/Unnati. Request completed and an SMS is sent to the customer.'),
('APZCBO', 'CMSYSTEM', 2, 'T24UPDATE', 'QUEUE', 'T24PENDING', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'CHT', 'T24_PENDING', NULL, 'No or failed T24 response. Request moves to the T24 Pending queue.'),
('APZCBO', 'CMSYSTEM', 1, 'T24PENDING', 'NEXT', 'COMPLETED', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM', 'T24_UPDATED', NULL, 'Scheduler retry of T24 succeeds. Request completed.'),
('APZCBO', 'CMSYSTEM', 2, 'T24PENDING', 'QUEUE', 'T24PENDING', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM', 'T24_RETRY', NULL, 'Scheduler retry of T24 fails. Request stays in the T24 Pending queue.'),

-- ==========================================
-- AML HEAD OFFICE WORKFLOW (CMAMLHO)
-- ==========================================
('APZCBO', 'CMAMLHO', 1, 'AMLQUEUE', 'APPROVED', 'BREQUEUE', CURRENT_TIMESTAMP, NULL, 'AMLHO', 'SYSTEM', 'AML_SUCCESS', NULL, 'AML HO clears the case (AML pass). Request moves to the BRE queue.'),
('APZCBO', 'CMAMLHO', 2, 'AMLQUEUE', 'REJECT', 'AMLREJECT', CURRENT_TIMESTAMP, NULL, 'AMLHO', 'AMLHO', 'AML_REJECTED', NULL, 'AML HO rejects the case. Member is tagged as an AML hit and the request moves to AML Reject.'),

-- ==========================================
-- CREDIT RISK TEAM WORKFLOW (CMCRT)
-- ==========================================
('APZCBO', 'CMCRT', 1, 'CRTQUEUE', 'CRTAPPROVE', 'T24UPDATE', CURRENT_TIMESTAMP, NULL, 'CRT', 'SYSTEM', 'CRT_APPROVED', NULL, 'CRT approves the CB-fail case. Request moves to T24 updation.'),
('APZCBO', 'CMCRT', 2, 'CRTQUEUE', 'REJECT', 'CRTREJECTED', CURRENT_TIMESTAMP, NULL, 'CRT', 'CRT', 'CRT_REJECTED', NULL, 'CRT rejects the request. Request moves to the Rejected queue.'),

-- ==========================================
-- INSURANCE TEAM WORKFLOW (CMINSURANCE)
-- ==========================================
('APZCBO', 'CMINSURANCE', 1, 'INSQUEUE', 'APPROVED', 'T24UPDATE', CURRENT_TIMESTAMP, NULL, 'INSURANCE', 'SYSTEM', 'INSURANCE_APPROVED', NULL, 'Insurance team approves the Widowed -> Married change. Request moves to T24 updation.'),
('APZCBO', 'CMINSURANCE', 2, 'INSQUEUE', 'REJECT', 'INSREJECTED', CURRENT_TIMESTAMP, NULL, 'INSURANCE', 'INSURANCE', 'INSURANCE_REJECTED', NULL, 'Insurance team rejects. Request moves to the Rejected queue.'),

-- ==========================================
-- CENTRAL HELPDESK TEAM (CMCHT)
-- ==========================================
('APZCBO', 'CMCHT', 1, 'T24PENDING', 'RETRIGGER', 'T24UPDATE', CURRENT_TIMESTAMP, NULL, 'CHT', 'SYSTEM', 'T24_RETRIGGERED', NULL, 'CHT user manually re-triggers T24 updation (single, multiple selection or bulk upload).'),

-- ==========================================
-- RPC TEAM LEAD SAMPLING (CMRPCTL)
-- ==========================================
('APZCBO', 'CMRPCTL', 1, 'COMPLETED', 'VERIFY', 'TLVERIFIED', CURRENT_TIMESTAMP, NULL, 'RPCTL', 'RPCTL', 'TL_VERIFIED', NULL, 'RPC TL sample check (5-10%) of completed cases. Marked as Verified with remarks.'),
('APZCBO', 'CMRPCTL', 2, 'COMPLETED', 'RECOMMEND', 'BSTQUEUE', CURRENT_TIMESTAMP, NULL, 'RPCTL', 'BST', 'BST_RECOMMENDED', NULL, 'RPC TL recommends the case for BST review.'),

-- ==========================================
-- RPC HEAD OFFICE SAMPLING (CMRPCHO)
-- ==========================================
('APZCBO', 'CMRPCHO', 1, 'COMPLETED', 'VERIFY', 'RPCHOVERIFIED', CURRENT_TIMESTAMP, NULL, 'RPCHO', 'RPCHO', 'RPCHO_VERIFIED', NULL, 'RPC HO sample check (5-10%, configurable) of RPC-cleared and Green channel cases. Marked as Verified with remarks.'),
('APZCBO', 'CMRPCHO', 2, 'COMPLETED', 'RECOMMEND', 'BSTQUEUE', CURRENT_TIMESTAMP, NULL, 'RPCHO', 'BST', 'BST_RECOMMENDED', NULL, 'RPC HO recommends the case for BST review with remarks. The case appears in the BST report.'),

-- ==========================================
-- BUSINESS SUPPORT TEAM AUDIT (CMBST)
-- ==========================================
('APZCBO', 'CMBST', 1, 'BSTQUEUE', 'VERIFY', 'BSTVERIFIED', CURRENT_TIMESTAMP, NULL, 'BST', 'BST', 'BST_VERIFIED', NULL, 'BST user verifies the sample case and adds comments.')

ON CONFLICT (app_id, workflow_id, from_stage_id, action) DO UPDATE
SET stage_seq_no = EXCLUDED.stage_seq_no,
    next_stage_id = EXCLUDED.next_stage_id,
    create_ts = EXCLUDED.create_ts,
    rule_id = EXCLUDED.rule_id,
    curr_role = EXCLUDED.curr_role,
    next_role = EXCLUDED.next_role,
    next_workflow_status = EXCLUDED.next_workflow_status,
    present_role = EXCLUDED.present_role,
    remarks = EXCLUDED.remarks;
