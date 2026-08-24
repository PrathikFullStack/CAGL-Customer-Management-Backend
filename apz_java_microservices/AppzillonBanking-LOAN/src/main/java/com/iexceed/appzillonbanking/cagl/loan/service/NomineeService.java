package com.iexceed.appzillonbanking.cagl.loan.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iexceed.appzillonbanking.cagl.loan.payload.CustomerApplicationDtls;
import com.iexceed.appzillonbanking.cagl.loan.payload.NomineeDetails;
import com.iexceed.appzillonbanking.cagl.loan.payload.OCRResponselog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class NomineeService {

	private static final Logger logger = LogManager.getLogger(NomineeService.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public JSONObject updateNomineeDetails(CustomerApplicationDtls nomineeDtls, String applicationId, String versionNo)
			throws Exception {
		logger.debug("Printing update nomineeDtls for KM :{} ", nomineeDtls);
		logger.debug("Printing update versionNo :{} ", versionNo);
		int ver = Integer.parseInt(versionNo) + 1;
		String verNo = String.valueOf(ver);
		NomineeDetails nominee = nomineeDtls.getNomineeDetails();
		ObjectMapper mapper = new ObjectMapper();
		OCRResponselog ocrLogs = nominee.getOCRresponselog();
		Map<String, Object> formattedOCR = new HashMap<>();
		if (ocrLogs.getFront() != null) {
			formattedOCR.put("front", mapper.readValue(ocrLogs.getFront().toString(), Object.class));
		}
		if (ocrLogs.getBack() != null) {
			formattedOCR.put("back", mapper.readValue(ocrLogs.getBack().toString(), Object.class));
		}
		String ocrResponsePayload = mapper.writeValueAsString(formattedOCR);

		String sql = """
				    WITH nominee_update AS (
				        UPDATE public.tb_ucno_customer_nominee_details
				        SET latest_version_no = :versionNum,
				            docuNoF = :docuNoF,
				            docuNoB = :docuNoB,
				            ocrresponsepayload = :ocrresponsepayload
				        WHERE application_id = :applicationId
				        AND latest_version_no = :versionNo
				        RETURNING application_id
				    )
				    UPDATE public.tb_ucao_customer_application_master m
				        SET application_type = :applicationType,
				        application_status = :applicationStatus,
				        remarks = :remarks,
				        latest_version_no = :versionNum
				    FROM nominee_update n
				    WHERE m.application_id = n.application_id
				    AND m.latest_version_no = :versionNo
				""";
		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("versionNum", verNo);
		query.setParameter("docuNoF", nominee.getDocuNoF());
		query.setParameter("docuNoB", nominee.getDocuNoB());
		query.setParameter("applicationId", applicationId);
		query.setParameter("versionNo", versionNo);
		query.setParameter("applicationType", nomineeDtls.getApplicationType());
		query.setParameter("applicationStatus", nomineeDtls.getApplicationStatus());
		query.setParameter("ocrresponsepayload", ocrResponsePayload);
		query.setParameter("remarks", nomineeDtls.getRemarks());
		int updated = query.executeUpdate();
		logger.debug("Printing updated :{} ", updated);
		JSONObject json = new JSONObject();
		json.put("versionNo", verNo);
		json.put("rowsUpdated", updated);
		return json;
	}

	@Transactional
	public JSONObject updateNomineeDetailsforInputData(CustomerApplicationDtls nomineeDtls, String applicationId,
			String versionNo) throws Exception {
		logger.debug("Printing update nomineeDtls for RPC :{} ", nomineeDtls);
		int ver = Integer.parseInt(versionNo) + 1;
		String verNo = String.valueOf(ver);
		NomineeDetails nominee = nomineeDtls.getNomineeDetails();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode inputNode = mapper.createObjectNode();
		inputNode.put("name", nominee.getInputData().getName());
		inputNode.put("dob", nominee.getInputData().getDob());
		inputNode.put("memRelation", nominee.getMemRelation());
		inputNode.put("legaldocName", nominee.getLegaldocName());
		inputNode.put("legaldocId", nominee.getLegaldocId());
		inputNode.put("mobileNum", nominee.getInputData().getMobileNum());
		inputNode.put("gender", nominee.getInputData().getGender());

		String updatedInputData = mapper.writeValueAsString(inputNode);
		Map<String, Object> addInfoMap = (Map<String, Object>) nomineeDtls.getAddInfo();
		Boolean isRework = addInfoMap.get("isRework") != null ? (Boolean) addInfoMap.get("isRework") : false;
		Boolean isReworkByMakerChecker = addInfoMap.get("isReworkByMakerChecker") != null
				? (Boolean) addInfoMap.get("isReworkByMakerChecker")
				: false;
		Boolean isOnholdRpc = addInfoMap.get("isOnholdRpc") != null ? (Boolean) addInfoMap.get("isOnholdRpc") : false;

		String sql = """
				     WITH nominee_update AS (
				         UPDATE public.tb_ucno_customer_nominee_details
				         SET latest_version_no = :versionNum,
				             InputData = :inputData
				         WHERE application_id = :applicationId
				         AND latest_version_no = :versionNo
				         RETURNING application_id
				     )
				     UPDATE public.tb_ucao_customer_application_master m
				SET add_info1 = jsonb_set(
				                    jsonb_set(
				                        jsonb_set(
				                            COALESCE(m.add_info1::jsonb, '{}'::jsonb),
				                            '{isRework}', to_jsonb(:isRework)
				                        ),
				                        '{isReworkByMakerChecker}', to_jsonb(:isReworkByMakerChecker)
				                    ),
				                    '{isOnholdRpc}', to_jsonb(:isOnholdRpc)
				                ),
				    application_status = :applicationStatus,
				    request_type = :requestType,
				    remarks = :remarks,
				    updated_by =:updated_by,
				    latest_version_no = :versionNum
				     FROM nominee_update n
				     WHERE m.application_id = n.application_id
				     AND m.latest_version_no = :versionNo
				 """;
		
		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("versionNum", verNo);
		query.setParameter("inputData", updatedInputData);
		query.setParameter("applicationId", applicationId);
		query.setParameter("versionNo", versionNo);
		query.setParameter("isRework", isRework);
		query.setParameter("isReworkByMakerChecker", isReworkByMakerChecker);
		query.setParameter("isOnholdRpc", isOnholdRpc);
		query.setParameter("applicationStatus", nomineeDtls.getApplicationStatus());
		query.setParameter("remarks", nomineeDtls.getRemarks());
		query.setParameter("updated_by", nomineeDtls.getCreatedBy());
		query.setParameter("requestType", nomineeDtls.getRequestType());
		int updated = query.executeUpdate();
		logger.debug("Total rows updated: {}", updated);
		JSONObject json = new JSONObject();
		json.put("versionNo", verNo);
		json.put("rowsUpdated", updated);
		return json;
	}

}
