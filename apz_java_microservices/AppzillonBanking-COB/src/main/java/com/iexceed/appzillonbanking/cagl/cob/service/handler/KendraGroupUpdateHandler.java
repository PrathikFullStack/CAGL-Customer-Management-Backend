package com.iexceed.appzillonbanking.cagl.cob.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.iexceed.appzillonbanking.cagl.cob.enums.UpdateType;
import com.iexceed.appzillonbanking.cagl.cob.payload.UpdateContext;
import com.iexceed.appzillonbanking.cagl.cob.service.HandlerResult;
import com.iexceed.appzillonbanking.cagl.cob.utils.JsonNodeUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Substage 1.7 - Kendra & Group selection -> application_master. */
@Component
public class KendraGroupUpdateHandler implements UpdateHandler {

    @Override
    public UpdateType supports() {
        return UpdateType.KENDRA_GROUP;
    }

    @Override
    public HandlerResult handle(UpdateContext ctx) {
        JsonNode p = ctx.payload();
        List<String> changed = new ArrayList<>();

        String kendraId = JsonNodeUtil.text(p, "kendraId");
        String groupId = JsonNodeUtil.text(p, "groupId");

        if (kendraId != null && JsonNodeUtil.differs(ctx.applicationMaster().getKendraId(), kendraId)) {
            ctx.applicationMaster().setKendraId(kendraId);
            changed.add("kendraId");
        }
        if (groupId != null && JsonNodeUtil.differs(ctx.applicationMaster().getGroupId(), groupId)) {
            ctx.applicationMaster().setGroupId(groupId);
            changed.add("groupId");
        }

        return HandlerResult.of(changed, "1.7");
    }
}
