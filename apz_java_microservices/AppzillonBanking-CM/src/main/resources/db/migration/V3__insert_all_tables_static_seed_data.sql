-- ==============================================================================
-- V3__insert_all_tables_static_seed_data.sql
-- Database: PostgreSQL (CAGL Customer Management - Primary & CDH)
-- Purpose: Complete static configuration, workflow rules, and seed data for ALL tables
-- Order of Execution: Respects all Primary/Foreign Key constraints
-- ==============================================================================

BEGIN;

-- ==============================================================================
-- 1. tb_ob_workflow_master (Workflow Master Definitions)
-- ==============================================================================
INSERT INTO public.tb_ob_workflow_master (app_id, workflow_id, workflow_desc, workflow_status) VALUES
('APZCBO', 'CMKMINPUT',      'Customer Management - Kendra Manager (KM) Edit Flow',                 'ACTIVE'),
('APZCBO', 'CMDEOINPUT',     'Customer Management - Data Entry Operator (DEO) Edit Flow',           'ACTIVE'),
('APZCBO', 'CMBMINPUT',      'Customer Management - Branch Manager (BM) Edit & Review Flow',        'ACTIVE'),
('APZCBO', 'CMAMINPUT',      'Customer Management - Area Manager (AM) Edit & Review Flow',          'ACTIVE'),
('APZCBO', 'CMRPCMAKER',     'Customer Management - Regional Processing Center Maker Flow',         'ACTIVE'),
('APZCBO', 'CMRPCCHECKER',   'Customer Management - Regional Processing Center Checker Flow',       'ACTIVE'),
('APZCBO', 'CMSYSTEM',       'Customer Management - System STP, BRE & Core Banking Integration',   'ACTIVE'),
('APZCBO', 'CMAMLHO',        'Customer Management - Anti-Money Laundering Head Office Review',      'ACTIVE'),
('APZCBO', 'CMCRT',          'Customer Management - Credit Risk Team (CRT) Review Flow',            'ACTIVE'),
('APZCBO', 'CMINSURANCE',    'Customer Management - Insurance Team Review Flow',                    'ACTIVE'),
('APZCBO', 'CMCHT',          'Customer Management - Central Helpdesk Team (CHT) Retrigger Flow',    'ACTIVE'),
('APZCBO', 'CMRPCTL',        'Customer Management - RPC Team Leader Sample Verification',           'ACTIVE'),
('APZCBO', 'CMRPCHO',        'Customer Management - RPC Head Office Sample Verification',           'ACTIVE'),
('APZCBO', 'CMBST',          'Customer Management - Business Support Team (BST) Verification',      'ACTIVE')
ON CONFLICT (app_id, workflow_id) DO UPDATE 
SET workflow_desc = EXCLUDED.workflow_desc, workflow_status = EXCLUDED.workflow_status;

-- ==============================================================================
-- 2. tb_ob_workflow_definition (All 64 Workflow State Transitions)
-- ==============================================================================
INSERT INTO public.tb_ob_workflow_definition 
("action", app_id, from_stage_id, workflow_id, create_ts, curr_role, next_role, next_stage_id, next_workflow_status, present_role, remarks, rule_id, stage_seq_no) VALUES
-- KM Initiated Flow
('EDIT', 'APZCBO', 'INITIATE', 'CMKMINPUT', CURRENT_TIMESTAMP, 'KM', 'KM', 'DRAFT', 'DRAFT_CREATED', 'KM', 'KM clicks Edit/Modify Member Details on the Member Profile screen and starts the edit flow.', NULL, 1),
('SAVE', 'APZCBO', 'DRAFT', 'CMKMINPUT', CURRENT_TIMESTAMP, 'KM', 'KM', 'DRAFT', 'DRAFT_SAVED', 'KM', 'KM saves the request. It stays in the Drafts queue.', NULL, 1),
('SUBMIT', 'APZCBO', 'DRAFT', 'CMKMINPUT', CURRENT_TIMESTAMP, 'KM', 'BM', 'BMQUEUE', 'BMQUEUE_SUBMITTED', 'KM', 'KM edits one or more KYC fields and submits (non-STP). Request moves to Pending for BM Review.', NULL, 2),
('STPSUBMIT', 'APZCBO', 'DRAFT', 'CMKMINPUT', CURRENT_TIMESTAMP, 'KM', 'SYSTEM', 'STPSUBMITTED', 'STP_SUBMITTED', 'KM', 'KM submits without editing any KYC field (STP). Request moves to AML/BRE.', NULL, 3),
('REJECT', 'APZCBO', 'DRAFT', 'CMKMINPUT', CURRENT_TIMESTAMP, 'KM', 'KM', 'DRAFTREJECTED', 'DRAFT_REJECTED', 'KM', 'Request rejected in Draft by KM (OTP consent declined / dedupe match).', NULL, 4),
('SUBMIT', 'APZCBO', 'BMONHOLD', 'CMKMINPUT', CURRENT_TIMESTAMP, 'KM', 'BM', 'BMQUEUE', 'BM_QUEUE_RESUBMIT', 'KM', 'KM resolves queries raised by BM and resubmits.', NULL, 1),
('SUBMIT', 'APZCBO', 'RPCONHOLD', 'CMKMINPUT', CURRENT_TIMESTAMP, 'KM', 'RPC', 'RPCMAKERQUEUE', 'RPC_REWORK', 'KM', 'KM resolves RPC queries and resubmits to RPC Maker Rework queue.', NULL, 1),

-- DEO Initiated Flow
('EDIT', 'APZCBO', 'INITIATE', 'CMDEOINPUT', CURRENT_TIMESTAMP, 'DEO', 'DEO', 'DRAFT', 'DRAFT_CREATED', 'DEO', 'DEO starts the edit flow from Member Profile screen.', NULL, 1),
('SAVE', 'APZCBO', 'DRAFT', 'CMDEOINPUT', CURRENT_TIMESTAMP, 'DEO', 'DEO', 'DRAFT', 'DRAFT_SAVED', 'DEO', 'DEO saves request into Drafts.', NULL, 1),
('SUBMIT', 'APZCBO', 'DRAFT', 'CMDEOINPUT', CURRENT_TIMESTAMP, 'DEO', 'BM', 'BMQUEUE', 'BMQUEUE_SUBMITTED', 'DEO', 'DEO edits KYC fields and submits for BM Review.', NULL, 2),
('STPSUBMIT', 'APZCBO', 'DRAFT', 'CMDEOINPUT', CURRENT_TIMESTAMP, 'DEO', 'SYSTEM', 'STPSUBMITTED', 'STP_SUBMITTED', 'DEO', 'DEO submits without editing KYC fields (STP path).', NULL, 3),
('REJECT', 'APZCBO', 'DRAFT', 'CMDEOINPUT', CURRENT_TIMESTAMP, 'DEO', 'DEO', 'DRAFTREJECTED', 'DRAFT_REJECTED', 'DEO', 'Request rejected in Draft by DEO.', NULL, 4),
('SUBMIT', 'APZCBO', 'BMONHOLD', 'CMDEOINPUT', CURRENT_TIMESTAMP, 'DEO', 'BM', 'BMQUEUE', 'BM_QUEUE_RESUBMIT', 'DEO', 'DEO resolves BM queries and resubmits.', NULL, 1),
('SUBMIT', 'APZCBO', 'RPCONHOLD', 'CMDEOINPUT', CURRENT_TIMESTAMP, 'DEO', 'RPC', 'RPCMAKERQUEUE', 'RPC_REWORK', 'DEO', 'DEO resolves RPC queries and resubmits.', NULL, 1),

