package com.iexceed.appzillonbanking.cagl.cob.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplnWorkflow;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.*;
import com.iexceed.appzillonbanking.cagl.cob.enums.DocumentCategory;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonInliningUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Pure mapping logic from JPA entities to the nested API 9 response contract.
 * Kept side-effect free and unit-testable in isolation from the service layer.
 *
 * All response DTOs are now immutable records (see payload package). Builder
 * call sites below are unchanged from the class-based version - Lombok's
 * @Builder generates the same fluent builder API on records.
 */
@Component
public class ApplicationDetailsMapper {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public final JsonInliningUtil jsonInliningUtil = new JsonInliningUtil(objectMapper);

    public ApplicationDetailsResponse toResponse(TbObApplicationMaster app,
                                                 TbObCustomer customer,
                                                 TbObCustOthers custOthers,
                                                 List<TbObAddress> addresses,
                                                 List<TbObFamilyMember> familyMembers,
                                                 List<TbObDocument> documents,
                                                 List<TbObOtherDocument> otherDocuments,
                                                 List<TbObLoan> loans,
                                                 AdditionalApplicationData applicationData) {

        List<DocumentEntryDto> allDocEntries = documents.stream()
                .map(this::toDocumentEntryDto)
                .collect(Collectors.toList());

        List<DocumentEntryDto> otherDocEntries = otherDocuments.stream()
                .map(this::toDocumentEntryDto).toList();

        CustomerDetailsDto customerDetails = CustomerDetailsDto.builder()
                .mobileNum(app != null ? app.getMobileNumber() : null)
                .alterMobileNum(app != null ? app.getMobileNumber() : null)
                .customerId(Optional.ofNullable(customer)
                        .map(TbObCustomer::getCustomerId)
                        .map(Object::toString)
                        .orElse(null))
                .photoDocId(Optional.ofNullable(customer)
                        .map(TbObCustomer::getPhotoDocId)
                        .map(Object::toString)
                        .orElse(null))
                .kycDetails(Optional.of(Objects.requireNonNull(customer))
                        .map(TbObCustomer::getKycDetails)
//                        .map(Map::copyOf)
                        .orElse(null))
                .memberPhoto(toMemberPhotoDto(customer, allDocEntries))
                .memberKycDetails(toMemberKycDetailsDto(customer, allDocEntries))
                .personalAddressDetails(toPersonalDetailsDto(customer, addresses, allDocEntries))
                .familyDetails(toFamilyDetailsDto(customer, familyMembers, allDocEntries))
                .incomeDet(toIncomeDetailsDto(custOthers, allDocEntries))
                .bankDet(toBankDetailsDto(customer, allDocEntries))
                .additionalDocuDet(toAdditionalDetailsDto(customer, allDocEntries, otherDocEntries))
                .verificationDet(Optional.of(customer)
                        .map(TbObCustomer::getVerificationDet)
//                        .map(Map::copyOf)
                        .orElse(null))
                .build();

        return ApplicationDetailsResponse.builder()
                .applicationId(app != null ? app.getApplicationId() : null)
                .kendraId(app != null ? app.getKendraId() : null)
                .groupId(app != null ? app.getGroupId() : null)
                .branchId(app != null ? app.getBranchId() : null)
                .stage(app != null ? app.getStage() : null)
                .subStage(app != null ? app.getSubStage() : null)
                .wfStage(app != null ? app.getWfStage() : null)
                .customerId(Optional.ofNullable(app)
                        .map(TbObApplicationMaster::getCustomerId)
                        .map(Object::toString)
                        .orElse(null))
                .customerDetails(customerDetails)
                .breCheck(toBreCheckDto(customer, documents.stream()
                        .filter(document -> document.getSubCat().equalsIgnoreCase("Other-1"))
                        .findFirst().orElse(new TbObDocument()), loans))
                .applicationData(applicationData)
                .addresses(toAddressDetailsDtoList(addresses))
                .build();
    }

    private MemberPhotoDto toMemberPhotoDto(TbObCustomer customer, List<DocumentEntryDto> allDocs) {
        return MemberPhotoDto.builder()
                .documentList(wrapDocsForCategory(allDocs, DocumentCategory.MEMBERIMG))
                .build();
    }

    // ---------------------------------------------------------------------
    // Section builders - each pulls out the documents whose category matches
    // that section, and adds whatever scalar fields belong alongside them.
    // ---------------------------------------------------------------------

    private MemberKycDetailsDto toMemberKycDetailsDto(TbObCustomer customer, List<DocumentEntryDto> allDocs) {
        return MemberKycDetailsDto.builder()
                .documentList(wrapDocsForCategory(allDocs, DocumentCategory.MEMBERKYC))
                .ckycId(Optional.ofNullable(customer.getKycDetails())
                        .map(map -> map.get("ckycId"))
                        .map(Object::toString)
                        .orElse(null))
                .build();
    }

