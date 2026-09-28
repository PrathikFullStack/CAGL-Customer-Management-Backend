package com.iexceed.appzillonbanking.scheduler.dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import com.iexceed.appzillonbanking.scheduler.model.DMSServiceResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class DMSServiceStatusDAO {

	@PersistenceContext
	EntityManager em;

	@Value("${DMS_SERVICE_FAILED_QUERY}")
	private String dmsServiceFailedQuery;

	private static final Logger logger = LogManager.getLogger(DMSServiceStatusDAO.class);

	public List<DMSServiceResponse> fetchDMSServiceFailedErrors() {
		List<DMSServiceResponse> list = new ArrayList<DMSServiceResponse>();
		Query q = em.createNativeQuery(dmsServiceFailedQuery);
		List<Object[]> objList = q.getResultList();
		for (Object[] obj : objList) {
			DMSServiceResponse resp = new DMSServiceResponse();
			resp.setApplicationId((String) obj[0]);
			resp.setPayload((String) obj[1]);
			list.add(resp);
		}
		logger.debug("Printing list for DMS Failed cases: {}", list);
		return list;
	}
}
