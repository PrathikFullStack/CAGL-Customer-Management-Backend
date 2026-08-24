package com.iexceed.appzillonbanking.cagl.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.constants.CommonConstants;
import com.iexceed.appzillonbanking.cagl.customer.payload.CustomerResponseObject;
import com.iexceed.appzillonbanking.cagl.domain.cus.GkEarningMember;
import com.iexceed.appzillonbanking.cagl.domain.cus.GkIncomeAssesment;
import com.iexceed.appzillonbanking.cagl.domain.cus.GkKendraAssignment;
import com.iexceed.appzillonbanking.cagl.domain.cus.GkKendraUserId;
import com.iexceed.appzillonbanking.cagl.domain.cus.GkLoanData;
import com.iexceed.appzillonbanking.cagl.domain.cus.GkLoanPurpose;
import com.iexceed.appzillonbanking.cagl.domain.cus.GkMLoanProduct;
import com.iexceed.appzillonbanking.cagl.domain.cus.GkProductSubPurpose;
import com.iexceed.appzillonbanking.cagl.domain.cus.ProductResponseObject;
import com.iexceed.appzillonbanking.cagl.dto.CustData;
import com.iexceed.appzillonbanking.cagl.dto.CustEarnings;
import com.iexceed.appzillonbanking.cagl.dto.GkNomineeDTO;
import com.iexceed.appzillonbanking.cagl.dto.GroupNameDto;
import com.iexceed.appzillonbanking.cagl.dto.IncomeAssesment;
import com.iexceed.appzillonbanking.cagl.dto.KendraData;
import com.iexceed.appzillonbanking.cagl.dto.KendraDetailsDto;
import com.iexceed.appzillonbanking.cagl.dto.LoanData;
import com.iexceed.appzillonbanking.cagl.dto.LoanEligible;
import com.iexceed.appzillonbanking.cagl.dto.MahiLeadDto;
import com.iexceed.appzillonbanking.cagl.dto.CustomerInsuranceNotificationDto;
import com.iexceed.appzillonbanking.cagl.dto.OfficeDataDto;
import com.iexceed.appzillonbanking.cagl.dto.ProductData;
import com.iexceed.appzillonbanking.cagl.dto.ProductPurpose;
import com.iexceed.appzillonbanking.cagl.entity.BranchLatlong;
import com.iexceed.appzillonbanking.cagl.entity.GkCustomerData;
import com.iexceed.appzillonbanking.cagl.entity.GkKendraData;
import com.iexceed.appzillonbanking.cagl.entity.GkNominee;
import com.iexceed.appzillonbanking.cagl.entity.GkUnifiedData;
import com.iexceed.appzillonbanking.cagl.entity.GkUserData;
import com.iexceed.appzillonbanking.cagl.entity.KendraLatLongEntity;
import com.iexceed.appzillonbanking.cagl.entity.MahiLead;
import com.iexceed.appzillonbanking.cagl.entity.CustomerInsuranceNotification;
import com.iexceed.appzillonbanking.cagl.entity.OfficeData;
import com.iexceed.appzillonbanking.cagl.payload.AsmiUserData;
import com.iexceed.appzillonbanking.cagl.payload.AsmiUserResponse;
import com.iexceed.appzillonbanking.cagl.payload.KendraDataProjection;
import com.iexceed.appzillonbanking.cagl.payload.KendraRequestField;
import com.iexceed.appzillonbanking.cagl.payload.ResponseObject;
import com.iexceed.appzillonbanking.cagl.repository.cus.GkGroupDataRepository;
import com.iexceed.appzillonbanking.cagl.repository.cus.GkNomineeRepository;
import com.iexceed.appzillonbanking.cagl.repository.cus.GkUnifiedDataRepository;
import com.iexceed.appzillonbanking.cagl.repository.cus.MahiLeadRepository;
import com.iexceed.appzillonbanking.cagl.repository.cus.CustInsNotificationRepository;
import com.iexceed.appzillonbanking.cagl.user.roles.payload.UserResponseObject;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;

@Service
public class FetchDetailsService {

	private static final Logger logger = LogManager.getLogger(FetchDetailsService.class);

	public static final String EXCEPTION_MSG = "Something went wrong, Please try again!!";

	@Autowired
	CdhDao dao;

	@Value("${url.kendra.assignment.service}")
	String kendraAssinedUrl;

	@Value("${url.kendra.amiuserdetails.service}")
	String fetchUserDataUrl;	

	@Autowired
	private RestTemplate template;

	@Autowired
	private GkUnifiedDataRepository gkunifiedrepo;

	@Autowired
	private GkGroupDataRepository gkGroupDataRepository;

	@Autowired
	private MahiLeadRepository leadRepository;

	@Autowired
	private GkNomineeRepository gkNomineeRepository;
	
	@Autowired
	private CustInsNotificationRepository custInsNotificationRepository;
	
	
	@CircuitBreaker(name = "Fetch_userdata")
	public AsmiUserResponse getUserDetailsData(GkKendraUserId userId) throws URISyntaxException {
		URI url = new URI(fetchUserDataUrl);
		logger.debug("Fetched branchName & branchId based userId for URL is: {}", url);
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<GkKendraUserId> entity = new HttpEntity<>(userId, header);
		AsmiUserResponse userDetails = new AsmiUserResponse();
		try {
			ResponseEntity<AsmiUserResponse> response = template.exchange(url, HttpMethod.POST, entity,
					AsmiUserResponse.class);
			logger.debug("Fetched branchName & branchId from ASMI table: {}", response);
			if (response.getBody() == null) {
				logger.error("No user details returned for userId: {}", userId);
				userDetails.setAddInfo1("APIIssue");
				userDetails.setAddInfo2("APIIssue");
				return userDetails;
			}
			return response.getBody();
		} catch (Exception ex) {
			userDetails.setAddInfo1("APIIssue");
			userDetails.setAddInfo2("APIIssue");
			logger.error("Exception during ASMI API call for userId: {}", userId, ex);
			return userDetails;
		}
	}
		