    private PersonalDetailsDto toPersonalDetailsDto(TbObCustomer customer, List<TbObAddress> addresses, List<DocumentEntryDto> allDocs) {
        TbObAddress permanentAddress = addresses.stream()
                .filter(tbObAddress -> tbObAddress.getAddressType().equalsIgnoreCase("P"))
                .findFirst()
                .orElse(new TbObAddress());
        return PersonalDetailsDto.builder()
                .nameSelected(Optional.of(permanentAddress)
                        .map(TbObAddress::getAddrPayload)
                        .map(addr -> addr.get("nameSelected"))
                        .map(Object::toString)
                        .orElse(null))
                .dobSelected(Optional.of(permanentAddress)
                        .map(TbObAddress::getAddrPayload)
                        .map(addr -> addr.get("dobSelected"))
                        .map(Object::toString)
                        .orElse(null))
                .pa(Optional.ofNullable(customer)
                        .map(TbObCustomer::getKycDetails)
                        .map(kyc -> kyc.get("PA"))
                        .map(Object::toString)
                        .orElse(null))
                .ca(Optional.ofNullable(customer)
                        .map(TbObCustomer::getKycDetails)
                        .map(kyc -> kyc.get("CA"))
                        .map(Object::toString)
                        .orElse(null))
                .documentList(wrapDocsForCategory(allDocs, DocumentCategory.ADDRESS))
                .religion(Optional.ofNullable(customer.getPayload())
                        .map(map -> map.get("religion"))
                        .map(Object::toString)
                        .orElse(null))
                .caste(Optional.ofNullable(customer.getPayload())
                        .map(map -> map.get("caste"))
                        .map(Object::toString)
                        .orElse(null))
                .nationality(Optional.ofNullable(customer.getPayload())
                        .map(map -> map.get("nationality"))
                        .map(Object::toString)
                        .orElse(null))
                .email(Optional.ofNullable(customer.getPayload())
                        .map(map -> map.get("email"))
                        .map(Object::toString)
                        .orElse(null))
                .incomeSource(Optional.ofNullable(customer.getPayload())
                        .map(map -> map.get("incomeSource"))
                        .map(Object::toString)
                        .orElse(null))
                .build();
    }

    private FamilyDetailsDto toFamilyDetailsDto(TbObCustomer customer,
                                                List<TbObFamilyMember> familyMembers,
                                                List<DocumentEntryDto> allDocs) {

        List<DocumentDetailsWrapper> familyDocs = wrapDocsForCategory(allDocs, DocumentCategory.FAMILY);

        Map<String, List<DocumentDetailsWrapper>> docsByDocuId = familyDocs.stream()
                .filter(d -> d.documentDetails().mappingId() != null)
                .collect(Collectors.groupingBy(d -> d.documentDetails().mappingId()));

        List<TbObFamilyMember> orderedMembers = familyMembers.stream()
                .sorted(Comparator.comparing(TbObFamilyMember::getCreatedTs))
                .toList();

        List<FamilyMemberDetailsDto> memberList = orderedMembers.stream()
                .map(member -> toFamilyMemberDetailsDto(member, docsByDocuId))
                .collect(Collectors.toList());

        return FamilyDetailsDto.builder()
                .maritalStatus(customer != null ? customer.getMaritalStatus() : null)
                .status(resolveFamilyStatus(familyDocs))
                .memberList(memberList)
                .build();
    }

    private FamilyMemberDetailsDto toFamilyMemberDetailsDto(TbObFamilyMember member,
                                                            Map<String, List<DocumentDetailsWrapper>> docsByFamilyMemId) {

        return FamilyMemberDetailsDto.builder()
                .memberType(member.getMemberType())
                .relationType(member.getRelation())
                .isEarning(member.getIsEarningMember())
                .isNominee(member.getIsNominee())
                .mobileNum(member.getMobileNum())
                .documentList(docsByFamilyMemId.getOrDefault(member.getFamilyMemId(), Collections.emptyList()))
                .build();
    }

    private String resolveFamilyStatus(List<DocumentDetailsWrapper> familyDocs) {
        if (familyDocs.isEmpty()) {
            return null;
        }
        return familyDocs.stream()
                .map(doc -> doc.documentDetails().validationStatus())
                .allMatch("captured"::equalsIgnoreCase) ? "captured" : "pending";
    }