-- BM Initiated & Review Flow
('EDIT', 'APZCBO', 'INITIATE', 'CMBMINPUT', CURRENT_TIMESTAMP, 'BM', 'BM', 'DRAFT', 'DRAFT_CREATED', 'BM', 'BM starts edit flow.', NULL, 1),
('SAVE', 'APZCBO', 'DRAFT', 'CMBMINPUT', CURRENT_TIMESTAMP, 'BM', 'BM', 'DRAFT', 'DRAFT_SAVED', 'BM', 'BM saves draft.', NULL, 1),
('SUBMIT', 'APZCBO', 'DRAFT', 'CMBMINPUT', CURRENT_TIMESTAMP, 'BM', 'AM', 'AMQUEUE', 'AMQUEUE_SUBMITTED', 'BM', 'BM-initiated request with KYC edited moves to Area Manager review.', NULL, 2),
('STPSUBMIT', 'APZCBO', 'DRAFT', 'CMBMINPUT', CURRENT_TIMESTAMP, 'BM', 'SYSTEM', 'STPSUBMITTED', 'STP_SUBMITTED', 'BM', 'BM STP submit moves directly to AML/BRE.', NULL, 3),
('REJECT', 'APZCBO', 'DRAFT', 'CMBMINPUT', CURRENT_TIMESTAMP, 'BM', 'BM', 'DRAFTREJECTED', 'DRAFT_REJECTED', 'BM', 'BM cancels draft request.', NULL, 4),
('APPROVED', 'APZCBO', 'BMQUEUE', 'CMBMINPUT', CURRENT_TIMESTAMP, 'BM', 'RPC', 'RPCMAKERQUEUE', 'BM_APPROVED', 'BM', 'BM approves KM/DEO request. Moves to RPC Maker Pool.', NULL, 1),
('PUSHBACK', 'APZCBO', 'BMQUEUE', 'CMBMINPUT', CURRENT_TIMESTAMP, 'BM', 'KM', 'BMONHOLD', 'BM_PUSHBACK', 'BM', 'BM pushes back with queries to KM/DEO.', NULL, 2),
('REJECT', 'APZCBO', 'BMQUEUE', 'CMBMINPUT', CURRENT_TIMESTAMP, 'BM', 'BM', 'BMREJECTED', 'BM_REJECTED', 'BM', 'BM rejects request with reason and remarks.', NULL, 3),
('SUBMIT', 'APZCBO', 'AMONHOLD', 'CMBMINPUT', CURRENT_TIMESTAMP, 'BM', 'AM', 'AMQUEUE', 'AM_QUEUE_RESUBMIT', 'BM', 'BM resolves AM queries and resubmits to AM Review.', NULL, 1),
('SUBMIT', 'APZCBO', 'RPCONHOLD', 'CMBMINPUT', CURRENT_TIMESTAMP, 'BM', 'RPC', 'RPCMAKERQUEUE', 'RPC_REWORK', 'BM', 'BM resolves RPC queries and resubmits.', NULL, 1),

-- AM Initiated & Review Flow
('EDIT', 'APZCBO', 'INITIATE', 'CMAMINPUT', CURRENT_TIMESTAMP, 'AM', 'AM', 'DRAFT', 'DRAFT_CREATED', 'AM', 'AM starts edit flow.', NULL, 1),
('SAVE', 'APZCBO', 'DRAFT', 'CMAMINPUT', CURRENT_TIMESTAMP, 'AM', 'AM', 'DRAFT', 'DRAFT_SAVED', 'AM', 'AM saves draft.', NULL, 1),
('SUBMIT', 'APZCBO', 'DRAFT', 'CMAMINPUT', CURRENT_TIMESTAMP, 'AM', 'RPC', 'RPCMAKERQUEUE', 'RPC_QUEUE', 'AM', 'AM-initiated request with KYC edited goes directly to RPC Maker Pool.', NULL, 2),
('STPSUBMIT', 'APZCBO', 'DRAFT', 'CMAMINPUT', CURRENT_TIMESTAMP, 'AM', 'SYSTEM', 'STPSUBMITTED', 'STP_SUBMITTED', 'AM', 'AM STP submit moves to AML/BRE.', NULL, 3),
('REJECT', 'APZCBO', 'DRAFT', 'CMAMINPUT', CURRENT_TIMESTAMP, 'AM', 'AM', 'DRAFTREJECTED', 'DRAFT_REJECTED', 'AM', 'AM cancels draft.', NULL, 4),
('APPROVED', 'APZCBO', 'AMQUEUE', 'CMAMINPUT', CURRENT_TIMESTAMP, 'AM', 'RPC', 'RPCMAKERQUEUE', 'AM_APPROVED', 'AM', 'AM approves BM-initiated request. Moves to RPC Maker.', NULL, 1),
('PUSHBACK', 'APZCBO', 'AMQUEUE', 'CMAMINPUT', CURRENT_TIMESTAMP, 'AM', 'BM', 'AMONHOLD', 'AM_PUSHBACK', 'AM', 'AM pushes back queries to BM.', NULL, 2),
('REJECT', 'APZCBO', 'AMQUEUE', 'CMAMINPUT', CURRENT_TIMESTAMP, 'AM', 'AM', 'AMREJECTED', 'AM_REJECTED', 'AM', 'AM rejects request.', NULL, 3),
('SUBMIT', 'APZCBO', 'RPCONHOLD', 'CMAMINPUT', CURRENT_TIMESTAMP, 'AM', 'RPC', 'RPCMAKERQUEUE', 'RPC_REWORK', 'AM', 'AM resolves RPC queries and resubmits.', NULL, 1),

