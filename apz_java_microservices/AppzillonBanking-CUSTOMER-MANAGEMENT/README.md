# CAGL Customer Management Standalone Microservice

**Service Name**: `AppzillonBanking-CUSTOMER-MANAGEMENT`  
**Framework**: Java 17 | Spring Boot 3.5.9 | Jakarta EE  
**Architecture**: Multi-Datasource Microservice (PostgreSQL `tb_cm_*` + CDH MySQL `gk_unified_data`)  
**Context Path**: `/appzillonbankingcm` (Port: `9296`)

---

## 1. Executive Summary & Purpose

The **Customer Management Microservice** is a standalone Spring Boot service in the CAGL Maitri banking ecosystem responsible for managing the post-onboarding lifecycle of borrowers:
* **Member Profile 360° View**: Consolidated view of personal details, multi-KYC, 3-line addresses, penny-drop verified bank accounts, family/nominee details, household cashflow & land assets, and active loans.
* **On-Demand CDH Ingestion**: Real-time hydration of legacy and active customer records from Central Data Hub (`gk_unified_data`) into normalized `tb_cm_*` tables.
* **Data Modification & Re-KYC Workflows**: Supports STP (Straight-Through Processing) and Maker-Checker approval workflows (Kendra Manager $\rightarrow$ Branch Manager $\rightarrow$ RPC Maker $\rightarrow$ RPC Checker).
* **Integrations & Automated Validations**:
  * **Penny Drop API** & Fuzzy Name Matching for Bank verification.
  * **OCR Extraction & Image Clarity Scoring** for uploaded KYC documents.
  * **Cross-Dedupe Checks** for Mobile Numbers, KYC IDs, and Bank Accounts.
  * **UIDAI Aadhaar e-KYC & CKYC** search and download.
  * **DMS Integration** for secure image storage and document UUID retrieval.
  * **T24 Core Banking Synchronization** upon final approval.
* **Pessimistic Concurrency Locking**: Prevents simultaneous edits using `tb_cm_record_lock`.
* **Field-Level Audit Trail**: Comprehensive JSON diff logging in `tb_cm_cust_audit_trail`.

---

## 2. Directory & Package Structure

