package com.iexceed.appzillonbanking.cagl.cob.service;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.enums.ChannelType;
import com.iexceed.appzillonbanking.cagl.cob.enums.UserRole;
import org.springframework.stereotype.Service;

/**
 * Implements the channel-drift rules from the schema remarks:
 * - km_edited (KM changed a field post-BRE) -> YELLOW
 * - maker_edited (RPC Maker changed a field) -> upgrades YELLOW to RED
 * A GREEN channel with no edits is left untouched; channel_type is only
 * ever set once BRE has run (channelType starts blank in the payload).
 */
@Service
public class ChannelClassificationService {

    public void applyEditFlags(TbObApplicationMaster app, TbObCustomer customer, UserRole actorRole) {
        if (actorRole == UserRole.KM) {
//            customer.setIsKmEdited("Y");
            escalateTo(app, customer, ChannelType.YELLOW);
        } else if (actorRole == UserRole.RPC_MAKER) {
            escalateTo(app, customer, ChannelType.RED);
        }
    }

    private void escalateTo(TbObApplicationMaster app, TbObCustomer customer, ChannelType minimum) {
        ChannelType current = parse(app.getChannelType());
        if (current == null || rank(minimum) > rank(current)) {
            app.setChannelType(minimum.name());
//            customer.setChannelType(minimum.name());
        }
    }

    private ChannelType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return ChannelType.valueOf(raw);
    }

    private int rank(ChannelType channelType) {
        return switch (channelType) {
            case GREEN -> 0;
            case YELLOW -> 1;
            case RED -> 2;
        };
    }
}