-- RPC Maker & Checker Flow
('APPROVED', 'APZCBO', 'RPCMAKERQUEUE', 'CMRPCMAKER', CURRENT_TIMESTAMP, 'RPCMAKER', 'SYSTEM', 'RPCAPPROVED', 'RPC_MAKER_APPROVED', 'RPCMAKER', 'RPC Maker approves without edits. Moves to RPC Approved.', NULL, 1),
('CHECKER', 'APZCBO', 'RPCMAKERQUEUE', 'CMRPCMAKER', CURRENT_TIMESTAMP, 'RPCMAKER', 'RPCCHECKER', 'RPCCHECKERQUEUE', 'RPC_CHECKER_QUEUE', 'RPCMAKER', 'RPC Maker edited fields. Moves to RPC Checker Pool.', NULL, 2),
('ONHOLD', 'APZCBO', 'RPCMAKERQUEUE', 'CMRPCMAKER', CURRENT_TIMESTAMP, 'RPCMAKER', 'KM', 'RPCONHOLD', 'RPC_ONHOLD', 'RPCMAKER', 'RPC Maker raises queries (On Hold).', NULL, 3),
('RESPOND', 'APZCBO', 'RPCONHOLD', 'CMRPCMAKER', CURRENT_TIMESTAMP, 'RPCMAKER', 'RPCCHECKER', 'RPCCHECKERQUEUE', 'RPC_ONHOLD_CLEARED', 'RPCMAKER', 'RPC Maker clears on-hold queries and responds to Checker.', NULL, 1),
('APPROVED', 'APZCBO', 'RPCCHECKERQUEUE', 'CMRPCCHECKER', CURRENT_TIMESTAMP, 'RPCCHECKER', 'SYSTEM', 'RPCAPPROVED', 'RPC_CHECKER_APPROVED', 'RPCCHECKER', 'RPC Checker approves. Moves to RPC Approved.', NULL, 1),
('ONHOLD', 'APZCBO', 'RPCCHECKERQUEUE', 'CMRPCCHECKER', CURRENT_TIMESTAMP, 'RPCCHECKER', 'KM', 'RPCONHOLD', 'RPC_ONHOLD', 'RPCCHECKER', 'RPC Checker raises queries (On Hold).', NULL, 2),
('RESPOND', 'APZCBO', 'RPCONHOLD', 'CMRPCCHECKER', CURRENT_TIMESTAMP, 'RPCCHECKER', 'RPCCHECKER', 'RPCCHECKERQUEUE', 'RPC_ONHOLD_CLEARED', 'RPCCHECKER', 'RPC Checker clears queries for another Checker review.', NULL, 1),

