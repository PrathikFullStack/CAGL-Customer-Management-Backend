package com.iexceed.appzillonbanking.cagl.cob.service.handler.substage;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.payload.CustomerUpdateDtls;
import com.iexceed.appzillonbanking.cagl.cob.payload.KendraSelectionDetails;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGroupRepository;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandlerContext;
import com.iexceed.appzillonbanking.cagl.cob.utils.GroupCountUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class KendraSelectionHandler implements SubStageHandler {
    private final TbObGroupRepository groupRepository;

    public KendraSelectionHandler(TbObGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public void handle(SubStageHandlerContext context) {
        CustomerUpdateDtls cd = context.getCustomerUpdateDtls();
        TbObApplicationMaster master = context.getMaster();
        TbObCustomer customer = context.getCustomer();

        KendraSelectionDetails ks = cd.getKendraSelectionDetails();
        if (ks == null) {
            return;
        }
        String groupId = ks.getGroupId();
        master.setKendraId(ks.getKendraId());
        master.setKendraName(ks.getKendraName());
        master.setGroupId(groupId);
        customer.setDistanceFromKendra(ks.getDistanceFromKendra());

        TbObGroup fromGroup = groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Source group record not found for groupId: " + groupId + "."));
        fromGroup.setTotalMemberCount(GroupCountUtil.adjust(fromGroup.getTotalMemberCount(), 1));
        fromGroup.setInprogressCount(GroupCountUtil.adjust(fromGroup.getReleasedCount(), 1));
        fromGroup.setUpdatedBy(context.getUserId());
        fromGroup.setUpdatedTs(LocalDateTime.now());
        groupRepository.save(fromGroup);

        if (cd.getPayload() != null) {
            Map<String, Object> payload = customer.getPayload() != null ? customer.getPayload() : new HashMap<>();
            payload.putAll(cd.getPayload());
            customer.setPayload(payload);
        }
    }
}
