package com.iexceed.appzillonbanking.cagl.cob.payload;

import java.util.List;
import lombok.Builder;

@Builder
public record FetchKendraGroupCountResponse(String kendraId,
                                            String kendraName,
                                            List<FetchGroupMemberCountResponse> groups) {
}