-- System Automation, AML, BRE, and T24 Core Banking Integration
('AML', 'APZCBO', 'RPCAPPROVED', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'AMLHO', 'AMLQUEUE', 'AML_QUEUE', 'SYSTEM', 'Post RPC: AML check triggered.', NULL, 1),
('BRE', 'APZCBO', 'RPCAPPROVED', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM', 'BREQUEUE', 'BRE_QUEUE', 'SYSTEM', 'Post RPC: AML not required, moves to BRE.', NULL, 2),
('AML', 'APZCBO', 'STPSUBMITTED', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'AMLHO', 'AMLQUEUE', 'AML_QUEUE', 'SYSTEM', 'STP submit: AML check triggered.', NULL, 1),
('BRE', 'APZCBO', 'STPSUBMITTED', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM', 'BREQUEUE', 'BRE_QUEUE', 'SYSTEM', 'STP submit: AML not required, moves to BRE.', NULL, 2),
('APPROVED', 'APZCBO', 'AMLQUEUE', 'CMAMLHO', CURRENT_TIMESTAMP, 'AMLHO', 'SYSTEM', 'BREQUEUE', 'AML_SUCCESS', 'AMLHO', 'AML HO clears case. Moves to BRE.', NULL, 1),
('REJECT', 'APZCBO', 'AMLQUEUE', 'CMAMLHO', CURRENT_TIMESTAMP, 'AMLHO', 'AMLHO', 'AMLREJECT', 'AML_REJECTED', 'AMLHO', 'AML HO rejects (tagged AML hit).', NULL, 2),
('PASS', 'APZCBO', 'BREQUEUE', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM', 'T24UPDATE', 'BRE_SUCCESS', 'SYSTEM', 'BRE/CB pass. Moves to T24 core banking update.', NULL, 1),
('INSURANCE', 'APZCBO', 'BREQUEUE', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'INSURANCE', 'INSQUEUE', 'INSURANCE_QUEUE', 'SYSTEM', 'Marital status Widowed -> Married, sent to Insurance review.', NULL, 2),
('FAIL', 'APZCBO', 'BREQUEUE', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'CRT', 'CRTQUEUE', 'CB_FAIL_PENDING', 'SYSTEM', 'BRE decision FAIL (CB fail). Sent to CRT queue.', NULL, 3),
('QUEUE', 'APZCBO', 'BREQUEUE', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM', 'BREQUEUE', 'BRE_RETRY', 'SYSTEM', 'BRE API timeout. Stays in BRE queue for scheduler retry.', NULL, 4),
('NEXT', 'APZCBO', 'T24UPDATE', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM', 'COMPLETED', 'T24_UPDATED', 'SYSTEM', 'T24/CDH updated. Request completed and SMS dispatched.', NULL, 1),
('QUEUE', 'APZCBO', 'T24UPDATE', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'CHT', 'T24PENDING', 'T24_PENDING', 'SYSTEM', 'T24 update failed. Moves to T24 Pending queue.', NULL, 2),
('NEXT', 'APZCBO', 'T24PENDING', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM', 'COMPLETED', 'T24_UPDATED', 'SYSTEM', 'Scheduler retry of T24 succeeds. Request completed.', NULL, 1),
('QUEUE', 'APZCBO', 'T24PENDING', 'CMSYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM', 'T24PENDING', 'T24_RETRY', 'SYSTEM', 'Scheduler retry of T24 fails. Stays in T24 Pending.', NULL, 2),

-- Exception Reviews & Post-Completion Sample Checks
('CRTAPPROVE', 'APZCBO', 'CRTQUEUE', 'CMCRT', CURRENT_TIMESTAMP, 'CRT', 'SYSTEM', 'T24UPDATE', 'CRT_APPROVED', 'CRT', 'CRT approves CB-fail case. Moves to T24 update.', NULL, 1),
('REJECT', 'APZCBO', 'CRTQUEUE', 'CMCRT', CURRENT_TIMESTAMP, 'CRT', 'CRT', 'CRTREJECTED', 'CRT_REJECTED', 'CRT', 'CRT rejects case.', NULL, 2),
('APPROVED', 'APZCBO', 'INSQUEUE', 'CMINSURANCE', CURRENT_TIMESTAMP, 'INSURANCE', 'SYSTEM', 'T24UPDATE', 'INSURANCE_APPROVED', 'INSURANCE', 'Insurance team approves marital status change.', NULL, 1),
('REJECT', 'APZCBO', 'INSQUEUE', 'CMINSURANCE', CURRENT_TIMESTAMP, 'INSURANCE', 'INSURANCE', 'INSREJECTED', 'INSURANCE_REJECTED', 'INSURANCE', 'Insurance team rejects request.', NULL, 2),
('RETRIGGER', 'APZCBO', 'T24PENDING', 'CMCHT', CURRENT_TIMESTAMP, 'CHT', 'SYSTEM', 'T24UPDATE', 'T24_RETRIGGERED', 'CHT', 'CHT manually re-triggers T24 update.', NULL, 1),
('VERIFY', 'APZCBO', 'COMPLETED', 'CMRPCTL', CURRENT_TIMESTAMP, 'RPCTL', 'RPCTL', 'TLVERIFIED', 'TL_VERIFIED', 'RPCTL', 'RPC Team Leader sample check verified.', NULL, 1),
('RECOMMEND', 'APZCBO', 'COMPLETED', 'CMRPCTL', CURRENT_TIMESTAMP, 'RPCTL', 'BST', 'BSTQUEUE', 'BST_RECOMMENDED', 'RPCTL', 'RPC TL recommends case for BST review.', NULL, 2),
('VERIFY', 'APZCBO', 'COMPLETED', 'CMRPCHO', CURRENT_TIMESTAMP, 'RPCHO', 'RPCHO', 'RPCHOVERIFIED', 'RPCHO_VERIFIED', 'RPCHO', 'RPC Head Office sample check verified.', NULL, 1),
('RECOMMEND', 'APZCBO', 'COMPLETED', 'CMRPCHO', CURRENT_TIMESTAMP, 'RPCHO', 'BST', 'BSTQUEUE', 'BST_RECOMMENDED', 'RPCHO', 'RPC HO recommends case for BST review.', NULL, 2),
('VERIFY', 'APZCBO', 'BSTQUEUE', 'CMBST', CURRENT_TIMESTAMP, 'BST', 'BST', 'BSTVERIFIED', 'BST_VERIFIED', 'BST', 'BST verifies sample case and adds comments.', NULL, 1)
ON CONFLICT (app_id, workflow_id, from_stage_id, "action") DO NOTHING;

-- ==============================================================================
-- 3. tb_cm_configurable_data (Static UI Dropdown Configurations)
-- ==============================================================================
INSERT INTO public.tb_cm_configurable_data (app_id, "type", json_payload) VALUES
('CM', 'GENDER',         '[{"code":"F","value":"Female"},{"code":"M","value":"Male"},{"code":"T","value":"Transgender"}]'::jsonb),
('CM', 'MARITAL_STATUS', '[{"code":"M","value":"Married"},{"code":"S","value":"Single"},{"code":"W","value":"Widowed"},{"code":"D","value":"Divorced"}]'::jsonb),
('CM', 'RELATIONSHIP',   '[{"code":"SPO","value":"Spouse"},{"code":"SON","value":"Son"},{"code":"DAU","value":"Daughter"},{"code":"FAT","value":"Father"},{"code":"MOT","value":"Mother"}]'::jsonb),
('CM', 'KYC_TYPE',       '[{"code":"AADHAAR","value":"Aadhaar"},{"code":"VOTER","value":"Voter ID"},{"code":"PAN","value":"PAN Card"},{"code":"DL","value":"Driving Licence"}]'::jsonb),
('CM', 'RELIGION',       '[{"code":"HIN","value":"Hindu"},{"code":"MUS","value":"Muslim"},{"code":"CHR","value":"Christian"},{"code":"OTH","value":"Others"}]'::jsonb),
('CM', 'CASTE',          '[{"code":"GEN","value":"General"},{"code":"OBC","value":"OBC"},{"code":"SC","value":"SC"},{"code":"ST","value":"ST"}]'::jsonb),
('CM', 'QUALIFICATION',  '[{"code":"ILL","value":"Illiterate"},{"code":"PRI","value":"Primary"},{"code":"SSLC","value":"SSLC"},{"code":"PUC","value":"PUC"},{"code":"GRD","value":"Graduate"}]'::jsonb),
('CM', 'INCOME_SOURCE',  '[{"code":"AGR","value":"Agriculture"},{"code":"DAIRY","value":"Dairy"},{"code":"TAIL","value":"Tailoring"},{"code":"SHOP","value":"Petty Shop"},{"code":"LAB","value":"Daily Labour"}]'::jsonb),
('CM', 'LANGUAGE',       '[{"code":"KN","value":"Kannada"},{"code":"TA","value":"Tamil"},{"code":"TE","value":"Telugu"},{"code":"HI","value":"Hindi"},{"code":"EN","value":"English"}]'::jsonb),
('CM', 'ADDRESS_PROOF',  '[{"code":"AADHAAR","value":"Aadhaar"},{"code":"VOTER","value":"Voter ID"},{"code":"RATION","value":"Ration Card"},{"code":"EB","value":"Electricity Bill"}]'::jsonb)
ON CONFLICT (app_id, "type") DO UPDATE SET json_payload = EXCLUDED.json_payload;

-- ==============================================================================
-- 4. tb_cm_customer (Customer Master Core Profiles)
-- ==============================================================================
INSERT INTO public.tb_cm_customer
(customer_id, aml_status, application_id, bank_details, bre_status, cgt_status, created_by, created_ts, customer_name, dob, grt_status, kyc_details, kyc_status, live_photo_status, marital_status, primary_kyc_id, primary_kyc_type, updated_by, updated_ts) VALUES
('100294',   'CLEAR',   'APP100294',     '{"bankName":"State Bank of India","accountNo":"30123456781","ifsc":"SBIN0001234","branch":"Mandya"}'::jsonb,        'PASS',    'COMPLETED', '8282828230', '2026-09-20 09:15:00', 'Sunita Gowda',  '1988-06-15', 'COMPLETED', '{"aadhaar":"XXXX-XXXX-1001","voterId":"KA0110001"}'::jsonb, 'VERIFIED', 'CAPTURED', 'Married', 'XXXXXXXX1001', 'AADHAAR', '8282828230', '2026-09-20 10:00:00'),
('100301',   'CLEAR',   'APP100301',     '{"bankName":"Canara Bank","accountNo":"0412101002","ifsc":"CNRB0000412","branch":"Bangalore"}'::jsonb,                'PASS',    'COMPLETED', '8282828237', '2026-09-20 09:30:00', 'Lakshmi Devi',  '1985-04-12', 'COMPLETED', '{"aadhaar":"XXXX-XXXX-1002","voterId":"KA0110002"}'::jsonb, 'VERIFIED', 'CAPTURED', 'Married', 'XXXXXXXX1002', 'AADHAAR', '8282828237', '2026-09-20 10:10:00'),
('CUST1001', 'CLEAR',   'APP20260929001', '{"bankName":"State Bank of India","accountNo":"30123456781","ifsc":"SBIN0001234","branch":"Jayanagar"}'::jsonb,   'PASS',    'COMPLETED', 'EMP101',     '2026-09-20 09:15:00', 'Lakshmi Devi',  '1985-04-12', 'COMPLETED', '{"aadhaar":"XXXX-XXXX-1001","voterId":"KA0110001"}'::jsonb, 'VERIFIED', 'CAPTURED', 'Married', 'XXXXXXXX1001', 'AADHAAR', 'EMP101',     '2026-09-20 10:00:00'),
('CUST1002', 'CLEAR',   'APP20260929002', '{"bankName":"Canara Bank","accountNo":"0412101002","ifsc":"CNRB0000412","branch":"Jayanagar"}'::jsonb,            'PASS',    'COMPLETED', 'EMP101',     '2026-09-20 09:30:00', 'Savitha Rao',   '1988-07-23', 'PENDING',   '{"aadhaar":"XXXX-XXXX-1002","voterId":"KA0110002"}'::jsonb, 'VERIFIED', 'CAPTURED', 'Married', 'XXXXXXXX1002', 'AADHAAR', 'EMP101',     '2026-09-20 10:10:00')
ON CONFLICT (customer_id) DO NOTHING;

-- ==============================================================================
-- 5. tb_cm_address (Permanent and Communication Addresses)
-- ==============================================================================
INSERT INTO public.tb_cm_address
(address_id, addr_payload, address_proof_doc_id, address_proof_type, address_type, application_id, comm_same_as_perm, created_ts, customer_id, distance_from_branch, sub_type, updated_by, updated_ts) VALUES
('ADR100294', '{"line1":"No 12, Main Road","line2":"Mandya Town","line3":"","village":"Mandya","taluk":"Mandya","district":"Mandya","state":"Karnataka","pincode":"571401"}'::jsonb, 'DOC1001', 'AADHAAR', 'P', 'APP100294', 'Y', '2026-09-20 09:20:00', '100294', '1.5',  'OWNED',  '8282828230', '2026-09-20 09:20:00'),
('ADR100301', '{"line1":"No 45, 5th Main","line2":"Ejipura","line3":"","village":"Ejipura","taluk":"Bengaluru South","district":"Bengaluru Urban","state":"Karnataka","pincode":"560047"}'::jsonb, 'DOC1002', 'AADHAAR', 'P', 'APP100301', 'Y', '2026-09-20 09:35:00', '100301', '2.0',  'RENTED', '8282828237', '2026-09-20 09:35:00'),
('ADR1001',   '{"line1":"No 12, 3rd Cross","line2":"Tilak Nagar","line3":"Near Temple","village":"Jayanagar","taluk":"Bengaluru South","district":"Bengaluru Urban","state":"Karnataka","pincode":"560041"}'::jsonb, 'DOC1001', 'AADHAAR', 'P', 'APP20260929001', 'Y', '2026-09-20 09:20:00', 'CUST1001', '2.5',  'OWNED',  'EMP101', '2026-09-20 09:20:00'),
('ADR1002',   '{"line1":"No 45, 5th Main","line2":"JP Nagar","line3":"Opp School","village":"JP Nagar","taluk":"Bengaluru South","district":"Bengaluru Urban","state":"Karnataka","pincode":"560078"}'::jsonb,        'DOC1002', 'AADHAAR', 'P', 'APP20260929002', 'Y', '2026-09-20 09:35:00', 'CUST1002', '4.0',  'RENTED', 'EMP101', '2026-09-20 09:35:00')
ON CONFLICT (address_id) DO NOTHING;

-- ==============================================================================
-- 6. tb_cm_family_member (Spouses, Nominees, Earning Members)
-- ==============================================================================
INSERT INTO public.tb_cm_family_member
(family_mem_id, application_id, created_ts, customer_id, dob, gender, is_earning_member, is_nominee, kyc_doc_id, kyc_type, member_type, mobile_num, "name", nominee_bank_details, relation) VALUES
('FAM100294', 'APP100294', '2026-09-20 09:25:00', '100294', '1985-02-10', 'Male',   true,  true,  'XXXXXXXX2001', 'AADHAAR', 'SPO', '9845000001', 'Ramesh Gowda', '{"bankName":"State Bank of India","accountNo":"30123450001","ifsc":"SBIN0001234"}'::jsonb, 'Spouse'),
('FAM100301', 'APP100301', '2026-09-20 09:40:00', '100301', '1982-05-15', 'Male',   true,  true,  'XXXXXXXX2002', 'AADHAAR', 'SPO', '9845000002', 'Manjunath K',  '{"bankName":"Canara Bank","accountNo":"0412100002","ifsc":"CNRB0000412"}'::jsonb,         'Spouse'),
('FAM1001',   'APP20260929001', '2026-09-20 09:25:00', 'CUST1001', '1982-02-10', 'Male',   true,  true,  'XXXXXXXX2001', 'AADHAAR', 'SPO', '9845000001', 'Ramesh',       '{"bankName":"State Bank of India","accountNo":"30123450001","ifsc":"SBIN0001234"}'::jsonb, 'Spouse')
ON CONFLICT (family_mem_id) DO NOTHING;

-- ==============================================================================
-- 7. tb_cm_application_master (Customer Management Applications)
-- ==============================================================================
INSERT INTO public.tb_cm_application_master
(application_id, branch_id, branch_name, channel_type, created_by, created_ts, customer_id, customer_name, group_id, kendra_id, kendra_name, km_name, leader, loan_eligible, loan_id, mobile_number, record_type, stage, status, sub_stage, updated_by, updated_ts, "version", wfstage) VALUES
-- Drafts (2)
('APP100294', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828230', '2026-09-20 09:15:00', '100294', 'Sunita Gowda',  'GRP01', 'KEN101', 'Mandya',          '8282828230', 'Y', 'ELIGIBLE',     'LN900001', '9876543201', 'KYC_UPDATE',  'DRAFT',      'PENDING',   'DOC_UPLOAD',  '8282828230', '2026-09-20 10:00:00', '1', 'DRAFT'),
('APP100301', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828237', '2026-09-20 09:30:00', '100301', 'Lakshmi Devi',  'GRP02', 'KEN102', 'Bangalore',       '8282828237', 'N', 'ELIGIBLE',     'LN900002', '9876543202', 'BANK_UPDATE', 'DRAFT',      'PENDING',   'DOC_UPLOAD',  '8282828237', '2026-09-20 10:10:00', '1', 'DRAFT'),
-- Onhold (4)
('APP100302', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828230', '2026-09-21 10:00:00', '100302', 'Savitha Rao',   'GRP01', 'KEN101', 'Mandya',          '8282828230', 'Y', 'ELIGIBLE',     'LN900003', '9876543203', 'LOC_UPDATE',  'BMONHOLD',   'ON_HOLD',   'QUERY_RAISED','BM001',      '2026-09-21 14:00:00', '1', 'BMONHOLD'),
('APP100303', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828230', '2026-09-21 10:30:00', '100303', 'Geetha Kumari', 'GRP01', 'KEN101', 'Mandya',          '8282828230', 'N', 'ELIGIBLE',     'LN900004', '9876543204', 'KYC_UPDATE',  'BMONHOLD',   'ON_HOLD',   'QUERY_RAISED','BM001',      '2026-09-21 14:30:00', '1', 'BMONHOLD'),
('APP100304', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828237', '2026-09-21 11:00:00', '100304', 'Roopa Shetty',  'GRP02', 'KEN102', 'Bangalore',       '8282828237', 'Y', 'ELIGIBLE',     'LN900005', '9876543205', 'BANK_UPDATE', 'RPCONHOLD',  'ON_HOLD',   'QUERY_RAISED','RPCM01',    '2026-09-21 15:00:00', '1', 'RPCONHOLD'),
('APP100305', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828237', '2026-09-21 11:30:00', '100305', 'Manjula N',     'GRP02', 'KEN102', 'Bangalore',       '8282828237', 'N', 'ELIGIBLE',     'LN900006', '9876543206', 'FAM_UPDATE',  'AMONHOLD',   'ON_HOLD',   'QUERY_RAISED','AM001',      '2026-09-21 15:30:00', '1', 'AMONHOLD'),
-- Pending for BM review (2)
('APP100306', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828230', '2026-09-22 09:00:00', '100306', 'Kavitha M',     'GRP01', 'KEN101', 'Mandya',          '8282828230', 'Y', 'ELIGIBLE',     'LN900007', '9876543207', 'KYC_UPDATE',  'BMQUEUE',    'IN_REVIEW', 'VERIFY',      '8282828230', '2026-09-22 10:00:00', '1', 'BMQUEUE'),
('APP100307', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828237', '2026-09-22 09:30:00', '100307', 'Pushpa L',      'GRP02', 'KEN102', 'Bangalore',       '8282828237', 'N', 'ELIGIBLE',     'LN900008', '9876543208', 'BANK_UPDATE', 'BMQUEUE',    'IN_REVIEW', 'VERIFY',      '8282828237', '2026-09-22 10:30:00', '1', 'BMQUEUE'),
-- Pending for AM review (2)
('APP100308', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828230', '2026-09-22 11:00:00', '100308', 'Anitha S',      'GRP01', 'KEN101', 'Mandya',          '8282828230', 'Y', 'ELIGIBLE',     'LN900009', '9876543209', 'LOC_UPDATE',  'AMQUEUE',    'IN_REVIEW', 'VERIFY',      'BM001',      '2026-09-22 12:00:00', '1', 'AMQUEUE'),
('APP100309', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828237', '2026-09-22 11:30:00', '100309', 'Rekha D',       'GRP02', 'KEN102', 'Bangalore',       '8282828237', 'N', 'ELIGIBLE',     'LN900010', '9876543210', 'KYC_UPDATE',  'AMQUEUE',    'IN_REVIEW', 'VERIFY',      'BM001',      '2026-09-22 12:30:00', '1', 'AMQUEUE'),
-- Pending for RPC review (2)
('APP100310', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828230', '2026-09-23 09:00:00', '100310', 'Suma B',        'GRP01', 'KEN101', 'Mandya',          '8282828230', 'Y', 'ELIGIBLE',     'LN900011', '9876543211', 'KYC_UPDATE',  'RPCMAKERQUEUE','IN_REVIEW', 'VERIFY',    'BM001',      '2026-09-23 10:00:00', '1', 'RPCMAKERQUEUE'),
('APP100311', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828237', '2026-09-23 09:30:00', '100311', 'Radha K',       'GRP02', 'KEN102', 'Bangalore',       '8282828237', 'N', 'ELIGIBLE',     'LN900012', '9876543212', 'BANK_UPDATE', 'RPCCHECKERQUEUE','IN_REVIEW','4_EYES', 'RPCM01',     '2026-09-23 11:00:00', '1', 'RPCCHECKERQUEUE'),
-- Completed (2)
('APP100312', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828230', '2026-09-24 09:00:00', '100312', 'Bhavani T',     'GRP01', 'KEN101', 'Mandya',          '8282828230', 'Y', 'ELIGIBLE',     'LN900013', '9876543213', 'KYC_UPDATE',  'COMPLETED',  'APPROVED',  'T24_SYNCED',  'RPCC01',     '2026-09-24 16:00:00', '1', 'COMPLETED'),
('APP100313', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828237', '2026-09-24 09:30:00', '100313', 'Chaitra V',     'GRP02', 'KEN102', 'Bangalore',       '8282828237', 'N', 'ELIGIBLE',     'LN900014', '9876543214', 'BANK_UPDATE', 'COMPLETED',  'APPROVED',  'T24_SYNCED',  'RPCC01',     '2026-09-24 16:30:00', '1', 'COMPLETED'),
-- Rejected (2)
('APP100314', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828230', '2026-09-25 09:00:00', '100314', 'Deepa G',       'GRP01', 'KEN101', 'Mandya',          '8282828230', 'Y', 'NOT_ELIGIBLE', NULL,       '9876543215', 'KYC_UPDATE',  'REJECTED',   'REJECTED',  'AML_HIT',     'AML001',     '2026-09-25 15:00:00', '1', 'REJECTED'),
('APP100315', 'Ejipura', 'Ejipura Branch', 'ONLINE',  '8282828237', '2026-09-25 09:30:00', '100315', 'Usha N',        'GRP02', 'KEN102', 'Bangalore',       '8282828237', 'N', 'NOT_ELIGIBLE', NULL,       '9876543216', 'BANK_UPDATE', 'REJECTED',   'REJECTED',  'BRE_FAIL',    'BRE001',     '2026-09-25 15:30:00', '1', 'REJECTED')
ON CONFLICT (application_id) DO NOTHING;

-- ==============================================================================
-- 8. tb_cm_document (Document Storage & OCR Statuses)
-- ==============================================================================
INSERT INTO public.tb_cm_document
(docu_id, application_id, category, clarity_pass, clarity_score, created_ts, customer_id, dedupe_status, dms_doc_id_back, dms_doc_id_front, doc_version, kyc_type, legal_doc_id, legal_doc_name, status, sub_cat, updated_ts, uploaded_at, uploaded_by, validation_status) VALUES
('DOC1001', 'APP20260929001', 'KYC', 'Y', 92.50, '2026-09-20 09:18:00', 'CUST1001', 'UNIQUE', 'DMS-B-1001', 'DMS-F-1001', 1, 'POI', 'XXXXXXXX1001', 'AADHAAR', 'ACTIVE', 'PRIMARY', '2026-09-20 09:18:00', '2026-09-20 09:18:00', 'EMP101', 'VALID'),
('DOC1002', 'APP20260929002', 'KYC', 'Y', 88.00, '2026-09-20 09:33:00', 'CUST1002', 'UNIQUE', 'DMS-B-1002', 'DMS-F-1002', 1, 'POI', 'XXXXXXXX1002', 'AADHAAR', 'ACTIVE', 'PRIMARY', '2026-09-20 09:33:00', '2026-09-20 09:33:00', 'EMP101', 'VALID'),
('DOC1003', 'APP20260929003', 'KYC', 'Y', 95.25, '2026-09-21 11:03:00', 'CUST1003', 'UNIQUE', 'DMS-B-1003', 'DMS-F-1003', 1, 'POI', 'XXXXXXXX1003', 'AADHAAR', 'ACTIVE', 'PRIMARY', '2026-09-21 11:03:00', '2026-09-21 11:03:00', 'EMP102', 'VALID'),
('DOC1004', 'APP20260929004', 'KYC', 'N', 41.75, '2026-09-21 11:23:00', 'CUST1004', 'DUPLICATE', 'DMS-B-1004', 'DMS-F-1004', 1, 'POI', 'XXXXXXXX1004', 'AADHAAR', 'REJECTED', 'PRIMARY', '2026-09-21 16:30:00', '2026-09-21 11:23:00', 'EMP102', 'INVALID'),
('DOC1005', 'APP20260929005', 'KYC', 'Y', 90.10, '2026-09-22 09:48:00', 'CUST1005', 'UNIQUE', 'DMS-B-1005', 'DMS-F-1005', 2, 'POI', 'XXXXXXXX1005', 'AADHAAR', 'ACTIVE', 'PRIMARY', '2026-09-23 12:00:00', '2026-09-22 09:48:00', 'EMP103', 'VALID')
ON CONFLICT (docu_id) DO NOTHING;

-- ==============================================================================
-- 9. tb_cm_record_lock (Concurrency Locks)
-- ==============================================================================
INSERT INTO public.tb_cm_record_lock
(lock_id, application_id, lock_expiry_ts, lock_type, locked_at, locked_by, locked_by_role, released_at, status) VALUES
('LCK1001', 'APP20260929001', '2026-09-20 10:30:00', 'EDIT',   '2026-09-20 10:00:00', 'EMP101', 'KM',  '2026-09-20 10:05:00', 'RELEASED'),
('LCK1002', 'APP20260929002', '2026-09-20 10:40:00', 'EDIT',   '2026-09-20 10:10:00', 'EMP101', 'KM',  '2026-09-20 10:12:00', 'RELEASED'),
('LCK1003', 'APP20260929003', '2026-09-21 15:30:00', 'REVIEW', '2026-09-21 15:00:00', 'EMP201', 'BM',  NULL,                  'ACTIVE')
ON CONFLICT (lock_id) DO NOTHING;

-- ==============================================================================
-- 10. tb_cm_cust_audit_trail (Audit Logging of Customer Modifications)
-- ==============================================================================
INSERT INTO public.tb_cm_cust_audit_trail
(id, application_id, create_ts, customer_id, editeddetails, isedited, payload, stage_id, sub_stage, user_id, user_name, user_role, wfstatus) VALUES
('AUD1001', 'APP20260929001', '2026-09-20 10:00:00', 'CUST1001', NULL, 'N', '{"action":"SUBMIT"}'::jsonb, 'INITIATED', 'DOC_UPLOAD', 'EMP101', 'Ravi Kumar', 'KM', 'PENDING'),
('AUD1002', 'APP20260929002', '2026-09-20 10:10:00', 'CUST1002', NULL, 'N', '{"action":"SUBMIT"}'::jsonb, 'INITIATED', 'DOC_UPLOAD', 'EMP101', 'Ravi Kumar', 'KM', 'PENDING'),
('AUD1003', 'APP20260929003', '2026-09-21 15:00:00', 'CUST1003', '{"mobile_number":{"old":"9876500003","new":"9876543203"}}'::jsonb, 'Y', '{"action":"APPROVE"}'::jsonb, 'BM_REVIEW', 'VERIFY', 'EMP201', 'Anil Kumar', 'BM', 'PENDING')
ON CONFLICT (id) DO NOTHING;

-- ==============================================================================
-- 11. tb_cm_appln_workflow (Live Application Movement History)
-- ==============================================================================
INSERT INTO public.tb_cm_appln_workflow
(app_id, application_id, version_no, workflow_seq_no, application_status, created_by, created_ts, created_username, next_workflow_stage, present_role, remarks) VALUES
('APZCBO', 'APP20260929001', 1, 1, 'PENDING',  'EMP101', '2026-09-20 10:00:00', 'Ravi Kumar',  'BM_REVIEW',  'KM',  'Submitted for BM review'),
('APZCBO', 'APP20260929002', 1, 1, 'PENDING',  'EMP101', '2026-09-20 10:10:00', 'Ravi Kumar',  'BM_REVIEW',  'KM',  'Submitted for BM review'),
('APZCBO', 'APP20260929003', 1, 1, 'PENDING',  'EMP102', '2026-09-21 11:30:00', 'Suresh Babu', 'BM_REVIEW',  'KM',  'Submitted for BM review'),
('APZCBO', 'APP20260929003', 1, 2, 'PENDING',  'EMP201', '2026-09-21 15:00:00', 'Anil Kumar',  'CPU_REVIEW', 'BM',  'Documents verified by BM')
ON CONFLICT (app_id, application_id, version_no, workflow_seq_no) DO NOTHING;

-- ==============================================================================
-- 12. gk_unified_data (CDH Central Data Hub 96-Column Member Seed Records)
-- ==============================================================================
INSERT INTO public.gk_unified_data (
  id, aadhaar_dob, aadhaar_name, aadhaar_number_masked, activation_date, address_proof_doc_id,
  amount, approved_amt, bankaccountname, bankacno, bankbranchname, bankifsccode, bankname,
  bank_verification_status, bank_verified_date, branch_id, branchname, caste, ckyc_captured_date,
  ckyc_id, comm_language, communication_address_line1, communication_address_line2, communication_address_line3,
  communication_district, communication_pincode, communication_state, communication_taluk, communication_village_locality,
  cust_photo_doc_id, customerid, customername, cust_qualify, custstatus, cust_vintage,
  depdob, depdocid, depdoctype, depname, device_type, distance_from_branch, dob, dry_land_acres,
  eligible_cagl_amt, eligible_cagl_product, email_id, gender, groupid, house_latitude, house_longitude,
  interest_rate, kendraid, kendra_name, km_name, leader_id, loan_id, maritalstatus, mem_relation,
  mobile_number, nationality, no_of_adults, no_of_children, nominee_bank_details, dobe, legal_id,
  legal_doc_name, "name", mem_relatione, outstanding_principal, overall_cb_eligible_amount, overdue_status,
  pan_dob, pan_father_name, pan_name, pan_number, passbook_doc_id, permanent_address_line1, permanent_address_line2,
  permanent_address_line3, permanent_district, permanent_pincode, permanent_state, permanent_taluk,
  permanent_village_locality, primaryid, primarytype, product, religion, source_of_income, spouse_ckyc_id,
  spouse_mobile_number, spouse_photo_doc_id, title, tot_expenses, tot_income, wet_land_acres
) VALUES 
(
  10317774, '12/05/1988', 'Lakshmi Devi', 'XXXXXXXX1234', '15/06/2019', 'DOC_ADDR_100023451',
  '50000', '50000', 'LAKSHMI DEVI', '100234567890', 'Ramanagara Town Branch', 'SBIN0040123', 'State Bank of India',
  'Verified', '2024-01-15 11:30:00', 'BR001', 'Ramanagara Main Branch', 'OBC', '10/01/2022',
  '50012345678901', 'Kannada', '#45, 2nd Cross', 'Near Anjaneya Temple', 'Shanthi Nagar',
  'Ramanagara', '562159', 'Karnataka', 'Ramanagara Taluk', 'Ramanagara',
  'DOC_PHOTO_100023451', '100023451', 'Lakshmi Devi', 'Secondary', 'Active', '5',
  '15/08/1984', 'KA/04/023/987654', 'VOTER ID', 'Manjunath K', 'Smartphone', 4.50, '12/05/1988', 2.00,
  75000, 'PRAGATI PLUS', 'lakshmi.devi@example.com', 'Female', 12, 12.7208000, 77.2789000,
  '21.50', 101, 'Kendra Shanthi Nagar', 'Ramesh Kumar', 'LDR_01', 'LN1002345101', 'Married', 'Husband',
  '9845012345', 'INDIAN', 2, 2, '{"bankName":"SBI","acNo":"100234567890","ifsc":"SBIN0040123"}',
  '20/10/2006', 'XXXXXXXX9988', 'AADHAAR', 'Praveen M', 'Son', '18500', 100000, '0 DPD',
  '12/05/1988', 'RAMAPPA', 'LAKSHMI DEVI', 'ABCPL1234K', 'DOC_PASSBOOK_100023451', '#45, 2nd Cross',
  'Near Anjaneya Temple', 'Shanthi Nagar', 'Ramanagara', '562159', 'Karnataka', 'Ramanagara Taluk',
  'Ramanagara', 'KA/04/023/123456', 'VOTER ID', 'UNNATI', 'Hindu', 'Dairy & Agriculture', '50012345678999',
  '9845055555', 'DOC_SP_PHOTO_100023451', 'Mrs', '90000', '180000', 1.50
),
(
  10317775, '05/11/1992', 'Sunitha R', 'XXXXXXXX5678', '20/09/2021', 'DOC_ADDR_100023452',
  '60000', '60000', 'SUNITHA R', '200345678901', 'Kolar Bazaar Branch', 'CNRB0001234', 'Canara Bank',
  'Verified', '2024-02-10 14:15:00', 'BR002', 'Kolar Town Branch', 'General', '15/03/2022',
  '50012345678902', 'Kannada', 'Door #12, Market Road', 'Opposite Govt High School', 'Gandhi Nagar',
  'Kolar', '563101', 'Karnataka', 'Kolar Taluk', 'Kolar',
  'DOC_PHOTO_100023452', '100023452', 'Sunitha R', 'Graduate', 'Active', '3',
  '10/04/1990', 'KA/06/045/112233', 'VOTER ID', 'Ravi Kumar', 'Smartphone', 2.80, '05/11/1992', 1.00,
  80000, 'UNNATI PLUS', 'sunitha.r@example.com', 'Female', 15, 13.1367000, 78.1291000,
  '21.50', 102, 'Kendra Gandhi Nagar', 'Suresh Babu', 'LDR_02', 'LN1002345201', 'Married', 'Husband',
  '9845123456', 'INDIAN', 2, 1, '{"bankName":"Canara Bank","acNo":"200345678901","ifsc":"CNRB0001234"}',
  '15/07/2012', 'XXXXXXXX3344', 'AADHAAR', 'Anitha R', 'Daughter', '24000', 120000, '0 DPD',
  '05/11/1992', 'RAMESH', 'SUNITHA R', 'BKPPS9876M', 'DOC_PASSBOOK_100023452', 'Door #12, Market Road',
  'Opposite Govt High School', 'Gandhi Nagar', 'Kolar', '563101', 'Karnataka', 'Kolar Taluk',
  'Kolar', 'KA/06/045/654321', 'VOTER ID', 'SUVIDHA', 'Hindu', 'Kirana Business', '50012345678988',
  '9845133333', 'DOC_SP_PHOTO_100023452', 'Mrs', '110000', '240000', 0.00
)
ON CONFLICT (id) DO NOTHING;

COMMIT;
