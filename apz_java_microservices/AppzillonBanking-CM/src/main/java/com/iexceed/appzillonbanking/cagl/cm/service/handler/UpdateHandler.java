package com.iexceed.appzillonbanking.cagl.cm.service.handler;

import com.iexceed.appzillonbanking.cagl.cm.payload.common.RequestWrapper.RequestHeader;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.CustomerUpdateRequest;
import com.iexceed.appzillonbanking.cagl.cm.payload.update.UpdateResponseDto;

public interface UpdateHandler {

    String getSectionName();

    UpdateResponseDto handleUpdate(CustomerUpdateRequest request, RequestHeader header);
}
