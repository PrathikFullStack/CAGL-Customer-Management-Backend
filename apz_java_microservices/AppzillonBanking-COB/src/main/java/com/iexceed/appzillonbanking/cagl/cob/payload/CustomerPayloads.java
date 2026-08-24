package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Java 17 model of the CreateCustomer / UpdateCustomer API payloads.
 *
 * Design notes:
 *  - Every model class is annotated @JsonInclude(NON_NULL) so that fields not
 *    relevant to a particular sub-stage are simply omitted from the JSON,
 *    exactly like the source payloads (e.g. sub-stage 1.2 has no
 *    personalAddressDet, sub-stage 1.3 has no familDetails, etc).
 *  - Fields that ARE present but intentionally empty in the sample payloads
 *    (addInfo: {}, questionDetails: {}, pennyRes: {}) are initialised to an
 *    empty LinkedHashMap rather than left null, so they still serialize as {}.
 *  - "oCRData" / "inputData" inside a document are structurally identical but
 *    their key-sets differ per document category (KYC doc vs address doc vs
 *    family doc). Rather than creating one record per category, they are
 *    modelled as an ordered Map<String,Object> — this keeps one reusable
 *    DocumentDetail type instead of ~6 near-duplicate ones.
 *  - Records are used for the strictly fixed, always-fully-populated shapes
 *    (envelope, requestObj, DocTypeStatus). Everything that is optional /
 *    varies by sub-stage is a plain class with a fluent builder, since Java
 *    records can't have optional/absent fields cleanly.
 */
public final class CustomerPayloads {

    private CustomerPayloads() {}

    // =====================================================================
    // Top-level envelope (identical shape for create + every update call)
    // =====================================================================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApiRequest(
            String appId,
            String interfaceName,
            String userId,
            String userRole,
            String appVersion,
            String userName,
            String branchId,
            String remarks,
            RequestObj requestObj
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RequestObj(
            List<ApplicationDtl> applicationdtls,
            String requestType
    ) {}

