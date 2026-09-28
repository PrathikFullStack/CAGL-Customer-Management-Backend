package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Builder;

@Builder
public record FetchGroupMemberCountResponse(String groupId,
                                            String totalMemberCount,
                                            String activatedCount,
                                            String inactiveCount,
                                            String inprogessCount,
                                            String releasedCount) {
}