	public Map<String, Object> fetchCdhKendraDetailsForDeo(String userId,String branch, List<String> kList,
			KendraRequestField kendraRequestField) {
		List<ResponseObject> respObjList = new ArrayList<>();
		List<OfficeData> officeList = null;
		Map<Integer, List<CustData>> customerListByKendra = null;
		Map<String, Object> responseMap = new HashMap<>();
		try {
			GkKendraUserId gkKendraUserId = new GkKendraUserId();
			gkKendraUserId.setUserId(userId);
			AsmiUserResponse asmiUserResponse = getUserDetailsData(gkKendraUserId);
			logger.debug("asmiUserResponse for DEO/BST: {}", asmiUserResponse);
			String designation;
			List<GkKendraData> kendraList = new ArrayList<>();
			if (asmiUserResponse == null || "APIIssue".equalsIgnoreCase(asmiUserResponse.getAddInfo1())
					|| "APIIssue".equalsIgnoreCase(asmiUserResponse.getAddInfo2())) {			
				GkUserData userData = dao.fetchUserRole(userId);
				String branchId = userData.getHierarchyId();
				userData.setHierarchyId(branchId);			
				if(branch != null && !branch.trim().isEmpty()) {
					logger.error("BST User for branch for Exiting code : {}", branch);
					branchId=branch;
				}			
				logger.debug("userData for DEO/BST: {}", userData);
				designation = (null != userData) ? userData.getUserdesignation() : "";
				kendraList = dao.fetchKendraDetailsForDEO(userId, userData, CommonConstants.KENDRA_STATUS, kList);
				logger.debug("CDH Hierarchy for DEO/BST : {}", designation);
				logger.debug("CDH User Data for DEO/BST : {}", kendraList);				
			} else {	
				String userRole = asmiUserResponse.getAddInfo1();
				String branchId = asmiUserResponse.getAddInfo2();
				if(branch != null && !branch.trim().isEmpty()) {
					logger.debug("BST User for branch : {}", branch);
					branchId=branch;
				}
				AsmiUserData asmiUserData = new AsmiUserData();
				asmiUserData.setUserId(userId);
				asmiUserData.setUserdesignation(userRole);
				asmiUserData.setHierarchyId(branchId);
				logger.debug("ASMI asmiUserData for BST : {}", asmiUserData);
				designation = (null != asmiUserData) ? asmiUserData.getUserdesignation() : "";
				kendraList = dao.fetchKendraDetailsForDEONew(userId, asmiUserData, CommonConstants.KENDRA_STATUS,
						kList);
				logger.debug("ASMI designation : {}", designation);
				logger.debug("ASMI User Data: {}", kendraList);			
			}
		     
		     //Exiting code		     
		   //  GkUserData userData = dao.fetchUserRole(userId);
			 //String designation = (null != userData) ? userData.getUserdesignation() : "";
			 //List<GkKendraData> kendraList = dao.fetchKendraDetailsForDEO(userId, userData, CommonConstants.KENDRA_STATUS, kList);				
			
				/**
				 * @author Ankit.CAG Fetching Mahi-Leads count based on KendraIds
				 */
				Map<String, Integer> pendingKendrasCount = null;
				Map<String, Integer> KendrasCount = null;
				try {
					logger.debug("Fetching distKendras");
					List<String> distKendras = kendraList.stream().map(e -> e.getKendraId() + "").distinct()
							.collect(Collectors.toList());
					logger.debug("Fetching distKendras Count", distKendras);
					logger.debug("Fetching pending Mahi-LEAD count");
					pendingKendrasCount = leadRepository.fetchPendingLeadCount(distKendras).stream().collect(Collectors
							.toMap(row -> (String) row[0], row -> row[1] == null ? 0 : ((Number) row[1]).intValue()));
					logger.debug("Fetching  Mahi-LEAD count");
					KendrasCount = leadRepository.fetchMahiLeadCount(distKendras).stream().collect(Collectors
							.toMap(row -> (String) row[0], row -> row[1] == null ? 0 : ((Number) row[1]).intValue()));
					logger.debug("END Fetching Mahi-LEAD count");
				} catch (Exception e) {
					logger.error("Error Found" + e.getStackTrace());
				}
				// --------END-----------

			if (StringUtils.isNotBlank(designation) && CommonConstants.KM_ROLE.equalsIgnoreCase(designation)
					&& !kendraList.isEmpty() || StringUtils.isNotBlank(designation) && CommonConstants.BST_ROLE.equalsIgnoreCase(designation)
					&& !kendraList.isEmpty()) {
				List<Integer> kendraIds = kendraList.stream().map(GkKendraData::getKendraId).distinct().toList();
				List<String> branchIds = kendraList.stream().map(GkKendraData::getBranchId).distinct().toList();				
				List<CustData> customerList =null;
				officeList = dao.fetchKendraOfficeDataList(branchIds);
			}
			for (GkKendraData gk : kendraList) {
				ResponseObject respobj = new ResponseObject();
				KendraData kendra = new KendraData();
				kendra.setKendraId(gk.getKendraId());
				kendra.setKendraName(gk.getKendraName());
				kendra.setKmName(gk.getKmName());
				kendra.setBranchId(gk.getBranchId());
				kendra.setVillageType(gk.getVillageType());
				kendra.setKendraAddr(gk.getKendraAddr());
				kendra.setState(gk.getState());
				kendra.setDistrict(gk.getDistrict());
				kendra.setTaluk(gk.getTaluk());
				kendra.setAreaType(gk.getAreaType());
				kendra.setVillage(gk.getVillage());
				kendra.setPincode(gk.getPincode());
				kendra.setMeetingFrequency(gk.getMeetingFrequency());
				kendra.setFirstMeetingDate(gk.getFirstMeetingDate());
				kendra.setNextMeetingDate(gk.getNextMeetingDate());
				kendra.setMeetingDay(gk.getMeetingDay());
				kendra.setMeetingPlace(gk.getMeetingPlace());
				kendra.setMeetingStartTime(gk.getMeetingStartTime());
				kendra.setEndingTime(gk.getEndingTime());
				kendra.setDistance(gk.getDistance());
				kendra.setLeader(gk.getLeader());
				kendra.setSecretary(gk.getSecretary());
				kendra.setCreatedBy(gk.getCreatedBy());
				kendra.setCreatedTS(gk.getCreatedTS());
				kendra.setUpdatedBy(gk.getUpdatedBy());
				kendra.setKendraStatus(gk.getKendraStatus());
				kendra.setActivationDate(gk.getActivationDate());
				kendra.setKmId(gk.getKmId());

				if (customerListByKendra != null && customerListByKendra.get(gk.getKendraId()) != null) {
					kendra.setCustomerDtls(customerListByKendra.get(gk.getKendraId()));
				}
				if (officeList != null && !officeList.isEmpty()) {
					List<OfficeData> officeDataFiltered = officeList.stream()
							.filter(office -> gk.getBranchId().equals(office.getBranchId())).toList();
					if (!officeDataFiltered.isEmpty()) {
						OfficeData office = officeDataFiltered.get(0);
						OfficeDataDto officeDto = new OfficeDataDto();
						BeanUtils.copyProperties(officeDto, office);
						kendra.setOfficeData(officeDto);
					}
				}

				/**
				 * @author Ankit.CAG Set Mahi-Leads count based on KendraId
				 */
				logger.debug("Set Mahi-LEAD count1");
				try {
					kendra.setPendingLeads(pendingKendrasCount.get(gk.getKendraId() + ""));
					logger.debug("Set Prnding Mahi-LEAD count1::{}");
					kendra.setMahiLeads(KendrasCount.get(gk.getKendraId() + ""));
				} catch (Exception e) {
					logger.error("Error Found" + e.getStackTrace());
				}
				logger.debug("END Set Mahi-LEAD count");
				// ------END-----
				respobj.setKendraDtls(kendra);
				respObjList.add(respobj);
			}
			responseMap.put("loans", respObjList);
		} catch (Exception exp) {
			logger.error(CommonConstants.EXCEP_OCCURED, exp);
		}
		logger.debug("Final Response Time::{}", LocalDateTime.now());
		logger.debug("Kendra Response object::{}", responseMap);
		return responseMap;
	}

	// new added for Active User
	public Map<String, Object> fetchCdhKendraIdListDetails(String userId, KendraRequestField kendraRequestField) {
		Map<String, Object> responseMap = new HashMap<>();
		try {
			List<KendraDataProjection> kendraList = dao.fetchKendraIdListDetailsNew(userId,
					CommonConstants.KENDRA_STATUS, kendraRequestField.getBranchId());
			logger.debug("Printing kendraList for BM::{}", kendraList);
			if (kendraList != null && !kendraList.isEmpty()) {
				List<String> branchIds = kendraList.stream().map(KendraDataProjection::getBranchId).distinct().toList();
				List<OfficeData> officeList = dao.fetchKendraOfficeDataList(branchIds);
				ObjectMapper mapper = new ObjectMapper();
				List<Map<String, Object>> finalList = new ArrayList<>();
				for (KendraDataProjection kendra : kendraList) {
					Map<String, Object> kendraMap = mapper.convertValue(kendra, Map.class);
					if (officeList != null && !officeList.isEmpty()) {
						List<OfficeData> officeDataFiltered = officeList.stream()
								.filter(office -> kendra.getBranchId().equals(office.getBranchId())).toList();
						if (!officeDataFiltered.isEmpty()) {
							kendraMap.put("officeData", officeDataFiltered.get(0));
						}
					}
					finalList.add(kendraMap);
				}
				responseMap.put("loans", finalList);
			}
		} catch (Exception exp) {
			logger.error(CommonConstants.EXCEP_OCCURED, exp);
		}
		logger.debug("Printing responseMap::{}", responseMap);
		return responseMap;
	}

