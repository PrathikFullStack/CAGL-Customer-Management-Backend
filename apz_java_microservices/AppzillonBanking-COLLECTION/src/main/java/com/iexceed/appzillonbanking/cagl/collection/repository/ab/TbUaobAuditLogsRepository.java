package com.iexceed.appzillonbanking.cagl.collection.repository.ab;

import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUaobAuditLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TbUaobAuditLogsRepository extends JpaRepository<TbUaobAuditLogs, String> {

}
