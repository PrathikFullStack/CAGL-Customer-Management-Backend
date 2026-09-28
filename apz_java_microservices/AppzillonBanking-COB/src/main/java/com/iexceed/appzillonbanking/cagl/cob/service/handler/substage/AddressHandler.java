package com.iexceed.appzillonbanking.cagl.cob.service.handler.substage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iexceed.appzillonbanking.cagl.cob.constants.ApplicationConstants;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObAddress;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.payload.CustomerUpdateDtls;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentDetail;
import com.iexceed.appzillonbanking.cagl.cob.payload.DocumentListItem;
import com.iexceed.appzillonbanking.cagl.cob.payload.PersonalAddressDet;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObAddressRepository;
import com.iexceed.appzillonbanking.cagl.cob.service.DocumentService;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AddressHandler implements SubStageHandler {
    private final DocumentService documentService;
    private final TbObAddressRepository addressRepository;

    @Override
    public void handle(SubStageHandlerContext context) throws JsonProcessingException {
        CustomerUpdateDtls cd = context.getCustomerUpdateDtls();
        TbObCustomer customer = context.getCustomer();
        String applicationId = context.getApplicationId();
        String userId = context.getUserId();
        var now = context.getNow();

        if (cd.getCustomerName() != null) {
            customer.setCustomerName(cd.getCustomerName());
        }
        Map<String, Object> kycMap = customer.getKycDetails() != null ? customer.getKycDetails() : new HashMap<>();
        if (cd.getKycDetails() != null) {
            kycMap.putAll(cd.getKycDetails());
        }
        customer.setKycDetails(kycMap);
        Object dob = kycMap.get("dob");
        if (dob != null) {
            customer.setDob(dob.toString());
        }

        PersonalAddressDet pad = cd.getPersonalAddressDet();
        if (pad == null || CollectionUtils.isEmpty(pad.getDocumentList())) {
            return;
        }

        for (DocumentListItem document : pad.getDocumentList()) {
            DocumentDetail doc = document.getDocumentDetails();
            String addressType = "ADDC".equalsIgnoreCase(doc.getSubCat())
                    ? ApplicationConstants.ADDRESS_TYPE_COMMUNICATION
                    : ApplicationConstants.ADDRESS_TYPE_PERMANENT;

            Map<String, Object> addrPayload = new HashMap<>();
            addrPayload.put("nameSelected", cd.getPersonalAddressDet().getNameSelected());
            addrPayload.put("dobSelected", cd.getPersonalAddressDet().getDobSelected());
            addrPayload.put("PA", cd.getPersonalAddressDet().getPa());
            addrPayload.put("CA", cd.getPersonalAddressDet().getCa());

            TbObAddress address = addressRepository.findByApplicationIdAndAddressType(applicationId, addressType)
                    .orElse(TbObAddress.builder()
                            .customerId(customer.getCustomerId())
                            .applicationId(applicationId)
                            .addressType(addressType)
                            .commSameAsPerm("N")
                            .createdTs(now)
                            .build());
            address.setAddrPayload(addrPayload);
            address.setAddressProofDocId(doc.getPhoto());
            address.setUpdatedTs(now);
            address.setUpdatedBy(userId);
            addressRepository.save(address);

            documentService.saveDocument(doc, applicationId, customer.getCustomerId(), userId, now,
                    ApplicationConstants.DOCUMENT_CATEGORY_ADDRESS);
        }
    }
}
