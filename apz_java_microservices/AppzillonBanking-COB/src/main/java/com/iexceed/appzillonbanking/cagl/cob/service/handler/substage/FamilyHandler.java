package com.iexceed.appzillonbanking.cagl.cob.service.handler.substage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iexceed.appzillonbanking.cagl.cob.constants.ApplicationConstants;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObFamilyMember;
import com.iexceed.appzillonbanking.cagl.cob.payload.*;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObFamilyMemberRepository;
import com.iexceed.appzillonbanking.cagl.cob.service.DocumentService;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FamilyHandler implements SubStageHandler {
    private static final DateTimeFormatter DOB_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DocumentService documentService;
    private final TbObFamilyMemberRepository familyMemberRepository;

    @Override
    public void handle(SubStageHandlerContext context) throws JsonProcessingException {
        CustomerUpdateDtls cd = context.getCustomerUpdateDtls();
        TbObCustomer customer = context.getCustomer();
        String applicationId = context.getApplicationId();
        String userId = context.getUserId();
        var now = context.getNow();

        log.info("Enter handle family: " + cd + " : " + applicationId + " : " + customer + " : " + userId + " : " + now);
        Map<String, Object> kycMap = customer.getKycDetails() != null ? customer.getKycDetails() : new HashMap<>();
        if (cd.getKycDetails() != null) {
            kycMap.putAll(cd.getKycDetails());
        }
        customer.setKycDetails(kycMap);

        FamilyDet fd = cd.getFamilyDetails();
        if (fd == null || CollectionUtils.isEmpty(fd.getMemberList())) {
            return;
        }

        for (FamilyMemberItem item : fd.getMemberList()) {
            String name = null;
            String gender = null;
            LocalDate dob = null;
            String kycType = null;
            String kycDocId = null;
            String kycDocFront = null;
            String kycDocBack = null;
            String photoDocId = null;

            if (!CollectionUtils.isEmpty(item.getDocumentList())) {
                for (DocumentListItem docListItem : item.getDocumentList()) {
                    DocumentDetail doc = docListItem.getDocumentDetails();
                    if (doc == null) continue;

                    Map<String, Object> inputData = doc.getInputData();
                    if (inputData != null) {
                        if (name == null && inputData.get("name") != null) name = inputData.get("name").toString();
                        if (gender == null && inputData.get("gender") != null) gender = inputData.get("gender").toString();
                        if (dob == null && inputData.get("dob") != null) dob = parseDob(inputData.get("dob").toString());
                    }
                    if ("VOTER-ID".equalsIgnoreCase(doc.getLegalDocName()) && doc.getSubCat().contains("FD1")
                            && item.getMemberType().equalsIgnoreCase("P")) {
                        kycType = doc.getLegalDocName();
                        kycDocId = doc.getLegalDocId();
                        kycDocFront = doc.getDocuNoF();
                        kycDocBack = doc.getDocuNoB();
                    } else if(item.getMemberType().equalsIgnoreCase("S")) {
                        kycType = doc.getLegalDocName();
                        kycDocId = doc.getLegalDocId();
                        kycDocFront = doc.getDocuNoF();
                        kycDocBack = doc.getDocuNoB();
                    }
                    if (doc.getPhoto() != null && !doc.getPhoto().isBlank() && doc.getDocuNoF() == null) {
                        photoDocId = doc.getPhoto();
                    }
                }
            }
            customer.setMaritalStatus(cd.getFamilyDetails().getMaritalStatus());
            TbObFamilyMember familyMember = familyMemberRepository
                    .findByApplicationIdAndRelationAndMemberType(
                            applicationId,
                            item.getRelationType(),
                            item.getMemberType())
                    .orElseGet(TbObFamilyMember::new);

            familyMember.setCustomerId(customer.getCustomerId());
            familyMember.setApplicationId(applicationId);
            familyMember.setMemberType(item.getMemberType());
            familyMember.setRelation(item.getRelationType());
            familyMember.setName(name != null ? name : ApplicationConstants.PLACEHOLDER_NOT_CAPTURED);
            familyMember.setDob(dob);
            familyMember.setGender(gender);
            familyMember.setMobileNum(item.getMobileNum());
            familyMember.setKycType(kycType);
            familyMember.setKycDocId(kycDocId);
            familyMember.setKycDocFront(kycDocFront);
            familyMember.setKycDocBack(kycDocBack);
            familyMember.setPhotoDocId(photoDocId);
            familyMember.setIsNominee(Boolean.TRUE.equals(item.getIsNominee()));
            familyMember.setIsEarningMember(Boolean.TRUE.equals(item.getIsEarning()));

            if (familyMember.getFamilyMemId() == null) {
                familyMember.setCreatedTs(now);
            }

            familyMemberRepository.save(familyMember);
            System.out.println("Family Member Id: " + familyMember.getFamilyMemId());
            if (!CollectionUtils.isEmpty(item.getDocumentList())) {
                for (DocumentListItem docListItem : item.getDocumentList()) {
                    if (docListItem.getDocumentDetails() != null) {
                        docListItem.getDocumentDetails().setMappingId(familyMember.getFamilyMemId());
                        documentService.saveDocument(docListItem.getDocumentDetails(), applicationId, customer.getCustomerId(), userId, now);
                    }
                }
            }
        }
    }

    private LocalDate parseDob(String dob) {
        try {
            return LocalDate.parse(dob, DOB_FORMAT);
        } catch (Exception ex) {
            log.warn("Could not parse dob '{}', expected dd/MM/yyyy", dob);
            return null;
        }
    }
}
