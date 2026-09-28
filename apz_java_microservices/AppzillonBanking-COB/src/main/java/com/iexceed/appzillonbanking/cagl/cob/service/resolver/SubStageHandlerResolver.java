package com.iexceed.appzillonbanking.cagl.cob.service.resolver;

import com.iexceed.appzillonbanking.cagl.cob.constants.ApplicationConstants;
import com.iexceed.appzillonbanking.cagl.cob.exception.InvalidSubStageException;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.SubStageHandler;
import com.iexceed.appzillonbanking.cagl.cob.service.handler.substage.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SubStageHandlerResolver {

    private final Map<String, SubStageHandler> handlerMap = new HashMap<>();

    public SubStageHandlerResolver(MemberKycHandler memberKycHandler, AddressHandler addressHandler,
                                   FamilyHandler familyHandler, IncomeHandler incomeHandler,
                                   KendraSelectionHandler kendraSelectionHandler, BankHandler bankHandler,
                                   AdditionalDocsHandler additionalDocsHandler) {
        // KM VERIFICATION
        handlerMap.put(ApplicationConstants.SUB_STAGE_MEMBER_KYC, memberKycHandler);
        handlerMap.put(ApplicationConstants.SUB_STAGE_ADDRESS, addressHandler);
        handlerMap.put(ApplicationConstants.SUB_STAGE_FAMILY, familyHandler);
        handlerMap.put(ApplicationConstants.SUB_STAGE_INCOME, incomeHandler);
        handlerMap.put(ApplicationConstants.SUB_STAGE_KENDRA_SELECTION, kendraSelectionHandler);
        handlerMap.put(ApplicationConstants.SUB_STAGE_BANK, bankHandler);
        handlerMap.put(ApplicationConstants.SUB_STAGE_ADDITIONAL_DOCS, additionalDocsHandler);
        handlerMap.put(ApplicationConstants.SUB_STAGE_ADDITIONAL_DOCS_1, additionalDocsHandler);

        // RPC VERIFICATION
        handlerMap.put(ApplicationConstants.RPC_SUB_STAGE_MEMBER_KYC, memberKycHandler);
        handlerMap.put(ApplicationConstants.RPC_SUB_STAGE_ADDRESS, addressHandler);
        handlerMap.put(ApplicationConstants.RPC_SUB_STAGE_FAMILY, familyHandler);
        handlerMap.put(ApplicationConstants.RPC_SUB_STAGE_INCOME, incomeHandler);
        handlerMap.put(ApplicationConstants.RPC_SUB_STAGE_KENDRA_SELECTION, kendraSelectionHandler);
        handlerMap.put(ApplicationConstants.RPC_SUB_STAGE_BANK, bankHandler);
        handlerMap.put(ApplicationConstants.RPC_SUB_STAGE_ADDITIONAL_DOCS, additionalDocsHandler);
        handlerMap.put(ApplicationConstants.RPC_SUB_STAGE_ADDITIONAL_DOCS_1, additionalDocsHandler);
    }

    public SubStageHandler resolve(String subStage) {
        SubStageHandler handler = handlerMap.get(subStage);
        if (handler == null) {
            throw new InvalidSubStageException(subStage);
        }
        return handler;
    }
}
