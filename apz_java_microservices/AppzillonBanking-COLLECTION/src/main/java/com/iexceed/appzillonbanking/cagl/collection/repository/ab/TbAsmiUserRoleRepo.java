package com.iexceed.appzillonbanking.cagl.collection.repository.ab;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.collection.domain.ab.UserRole;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.UserRoleId;

@Repository
public interface TbAsmiUserRoleRepo extends JpaRepository<UserRole, UserRoleId> {

	List<UserRole> findByUserId(String userId);
}
