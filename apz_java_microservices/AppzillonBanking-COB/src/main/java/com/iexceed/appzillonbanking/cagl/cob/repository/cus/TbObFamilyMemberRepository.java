package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObFamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TbObFamilyMemberRepository extends JpaRepository<TbObFamilyMember, String> {
    List<TbObFamilyMember> findByApplicationIdOrderByFamilyMemId(String applicationId);

    Optional<TbObFamilyMember> findByApplicationIdAndKycDocId(String applicationId, String kycDocId);

    void deleteByApplicationId(String applicationId);

    Optional<TbObFamilyMember> findByApplicationIdAndRelationAndMemberType(String applicationId, String relationType, String memberType);

    List<TbObFamilyMember> findByApplicationId(String applicationId);
}