```
AppzillonBanking-CUSTOMER-MANAGEMENT/
├── pom.xml                                                ──► Java 17, Spring Boot 3.5.9, Postgres, MySQL
├── README.md                                              ──► Microservice Documentation
├── src/main/resources/
│   ├── application.properties                             ──► Dual Datasource (Postgres + CDH MySQL), Ports, URLs
│   └── db/migration/
│       └── V1__create_customer_management_tables.sql       ──► DDL for all 11 tb_cm_* relational tables
│
└── src/main/java/com/iexceed/appzillonbanking/cagl/cm/
    │
    ├── AppzillonBankingCustomerManagementApplication.java ──► Main Spring Boot Application Entry Point
    │
    ├── config/                                            ──► [Configuration Layer]
    │   ├── PrimaryDatabaseConfig.java                     ──► Primary Datasource (PostgreSQL - tb_cm_*)
    │   ├── CdhDatabaseConfig.java                         ──► Secondary Datasource (MySQL - CDH gk_unified_data)
    │   ├── RestTemplateConfig.java                        ──► HTTP Client with connection pooling & timeouts
    │   └── JacksonConfig.java                             ──► JSON date formatting & JSONB object mappers
    │
    ├── entity/                                            ──► [JPA Entity Layer]
    │   ├── primary/                                       ──► (PostgreSQL tb_cm_* tables)
    │   │   ├── CmCustomerEntity.java                      ──► tb_cm_customer (Core profile & statuses)
    │   │   ├── CmAddressEntity.java                       ──► tb_cm_address (Permanent & Communication JSONs)
    │   │   ├── CmFamilyMemberEntity.java                  ──► tb_cm_family_member (Spouse, Nominee, Dependents)
    │   │   ├── CmApplicationMasterEntity.java             ──► tb_cm_application_master (Center, Loans, Branch)
    │   │   ├── CmDocumentEntity.java                      ──► tb_cm_document (OCR, Clarity, DMS Links)
    │   │   ├── CmRecordLockEntity.java                    ──► tb_cm_record_lock (Pessimistic concurrency lock)
    │   │   ├── CmCustAuditTrailEntity.java                ──► tb_cm_cust_audit_trail (Field-level change logs)
    │   │   ├── CmConfigurableDataEntity.java              ──► tb_cm_configurable_data (JSON configs)
    │   │   ├── CmWorkflowMasterEntity.java                ──► tb_ob_workflow_master (Workflow setups)
    │   │   ├── CmWorkflowDefinitionEntity.java            ──► tb_ob_workflow_definition (Transition rules)
    │   │   ├── CmWorkflowDefinitionPK.java                ──► Composite Primary Key for definition
    │   │   ├── CmApplnWorkflowEntity.java                 ──► tb_cm_appln_workflow (Audit of transitions)
    │   │   └── CmApplnWorkflowPK.java                     ──► Composite Primary Key for workflow history
    │   └── cdh/                                           ──► (CDH MySQL tables)
    │       └── GkUnifiedDataEntity.java                   ──► gk_unified_data (With all 34 new fields enabled)
    │
    ├── repository/                                        ──► [Data Access Layer]
    │   ├── primary/
    │   │   ├── CmCustomerRepository.java                  ──► Query customer by ID, Aadhaar, Mobile
    │   │   ├── CmAddressRepository.java                   ──► Query addresses by customer & type ('P'/'C')
    │   │   ├── CmFamilyMemberRepository.java              ──► Query family & nominee by customer ID
    │   │   ├── CmApplicationMasterRepository.java         ──► Query applications by branch, kendra, status
    │   │   ├── CmDocumentRepository.java                  ──► Query documents by customer & KYC type
    │   │   ├── CmRecordLockRepository.java                ──► Query active locks, check lock expiry
    │   │   ├── CmCustAuditTrailRepository.java            ──► Query last 3 changes for profile card
    │   │   ├── CmConfigurableDataRepository.java          ──► Fetch JSON configs by type
    │   │   ├── CmWorkflowDefinitionRepository.java        ──► Lookup next stage based on from_stage + action
    │   │   └── CmApplnWorkflowRepository.java             ──► Save stage transitions & query history
    │   └── cdh/
    │       └── GkUnifiedDataRepository.java               ──► Query CDH by CUSTOMERID, MOBILE_NUMBER, PRIMARYID
    │
    ├── payload/                                           ──► [DTO & Request/Response Layer]
    │   ├── common/
    │   │   ├── RequestWrapper.java                        ──► Standard Appzillon Request Envelope
    │   │   ├── ResponseWrapper.java                       ──► Standard Appzillon Response Envelope
    │   │   └── ResponseHeader.java                        ──► Response status, code, message
    │   ├── search/
    │   │   ├── CustomerSearchRequest.java                 ──► Search filters (ID, Mobile, Kendra, Name, KYC)
    │   │   └── CustomerSearchResponse.java                ──► List of matching members with status badges
    │   ├── profile/
    │   │   ├── CustomerProfileResponseDto.java            ──► Full consolidated profile payload
    │   │   ├── ProfileHeaderCardDto.java                  ──► Header (Photo, Progress %, Nudges)
    │   │   ├── KycDetailsCardDto.java                     ──► Aadhaar, PAN, CKYC, Renewal date
    │   │   ├── AddressCardDto.java                        ──► Perm & Comm addresses, Lat/Long, Proof
    │   │   ├── BankDetailsCardDto.java                    ──► Bank A/C, IFSC, Penny Drop status
    │   │   ├── FamilyDetailsCardDto.java                  ──► Spouse, Nominee bank details, Dependents
    │   │   ├── IncomeAssessmentCardDto.java               ──► Total income, expenses, land details
    │   │   └── ActiveLoansCardDto.java                    ──► Active loans, overdue, eligible products
    │   └── update/
    │       ├── CustomerUpdateRequest.java                 ──► Unified update request wrapper
    │       └── UpdateResponseDto.java                     ──► Update response with verification details
    │
    ├── service/                                           ──► [Core Business Logic Layer]
    │   ├── CdhIngestionService.java                       ──► Fetches CDH record & hydrates tb_cm_* tables
    │   ├── CustomerProfileService.java                    ──► Aggregates data from tb_cm_* into Profile DTOs
    │   ├── RecordLockService.java                         ──► Lock acquisition, timeout check, release lock
    │   ├── AuditTrailService.java                         ──► Compares before/after JSON & logs to audit table
    │   ├── WorkflowEngineService.java                     ──► Evaluates Approval Matrix (STP vs Maker-Checker)
    │   │
    │   └── handler/                                       ──► [Strategy / Handler Pattern for Updates]
    │       ├── UpdateHandler.java                         ──► Base Interface for all update handlers
    │       ├── UpdateHandlerRegistry.java                 ──► Handler registry & dispatcher
    │       ├── PersonalDetailsUpdateHandler.java          ──► Updates Name, DOB, Gender, Marital status
    │       ├── KycDataUpdateHandler.java                  ──► Updates Aadhaar/PAN/CKYC, triggers OCR/Dedupe
    │       ├── AddressUpdateHandler.java                  ──► Updates 3-line address, GPS coordinates
    │       ├── BankDetailsUpdateHandler.java              ──► Updates Bank A/C, triggers Penny Drop
    │       ├── FamilyMemberUpdateHandler.java             ──► Updates Spouse, Nominee (with Bank JSON)
    │       └── SubmitWorkflowUpdateHandler.java           ──► Finalizes maker submission & transitions stage
    │
    ├── client/                                            ──► [External Microservices Gateway Clients]
    │   ├── KycServiceClient.java                          ──► Calls Penny Drop, Dedupe, OCR, e-KYC
    │   ├── DmsServiceClient.java                          ──► Uploads & retrieves photos/documents from DMS
    │   └── CbsSyncClient.java                             ──► Pushes approved data to T24 Core Banking
    │
    └── rest/                                              ──► [REST API Controller Layer]
        ├── CustomerSearchRestController.java              ──► POST `/api/v1/cm/search`
        ├── CustomerProfileRestController.java             ──► GET  `/api/v1/cm/profile/{customerId}`
        ├── CustomerUpdateRestController.java              ──► POST `/api/v1/cm/update/{section}`
        ├── RecordLockRestController.java                  ──► POST `/api/v1/cm/lock/acquire` & `/release`
        └── WorkflowRestController.java                    ──► POST `/api/v1/cm/workflow/transition`
```