	public Map<String, Object> fetchCdhKendraDetails(String userId, List<String> kList, List<String> excludedkList, String roleId,KendraRequestField kendraRequestField) { 
		List<ResponseObject> respObjList = new ArrayList<>();
		List<OfficeData> officeList = null;
		Map<Integer, List<CustData>> customerListByKendra = null;
		Map<String, Object> responseMap = new HashMap<>();
		try {
			GkKendraUserId gkKendraUserId = new GkKendraUserId();
			gkKendraUserId.setUserId(userId);
			AsmiUserResponse asmiUserResponse = getUserDetailsData(gkKendraUserId);
			logger.debug("Printing asmiUserResponse for KM: {}", asmiUserResponse); 
			String designation;
			List<GkKendraData> kendraList = new ArrayList<>();
			if ("APIIssue".equalsIgnoreCase(asmiUserResponse.getAddInfo1())
					|| "APIIssue".equalsIgnoreCase(asmiUserResponse.getAddInfo2())) {
				GkUserData userData = dao.fetchUserRole(userId);
				designation = (null != userData) ? userData.getUserdesignation() : "";
				kendraList = dao.fetchKendraDetails(userId, userData, CommonConstants.KENDRA_STATUS, kList,
						excludedkList, roleId);
				logger.debug("CDH data for KM: {}", designation);
				logger.debug("CDH User Data for KM: {}", kendraList);
			} else {
				String userRole = asmiUserResponse.getAddInfo1();
				String branchId = asmiUserResponse.getAddInfo2();
				AsmiUserData asmiUserData = new AsmiUserData();
				asmiUserData.setUserId(userId);
				asmiUserData.setUserdesignation(userRole);
				asmiUserData.setHierarchyId(branchId);
				designation = (null != asmiUserData) ? asmiUserData.getUserdesignation() : "";
				kendraList = dao.fetchKendraDetailsNew(userId, asmiUserData, CommonConstants.KENDRA_STATUS, kList,
						excludedkList, roleId);
				logger.debug("ASMI designation for KM : {}", designation);
				logger.debug("ASMI User Data for KM :  {}", kendraList);
			}

			//Exiting old code
			//GkUserData userData = dao.fetchUserRole(userId);
			//String designation = (null != userData) ? userData.getUserdesignation() : "";
		 //	List<GkKendraData> kendraList = dao.fetchKendraDetails(userId, userData, CommonConstants.KENDRA_STATUS, kList,excludedkList, roleId);
								
			if ((StringUtils.isNotBlank(designation) && CommonConstants.KM_ROLE.equalsIgnoreCase(designation)
					&& !kendraList.isEmpty()) || roleId.equalsIgnoreCase("KM")) {
				
				List<Integer> kendraIds = kendraList.stream().map(GkKendraData::getKendraId).distinct().toList();
				List<String> branchIds = kendraList.stream().map(GkKendraData::getBranchId).distinct().toList();			
				List<CustData> customerList = getCustomerDetailListByKendra(kendraIds);
				customerListByKendra = customerList.stream().collect(Collectors.groupingBy(CustData::getKendraId));
				officeList = dao.fetchKendraOfficeDataList(branchIds);			
			}
	
			for (GkKendraData gk : kendraList) {
				ResponseObject respobj = new ResponseObject();
				KendraData kendra = new KendraData();
				kendra.setKendraId(gk.getKendraId());									
				kendra.setKendraName(gk.getKendraName());
				kendra.setKmName(gk.getKmName());
				kendra.setBranchId(gk.getBranchId());
				kendra.setVillageType(gk.getVillageType());
				kendra.setKendraAddr(gk.getKendraAddr());
				kendra.setState(gk.getState());
				kendra.setDistrict(gk.getDistrict());
				kendra.setTaluk(gk.getTaluk());
				kendra.setAreaType(gk.getAreaType());
				kendra.setVillage(gk.getVillage());
				kendra.setPincode(gk.getPincode());
				kendra.setMeetingFrequency(gk.getMeetingFrequency());
				kendra.setFirstMeetingDate(gk.getFirstMeetingDate());
				kendra.setNextMeetingDate(gk.getNextMeetingDate());
				kendra.setMeetingDay(gk.getMeetingDay());
				kendra.setMeetingPlace(gk.getMeetingPlace());
				kendra.setMeetingStartTime(gk.getMeetingStartTime());
				kendra.setEndingTime(gk.getEndingTime());
				kendra.setDistance(gk.getDistance());
				kendra.setLeader(gk.getLeader());
				kendra.setSecretary(gk.getSecretary());
				kendra.setCreatedBy(gk.getCreatedBy());
				kendra.setCreatedTS(gk.getCreatedTS());
				kendra.setUpdatedBy(gk.getUpdatedBy());
				kendra.setKendraStatus(gk.getKendraStatus());
				kendra.setActivationDate(gk.getActivationDate());
				kendra.setKmId(gk.getKmId());

				if (customerListByKendra != null && customerListByKendra.get(gk.getKendraId()) != null) {
					kendra.setCustomerDtls(customerListByKendra.get(gk.getKendraId()));
				}
				if (officeList != null && !officeList.isEmpty()) {
					List<OfficeData> officeDataFiltered = officeList.stream()
							.filter(office -> gk.getBranchId().equals(office.getBranchId())).toList();
					if (!officeDataFiltered.isEmpty()) {
						OfficeData office = officeDataFiltered.get(0);
						OfficeDataDto officeDto = new OfficeDataDto();
						BeanUtils.copyProperties(officeDto, office);
						kendra.setOfficeData(officeDto);
					}
				}
				respobj.setKendraDtls(kendra);
				respObjList.add(respobj);
			}
			responseMap.put("loans", respObjList);
		} catch (Exception exp) {
			logger.error(CommonConstants.EXCEP_OCCURED, exp);
		}
		logger.debug("Final Response Time::{}", LocalDateTime.now());
		logger.debug("Kendra Response object::{}", responseMap);
		return responseMap;
	}

	
	public Map<String, Object> fetchCDHCustomerDetails(String customerId) {
		List<ResponseObject> respObjList = new ArrayList<>();
		Map<Integer, List<CustData>> customerListByKendra = null;
		Map<String, Object> responseMap = new HashMap<>();
		List<CustData> customerList = getCustomerDetailListByCustomerId(customerId);
		logger.debug("Customer Details By CustomerID {}" + customerList);
		// Handle empty list
		if (customerList == null || customerList.isEmpty()) {
			logger.debug("No customer data found for customerId: {}", customerId);
			responseMap.put("loans", respObjList);
			return responseMap;
		}
		customerListByKendra = customerList.stream().collect(Collectors.groupingBy(CustData::getKendraId));
		logger.debug("Customer Details By CustomerID" + customerListByKendra);
		ResponseObject respobj = new ResponseObject();
		KendraData kendra = new KendraData();
		if (customerListByKendra != null) {
			kendra.setCustomerDtls(customerListByKendra.get(customerList.get(0).getKendraId()));
		}
		respobj.setKendraDtls(kendra);
		respObjList.add(respobj);
		responseMap.put("loans", respObjList);
		logger.debug("responseMap" + responseMap);
		return responseMap;
	}
	
