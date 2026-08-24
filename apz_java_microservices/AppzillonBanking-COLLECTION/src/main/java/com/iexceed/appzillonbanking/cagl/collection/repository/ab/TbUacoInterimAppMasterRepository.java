package com.iexceed.appzillonbanking.cagl.collection.repository.ab;

import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoInterimApplicationMaster;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoInterimApplicationMasterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TbUacoInterimAppMasterRepository
        extends JpaRepository<TbUacoInterimApplicationMaster, TbUacoInterimApplicationMasterId> {

}