    private IncomeDetailsDto toIncomeDetailsDto(
            TbObCustOthers customer,
            List<DocumentEntryDto> allDocs) {
        IncomeDet incomeDet = null;

        try {
            if (customer != null && customer.getIncomedet() != null) {
                incomeDet = objectMapper.readValue(
                        customer.getIncomedet(),
                        IncomeDet.class
                );
                return IncomeDetailsDto.builder()
                        .incomePayload(Objects.requireNonNull(incomeDet).incomePayload())
                        .questPayload(incomeDet.questPayload())
                        .documentList(
                                wrapDocsForCategory(
                                        allDocs,
                                        DocumentCategory.INCOMEDOC
                                )
                        )
                        .build();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to parse income details for customer: "
                            + customer.getCustomerId(),
                    e
            );
        }
        return IncomeDetailsDto.builder().build();
    }

    private BankDetailsDto toBankDetailsDto(TbObCustomer customer, List<DocumentEntryDto> allDocs) {
        BankDetailsDto bankDetails = objectMapper.convertValue(customer.getBankDetails(), BankDetailsDto.class);
        return BankDetailsDto.builder()
                .bankAccNo(bankDetails != null ? bankDetails.bankAccNo() : null)
                .bankAccName(bankDetails != null ? bankDetails.bankAccName() : null)
                .bankBranchName(bankDetails != null ? bankDetails.bankBranchName() : null)
                .bankName(bankDetails != null ? bankDetails.bankName() : null)
                .bankIfscCode(bankDetails != null ? bankDetails.bankIfscCode() : null)
                .status(bankDetails != null ? bankDetails.status() : null)
                .pennyRes(bankDetails != null ? bankDetails.pennyRes() : null)
                .documentList(wrapDocsForCategory(allDocs, DocumentCategory.BANKDOC))
                .build();
    }

    private AdditionalDetailsDto toAdditionalDetailsDto(TbObCustomer customer, List<DocumentEntryDto> documents, List<DocumentEntryDto> otherDocuments) {
        List<DocumentDetailsWrapper> homePhotoDoc = wrapDocsForCategory(documents, DocumentCategory.HOMEPHOTO);
        List<DocumentDetailsWrapper> bankDocuments = wrapDocsForCategory(documents, DocumentCategory.BANKDOC);
        List<DocumentDetailsWrapper> others = wrapDocsForCategory(documents, DocumentCategory.OTHER);
        List<DocumentDetailsWrapper> documentList = Stream.of(homePhotoDoc, bankDocuments, others)
                                                    .flatMap(Collection::stream)
                                                    .toList();
        return AdditionalDetailsDto.builder()
                .documentList(documentList)
                .build();
    }

    /** BRE check reflects the most recent loan/BRE-trigger row for the application. */
    private BreCheckDto toBreCheckDto(TbObCustomer customer, TbObDocument document, List<TbObLoan> loans) {
        if (loans == null || loans.isEmpty()) {
            return null;
        }
        TbObLoan latest = loans.stream()
                .max(Comparator.comparing(TbObLoan::getCreatedTs))
                .orElse(loans.get(0));

        return BreCheckDto.builder()
                .loanAmount(latest.getAmount().toString())
                .overDueAmount(
                        Optional.ofNullable(customer)
                                .map(TbObCustomer::getPayload)
                                .map(payload -> payload.get("overDueAmount"))
                                .map(Object::toString)
                                .orElse(null)
                )
                .writtenoffAmount(
                        Optional.ofNullable(customer)
                                .map(TbObCustomer::getPayload)
                                .map(payload -> payload.get("writtenoffAmount"))
                                .map(Object::toString)
                                .orElse(null)
                )
                .indebtedness(
                        Optional.ofNullable(customer)
                                .map(TbObCustomer::getPayload)
                                .map(payload -> payload.get("indebtedness"))
                                .map(Object::toString)
                                .orElse(null)
                )
                .foirAmount(
                        Optional.ofNullable(customer)
                                .map(TbObCustomer::getPayload)
                                .map(payload -> payload.get("foirAmount"))
                                .map(Object::toString)
                                .orElse(null)
                )
                .breStatus(latest.getBreResponseStatus())
                .finalFior(
                        Optional.ofNullable(customer)
                                .map(TbObCustomer::getPayload)
                                .map(payload -> payload.get("finalFoir"))
                                .map(Object::toString)
                                .orElse(null)
                )
                .breDate(
                        Optional.ofNullable(customer)
                                .map(TbObCustomer::getPayload)
                                .map(payload -> payload.get("breDate"))
                                .map(Object::toString)
                                .orElse(null)
                )
                .reason(document.getReason())
                .product(latest.getProduct())
                .questionnaire(customer != null ? Objects.toString(customer.getPayload().get("questionnaire"), null) : null)
                .build();
    }

    // ---------------------------------------------------------------------
    // Document mapping / grouping helpers
    // ---------------------------------------------------------------------