    // =====================================================================
    // applicationdtls[0]
    // =====================================================================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApplicationDtl {
        public String application_id;
        public String kendra_id;
        public String group_id;
        public String branch_id;
        public String km_name;
        public String lead_id;
        public String versionNum;
        public String stage;
        public String sub_stage;
        public String wfstage;
        public String channel_type;
        public String dmsFolderIdx;
        public String remarks;
        public Map<String, Object> addInfo;
        public CustomerDtls customerDtls;

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private final ApplicationDtl a = new ApplicationDtl();
            public Builder applicationId(String v) { a.application_id = v; return this; }
            public Builder kendraId(String v) { a.kendra_id = v; return this; }
            public Builder groupId(String v) { a.group_id = v; return this; }
            public Builder branchId(String v) { a.branch_id = v; return this; }
            public Builder kmName(String v) { a.km_name = v; return this; }
            public Builder leadId(String v) { a.lead_id = v; return this; }
            public Builder versionNum(String v) { a.versionNum = v; return this; }
            public Builder stage(String v) { a.stage = v; return this; }
            public Builder subStage(String v) { a.sub_stage = v; return this; }
            public Builder wfstage(String v) { a.wfstage = v; return this; }
            public Builder channelType(String v) { a.channel_type = v; return this; }
            public Builder dmsFolderIdx(String v) { a.dmsFolderIdx = v; return this; }
            public Builder remarks(String v) { a.remarks = v; return this; }
            public Builder addInfo(Map<String, Object> v) { a.addInfo = v; return this; }
            public Builder customerDtls(CustomerDtls v) { a.customerDtls = v; return this; }
            public ApplicationDtl build() { return a; }
        }
    }

    // =====================================================================
    // customerDtls (super-set of every sub-stage; unused fields stay null
    // and are dropped from JSON by @JsonInclude(NON_NULL))
    // =====================================================================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomerDtls {
        public String photo_doc_id;
        public String customerName;
        public KycDetails kycDetails;
        public MemberPhoto memberPhoto;
        public MemberKycDetails memberKycDetails;
        public PersonalAddressDet personalAddressDet;
        public FamilDetails familDetails;
        public PayloadInfo payload;
        public IncomDet incomDet;
        public KendraSelectionDetails kendraSelectionDetails;
        public BankDet bankDet;

        @JsonProperty("AdditionalDocuDet")
        public AdditionalDocuDet additionalDocuDet;

        public VerficationDet verficationDet;

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private final CustomerDtls c = new CustomerDtls();
            public Builder photoDocId(String v) { c.photo_doc_id = v; return this; }
            public Builder customerName(String v) { c.customerName = v; return this; }
            public Builder kycDetails(KycDetails v) { c.kycDetails = v; return this; }
            public Builder memberPhoto(MemberPhoto v) { c.memberPhoto = v; return this; }
            public Builder memberKycDetails(MemberKycDetails v) { c.memberKycDetails = v; return this; }
            public Builder personalAddressDet(PersonalAddressDet v) { c.personalAddressDet = v; return this; }
            public Builder familDetails(FamilDetails v) { c.familDetails = v; return this; }
            public Builder payload(PayloadInfo v) { c.payload = v; return this; }
            public Builder incomDet(IncomDet v) { c.incomDet = v; return this; }
            public Builder kendraSelectionDetails(KendraSelectionDetails v) { c.kendraSelectionDetails = v; return this; }
            public Builder bankDet(BankDet v) { c.bankDet = v; return this; }
            public Builder additionalDocuDet(AdditionalDocuDet v) { c.additionalDocuDet = v; return this; }
            public Builder verficationDet(VerficationDet v) { c.verficationDet = v; return this; }
            public CustomerDtls build() { return c; }
        }
    }

    // ---------------------------------------------------------------------
    // kycDetails — union of the "create" kyc fields, the primary-id fields,
    // the address/other fields and the dependent (family) fields added
    // progressively at each sub-stage.
    // ---------------------------------------------------------------------
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class KycDetails {
        public String mobileNum;
        public String altMobileNum;
        public String lang;
        public String gpsLat;
        public String gpsLong;
        public String primaryType;
        public String primaryId;

        @JsonProperty("PA") public String pa;
        @JsonProperty("CA") public String ca;
        public String dob;
        public String name;

        public String depname;
        public String depRelationType;
        public String depDob;
        public String depDocType;
        public String depDocId;

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private final KycDetails k = new KycDetails();
            public Builder mobileNum(String v) { k.mobileNum = v; return this; }
            public Builder altMobileNum(String v) { k.altMobileNum = v; return this; }
            public Builder lang(String v) { k.lang = v; return this; }
            public Builder gpsLat(String v) { k.gpsLat = v; return this; }
            public Builder gpsLong(String v) { k.gpsLong = v; return this; }
            public Builder primaryType(String v) { k.primaryType = v; return this; }
            public Builder primaryId(String v) { k.primaryId = v; return this; }
            public Builder pa(String v) { k.pa = v; return this; }
            public Builder ca(String v) { k.ca = v; return this; }
            public Builder dob(String v) { k.dob = v; return this; }
            public Builder name(String v) { k.name = v; return this; }
            public Builder depname(String v) { k.depname = v; return this; }
            public Builder depRelationType(String v) { k.depRelationType = v; return this; }
            public Builder depDob(String v) { k.depDob = v; return this; }
            public Builder depDocType(String v) { k.depDocType = v; return this; }
            public Builder depDocId(String v) { k.depDocId = v; return this; }
            public KycDetails build() { return k; }
        }
    }

    // ---------------------------------------------------------------------
    // Reusable document primitive. oCRData / inputData vary per document
    // category, so they're kept as ordered maps instead of one record per
    // category (VOTER-ID vs AADHAAR vs PAN vs address vs family, etc).
    // ---------------------------------------------------------------------
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DocumentDetail {
        public String docuId;
        public String category;

        @JsonProperty("SubCat") public String subCat;
        public String kycType;
        public String authMode;

        @JsonProperty("IDtype") public String idType;
        public String legalDocName;
        public String legalDocId;

        @JsonProperty("oCRData") public Map<String, Object> ocrData;
        public Map<String, Object> inputData;

        public String docuNoF;
        public String docuNoB;
        public String photo;
        public Boolean isEdited;
        public String editedBy;
        public List<String> editedFields;
        public String reUploadedBy;
        public String status;
        public String reason;
        public String memRelation; // only used by *PHOTO family docs

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private final DocumentDetail d = new DocumentDetail();
            public Builder docuId(String v) { d.docuId = v; return this; }
            public Builder category(String v) { d.category = v; return this; }
            public Builder subCat(String v) { d.subCat = v; return this; }
            public Builder kycType(String v) { d.kycType = v; return this; }
            public Builder authMode(String v) { d.authMode = v; return this; }
            public Builder idType(String v) { d.idType = v; return this; }
            public Builder legalDocName(String v) { d.legalDocName = v; return this; }
            public Builder legalDocId(String v) { d.legalDocId = v; return this; }
            public Builder ocrData(Map<String, Object> v) { d.ocrData = v; return this; }
            public Builder inputData(Map<String, Object> v) { d.inputData = v; return this; }
            public Builder docuNoF(String v) { d.docuNoF = v; return this; }
            public Builder docuNoB(String v) { d.docuNoB = v; return this; }
            public Builder photo(String v) { d.photo = v; return this; }
            public Builder isEdited(Boolean v) { d.isEdited = v; return this; }
            public Builder editedBy(String v) { d.editedBy = v; return this; }
            public Builder editedFields(List<String> v) { d.editedFields = v; return this; }
            public Builder reUploadedBy(String v) { d.reUploadedBy = v; return this; }
            public Builder status(String v) { d.status = v; return this; }
            public Builder reason(String v) { d.reason = v; return this; }
            public Builder memRelation(String v) { d.memRelation = v; return this; }
            public DocumentDetail build() { return d; }
        }
    }

    /** Wrapper used where the JSON shape is {"documentDetails": {...}} inside a list. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DocumentWrapper(DocumentDetail documentDetails) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MemberPhoto {
        public String status;
        public DocumentDetail documentDetails;

        public MemberPhoto() {}
        public MemberPhoto(String status, DocumentDetail documentDetails) {
            this.status = status;
            this.documentDetails = documentDetails;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MemberKycDetails {
        public String status;
        public List<DocumentWrapper> documentList;

        public MemberKycDetails() {}
        public MemberKycDetails(String status, List<DocumentWrapper> documentList) {
            this.status = status;
            this.documentList = documentList;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OtherDetails {
        public String religion;
        public String business;
        public String caste;
        public String srcIncome;
        public String nationality;

        public OtherDetails() {}
        public OtherDetails(String religion, String business, String caste, String srcIncome, String nationality) {
            this.religion = religion;
            this.business = business;
            this.caste = caste;
            this.srcIncome = srcIncome;
            this.nationality = nationality;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PersonalAddressDet {
        public String nameSelected;
        public String dobSelected;

        @JsonProperty("PA") public String pa;
        @JsonProperty("CA") public String ca;
        public String status;
        public OtherDetails otherDetails;
        public List<DocumentDetail> documentDetails;

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private final PersonalAddressDet p = new PersonalAddressDet();
            public Builder nameSelected(String v) { p.nameSelected = v; return this; }
            public Builder dobSelected(String v) { p.dobSelected = v; return this; }
            public Builder pa(String v) { p.pa = v; return this; }
            public Builder ca(String v) { p.ca = v; return this; }
            public Builder status(String v) { p.status = v; return this; }
            public Builder otherDetails(OtherDetails v) { p.otherDetails = v; return this; }
            public Builder documentDetails(List<DocumentDetail> v) { p.documentDetails = v; return this; }
            public PersonalAddressDet build() { return p; }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FamilyMember {
        @JsonProperty("MemberType") public String memberType;
        @JsonProperty("RelationType") public String relationType;
        public Boolean isEarning;
        public Boolean isNominee;
        public String mobileNum;
        public List<DocumentWrapper> documentList;

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private final FamilyMember m = new FamilyMember();
            public Builder memberType(String v) { m.memberType = v; return this; }
            public Builder relationType(String v) { m.relationType = v; return this; }
            public Builder isEarning(Boolean v) { m.isEarning = v; return this; }
            public Builder isNominee(Boolean v) { m.isNominee = v; return this; }
            public Builder mobileNum(String v) { m.mobileNum = v; return this; }
            public Builder documentList(List<DocumentWrapper> v) { m.documentList = v; return this; }
            public FamilyMember build() { return m; }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FamilDetails {
        public String status;
        public List<FamilyMember> memberList;

        public FamilDetails() {}
        public FamilDetails(String status, List<FamilyMember> memberList) {
            this.status = status;
            this.memberList = memberList;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PayloadInfo {
        public Boolean questioncaptured;
        public String income;
        public String expense;
        public String bankAccNo;
        public String bankAccName;
        public String bankBranchName;
        public String bankName;
        public String bankIfscCode;

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private final PayloadInfo p = new PayloadInfo();
            public Builder questioncaptured(Boolean v) { p.questioncaptured = v; return this; }
            public Builder income(String v) { p.income = v; return this; }
            public Builder expense(String v) { p.expense = v; return this; }
            public Builder bankAccNo(String v) { p.bankAccNo = v; return this; }
            public Builder bankAccName(String v) { p.bankAccName = v; return this; }
            public Builder bankBranchName(String v) { p.bankBranchName = v; return this; }
            public Builder bankName(String v) { p.bankName = v; return this; }
            public Builder bankIfscCode(String v) { p.bankIfscCode = v; return this; }
            public PayloadInfo build() { return p; }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IncomDet {
        public String status;
        public Boolean questioncaptured;
        public String income;
        public String expense;
        public Map<String, Object> questionDetails;
        public List<DocumentDetail> documentDetails;

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private final IncomDet i = new IncomDet();
            public Builder status(String v) { i.status = v; return this; }
            public Builder questioncaptured(Boolean v) { i.questioncaptured = v; return this; }
            public Builder income(String v) { i.income = v; return this; }
            public Builder expense(String v) { i.expense = v; return this; }
            public Builder questionDetails(Map<String, Object> v) { i.questionDetails = v; return this; }
            public Builder documentDetails(List<DocumentDetail> v) { i.documentDetails = v; return this; }
            public IncomDet build() { return i; }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class KendraSelectionDetails {
        public String group_id;
        public String kendra_id;
        public String kendra_name;
        public String distancefromKendra;

        public KendraSelectionDetails() {}
        public KendraSelectionDetails(String group_id, String kendra_id, String kendra_name, String distancefromKendra) {
            this.group_id = group_id;
            this.kendra_id = kendra_id;
            this.kendra_name = kendra_name;
            this.distancefromKendra = distancefromKendra;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BankDet {
        public String bankAccNo;
        public String bankAccName;
        public String bankBranchName;
        public String bankName;
        public String bankIfscCode;
        public String status;
        public Map<String, Object> pennyRes;
        public List<DocumentDetail> documentDetails;

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private final BankDet b = new BankDet();
            public Builder bankAccNo(String v) { b.bankAccNo = v; return this; }
            public Builder bankAccName(String v) { b.bankAccName = v; return this; }
            public Builder bankBranchName(String v) { b.bankBranchName = v; return this; }
            public Builder bankName(String v) { b.bankName = v; return this; }
            public Builder bankIfscCode(String v) { b.bankIfscCode = v; return this; }
            public Builder status(String v) { b.status = v; return this; }
            public Builder pennyRes(Map<String, Object> v) { b.pennyRes = v; return this; }
            public Builder documentDetails(List<DocumentDetail> v) { b.documentDetails = v; return this; }
            public BankDet build() { return b; }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdditionalDocuDet {
        public List<DocumentDetail> documentDetails;

        public AdditionalDocuDet() {}
        public AdditionalDocuDet(List<DocumentDetail> documentDetails) {
            this.documentDetails = documentDetails;
        }
    }

    // ---------------------------------------------------------------------
    // verficationDet: keys s1..s6 are dynamic (one added per completed
    // sub-stage) so it's modelled as an ordered map rather than fixed fields.
    // ---------------------------------------------------------------------
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DocTypeStatus(String type, String status) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StageVerification(
            @JsonProperty("DocList") List<DocTypeStatus> docList,
            String status
    ) {}

    /** Ordered map: "s1" -> {...}, "s2" -> {...}, etc. */
    public static class VerficationDet extends LinkedHashMap<String, StageVerification> {
        public VerficationDet put_(String key, StageVerification value) {
            this.put(key, value);
            return this;
        }
    }

    // =====================================================================
    // Factory: builds every payload from the source document, stage by stage
    // =====================================================================

    private static final String APP_ID = "APZCBO";
    private static final String USER_ID = "GK34577";
    private static final String USER_ROLE = "KM";
    private static final String APP_VERSION = "1.20.11";
    private static final String USER_NAME = "Prabha";
    private static final String BRANCH_ID = "IN12637127";
    private static final String APPLICATION_ID = "CO1928939192398129";
    private static final String CHANNEL_TYPE = "YELLOW";
    private static final String WFSTAGE = "DRAFT";

    private static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    /** POST /createApp — CreateCustomer */
    public static ApiRequest buildCreatePayload() {
        KycDetails kyc = KycDetails.builder()
                .mobileNum("9677779631")
                .altMobileNum("9972497003")
                .lang("kan")
                .gpsLat("12.9345")
                .gpsLong("77.6215")
                .build();

        CustomerDtls customerDtls = CustomerDtls.builder()
                .kycDetails(kyc)
                .build();

        ApplicationDtl app = ApplicationDtl.builder()
                .applicationId("")
                .kendraId("123123123")
                .groupId("12312312232")
                .branchId(BRANCH_ID)
                .kmName(USER_NAME)
                .leadId("12312312")
                .remarks("New")
                .customerDtls(customerDtls)
                .build();

        return new ApiRequest(
                APP_ID, "CreateCustomer", USER_ID, USER_ROLE, APP_VERSION, USER_NAME, BRANCH_ID,
                "customer created by " + USER_ID,
                new RequestObj(List.of(app), "create")
        );
    }

    /** POST /updateApp — sub_stage 1.2: Member (self) KYC details */
    public static ApiRequest buildSubStage1_2() {
        KycDetails kyc = KycDetails.builder()
                .primaryType("VOTER-ID").primaryId("7942394921")
                .mobileNum("9677779631").altMobileNum("9972497003")
                .lang("kan").gpsLat("12.9345").gpsLong("77.6215")
                .build();

        MemberPhoto memberPhoto = new MemberPhoto("captured",
                DocumentDetail.builder()
                        .docuId("1").category("MEMBERIMG").subCat("LIVEPHOTO").idType("IMG")
                        .inputData(map("livePhotoScore", "23", "score", ""))
                        .photo("12345566").reUploadedBy("").status("captured").reason("")
                        .build());

        DocumentDetail voterDoc = DocumentDetail.builder()
                .docuId("2").category("MEMBERKYC").subCat("MDVV").kycType("NON-KYC").authMode("OCR").idType("IMG")
                .legalDocName("VOTER-ID").legalDocId("7942394921")
                .ocrData(map("name", "GEETA SAMBREKAR", "dob", "01/01/1976", "gender", "FEMALE",
                        "addLine1", "Plot No 51 Sadashiv Nagar", "addLine2", "", "state", 17,
                        "district", "BELGAUM", "villageLocality", "Belgaum", "pincode", "590001", "taluk", ""))
                .inputData(map("name", "GEETA SAMBREKAR", "dob", "01/01/1976", "gender", "FEMALE",
                        "addLine1", "Plot No 51 Sadashiv Nagar", "addLine2", "", "state", 17,
                        "district", "BELGAUM", "villageLocality", "Belgaum", "pincode", "590001", "taluk", ""))
                .docuNoF("123231210823").docuNoB("123143429822").photo("")
                .isEdited(false).editedBy("").editedFields(List.of()).reUploadedBy("")
                .status("captured").reason("")
                .build();

        DocumentDetail aadhaarDoc = DocumentDetail.builder()
                .docuId("3").category("MEMBERKYC").subCat("MDVA").kycType("KYC").authMode("OTP").idType("IMG")
                .legalDocName("AADHAAR").legalDocId("693480513786")
                .ocrData(map("name", "GEETA SAMBRE", "dob", "02/01/1976", "gender", "FEMALE",
                        "addLine1", "Plot No 51 Sadashiv Nagar", "addLine2", "", "state", 17,
                        "district", "BELGAUM", "villageLocality", "Belgaum", "pincode", "590001", "taluk", ""))
                .inputData(map("name", "GEETA SAMBREKAR", "dob", "01/01/1976", "gender", "FEMALE",
                        "addLine1", "Plot No 51 Sadashiv Nagar", "addLine2", "", "state", 17,
                        "district", "BELGAUM", "villageLocality", "Belgaum", "pincode", "590001", "taluk", ""))
                .docuNoF("165164786211").docuNoB("165164678222").photo("")
                .isEdited(true).editedBy("GK121").editedFields(List.of("dob", "name")).reUploadedBy("")
                .status("edited").reason("")
                .build();

        DocumentDetail panDoc = DocumentDetail.builder()
                .docuId("4").category("MEMBERKYC").subCat("MDVP").kycType("NON-KYC").authMode("").idType("IMG")
                .legalDocName("PAN CARD").legalDocId("ABCDE1234F")
                .ocrData(map("name", "GEETA SAMBREKAR", "dob", "01/01/1976", "gender", "FEMALE"))
                .inputData(map("name", "GEETA SAMBREKAR", "dob", "01/01/1976", "gender", "FEMALE"))
                .docuNoF("16516462588").docuNoB("16516465299").photo("")
                .isEdited(false).editedBy("").editedFields(List.of()).reUploadedBy("")
                .status("captured").reason("")
                .build();

        MemberKycDetails memberKycDetails = new MemberKycDetails("captured", List.of(
                new DocumentWrapper(voterDoc), new DocumentWrapper(aadhaarDoc), new DocumentWrapper(panDoc)
        ));

        VerficationDet verf = new VerficationDet()
                .put_("s1", new StageVerification(List.of(
                        new DocTypeStatus("MDVV", "captured"),
                        new DocTypeStatus("MDVA", "captured"),
                        new DocTypeStatus("MDVP", "captured")), "captured"));

        CustomerDtls customerDtls = CustomerDtls.builder()
                .photoDocId("12345566")
                .kycDetails(kyc)
                .memberPhoto(memberPhoto)
                .memberKycDetails(memberKycDetails)
                .verficationDet(verf)
                .build();

        ApplicationDtl app = ApplicationDtl.builder()
                .applicationId(APPLICATION_ID).versionNum("2").stage("1").subStage("1.2").wfstage(WFSTAGE)
                .kmName(USER_NAME).channelType(CHANNEL_TYPE).dmsFolderIdx("1213")
                .remarks("Added member Kyc Details").addInfo(map())
                .customerDtls(customerDtls)
                .build();

        return updateEnvelope("Member details added by " + USER_ID, app);
    }

    /** POST /updateApp — sub_stage 1.3: Address details */
    public static ApiRequest buildSubStage1_3() {
        KycDetails kyc = KycDetails.builder()
                .pa("ADDP").ca("ADDC").dob("01/01/1976").name("GEETA SAMBRE")
                .primaryType("VOTER-ID").primaryId("7942394921")
                .mobileNum("9677779631").altMobileNum("9972497003")
                .lang("kan").gpsLat("12.9345").gpsLong("77.6215")
                .build();

        Map<String, Object> addrOcr = map("addLine1", "Plot No 51 Sadashiv Nagar", "addLine2", "", "state", 17,
                "district", "BELGAUM", "villageLocality", "Belgaum", "pincode", "590001", "taluk", "");

        DocumentDetail addp = DocumentDetail.builder()
                .docuId("5").category("ADDRESS").subCat("ADDP").idType("IMG").docuNoF("").docuNoB("")
                .ocrData(addrOcr).inputData(addrOcr).photo("23479327293423")
                .isEdited(false).editedBy("").editedFields(List.of()).reUploadedBy("")
                .status("captured").reason("")
                .build();

        DocumentDetail addc = DocumentDetail.builder()
                .docuId("6").category("ADDRESS").subCat("ADDC").idType("IMG").docuNoF("").docuNoB("")
                .ocrData(addrOcr).inputData(addrOcr).photo("234798920023234")
                .isEdited(false).editedBy("").editedFields(List.of()).reUploadedBy("")
                .status("captured").reason("")
                .build();

        PersonalAddressDet personalAddressDet = PersonalAddressDet.builder()
                .nameSelected("MDVA").dobSelected("MDVA").pa("Others-1").ca("Others-2").status("captured")
                .otherDetails(new OtherDetails("Hindu", "IT", "OC", "ASASDAD", "Indian"))
                .documentDetails(List.of(addp, addc))
                .build();

        VerficationDet verf = new VerficationDet()
                .put_("s1", new StageVerification(List.of(
                        new DocTypeStatus("MDVV", "captured"),
                        new DocTypeStatus("MDVA", "captured"),
                        new DocTypeStatus("MDVP", "captured")), "captured"))
                .put_("s2", new StageVerification(List.of(
                        new DocTypeStatus("ADDP", "captured"),
                        new DocTypeStatus("ADDC", "captured")), "captured"));

        CustomerDtls customerDtls = CustomerDtls.builder()
                .customerName("GEETA SAMBRE")
                .kycDetails(kyc)
                .personalAddressDet(personalAddressDet)
                .verficationDet(verf)
                .build();

        ApplicationDtl app = ApplicationDtl.builder()
                .applicationId(APPLICATION_ID).versionNum("3").stage("1").subStage("1.3").wfstage(WFSTAGE)
                .kmName(USER_NAME).channelType(CHANNEL_TYPE)
                .remarks("Added Address Details").addInfo(map())
                .customerDtls(customerDtls)
                .build();

        return updateEnvelope("Address details added by " + USER_ID, app);
    }

    /** POST /updateApp — sub_stage 1.4: Family details */
    public static ApiRequest buildSubStage1_4() {
        KycDetails kyc = KycDetails.builder()
                .depname("RAVI").depRelationType("SPOUSE").depDob("01/01/1970")
                .depDocType("VOTER-ID").depDocId("7942394922")
                .pa("ADDP").ca("ADDC").dob("01/01/1976").name("GEETA SAMBRE")
                .primaryType("VOTER-ID").primaryId("7942394921")
                .mobileNum("9677779631").altMobileNum("9972497003")
                .lang("kan").gpsLat("12.9345").gpsLong("77.6215")
                .build();

        Map<String, Object> spouseAddr = map("addLine1", "Plot No 51 Sadashiv Nagar", "addLine2", "", "state", 17,
                "district", "BELGAUM", "villageLocality", "Belgaum", "pincode", "590001", "taluk", "");

        DocumentDetail fd1v = DocumentDetail.builder()
                .docuId("7").category("FAMILY").subCat("FD1V").kycType("NON-KYC").authMode("OCR").idType("IMG")
                .legalDocName("VOTER-ID").legalDocId("7942394922")
                .ocrData(mergeName(spouseAddr, "RAJA", "01/01/1974", "MALE"))
                .inputData(mergeName(spouseAddr, "RAJA", "01/01/1974", "MALE"))
                .docuNoF("123223290903").docuNoB("123242143400").photo("")
                .isEdited(false).editedBy("").editedFields(List.of()).reUploadedBy("")
                .status("captured").reason("")
                .build();

        DocumentDetail fd1a = DocumentDetail.builder()
                .docuId("8").category("FAMILY").subCat("FD1A").kycType("KYC").authMode("OTP").idType("IMG")
                .legalDocName("AADHAAR").legalDocId("693480513785")
                .ocrData(mergeName(spouseAddr, "RAJA", "01/01/1974", "MALE"))
                .inputData(mergeName(spouseAddr, "RAJA", "01/01/1974", "MALE"))
                .docuNoF("165164574662").docuNoB("165164456462").photo("")
                .isEdited(false).editedBy("").editedFields(List.of()).reUploadedBy("")
                .status("captured").reason("")
                .build();

        DocumentDetail fd1Photo = DocumentDetail.builder()
                .docuId("9").category("FAMILY").subCat("FD1PHOTO").idType("IMG").memRelation("")
                .photo("023704023047237").status("captured").reason("")
                .build();

        FamilyMember spouse = FamilyMember.builder()
                .memberType("P").relationType("Spouse").isEarning(true).isNominee(true)
                .mobileNum("9677479631")
                .documentList(List.of(new DocumentWrapper(fd1v), new DocumentWrapper(fd1a), new DocumentWrapper(fd1Photo)))
                .build();

        DocumentDetail fd2v = DocumentDetail.builder()
                .docuId("10").category("FAMILY").subCat("FD2V").kycType("NON-KYC").authMode("OCR").idType("IMG")
                .legalDocName("VOTER-ID").legalDocId("794239423923")
                .ocrData(mergeName(spouseAddr, "RAVI", "01/01/2006", "MALE"))
                .inputData(mergeName(spouseAddr, "RAVI", "01/01/2006", "MALE"))
                .docuNoF("1232387822323").docuNoB("1230920301434").photo("")
                .isEdited(false).editedBy("").editedFields(List.of()).reUploadedBy("")
                .status("captured").reason("")
                .build();

        FamilyMember son = FamilyMember.builder()
                .memberType("S").relationType("Son").isEarning(true).isNominee(true)
                .mobileNum("9688779631")
                .documentList(List.of(new DocumentWrapper(fd2v)))
                .build();

        FamilDetails familDetails = new FamilDetails("captured", List.of(spouse, son));

        VerficationDet verf = new VerficationDet()
                .put_("s1", new StageVerification(List.of(
                        new DocTypeStatus("MDVV", "captured"),
                        new DocTypeStatus("MDVA", "captured"),
                        new DocTypeStatus("MDVP", "captured")), "captured"))
                .put_("s2", new StageVerification(List.of(
                        new DocTypeStatus("ADDP", "captured"),
                        new DocTypeStatus("ADDC", "captured")), "captured"))
                .put_("s3", new StageVerification(List.of(
                        new DocTypeStatus("FD1V", "captured"),
                        new DocTypeStatus("FD1A", "captured"),
                        new DocTypeStatus("FD2V", "captured")), "captured"));

        CustomerDtls customerDtls = CustomerDtls.builder()
                .customerName("GEETA SAMBRE")
                .kycDetails(kyc)
                .familDetails(familDetails)
                .verficationDet(verf)
                .build();

        ApplicationDtl app = ApplicationDtl.builder()
                .applicationId(APPLICATION_ID).versionNum("4").stage("1").subStage("1.4").wfstage(WFSTAGE)
                .kmName(USER_NAME).channelType(CHANNEL_TYPE)
                .remarks("Added Family Details").addInfo(map())
                .customerDtls(customerDtls)
                .build();

        return updateEnvelope("Family details added by " + USER_ID, app);
    }

    /** POST /updateApp — sub_stage 1.5: Income details */
    public static ApiRequest buildSubStage1_5() {
        DocumentDetail incomeDoc = DocumentDetail.builder()
                .docuId("11").category("INCOMEDOC").subCat("DOC").idType("IMG")
                .photo("234797543423234").reUploadedBy("").status("captured").reason("")
                .build();

        IncomDet incomDet = IncomDet.builder()
                .status("captured").questioncaptured(true).income("300000").expense("120000")
                .questionDetails(map())
                .documentDetails(List.of(incomeDoc))
                .build();

        VerficationDet verf = verfThroughS3().put_("s4",
                new StageVerification(List.of(new DocTypeStatus("INCOMEDOC", "captured")), "captured"));

        CustomerDtls customerDtls = CustomerDtls.builder()
                .payload(PayloadInfo.builder().questioncaptured(false).income("300000").expense("120000").build())
                .incomDet(incomDet)
                .verficationDet(verf)
                .build();

        ApplicationDtl app = ApplicationDtl.builder()
                .applicationId(APPLICATION_ID).versionNum("5").stage("1").subStage("1.5").wfstage(WFSTAGE)
                .kmName(USER_NAME).channelType(CHANNEL_TYPE)
                .remarks("Added Income Details").addInfo(map())
                .customerDtls(customerDtls)
                .build();

        return updateEnvelope("Income details added by " + USER_ID, app);
    }

    /** POST /updateApp — sub_stage 1.6: Kendra selection (no verficationDet in source sample) */
    public static ApiRequest buildSubStage1_6() {
        CustomerDtls customerDtls = CustomerDtls.builder()
                .payload(PayloadInfo.builder().questioncaptured(false).income("300000").expense("120000").build())
                .kendraSelectionDetails(new KendraSelectionDetails("12736172", "12735712", "Madiwala", "100"))
                .build();

        ApplicationDtl app = ApplicationDtl.builder()
                .applicationId(APPLICATION_ID).versionNum("6").stage("1").subStage("1.6").wfstage(WFSTAGE)
                .kmName(USER_NAME).channelType(CHANNEL_TYPE)
                .remarks("Added Kendra selection").addInfo(map())
                .customerDtls(customerDtls)
                .build();

        return updateEnvelope("Kendra selection updated by " + USER_ID, app);
    }

    /** POST /updateApp — sub_stage 1.7: Bank details */
    public static ApiRequest buildSubStage1_7() {
        DocumentDetail bankDoc = DocumentDetail.builder()
                .docuId("12").category("BANKDOC").subCat("DOC").idType("IMG")
                .photo("234797293643234").reUploadedBy("").status("captured").reason("")
                .build();

        BankDet bankDet = BankDet.builder()
                .bankAccNo("009310100115266").bankAccName("Salma Sultana").bankBranchName("CHAMRAJPET")
                .bankName("ANDHRA BANK").bankIfscCode("UBIN0800937").status("captured")
                .pennyRes(map())
                .documentDetails(List.of(bankDoc))
                .build();

        VerficationDet verf = verfThroughS4().put_("s5",
                new StageVerification(List.of(new DocTypeStatus("BANKDOC", "captured")), "captured"));

        CustomerDtls customerDtls = CustomerDtls.builder()
                .payload(PayloadInfo.builder()
                        .questioncaptured(false).income("300000").expense("120000")
                        .bankAccNo("009310100115266").bankAccName("Salma Sultana").bankBranchName("CHAMRAJPET")
                        .bankName("ANDHRA BANK").bankIfscCode("UBIN0800937")
                        .build())
                .bankDet(bankDet)
                .verficationDet(verf)
                .build();

        ApplicationDtl app = ApplicationDtl.builder()
                .applicationId(APPLICATION_ID).versionNum("7").stage("1").subStage("1.7").wfstage(WFSTAGE)
                .kmName(USER_NAME).channelType(CHANNEL_TYPE)
                .remarks("Added Bank Details").addInfo(map())
                .customerDtls(customerDtls)
                .build();

        return updateEnvelope("Bank details added by " + USER_ID, app);
    }

    /** POST /updateApp — sub_stage 1.8: Additional documents (home / business / other) */
    public static ApiRequest buildSubStage1_8() {
        DocumentDetail home1 = DocumentDetail.builder()
                .docuId("13").category("HOMEPHOTO").subCat("HOMEIMG1").idType("IMG")
                .photo("234744293423234").reUploadedBy("").status("captured").reason("")
                .build();
        DocumentDetail home2 = DocumentDetail.builder()
                .docuId("14").category("HOMEPHOTO").subCat("HOMEIMG2").idType("IMG")
                .photo("234797673423234").reUploadedBy("").status("captured").reason("")
                .build();
        DocumentDetail business1 = DocumentDetail.builder()
                .docuId("15").category("BUSINESSPHOTO").subCat("BUSINESSIMG1").idType("IMG")
                .photo("234797293423444").reUploadedBy("").status("captured").reason("")
                .build();
        DocumentDetail other1 = DocumentDetail.builder()
                .docuId("16").category("OTHER").subCat("OTHERDOCU1").idType("PDF")
                .docuNoF("234797283423234").docuNoB("").photo("").reUploadedBy("").status("captured").reason("")
                .build();

        AdditionalDocuDet additionalDocuDet = new AdditionalDocuDet(List.of(home1, home2, business1, other1));

        VerficationDet verf = verfThroughS5().put_("s6",
                new StageVerification(List.of(
                        new DocTypeStatus("HOMEIMG1", "captured"),
                        new DocTypeStatus("HOMEIMG2", "captured"),
                        new DocTypeStatus("BUSINESSIMG1", "captured"),
                        new DocTypeStatus("OTHERDOCU1", "captured")), "captured"));

        CustomerDtls customerDtls = CustomerDtls.builder()
                .payload(PayloadInfo.builder()
                        .questioncaptured(false).income("300000").expense("120000")
                        .bankAccNo("009310100115266").bankAccName("Salma Sultana").bankBranchName("CHAMRAJPET")
                        .bankName("ANDHRA BANK").bankIfscCode("UBIN0800937")
                        .build())
                .additionalDocuDet(additionalDocuDet)
                .verficationDet(verf)
                .build();

        ApplicationDtl app = ApplicationDtl.builder()
                .applicationId(APPLICATION_ID).versionNum("8").stage("1").subStage("1.8").wfstage(WFSTAGE)
                .kmName(USER_NAME).channelType(CHANNEL_TYPE)
                .remarks("Added AddDocu Details").addInfo(map())
                .customerDtls(customerDtls)
                .build();

        return updateEnvelope("AddDocu details added by " + USER_ID, app);
    }

    // ---- small helpers -----------------------------------------------

    private static ApiRequest updateEnvelope(String remarks, ApplicationDtl app) {
        return new ApiRequest(
                APP_ID, "UpdateCustomer", USER_ID, USER_ROLE, APP_VERSION, USER_NAME, BRANCH_ID,
                remarks,
                new RequestObj(List.of(app), "add")
        );
    }

    private static Map<String, Object> mergeName(Map<String, Object> addr, String name, String dob, String gender) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("dob", dob);
        m.put("gender", gender);
        m.putAll(addr);
        return m;
    }

    private static VerficationDet verfThroughS3() {
        return new VerficationDet()
                .put_("s1", new StageVerification(List.of(
                        new DocTypeStatus("MDVV", "captured"), new DocTypeStatus("MDVA", "captured"),
                        new DocTypeStatus("MDVP", "captured")), "captured"))
                .put_("s2", new StageVerification(List.of(
                        new DocTypeStatus("ADDP", "captured"), new DocTypeStatus("ADDC", "captured")), "captured"))
                .put_("s3", new StageVerification(List.of(
                        new DocTypeStatus("FD1V", "captured"), new DocTypeStatus("FD1A", "captured"),
                        new DocTypeStatus("FD2V", "captured")), "captured"));
    }

    private static VerficationDet verfThroughS4() {
        return verfThroughS3().put_("s4",
                new StageVerification(List.of(new DocTypeStatus("INCOMEDOC", "captured")), "captured"));
    }

    private static VerficationDet verfThroughS5() {
        return verfThroughS4().put_("s5",
                new StageVerification(List.of(new DocTypeStatus("BANKDOC", "captured")), "captured"));
    }

    // =====================================================================
    // Demo entry point
    // =====================================================================

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        List<Map.Entry<String, ApiRequest>> payloads = new ArrayList<>();
        payloads.add(Map.entry("createApp", buildCreatePayload()));
        payloads.add(Map.entry("updateApp (sub_stage 1.2)", buildSubStage1_2()));
        payloads.add(Map.entry("updateApp (sub_stage 1.3)", buildSubStage1_3()));
        payloads.add(Map.entry("updateApp (sub_stage 1.4)", buildSubStage1_4()));
        payloads.add(Map.entry("updateApp (sub_stage 1.5)", buildSubStage1_5()));
        payloads.add(Map.entry("updateApp (sub_stage 1.6)", buildSubStage1_6()));
        payloads.add(Map.entry("updateApp (sub_stage 1.7)", buildSubStage1_7()));
        payloads.add(Map.entry("updateApp (sub_stage 1.8)", buildSubStage1_8()));

        for (var entry : payloads) {
            System.out.println("=== " + entry.getKey() + " ===");
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(entry.getValue()));
            System.out.println();
        }
    }
}
