package com.iexceed.appzillonbanking.cagl.cob.mapper;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.payload.ApplicationSummary;
import com.iexceed.appzillonbanking.cagl.cob.repository.cus.TbObGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApplicationSummaryMapper {

    private final TbObGroupRepository groupRepository;

    public ApplicationSummary toApplicationSummary(TbObApplicationMaster app) {
        String groupName = Optional.ofNullable(app.getGroupId())
                .flatMap(groupRepository::findByGroupId)
                .map(TbObGroup::getGroupName)
                .orElse(null);

        return new ApplicationSummary(
                app.getApplicationId(),
                app.getCustomerId(),
                app.getCustomerName(),
                app.getKendraId(),
                app.getKendraName(),
                app.getGroupId(),
                groupName,
                app.getBranchId(),
                null, // branchName is not available in the entity
                app.getLeader(),
                app.getCreatedBy(),
                app.getCreatedTs(),
                app.getUpdatedTs(),
                app.getUpdatedBy(),
                app.getCreatedBy(), // Assuming kmId is the createdBy user
                app.getStage(),
                app.getSubStage(),
                app.getStatus(),
                app.getRemarks(),
                app.getCustLabel()
        );
    }
}