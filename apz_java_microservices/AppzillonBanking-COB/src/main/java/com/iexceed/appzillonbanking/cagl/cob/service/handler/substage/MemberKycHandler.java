package com.iexceed.appzillonbanking.cagl.cob.service.handler.substage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.payload.CustomerUpdateDtls;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentListItem;
import com.iexceed.appzillonbanking.cagl.cob.payload.MemberPhotoDet;
import com.iexceed.appzillonbanking.cagl.cob.service.DocumentService;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberKycHandler implements SubStageHandler {
    private final DocumentService documentService;

    @Override
    public void handle(SubStageHandlerContext context) throws JsonProcessingException {
        CustomerUpdateDtls cd = context.getCustomerUpdateDtls();
        TbObCustomer customer = context.getCustomer();
        String applicationId = context.getApplicationId();
        String userId = context.getUserId();
        var now = context.getNow();

        Map<String, Object> kycMap = customer.getKycDetails() != null ? customer.getKycDetails() : new HashMap<>();
        if (cd.getKycDetails() != null) {
            kycMap.putAll(cd.getKycDetails());
        }
        customer.setPhotoDocId(cd.getPhotoDocId());
        customer.setKycDetails(kycMap);
        customer.setLivePhotoStatus(cd.getMemberPhoto().getStatus());

        Object primaryType = kycMap.get("primaryType");
        Object primaryId = kycMap.get("primaryId");
        if (primaryType != null) customer.setPrimaryKycType(primaryType.toString());
        if (primaryId != null) customer.setPrimaryKycId(primaryId.toString());

        // member live photo
        Optional.ofNullable(cd.getMemberPhoto())
                .map(MemberPhotoDet::getDocumentList)
                .ifPresent(documentList ->
                        documentList.stream()
                                .map(DocumentListItem::getDocumentDetails)
                                .filter(Objects::nonNull)
                                .forEach(documentDetails ->
                                {
                                    try {
                                        documentService.saveDocument(
                                                documentDetails,
                                                applicationId,
                                                customer.getCustomerId(),
                                                userId,
                                                now
                                        );
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException(e);
                                    }
                                }));

        // member KYC documents (Voter ID / Aadhaar / PAN ...)
        if (cd.getMemberKycDetails() != null && !CollectionUtils.isEmpty(cd.getMemberKycDetails().getDocumentList())) {
            for (DocumentListItem item : cd.getMemberKycDetails().getDocumentList()) {
                if (item.getDocumentDetails() != null) {
                    documentService.saveDocument(item.getDocumentDetails(), applicationId, customer.getCustomerId(), userId, now);
                }
            }
        }
    }
}
