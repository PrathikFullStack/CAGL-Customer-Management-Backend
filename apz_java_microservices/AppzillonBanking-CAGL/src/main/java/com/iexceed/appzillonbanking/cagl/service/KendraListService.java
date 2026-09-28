package com.iexceed.appzillonbanking.cagl.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iexceed.appzillonbanking.cagl.dto.KendraNameIdDto;
import com.iexceed.appzillonbanking.cagl.entity.GkKendraData;
import com.iexceed.appzillonbanking.cagl.payload.KendraListRequestFields;
import com.iexceed.appzillonbanking.cagl.repository.cus.GkKendraDataRepository;

@Service
public class KendraListService {

	private static final Logger logger = LogManager.getLogger(KendraListService.class);

	@Autowired
	private GkKendraDataRepository gkKendraDataRepository;

	public List<KendraNameIdDto> fetchKendraListByBranch(KendraListRequestFields requestFields) {
		logger.debug("Start: fetchKendraListByBranch for request: {}", requestFields);
		if (requestFields == null || requestFields.getBranchIds() == null || requestFields.getBranchIds().isEmpty()) {
			logger.debug("No branchIds provided, returning empty kendra list");
			return Collections.emptyList();
		}

		List<String> branchIds = requestFields.getBranchIds();
		List<Integer> kendraIds = requestFields.getKendraIds();

		List<GkKendraData> kendraDataList;
		if (kendraIds != null && !kendraIds.isEmpty()) {
			kendraDataList = gkKendraDataRepository.findByBranchIdInAndKendraIdInAndKendraStatus(branchIds, kendraIds,
					"Active");
		} else {
			kendraDataList = gkKendraDataRepository.findByBranchIdInAndKendraStatus(branchIds, "Active");
		}
		logger.debug("Kendra data fetched for branchIds {} : {}", branchIds, kendraDataList);

		List<KendraNameIdDto> kendraList = kendraDataList.stream()
				.map(kendra -> KendraNameIdDto.builder().kendraId(kendra.getKendraId())
						.kendraName(kendra.getKendraName()).build())
				.distinct()
				.collect(Collectors.toList());
		logger.debug("End: fetchKendraListByBranch, result: {}", kendraList);
		return kendraList;
	}

}