    private List<DocumentDetailsWrapper> wrapDocsForCategory(List<DocumentEntryDto> allDocs, DocumentCategory category) {
        return allDocs.stream()
                .filter(d -> category.name().equalsIgnoreCase(d.category()))
                .map(DocumentDetailsWrapper::of)
                .collect(Collectors.toList());
    }

    private DocumentEntryDto toDocumentEntryDto(TbObDocument d) {
        return DocumentEntryDto.builder()
                .docuId(d.getDocuId())
                .category(d.getCategory())
                .subCat(d.getSubCat())
                .kycType(d.getKycType())
                .authMode(d.getAuthMode())
                .idType(d.getIdType())
                .memRelation(d.getMemRelation())
                .mappingId(d.getMappingId())
                .legalDocName(d.getLegalDocName())
                .legalDocId(d.getLegalDocId())
                .docuNoF(d.getDmsDocIdFront())
                .docuNoB(d.getDmsDocIdBack())
                .photo(d.getPhoto())
                .score(d.getScore())
                .status(d.getStatus())
                .payload(d.getPayload())
                .addInfo(d.getAddInfo())
                .isEdited(d.getIsEdited())
                .editedBy(d.getEditedBy())
                .editedFields(d.getEditedFields())
                .reUploadedBy(d.getReuploadedBy())
                .reason(d.getReason())
                .clarityScore(d.getClarityScore())
                .clarityPass(d.getClarityPass())
                .dedupeStatus(d.getDedupeStatus())
                .validationStatus(d.getValidationStatus())
                .docVersion(d.getDocVersion())
                .build();
    }

    private DocumentEntryDto toDocumentEntryDto(TbObOtherDocument d) {
        return DocumentEntryDto.builder()
                .docuId(d.getDocuId())
                .category(d.getCategory())
                .subCat(d.getSubCat())
                .kycType(d.getKycType())
                .authMode(d.getAuthMode())
                .idType(d.getIdType())
                .memRelation(d.getMemRelation())
                .legalDocName(d.getLegalDocName())
                .legalDocId(d.getLegalDocId())
                .docuNoF(d.getDmsDocIdFront())
                .docuNoB(d.getDmsDocIdBack())
                .photo(d.getPhoto())
                .isEdited(d.getIsEdited())
                .editedBy(d.getEditedBy())
                .editedFields(d.getEditedFields())
                .reUploadedBy(d.getReuploadedBy())
                .reason(d.getReason())
                .build();
    }

    // ---------------------------------------------------------------------
    // Workflow / audit trail mapping
    // ---------------------------------------------------------------------

    private WorkflowHistoryDto toWorkflowHistoryDto(TbObApplnWorkflow w) {
        return WorkflowHistoryDto.builder()
                .versionNo(w.getVersionNo())
                .workflowSeqNo(w.getWorkflowSeqNo())
                .applicationStatus(w.getApplicationStatus())
//                .createdTs(Instant.from(w.getCreatedTs()))
                .createdBy(w.getCreatedBy())
                .presentRole(w.getPresentRole())
                .nextWorkflowStage(w.getNextWorkflowStage())
                .remarks(w.getRemarks())
                .createdUsername(w.getCreatedUsername())
                .build();
    }

    private AuditEventDto toAuditEventDto(TbObCustAuditTrail a) {
        return AuditEventDto.builder()
                .userId(a.getUserId())
                .userName(a.getUserName())
                .userRole(a.getUserRole())
                .stageId(a.getStageId())
                .subStage(a.getSubStage())
//                .eventTs(Instant.from(a.getCreateTs()))
                .build();
    }

    /**
     * Common formatter for entity timestamps. Returns null if the input is null
     * so callers don't need repeated null checks at every call site.
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    private AddressDetailsDto toAddressDetailsDto(TbObAddress address) {
        if (address == null) {
            return null;
        }
        return AddressDetailsDto.builder()
                .addressId(address.getAddressId())
                .customerId(address.getCustomerId())
                .applicationId(address.getApplicationId())
                .addressType(address.getAddressType())
                .commSameAsPerm(address.getCommSameAsPerm())
                .addrPayload(Objects.toString(address.getAddrPayload(), null))
                .addressProofType(address.getAddressProofType())
                .addressProofDocId(address.getAddressProofDocId())
                .distanceFromBranch(address.getDistanceFromBranch() != null
                        ? address.getDistanceFromBranch().toString()
                        : null)
                .createdTs(formatDateTime(address.getCreatedTs()))
                .updatedTs(formatDateTime(address.getUpdatedTs()))
                .build();
    }

    private List<AddressDetailsDto> toAddressDetailsDtoList(List<TbObAddress> addresses) {
        if (addresses == null) {
            return List.of();
        }
        return addresses.stream()
                .map(this::toAddressDetailsDto)
                .collect(Collectors.toList());
    }
}