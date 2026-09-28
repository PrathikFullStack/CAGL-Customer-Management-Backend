package com.iexceed.appzillonbanking.cagl.cm.repository.primary;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmConfigurableDataEntity;
import com.iexceed.appzillonbanking.cagl.cm.entity.primary.CmConfigurableDataPK;

@Repository
public interface CmConfigurableDataRepository extends JpaRepository<CmConfigurableDataEntity, CmConfigurableDataPK> {

    Optional<CmConfigurableDataEntity> findByAppIdAndType(String appId, String type);

    List<CmConfigurableDataEntity> findByAppId(String appId);
}
