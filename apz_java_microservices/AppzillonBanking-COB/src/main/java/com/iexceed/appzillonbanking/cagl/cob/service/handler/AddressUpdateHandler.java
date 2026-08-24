package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObAddress;
import com.iexceed.appzillonbanking.cagl.cob.enums.UpdateType;
import com.iexceed.appzillonbanking.cagl.cob.payload.UpdateContext;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObAddressRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.ab.TbObApplnWorkflowRepository;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObDocumentRepository;
import com.iexceed.appzillonbanking.cagl.cob.service.HandlerResult;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonNodeUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Substage 1.3 (address slice) - upserts the P (permanent) row, and either
 * copies it to the C (communication) row when commSameAsPerm, or upserts C
 * independently from its own address-proof document entry.
 */
@Component
public class AddressUpdateHandler extends AbstractDocumentListHandler implements UpdateHandler {

    private final TbObAddressRepository addressRepository;

    public AddressUpdateHandler(TbObAddressRepository addressRepository,
                                 TbObDocumentRepository documentRepository,
                                 TbObApplnWorkflowRepository workflowVersionRepository,
                                 ObjectMapper objectMapper) {
        super(documentRepository, workflowVersionRepository, objectMapper);
        this.addressRepository = addressRepository;
    }

    @Override
    public UpdateType supports() {
        return UpdateType.ADDRESS;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        List<String> changed = new ArrayList<>();
        String addressType = JsonNodeUtil.text(ctx.payload(), "addressType", "P");
        String perAddr = JsonNodeUtil.text(ctx.payload(), "PA", "");
        String commAddr = JsonNodeUtil.text(ctx.payload(), "CA", "");
//        boolean commSameAsPerm = Boolean.TRUE.equals(JsonNodeUtil.bool(ctx.payload(), "commSameAsPerm"));

        JsonNode documentList = JsonNodeUtil.array(ctx.payload(), "documentList");
        changed.addAll(upsertDocuments(ctx, documentList));

        JsonNode addrDetails = findAddressPayload(documentList, addressType);
        TbObAddress permOrComm = upsertAddressRow(ctx, addressType, perAddr, commAddr, addrDetails, changed);

        if (perAddr.equalsIgnoreCase(commAddr)) {
            TbObAddress commRow = addressRepository.findByApplicationIdAndAddressType(ctx.applicationId(), "C")
                    .orElseGet(() -> TbObAddress.builder()
                            .customerId(ctx.customerId())
                            .applicationId(ctx.applicationId())
                            .addressType("C")
                            .createdTs(ctx.nowEpochMillis())
                            .build());
            commRow.setCommSameAsPerm("Y");
            commRow.setAddrPayload(permOrComm.getAddrPayload());
            commRow.setUpdatedTs(ctx.nowEpochMillis());
            commRow.setUpdatedBy(ctx.actorUserId());
            addressRepository.save(commRow);
            changed.add("address.C.copiedFromP");
        }

        return HandlerResult.of(changed, "1.3");
    }

    private TbObAddress upsertAddressRow(UpdateContext ctx, String addressType, String perAddr, String commAddr,
                                      JsonNode addrDetails, List<String> changed) {
        TbObAddress row = addressRepository.findByApplicationIdAndAddressType(ctx.applicationId(), addressType)
                .orElseGet(() -> TbObAddress.builder()
                        .customerId(ctx.customerId())
                        .applicationId(ctx.applicationId())
                        .addressType(addressType)
                        .commSameAsPerm((perAddr.equalsIgnoreCase(commAddr)) ? "Y" : "N")
                        .createdTs(ctx.nowEpochMillis())
                        .build());

        if (addrDetails != null) {
            JsonNode input = addrDetails.has("inputData") ? addrDetails.get("inputData") : addrDetails;
            Map<String, Object> newPayload = JsonNodeUtil.asMap(objectMapper, input);
            if (JsonNodeUtil.differs(row.getAddrPayload(), newPayload)) {
                row.setAddrPayload(newPayload);
                changed.add("address." + addressType + ".addrPayload");
            }
            String proofType = JsonNodeUtil.text(addrDetails, "legalDocName");
            String proofDocId = JsonNodeUtil.text(addrDetails, "docuNoF");
            if (proofType != null) {
                row.setAddressProofType(proofType);
            }
            if (proofDocId != null) {
                row.setAddressProofDocId(proofDocId);
            }
        }

        row.setCommSameAsPerm((perAddr.equalsIgnoreCase(commAddr)) ? "Y" : "N");
        row.setUpdatedTs(ctx.nowEpochMillis());
        row.setUpdatedBy(ctx.actorUserId());
        return addressRepository.save(row);
    }

    private JsonNode findAddressPayload(JsonNode documentList, String addressType) {
        if (documentList == null) {
            return null;
        }
//        String subCat = "P".equals(addressType) ? "PerAddr" : "CommAddr";
        for (JsonNode entry : documentList) {
            JsonNode details = entry.has("documentDetails") ? entry.get("documentDetails") : entry;
//            if (subCat.equalsIgnoreCase(JsonNodeUtil.text(details, "SubCat", JsonNodeUtil.text(details, "subCat")))) {
                return details;
//            }
        }
        return null;
    }
}