	public List<CustData> getCustomerDetailListByKendra(List<Integer> kendraIds) {
		List<CustData> custList = new ArrayList<>();
		try {
			logger.debug("Before Execution Time::{}", LocalDateTime.now());
			List<GkUnifiedData> results = gkunifiedrepo.fetchCustomerDataNew1(kendraIds);

			/**
			 * @author Ankit.CAG Fetching Mahi-Leads based on KendraIDs
			 */
			logger.debug("Fetching11 Mahi-LEAD::{}");
			List<MahiLead> fetchMahiLeads = leadRepository
					.fetchMahiLeadByKendra(kendraIds == null ? Collections.emptyList()
							: kendraIds.stream().map(String::valueOf).collect(Collectors.toList()));
			logger.debug("Fetching Mahi-LEAD::{}", fetchMahiLeads == null ? 0 : fetchMahiLeads.size());
			Map<String, Map<String, List<MahiLead>>> groupingMahiLeads = Optional.ofNullable(fetchMahiLeads)
					.orElse(Collections.emptyList()).stream().collect(
							Collectors.groupingBy(MahiLead::getKendra, Collectors.groupingBy(MahiLead::getCustomerId)));
			logger.debug("Grouping Mahi-LEAD::{}", groupingMahiLeads == null ? 0 : groupingMahiLeads.size());
			
			logger.debug("fetching GkNominee Data::{}");
			List<GkNominee> fetchGkNominee = gkNomineeRepository
					.fetchGkNomineeByCustId(results == null ? Collections.emptyList()
							: results.stream().map(GkUnifiedData::getCustomerId).collect(Collectors.toList()));
			logger.debug("fetchGkNominee ::{}", fetchGkNominee == null ? 0 : fetchGkNominee.size());
			Map<String, List<GkNominee>> groupingGkNominee = Optional.ofNullable(fetchGkNominee)
					.orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(GkNominee::getCustid));
			logger.debug("Grouping fetchGkNominee::{}", groupingGkNominee == null ? 0 : groupingGkNominee.size());

			// ----------END-------------

			logger.debug("After Execution Time::{}", LocalDateTime.now());
			if (results != null && !results.isEmpty()) {
				List<LoanData> loanDtlList = new ArrayList<>();
				List<LoanEligible> loanElgList = new ArrayList<>();
				List<CustEarnings> earnList = new ArrayList<>();
				List<IncomeAssesment> incomeList = new ArrayList<>();
				for (GkUnifiedData result : results) {
					//New Parameter Added @MapGroupIds Used to initialize @GroupName field
					this.appendCustListDataNew(result, custList);
					this.appendLoanDtlListDataNew(result, loanDtlList);
					this.appendEligibleLoanListDataNew(result, loanElgList, null);
					this.appendEarningMemberListDataNew(result, earnList);
					this.appendIncomeAssementListDataNew(result, incomeList);
				}
				custList = custList.stream().collect(Collectors.toMap(CustData::getCustomerId, person -> person,
						(existing, replacement) -> existing)).values().stream().toList();
				
				/**
				 * @Unnati_GroupId_Flag_CR
				 */
				Map<Integer, List<GroupNameDto>> MapGroupIds;
				List<GroupNameDto> groupNames = new ArrayList<>();
				logger.debug("Fetching unnatiGroupId Names::{}");
				List<Integer> distinctGroupIds = custList.stream().map(CustData::getGroupId).filter(Objects::nonNull)
						.distinct().collect(Collectors.toList());
				logger.debug("Fetching distinctGroupIds ::{}", distinctGroupIds);
				groupNames = gkGroupDataRepository.getGroupNames(distinctGroupIds);
				logger.debug("Fetching unnatiGroupId Names Size::{}", groupNames.size());
				logger.debug("Fetching unnatiGroupId Names::{}", groupNames);
				MapGroupIds = groupNames.stream().collect(Collectors.groupingBy(GroupNameDto::getGroupId));

				// ---End
				custList.forEach(cusData -> {
					List<LoanData> loanDtlListFiltered = loanDtlList.stream()
							.filter(loan -> cusData.getCustomerId().equals(loan.getCustomerId())).collect(Collectors
									.toMap(LoanData::getLoanId, loan -> loan, (existing, replacement) -> existing))
							.values().stream().toList();
					cusData.setLoanDtls(loanDtlListFiltered);
					List<LoanEligible> loanElgListFiltered = loanElgList.stream()
							.filter(elg -> cusData.getCustomerId().equals(elg.getCustomerId()))
							.collect(Collectors.toMap(LoanEligible::getCustomerId, elg -> elg,
									(existing, replacement) -> existing))
							.values().stream().toList();
					cusData.setEligibleLoan(loanElgListFiltered);

					List<CustEarnings> earnListFiltered = earnList.stream()
							.filter(earn -> cusData.getCustomerId().equals(earn.getCustomerId())).collect(Collectors
									.toMap(CustEarnings::getRecId, earn -> earn, (existing, replacement) -> existing))
							.values().stream().toList();
					cusData.setEarnings(earnListFiltered);

					List<IncomeAssesment> incomeListFiltered = incomeList.stream()
							.filter(loanDta -> cusData.getCustomerId().equals(loanDta.getCustomerId())).toList();
					if (!incomeListFiltered.isEmpty()) {
						cusData.setIncome(incomeListFiltered.subList(0, 1));
					}

					/**
					 * @author Ankit.CAG initialize DTO MahiLead
					 */
					try {
						logger.debug("cusData.getKendraId() :{}", cusData.getKendraId());
						logger.debug("initialize MahiLead1 in JSON");
						cusData.setMahiLeads(groupingMahiLeads
								.getOrDefault(String.valueOf(cusData.getKendraId()), Collections.emptyMap())
								.getOrDefault(cusData.getCustomerId(), Collections.emptyList()).stream()
								.map(mahilead -> {
									logger.debug("MahiLead Data1: {}", mahilead);
									try {
										MahiLeadDto mahiLeadDto = MahiLeadDto.builder().branchId(mahilead.getBranchId())
												.customerId(mahilead.getCustomerId())
												.customerName(mahilead.getCustomerName())
												.dateSubmitted(mahilead.getDateSubmitted().toLocalDate())
												.eligibleLoanAmount(mahilead.getEligibleLoanAmount())
												.kendra(mahilead.getKendra()).leadStatus(mahilead.getLeadStatus())
												.leadUpdateDate(mahilead.getLeadUpdateDate().toLocalDate())
												.loanPurpose(mahilead.getLoanPurpose())
												.productName(mahilead.getProductName())
												.transactionId(mahilead.getTransactionId())
												.applicationName(mahilead.getApplicationName())										
												.build();
										logger.debug("MahiLeadDto1: {}", mahiLeadDto);
										return mahiLeadDto;
									} catch (Exception x) {
										logger.error("Exception found in MahiLead", x);
										return new MahiLeadDto();
									}
								}).collect(Collectors.toList()));
					} catch (Exception e) {
						logger.error("Excption occer:initialize DTO MahiLead", e.getMessage());
					}
					// --------end----------
					/*
					 * @fetching-Nominee Details Base on CustomerId
					 */
					try {
						logger.debug("cusData.getCustomerId() :{}", cusData.getCustomerId());
						logger.debug("initialize CustId in JSON");
						cusData.setNomineeList(groupingGkNominee
								.getOrDefault(String.valueOf(cusData.getCustomerId()), Collections.emptyList()).stream()
								.map(gkNominee -> {
									logger.debug("gkNominee Data1: {}", gkNominee);
									try {
										GkNomineeDTO gkNomineedto = GkNomineeDTO.builder()
												.applicationId(gkNominee.getApplicationId())
												.createdAt(gkNominee.getCreatedAt().toString()).custid(gkNominee.getCustid())
												.nomineeName(gkNominee.getNomineeName())
												.dob(gkNominee.getDob()).docuNoB(gkNominee.getDocuNoB())
												.docuNoF(gkNominee.getDocuNoF()).gender(gkNominee.getGender())
												.legaldocId(gkNominee.getLegaldocId())
												.legaldocName(gkNominee.getLegaldocName())
												.memRelation(gkNominee.getMemRelation())
												.mobileNum(gkNominee.getMobileNum()).status(gkNominee.getStatus())
												.updatedAt(gkNominee.getUpdatedAt().toString()).build();
										logger.debug("gkNomineedto: {}", gkNomineedto);
										return gkNomineedto;
									} catch (Exception x) {
										logger.error("Exception found in gkNomineedto", x);
										return new GkNomineeDTO();
									}
								}).collect(Collectors.toList()));
					} catch (Exception e) {
						logger.error("Excption occer:initialize gkNomineedto", e.getMessage());
					}
					// --------end----------
					/*
					 * @fetching-GroupNameDto Details Base on GroupId
					 */
					try {
						List<GroupNameDto> list = MapGroupIds.get(cusData.getGroupId());
						// logger.debug("gkNomineedto: {}", MapGroupIds.get(cusData.getGroupId()));
						if (list != null && !list.isEmpty() && list.get(0).getName() != null) {
							cusData.setGroupName(list.get(0).getName());
						} else {
							// logger.debug("Group name not found for groupId: {}", cusData.getGroupId());
						}
					} catch (Exception e) {
						logger.error("Excption occer:initialize unnati GroupIds", e.getMessage());
					}
					// ----------END----------

				});
			}
		} catch (Exception exp) {
			logger.error(CommonConstants.EXCEP_OCCURED, exp);
		}
		logger.debug("Final response from getCustomerDetailListByKendra method::{}", custList);
		logger.debug("Final Execution Time::{}", LocalDateTime.now());
		return custList;
	}
	
	
	public List<CustData> getCustomerDetailListByCustomerId(String customerId) {
		List<CustData> custList = new ArrayList<>();
		try {
			logger.debug("Before Execution Time::{}", LocalDateTime.now());
			List<GkUnifiedData> results = gkunifiedrepo.fetchCustomerDataNewByCustomerId(customerId);
			logger.debug("After Execution Time::{}", LocalDateTime.now());
			/*
			 * if (results != null && !results.isEmpty()) {
			 * logger.error("Result is not empty"); for (GkUnifiedData result : results) {
			 * this.appendCustListDataNew(result, custList); } custList =
			 * custList.stream().collect(Collectors.toMap(CustData::getCustomerId, person ->
			 * person, (existing, replacement) -> existing)).values().stream().toList(); }
			 */
			if (results != null && !results.isEmpty()) {
				List<LoanData> loanDtlList = new ArrayList<>();
				List<LoanEligible> loanElgList = new ArrayList<>();
				List<CustEarnings> earnList = new ArrayList<>();
				List<IncomeAssesment> incomeList = new ArrayList<>();
				for (GkUnifiedData result : results) {
					this.appendCustListDataNew(result, custList);
					this.appendLoanDtlListDataNew(result, loanDtlList);
					this.appendEligibleLoanListDataNew(result, loanElgList, null);
					this.appendEarningMemberListDataNew(result, earnList);
					this.appendIncomeAssementListDataNew(result, incomeList);
				}

				/**
				 * @author Ankit.CAG
				 */
				logger.debug("fetching GkNominee Data::{}");
				List<GkNominee> fetchGkNominee = gkNomineeRepository.fetchGkNomineeByCustId(Arrays.asList(customerId));
				logger.debug("fetchGkNominee ::{}", fetchGkNominee == null ? 0 : fetchGkNominee.size());
				// -----END-----
				custList = custList.stream().collect(Collectors.toMap(CustData::getCustomerId, person -> person,
						(existing, replacement) -> existing)).values().stream().toList();
				custList.forEach(cusData -> {
					List<LoanData> loanDtlListFiltered = loanDtlList.stream()
							.filter(loan -> cusData.getCustomerId().equals(loan.getCustomerId())).collect(Collectors
									.toMap(LoanData::getLoanId, loan -> loan, (existing, replacement) -> existing))
							.values().stream().toList();
					cusData.setLoanDtls(loanDtlListFiltered);
					List<LoanEligible> loanElgListFiltered = loanElgList.stream()
							.filter(elg -> cusData.getCustomerId().equals(elg.getCustomerId()))
							.collect(Collectors.toMap(LoanEligible::getCustomerId, elg -> elg,
									(existing, replacement) -> existing))
							.values().stream().toList();
					cusData.setEligibleLoan(loanElgListFiltered);

					List<CustEarnings> earnListFiltered = earnList.stream()
							.filter(earn -> cusData.getCustomerId().equals(earn.getCustomerId())).collect(Collectors
									.toMap(CustEarnings::getRecId, earn -> earn, (existing, replacement) -> existing))
							.values().stream().toList();
					cusData.setEarnings(earnListFiltered);

					List<IncomeAssesment> incomeListFiltered = incomeList.stream()
							.filter(loanDta -> cusData.getCustomerId().equals(loanDta.getCustomerId())).toList();
					if (!incomeListFiltered.isEmpty()) {
						cusData.setIncome(incomeListFiltered.subList(0, 1));
					}
					/**
					 * @author Ankit.CAG
					 */
					if (fetchGkNominee == null || fetchGkNominee.isEmpty()) {
						logger.warn("No nominee data found for customerId: {}", customerId);
						cusData.setNomineeList(Collections.emptyList());
					} else {
						logger.debug("Total nominees fetched: {}", fetchGkNominee.size());

						cusData.setNomineeList(fetchGkNominee.stream().map(gkNominee -> {
							logger.debug("gkNominee Data1: {}", gkNominee);

							GkNomineeDTO gkNomineedto = GkNomineeDTO.builder()
									.applicationId(gkNominee.getApplicationId()).createdAt(gkNominee.getCreatedAt().toString())
									.custid(gkNominee.getCustid()).nomineeName(gkNominee.getNomineeName())
									.dob(gkNominee.getDob()).docuNoB(gkNominee.getDocuNoB())
									.docuNoF(gkNominee.getDocuNoF()).gender(gkNominee.getGender())
									.legaldocId(gkNominee.getLegaldocId()).legaldocName(gkNominee.getLegaldocName())
									.memRelation(gkNominee.getMemRelation()).mobileNum(gkNominee.getMobileNum())
									.status(gkNominee.getStatus()).updatedAt(gkNominee.getUpdatedAt().toString()).build();
							return gkNomineedto;
						}).collect(Collectors.toList()));
					}
					//---------END----------
				});
			}
		} catch (Exception exp) {
			logger.error(CommonConstants.EXCEP_OCCURED, exp);
		}
		logger.debug("Final response from getCustomerDetailListByKendra method::{}", custList);
		logger.debug("Final Execution Time::{}", LocalDateTime.now());
		return custList;
	}
	
	private void appendCustListDataNew(GkUnifiedData gkCustData, List<CustData> custList) {

		try {
			if (gkCustData == null) {
				return;
			}	
			
			boolean hasInsurance = false;
	        try {
	            if (gkCustData.getCustomerId() != null && !gkCustData.getCustomerId().isEmpty()) {
	                hasInsurance = custInsNotificationRepository.existsByCustomerId(gkCustData.getCustomerId());
	            }
	        } catch (Exception e) {
	            logger.error("Error checking insNotify for customerId {}: {}", gkCustData.getCustomerId(), e.getMessage());
	        }

	        String insNotifyValue = hasInsurance ? "yes" : "no";
			
			CustData cusData = CustData.builder().customerId(gkCustData.getCustomerId())
					.customerName(gkCustData.getCustomerName()).kendraId(safeParseInt(gkCustData.getKendraId()))
					.groupId(safeParseInt(gkCustData.getGroupId())).branchName(gkCustData.getBranchName())
					.primaryType(gkCustData.getPrimaryType()).primaryId(gkCustData.getPrimaryId())
					.dob(gkCustData.getDob()).maritalStatus(gkCustData.getMaritalStatus())
					.address(gkCustData.getAddress()).bankAccNo(gkCustData.getBankAccountNumber())
					.bankAccName(gkCustData.getBankAccountName()).bankBranchName(gkCustData.getBankBranchName())
					.bankName(gkCustData.getBankName()).bankIfscCode(gkCustData.getBankIfscCode())
					.memRelation(gkCustData.getMemberRelation()).mobileNum(gkCustData.getMobileNumber())
					.permAddLine1(gkCustData.getPermanentAddressLine1())
					.permAddLine2(gkCustData.getPermanentAddressLine2()).permanentState(gkCustData.getPermanentState())
					.permanentDistrict(gkCustData.getPermanentDistrict())
					.permanentVillageLocality(gkCustData.getPermanentVillageLocality())
					.permanentPincode(gkCustData.getPermanentPincode()).permanentTaluk(gkCustData.getPermanentTaluk())
					.commAddLIne1(gkCustData.getCommunicationAddressLine1())
					.commAddLIne2(gkCustData.getCommunicationAddressLine2())
					.commState(gkCustData.getCommunicationState()).commDistrict(gkCustData.getCommunicationDistrict())
					.commVillageLocality(gkCustData.getCommunicationVillageLocality())
					.commPincode(gkCustData.getCommunicationPincode()).commTaluk(gkCustData.getCommunicationTaluk())
					.custVintage(gkCustData.getCustomerVintage())
					.activationDate(gkCustData.getActivationDate()).gender(gkCustData.getGender())
					.depDocId(gkCustData.getDependentDocId()).depDob(gkCustData.getDependentDob())
					.depDocType(gkCustData.getDependentDocType()).depname(gkCustData.getDependentName())
					.custStatus(gkCustData.getCustomerStatus()).custQualify(gkCustData.getCustomerQualification())
					.insNotify(insNotifyValue)
					.build();
			custList.add(cusData);
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
		}
	}

	/*
	 * private void appendLoanDtlListData(CustomerLoanDtlsDto result, List<LoanData>
	 * loanDtlList) { try { if (null != result.getLoanData()) { LoanData loanData =
	 * new LoanData(); BeanUtils.copyProperties(loanData, result.getLoanData());
	 * loanDtlList.add(loanData); } } catch (Exception ex) {
	 * logger.error(CommonConstants.EXCEP_OCCURED, ex); } }
	 */

	private void appendLoanDtlListDataNew(GkUnifiedData loanData, List<LoanData> loanDtlList) {
		try {
			if (loanData == null) {
				return; // Early exit if input data is null
			}
			// Check if customerId exists
			String customerId = loanData.getCustomerId();
			if (customerId == null) {
				return; // Early exit if customerId is null
			}
			List<Object> otherValues = Arrays.asList(loanData.getLoanId(), loanData.getAmount(),
					loanData.getApprovedAmount(), loanData.getStatus(), loanData.getFrequency(), loanData.getTerm(),
					loanData.getProduct(), loanData.getLoanValueDate(), loanData.getLoanMaturityDate(),
					loanData.getInterestRate(), loanData.getOverduePrincipal(), loanData.getOverdueInterest(),
					loanData.getOverdueStatus(), loanData.getOutstandingPrincipal());
			// Validate that at least one other value is non-null
			if (otherValues.stream().noneMatch(Objects::nonNull)) {
				return; // Early exit if no relevant fields are provided
			}
			// Check if customerId is non-null and at least one other value is non-null
			LoanData loan = LoanData.builder().loanId(loanData.getLoanId()).customerId(customerId)
					.amount(loanData.getAmount()).approvedAmt(loanData.getApprovedAmount()).status(loanData.getStatus())
					.freq(loanData.getFrequency()).term(loanData.getTerm()).product(loanData.getProduct())
					.lnValueDate(loanData.getLoanValueDate()).lnMatDate(loanData.getLoanMaturityDate())
					.interestRate(loanData.getInterestRate()).overduePrincipal(loanData.getOverduePrincipal())
					.overdueInterest(loanData.getOverdueInterest()).overDueStatus(loanData.getOverdueStatus())
					.loanPurpose(loanData.getLoanPurpose()).pf(loanData.getPf()).GST(loanData.getGST())
					.mem_insu(loanData.getMem_insu()).sp_insu(loanData.getSp_insu()).APR(loanData.getAPR())
					.outstandingPrincipal(loanData.getOutstandingPrincipal()).build();
			loanDtlList.add(loan);
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
		}
	}

	
	// Need to corrected
	private void appendEligibleLoanListDataNew(GkUnifiedData gkEligLoans, List<LoanEligible> loanElgList,
			List<GkMLoanProduct> productdata) {
		if (gkEligLoans == null) {
			return; // Early exit if input is null
		}
		try {
			String customerId = gkEligLoans.getCustomerId();
			if (customerId == null) {
				return; // Early exit if customerId is null
			}
			List<Object> otherValues = Arrays.asList(gkEligLoans.getOverallCbEligibleAmount(),
					gkEligLoans.getEligibleCaglAmount(), gkEligLoans.getEligibleCaglProduct());

			if (otherValues.stream().noneMatch(Objects::nonNull)) {
				return; // Early exit if all relevant fields are null
			}
			LoanEligible loanElData = LoanEligible.builder()
					.cbAmt(parseDoubleSafely(gkEligLoans.getEligibleCaglAmount())) // interchanged the value as requested by business
					.caglAmt(parseDoubleSafely(gkEligLoans.getOverallCbEligibleAmount()))
					.productType(gkEligLoans.getEligibleCaglProduct()).product(gkEligLoans.getProductType()).intRate(21)
					.customerId(customerId).build();
			loanElgList.add(loanElData);
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
		}
	}
	private static double parseDoubleSafely(String value) {
		if (value == null || value.trim().isEmpty()) {
			return 0.0; // Default value when input is null or empty
		}
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			// Log the error and return a default value
			System.err.println("Invalid number format: " + value);
			return 0.0;
		}
	}

	private Integer safeParseInt(String value) {
		try {
			return value != null ? Integer.parseInt(value) : null;
		} catch (NumberFormatException ex) {
			logger.error("Invalid integer value: {}", value, ex);
			return null;
		}
	}
	
	
	private void appendEarningMemberListDataNew(GkUnifiedData earnings, List<CustEarnings> earnList) {
		if (earnings == null) {
			return; // Early exit if input is null
		}
		try {
			String customerId = earnings.getCustomerId();
			if (customerId == null) {
				return; // Early exit if customerId is null
			}
			List<Object> otherValues = Arrays.asList(earnings.getRecordId(), earnings.getName(), earnings.getDobe(),
					earnings.getMemberRelationE(), earnings.getLegalDocumentName(), earnings.getLegalId());
			if (otherValues.stream().noneMatch(Objects::nonNull)) {
				return; // Early exit if all relevant fields are null
			}
			CustEarnings earnData = CustEarnings.builder().recId(earnings.getRecordId()).customerId(customerId)
					.name(earnings.getName()).dob(earnings.getDobe()).memRelation(earnings.getMemberRelationE())
					.legaldocName(earnings.getLegalDocumentName()).legaldocId(earnings.getLegalId()).build();
			earnList.add(earnData);
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
		}
	}

	
	private void appendIncomeAssementListDataNew(GkUnifiedData gkIncomeAssesment, List<IncomeAssesment> incomeList) {
		if (gkIncomeAssesment == null) {
			return; // Early exit if input is null
		}
		try {
			String customerId = gkIncomeAssesment.getCustomerId();
			if (customerId == null) {
				return; // Early exit if customerId is null
			}
			List<Object> otherValues = Arrays.asList(gkIncomeAssesment.getTotalIncome(),
					gkIncomeAssesment.getTotalExpenses(), gkIncomeAssesment.getAssessmentDate());
			// Check if customerId is non-null and at least one other value is non-null
			if (otherValues.stream().noneMatch(Objects::nonNull)) {
				return; // Early exit if all relevant fields are null
			}
			IncomeAssesment incomeData = IncomeAssesment.builder().customerId(customerId)
					.totIncome(gkIncomeAssesment.getTotalIncome()).totExpense(gkIncomeAssesment.getTotalExpenses())
					.assesmentDt(gkIncomeAssesment.getAssessmentDate()).build();
			incomeList.add(incomeData);
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
		}
	}

	public List<CustData> custList(int kendraId) {
		List<CustData> custList = new ArrayList<>();
		try {
			List<GkCustomerData> customerList = dao.fetchCustData(kendraId);
			for (GkCustomerData gk : customerList) {
				CustData c = new CustData();
				c.setCustomerId(gk.getCustomerId());
				c.setCustomerName(gk.getCustomerName());
				c.setKendraId(gk.getKendraId());
				c.setGroupId(gk.getGroupId());
				c.setBranchName(gk.getBranchName());
				c.setPrimaryType(gk.getPrimaryType());
				c.setPrimaryId(gk.getPrimaryId());
				c.setDob(gk.getDob());
				c.setMaritalStatus(gk.getMaritalStatus());
				c.setAddress(gk.getAddress());
				c.setBankAccNo(gk.getBankAccNo());
				c.setBankAccName(gk.getBankAccName());
				c.setBankBranchName(gk.getBankBranchName());
				c.setBankName(gk.getBankName());
				c.setBankIfscCode(gk.getBankIfscCode());
				c.setMemRelation(gk.getMemRelation());
				c.setMobileNum(gk.getMobileNum());
				c.setPermAddLine1(gk.getPermAddLine1());
				c.setPermAddLine2(gk.getPermAddLine2());
				c.setPermanentState(gk.getPermanentState());
				c.setPermanentDistrict(gk.getPermanentDistrict());
				c.setPermanentVillageLocality(gk.getPermanentVillageLocality());
				c.setPermanentPincode(gk.getPermanentPincode());
				c.setPermanentTaluk(gk.getPermanentTaluk());
				c.setCommAddLIne1(gk.getCommAddLIne1());
				c.setCommAddLIne2(gk.getCommAddLIne2());
				c.setCommState(gk.getCommState());
				c.setCommDistrict(gk.getCommDistrict());
				c.setCommVillageLocality(gk.getCommVillageLocality());
				c.setCommPincode(gk.getCommPincode());
				c.setCommTaluk(gk.getCommTaluk());
				c.setCustVintage(gk.getCustVintage());
				c.setActivationDate(gk.getActivationDate());
				c.setGender(gk.getGender());
				c.setDepDob(gk.getDepDob());
				c.setDepDocId(gk.getDepDocId());
				c.setDepDocType(gk.getDepDocType());
				c.setDepname(gk.getDepname());
				c.setLoanDtls(loanList(gk.getCustomerId()));
				c.setEligibleLoan(loanEligList(gk.getCustomerId()));
				c.setIncome(custIncomeList(gk.getCustomerId()));
				c.setEarnings(custEarningsList(gk.getCustomerId()));
				custList.add(c);
			}
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
		}
		logger.debug("custList method response::{}", custList);
		return custList;
	}

	public List<LoanData> loanList(String custId) {
		List<LoanData> loanList = new ArrayList<>();
		for (GkLoanData gk : dao.fetchLoanDatails(custId)) {
			LoanData l = new LoanData();
			l.setLoanId(gk.getLoanId());
			l.setCustomerId(gk.getCustomerId());
			l.setAmount(gk.getAmount());
			l.setApprovedAmt(gk.getApprovedAmt());
			l.setStatus(gk.getStatus());
			l.setFreq(gk.getFreq());
			l.setTerm(gk.getTerm());
			l.setProduct(gk.getProduct());
			l.setLnValueDate(gk.getLnValueDate());
			l.setLnMatDate(gk.getLnMatDate());
			l.setInterestRate(gk.getInterestRate());
			l.setOverduePrincipal(gk.getOverduePrincipal());
			l.setOverdueInterest(gk.getOverdueInterest());
			l.setOverDueStatus(gk.getOverDueStatus());
			l.setOutstandingPrincipal(gk.getOutstandingPrincipal());
			loanList.add(l);
		}
		if (!loanList.isEmpty()) {
			logger.debug("Loan data response object successfully generated for cust in service class::{}", custId);
		} else {
			logger.debug("Loan data response object generated empty in service class for cust::{}", custId);
		}
		return loanList;
	}

	public List<LoanEligible> loanEligList(String custId) {
		return dao.fetchEligibleLoans(custId);
	}

	public List<IncomeAssesment> custIncomeList(String custId) {
		List<IncomeAssesment> iList = new ArrayList<>();
		for (GkIncomeAssesment gk : dao.fetchCustIncomeDetails(custId)) {
			IncomeAssesment i = new IncomeAssesment();
			i.setCustomerId(gk.getCustomerId());
			i.setTotIncome(gk.getTotIncome());
			i.setTotExpense(gk.getTotExpense());
			i.setAssesmentDt(gk.getAssesmentDt());
			iList.add(i);
		}
		if (!iList.isEmpty()) {
			logger.debug("Income assesment data response object successfully generated for cust in service class:{}",
					custId);
		} else {
			logger.debug("Income assesment response object generated empty in service class for cust:{}", custId);
		}
		return iList;
	}

	public List<CustEarnings> custEarningsList(String custId) {
		List<CustEarnings> eList = new ArrayList<>();
		for (GkEarningMember gk : dao.fetchEarningMemberDetails(custId)) {
			CustEarnings c = new CustEarnings();
			c.setCustomerId(gk.getCustomerId());
			c.setName(gk.getName());
			c.setDob(gk.getDob());
			c.setMemRelation(gk.getMemRelation());
			c.setLegaldocName(gk.getLegaldocName());
			c.setLegaldocId(gk.getLegaldocId());
			eList.add(c);
		}
		if (!eList.isEmpty()) {
			logger.debug("Earning member data response object successfully generated for cust in service class {}",
					custId);
		} else {
			logger.debug("Earning member response object generated empty in service class for cust {}", custId);
		}
		return eList;
	}

	public List<ProductResponseObject> fetchProductDetals() {
		List<ProductResponseObject> respObjList = new ArrayList<>();
		for (GkMLoanProduct gk : dao.fetchLoanProducts()) {
			ProductResponseObject respobj = new ProductResponseObject();
			ProductData prd = new ProductData();
			prd.setProductId(gk.getProductId());
			prd.setDescription(gk.getDescription());
			prd.setProductType(gk.getProductType());
			prd.setShortDesc(gk.getShortDesc());
			prd.setAmountLimit(gk.getAmountLimit());
			prd.setAmountMin(gk.getAmountMin());
			prd.setAmountMax(gk.getAmountMax());
			prd.setAmountDefault(gk.getAmountDefault());
			prd.setSpouseInsurance(gk.getSpouseInsurance());
			prd.setInsLnamount(gk.getInsLnamount());
			prd.setDisbOTP(gk.getDisbOTP()); 
			prd.setTerm(gk.getTerm());
			prd.setFreq(gk.getFreq().replace("#", "~"));
			prd.setInsuranceProvider(gk.getInsuProvider().replace("#", "~"));
			prd.setInsurancePercentage(CommonConstants.INSURANCE_PER);
			prd.setProductType(gk.getProductType());
			prd.setProductStatus(gk.getProduct_status());
			prd.setLoanProdType(gk.getLoan_prod_type());
			prd.setDisbursementType(gk.getDisbursementType());
			prd.setMemInsurance(gk.getMemInsurance());
			prd.setFeeCharge(gk.getFeeCharge());
			prd.setGst(gk.getGst());
			prd.setConsentType(gk.getConsentType());
			prd.setProdPurpose(productPurposeList(gk.getProductId()));
			prd.setPayload(gk.getPayload()); 
			prd.setAccessType(gk.getAccessType());
			respobj.setProductDtls(prd);
			respObjList.add(respobj);
		}
		if (!respObjList.isEmpty()) {
			logger.debug("Product response object successfully generated for in service class");
		} else {
			logger.debug("Product response object generated empty in service class");
		}
		return respObjList;
	}

	public List<ProductPurpose> productPurposeList(String prodId) {
		List<ProductPurpose> pList = new ArrayList<>();
		for (GkLoanPurpose gk : dao.fetchProductPurpose(prodId)) {
			List<String> list = new ArrayList<>();
			ProductPurpose p = new ProductPurpose();
			p.setProductId(gk.getProductId());
			p.setPurpose(gk.getPurpose());
			p.setPurposeDesc(gk.getPurposeDesc());
			for (GkProductSubPurpose gkp : dao.fetchProdSubPurpose(gk.getProductId(), gk.getPurpose())) {
				list.add(gkp.getSubPurpose().replace(".", " "));
			}
			p.setProductSubPurpose(list);
			pList.add(p);
		}
		if (!pList.isEmpty()) {
			logger.debug("Product purpose successfully generated for in service class ");
		} else {
			logger.debug("Product purpose generated empty in service class ");
		}
		return pList;
	}

	public UserResponseObject fetchUserDesignation(String userId) {
		UserResponseObject respObject = new UserResponseObject();
		respObject.setUserRoles(dao.fetchUserRole(userId));
		return respObject;
	}

	public List<CustomerResponseObject> fetchCustomerDetails(String custId) {
		List<CustomerResponseObject> respObjList = new ArrayList<>();
		for (GkCustomerData gk : dao.fetchCustomerData(custId)) {
			GkCustomerData c = new GkCustomerData();
			CustomerResponseObject respObject = new CustomerResponseObject();
			c.setCustomerId(gk.getCustomerId());
			c.setCustomerName(gk.getCustomerName());
			c.setKendraId(gk.getKendraId());
			c.setGroupId(gk.getGroupId());
			c.setBranchId(gk.getBranchId());
			c.setBranchName(gk.getBranchName());
			c.setPrimaryType(gk.getPrimaryType());
			c.setPrimaryId(gk.getPrimaryId());
			c.setDob(gk.getDob());
			c.setMaritalStatus(gk.getMaritalStatus());
			c.setAddress(gk.getAddress());
			c.setBankAccNo(gk.getBankAccNo());
			c.setBankAccName(gk.getBankAccName());
			c.setBankBranchName(gk.getBankBranchName());
			c.setBankName(gk.getBankName());
			c.setBankIfscCode(gk.getBankIfscCode());
			c.setDepname(gk.getDepname());
			c.setDepDob(gk.getDepDob());
			c.setDepDocId(gk.getDepDocId());
			c.setDepDocType(gk.getDepDocType());
			c.setRecordType(gk.getRecordType());
			c.setCustQualify(gk.getCustQualify());
			c.setReligion(gk.getReligion());
			c.setCaste(gk.getCaste());
			c.setCity(gk.getCity());
			c.setVillage(gk.getVillage());
			c.setPincode(gk.getPincode());
			c.setMemRelation(gk.getMemRelation());
			c.setMobileNum(gk.getMobileNum());
			c.setPermAddLine1(gk.getPermAddLine1());
			c.setPermAddLine2(gk.getPermAddLine2());
			c.setPermanentState(gk.getPermanentState());
			c.setPermanentDistrict(gk.getPermanentDistrict());
			c.setPermanentVillageLocality(gk.getPermanentVillageLocality());
			c.setPermanentPincode(gk.getPermanentPincode());
			c.setPermanentTaluk(gk.getPermanentTaluk());
			c.setCommAddLIne1(gk.getCommAddLIne1());
			c.setCommAddLIne2(gk.getCommAddLIne2());
			c.setCommState(gk.getCommState());
			c.setCommDistrict(gk.getCommDistrict());
			c.setCommVillageLocality(gk.getCommVillageLocality());
			c.setCommPincode(gk.getCommPincode());
			c.setCommTaluk(gk.getCommTaluk());
			c.setCustVintage(gk.getCustVintage());
			c.setActivationDate(gk.getActivationDate());
			c.setCustStatus(gk.getCustStatus());
			respObject.setCustData(c);
			respObjList.add(respObject);
		}
		return respObjList;
	}

	@CircuitBreaker(name = "kendra_assignment", fallbackMethod = "fallbackMethod")
	public List<String> getAssignedKendraList(GkKendraAssignment kmid) throws URISyntaxException {
		URI url = new URI(kendraAssinedUrl);
		logger.debug("Kendra assignment url is:{}", url);
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<GkKendraAssignment> entity = new HttpEntity<>(kmid, header);
		List<String> kList = template.exchange(url, HttpMethod.POST, entity, List.class).getBody();
		return kList;
	}

	public List<String> fallbackMethod(Exception exc) {
		List<String> errMessge = new ArrayList<>();
		errMessge.add("error occured in the Kendra ssignment service API :" + exc);
		logger.error(exc);
		return errMessge;
	}

	public ResponseWrapper createBranchLatLongRecordsService(BranchLatlong branchLatLoang) {
		String qryResult = dao.insertBranchLatLongRecords(branchLatLoang);
		return mapToResponseWrapper(qryResult);
	}

	public String getTimeStamp() {
		return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
	}

	public ResponseWrapper createKendraLatLongRecordsService(KendraLatLongEntity kendraLatLoang) {
		String qryResult = dao.insertKendraLatLongRecords(kendraLatLoang);
		return mapToResponseWrapper(qryResult);
	}

	public ResponseWrapper fetchBranchLatLong(String branchId) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String resp = mapper.writeValueAsString(dao.fetchBranchLatLong(branchId));
		return mapToResponseWrapper(resp);
	}

	public ResponseWrapper fetchKendrLatLong(int kendraId) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String resp = mapper.writeValueAsString(dao.fetchKendraLatLong(kendraId));
		return mapToResponseWrapper(resp);

	}
	
	// fetch Lat and Long Based on KrendraIDs
	public ResponseWrapper fetchKendraLatLongBasedOnListOfKendraId(List<Integer> kendraId) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String resp = mapper.writeValueAsString(dao.fetchListOfKendraLatLong(kendraId));
		logger.debug("Printing for resp {}", resp);
		return mapToResponseWrapper(resp);

	}

	private ResponseWrapper mapToResponseWrapper(String responseString) {
		ResponseWrapper resWrapper = new ResponseWrapper();
		ResponseBody responseBody = new ResponseBody();
		ResponseHeader resHeader = new ResponseHeader();
		Response response = new Response();
		responseBody.setResponseObj(responseString);
		response.setResponseBody(responseBody);
		resHeader.setResponseCode(CommonConstants.SUCCESS);
		resHeader.setResponseMessage(CommonConstants.RESP_SUCCESS_STATUS);
		response.setResponseHeader(resHeader);
		resWrapper.setApiResponse(response);
		return resWrapper;
	}

	/*
	 * Method to fetch basic KendraDetailsDto based on branchId.
	 * 
	 */
	@Transactional
	public Response fetchCdhKendraInfo(String branchId, String nextMeetingDt) {
		logger.debug("branchId{}", branchId);
		logger.debug("nextMeetingDt::{}", nextMeetingDt);
		Response response;
		try {
			List<KendraDetailsDto> kendraList;
			if (CommonUtils.checkStringNullOrEmpty(nextMeetingDt)) {
				kendraList = dao.fetchKendraInfoForBranchId(branchId);
			}
			/*
			 * else if ("CASHIER".equalsIgnoreCase(roleName)) { kendraList =
			 * dao.fetchKendraInfoForBranchIdByRole(branchId); }
			 */
			else {
				kendraList = dao.fetchKendraInfo(branchId, nextMeetingDt);
			}
			logger.debug("kendraList::{}", kendraList);
			String kendraRespObj = new ObjectMapper().writeValueAsString(kendraList);
			logger.debug("kendraRespObj::{}", kendraRespObj);
			ResponseHeader respHeader = ResponseHeader.builder().responseCode(CommonConstants.SUCCESS)
					.responseMessage("").build();
			logger.debug("respHeader::{}", respHeader);
			ResponseBody respBody = ResponseBody.builder().responseObj(kendraRespObj).build();
			logger.debug("respBody::{}", respBody);
			response = Response.builder().responseHeader(respHeader).responseBody(respBody).build();
		} catch (Exception exp) {
			logger.debug(CommonConstants.EXCEP_OCCURED, exp);
			ResponseHeader respHeader = ResponseHeader.builder().responseCode(CommonConstants.FAILURE)
					.responseMessage(CommonConstants.RESP_FAILURE_MSG).build();
			ResponseBody respBody = ResponseBody.builder().responseObj("").build();
			response = Response.builder().responseHeader(respHeader).responseBody(respBody).build();
		}
		logger.debug("Kendra Response object::{}", response);
		return response;
	}
	
	/**
	 * @author Ankit.CAG
	 * @param customerId
	 * @return
	 */
	public Response fetchInsuranceNotificationsInfo(String customerId) {
		logger.debug("customerId{}", customerId);
	
		List<CustomerInsuranceNotification> custInsNotifList = custInsNotificationRepository.fetchInsuranceDetailsByCustomerId(customerId);

		Response response;
		try {
			if ( custInsNotifList == null || custInsNotifList.isEmpty()) {
				logger.debug("customerInsuranceNotification:isEmpty()");
				ResponseHeader respHeader = ResponseHeader.builder().responseCode(CommonConstants.RESP_FAILURE_MSG)
						.responseMessage("Customer not found").build();
				ResponseBody respBody = ResponseBody.builder().responseObj(null).build();
				response = Response.builder().responseHeader(respHeader).responseBody(respBody).build();
			}
			else {
			// Take first record for customer info
            CustomerInsuranceNotification first = custInsNotifList.get(0);

            // Group messages by category
            Map<String, List<String>> groupedNotifications = custInsNotifList.stream()
                    .collect(Collectors.groupingBy(
                            CustomerInsuranceNotification::getCategoryOfNotification,
                            Collectors.mapping(CustomerInsuranceNotification::getMessage, Collectors.toList())
                    ));
            
			logger.debug("customerInsuranceNotification::{}", first);
			CustomerInsuranceNotificationDto custInsNotifDto = CustomerInsuranceNotificationDto.builder()
					.customerId(first.getCustomerId())
                    .customerName(first.getCustomerName())
                    .kendraId(first.getKendraId())
                    .kendraName(first.getKendraName())
                    .branchId(first.getBranchId())
                    .branchName(first.getBranchName())
                    .notifications(groupedNotifications)
                    .build();
			
			logger.debug("CustomerInsuranceNotificationDto::{}", custInsNotifDto);
			String custInsRespObj = new ObjectMapper().writeValueAsString(custInsNotifDto);
			logger.debug("customerInsuranceNotification::{}", custInsRespObj);
			ResponseHeader respHeader = ResponseHeader.builder().responseCode(CommonConstants.SUCCESS)
					.responseMessage("fetch Insurance Notifications Info").build();
			logger.debug("respHeader::{}", respHeader);
			ResponseBody respBody = ResponseBody.builder().responseObj(custInsRespObj).build();
			logger.debug("respBody::{}", respBody);
			response = Response.builder().responseHeader(respHeader).responseBody(respBody).build();
			}
		} catch (Exception exp) {
			logger.debug(CommonConstants.EXCEP_OCCURED, exp);
			ResponseHeader respHeader = ResponseHeader.builder().responseCode(CommonConstants.FAILURE)
					.responseMessage(CommonConstants.RESP_FAILURE_MSG).build();
			ResponseBody respBody = ResponseBody.builder().responseObj("").build();
			response = Response.builder().responseHeader(respHeader).responseBody(respBody).build();
		}
		logger.debug("CustomerInsuranceNotification Response object::{}", response);
		return response;

	}
}
