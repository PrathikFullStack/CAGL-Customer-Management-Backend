package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmFamilyMemberEntity;

@Repository
public interface CmFamilyMemberRepository extends JpaRepository<CmFamilyMemberEntity, String> {

    List<CmFamilyMemberEntity> findByCustomerId(String customerId);

    List<CmFamilyMemberEntity> findByApplicationId(String applicationId);

    List<CmFamilyMemberEntity> findByCustomerIdAndMemberType(String customerId, String memberType);
}