---

## 3. Database Schema Overview (`tb_cm_*`)

The microservice manages **11 relational tables** configured in PostgreSQL:

| Table Name | Purpose | Primary Key |
| :--- | :--- | :--- |
| **`tb_cm_customer`** | Master customer record, live photo status, KYC, AML, BRE, bank & KYC JSONs | `customer_id` |
| **`tb_cm_address`** | Normalized customer addresses (Permanent `'P'` and Communication `'C'`) | `address_id` |
| **`tb_cm_family_member`** | Family members, spouses, nominees (with bank JSON), earning members | `family_mem_id` |
| **`tb_cm_application_master`**| Main application record tracking branch, kendra, group, and loan eligibility | `application_id` |
| **`tb_cm_document`** | Document uploads, KYC verification, OCR results, clarity scores, DMS IDs | `docu_id` |
| **`tb_cm_record_lock`** | Concurrency management and pessimistic record locking | `lock_id` |
| **`tb_cm_cust_audit_trail`** | Field-level modification history and JSON diff tracking | `id` |
| **`tb_cm_configurable_data`** | Application-level dynamic key-value / JSON configuration store | `(app_id, type)` |
| **`tb_ob_workflow_master`** | Master configuration for workflow states | `(app_id, workflow_id)` |
| **`tb_ob_workflow_definition`**| Defines state machine transitions, actions, roles, and routing rules | `(app_id, workflow_id, from_stage_id, action)` |
| **`tb_cm_appln_workflow`** | Application movement history per version with stage, role, and remarks | `(app_id, application_id, version_no, workflow_seq_no)` |

---

## 4. CDH (`gk_unified_data`) Ingestion & Field Mapping

When a user searches for a customer:
1. `CustomerProfileService` checks local PostgreSQL `tb_cm_customer`.
2. If not found, `CdhIngestionService` queries CDH MySQL `gk_unified_data` by `CUSTOMERID` / `MOBILE_NUMBER` / `PRIMARYID`.
3. The flat single-row record is normalized and inserted into `tb_cm_customer`, `tb_cm_address`, `tb_cm_family_member`, and `tb_cm_application_master` in a single transaction.

### CDH Additions (34 Columns):
* **Hierarchy**: `BRANCH_ID`, `KENDRA_NAME`, `KM_NAME`, `LEADER_ID`
* **Proximity & Geo**: `HOUSE_LATITUDE`, `HOUSE_LONGITUDE`, `DISTANCE_FROM_BRANCH`
* **3-Line Address**: `PERMANENT_ADDRESS_LINE3`, `COMMUNICATION_ADDRESS_LINE3`
* **Multi-KYC**: `AADHAAR_NUMBER_MASKED`, `AADHAAR_NAME`, `AADHAAR_DOB`, `PAN_NUMBER`, `PAN_NAME`, `PAN_FATHER_NAME`, `PAN_DOB`, `CKYC_ID`, `CKYC_CAPTURED_DATE`
* **Demographics for T24**: `TITLE`, `RELIGION`, `CASTE`, `NATIONALITY`, `EMAIL_ID`, `NO_OF_ADULTS`, `NO_OF_CHILDREN`, `WET_LAND_ACRES`, `DRY_LAND_ACRES`, `SOURCE_OF_INCOME`, `DEVICE_TYPE`, `COMM_LANGUAGE`
* **Family & Nominee**: `SPOUSE_MOBILE_NUMBER`, `SPOUSE_CKYC_ID`, `NOMINEE_BANK_DETAILS`
* **Bank Verification**: `BANK_VERIFICATION_STATUS`, `BANK_VERIFIED_DATE`
* **DMS UUIDs**: `CUST_PHOTO_DOC_ID`, `SPOUSE_PHOTO_DOC_ID`, `ADDRESS_PROOF_DOC_ID`, `PASSBOOK_DOC_ID`

---

## 5. REST API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/cm/search` | Search member by Mobile / Customer ID / Kendra / Name across CDH & local DB |
| `GET` | `/api/v1/cm/profile/{customerId}` | Fetch consolidated 360° profile payload (Cards 1 to 7) |
| `POST` | `/api/v1/cm/update/{section}` | Update section (`PERSONAL_DETAILS`, `ADDRESS`, `BANK_DETAILS`, `KYC_DETAILS`, `FAMILY_DETAILS`, `SUBMIT`) |
| `POST` | `/api/v1/cm/lock/acquire` | Acquire pessimistic record lock (`tb_cm_record_lock`) |
| `POST` | `/api/v1/cm/lock/release` | Release pessimistic record lock |
| `POST` | `/api/v1/cm/workflow/transition` | Execute workflow state transition (STP / Maker-Checker approval) |

---

## 6. How to Build & Run

```bash
# Build the microservice
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run
```
