package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObFamilyMember;
import com.iexceed.appzillonbanking.cagl.cob.enums.UpdateType;
import com.iexceed.appzillonbanking.cagl.cob.payload.UpdateContext;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplnWorkflowRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObDocumentRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObFamilyMemberRepository;
import com.iexceed.appzillonbanking.cagl.cob.service.HandlerResult;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonNodeUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Substage 1.4 - spouse / father / earning members / nominee. Each entry in
 * documentList (category=FamilyMem) is upserted both as a tb_ob_document row
 * (via the shared document handling) and as a tb_ob_family_member row keyed
 * by (applicationId, kycDocId).
 */
@Component
public class FamilyMembersUpdateHandler extends AbstractDocumentListHandler implements UpdateHandler {

    private static final DateTimeFormatter INPUT_DOB_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT);

    private final TbObFamilyMemberRepository familyMemberRepository;

    public FamilyMembersUpdateHandler(TbObFamilyMemberRepository familyMemberRepository,
                                       TbObDocumentRepository documentRepository,
                                       TbObApplnWorkflowRepository workflowVersionRepository,
                                       ObjectMapper objectMapper) {
        super(documentRepository, workflowVersionRepository, objectMapper);
        this.familyMemberRepository = familyMemberRepository;
    }

    @Override
    public UpdateType supports() {
        return UpdateType.FAMILY_MEMBERS;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        List<String> changed = new ArrayList<>();

        String maritalStatus = JsonNodeUtil.text(ctx.payload(), "maritalStatus");
        if (maritalStatus != null && JsonNodeUtil.differs(ctx.customer().getMaritalStatus(), maritalStatus)) {
            ctx.customer().setMaritalStatus(maritalStatus);
            changed.add("maritalStatus");
        }

        JsonNode documentList = JsonNodeUtil.array(ctx.payload(), "documentList");
        changed.addAll(upsertDocuments(ctx, documentList));

        if (documentList != null) {
            for (JsonNode entry : documentList) {
                JsonNode details = entry.has("documentDetails") ? entry.get("documentDetails") : entry;
                if (!"FamilyMem".equalsIgnoreCase(
                        JsonNodeUtil.text(details, "category", JsonNodeUtil.text(details, "catogory")))) {
                    continue;
                }
                changed.addAll(upsertFamilyMember(ctx, details));
            }
        }

        return HandlerResult.of(changed, "1.4");
    }

    private List<String> upsertFamilyMember(UpdateContext ctx, JsonNode details) {
        List<String> changed = new ArrayList<>();
        JsonNode input = details.has("inputData") ? details.get("inputData") : details;

        String kycDocId = JsonNodeUtil.text(details, "legaldocId", JsonNodeUtil.text(details, "legalDocId"));
        String subCat = JsonNodeUtil.text(details, "SubCat", JsonNodeUtil.text(details, "subCat"));
        String relation = JsonNodeUtil.text(input, "memRelation", JsonNodeUtil.text(details, "memRelation"));

        TbObFamilyMember member = (kycDocId != null
                ? familyMemberRepository.findByApplicationIdAndKycDocId(ctx.applicationId(), kycDocId)
                : java.util.Optional.<TbObFamilyMember>empty())
                .orElseGet(() -> TbObFamilyMember.builder()
                        .customerId(ctx.customerId())
                        .applicationId(ctx.applicationId())
                        .memberType(memberTypeFrom(subCat))
                        .relation(relation)
                        .isNominee(false)
                        .isEarningMember(false)
                        .createdTs(ctx.nowEpochMillis())
                        .build());

        String name = JsonNodeUtil.text(input, "name");
        if (name != null && JsonNodeUtil.differs(member.getName(), name)) {
            member.setName(name);
            changed.add("familyMember[" + kycDocId + "].name");
        }

        String dobRaw = JsonNodeUtil.text(input, "dob");
        if (dobRaw != null) {
            LocalDate dob = parseDob(dobRaw);
            if (dob != null && JsonNodeUtil.differs(member.getDob(), dob)) {
                member.setDob(dob);
                changed.add("familyMember[" + kycDocId + "].dob");
            }
        }

        String gender = JsonNodeUtil.text(input, "gender");
        if (gender != null) {
            member.setGender(gender);
        }
        String mobileNum = JsonNodeUtil.text(input, "mobileNum");
        if (mobileNum != null) {
            member.setMobileNum(mobileNum);
        }
        String legalDocName = JsonNodeUtil.text(details, "legalDocName");
        if (legalDocName != null) {
            member.setKycType(legalDocName);
        }
        if (kycDocId != null) {
            member.setKycDocId(kycDocId);
        }
        member.setKycDocFront(JsonNodeUtil.text(details, "docuNoF", member.getKycDocFront()));
        member.setKycDocBack(JsonNodeUtil.text(details, "docuNoB", member.getKycDocBack()));
        member.setPhotoDocId(JsonNodeUtil.text(details, "photo", member.getPhotoDocId()));

        Boolean isNominee = JsonNodeUtil.bool(input, "isNominee");
        if (isNominee != null && !isNominee.equals(member.getIsNominee())) {
            member.setIsNominee(isNominee);
            changed.add("familyMember[" + kycDocId + "].isNominee");
        }
        Boolean isEarning = JsonNodeUtil.bool(input, "isEarningMember");
        if (isEarning != null && !isEarning.equals(member.getIsEarningMember())) {
            member.setIsEarningMember(isEarning);
            changed.add("familyMember[" + kycDocId + "].isEarningMember");
        }

        if (Boolean.TRUE.equals(isNominee)) {
            var nomineeBank = new java.util.HashMap<String, Object>();
            putIfPresent(input, "bankAccNo", nomineeBank);
            putIfPresent(input, "bankAccName", nomineeBank);
            putIfPresent(input, "bankBranchName", nomineeBank);
            putIfPresent(input, "bankName", nomineeBank);
            putIfPresent(input, "bankIfscCode", nomineeBank);
            if (!nomineeBank.isEmpty()) {
                member.setNomineeBankDetails(nomineeBank);
            }
        }

        String editedBy = JsonNodeUtil.text(input, "isEditedBy", JsonNodeUtil.text(details, "editedBy"));
        if (editedBy != null) {
            member.setIsEditedBy(editedBy);
        }

        familyMemberRepository.save(member);
        return changed;
    }

    private void putIfPresent(JsonNode node, String field, java.util.Map<String, Object> target) {
        String value = JsonNodeUtil.text(node, field);
        if (value != null) {
            target.put(field, value);
        }
    }

    private String memberTypeFrom(String subCat) {
        if (subCat == null) {
            return "EARNING";
        }
        return switch (subCat.toUpperCase(Locale.ROOT)) {
            case "NOMINEE" -> "NOMINEE";
            case "EARNING" -> "EARNING";
            default -> "EARNING";
        };
    }

    private LocalDate parseDob(String raw) {
        try {
            return LocalDate.parse(raw, INPUT_DOB_FORMAT);
        } catch (Exception ex) {
            return null;
        }
    }
}
