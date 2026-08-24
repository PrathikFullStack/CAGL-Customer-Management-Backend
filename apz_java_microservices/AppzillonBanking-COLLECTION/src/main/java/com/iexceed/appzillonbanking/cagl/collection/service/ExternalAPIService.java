package com.iexceed.appzillonbanking.cagl.collection.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoMaitriRetry;
import com.iexceed.appzillonbanking.cagl.collection.payload.*;
import com.iexceed.appzillonbanking.cagl.collection.repository.ab.TbUacoMaitriRetryRepo;
import com.iexceed.appzillonbanking.core.payload.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iexceed.appzillonbanking.cagl.collection.constants.CommonConstants;
import com.iexceed.appzillonbanking.cagl.collection.domain.ab.TbUacoQRDtls;
import com.iexceed.appzillonbanking.cagl.collection.repository.ab.TbUacoQRDtlsRepo;
import com.iexceed.appzillonbanking.core.payload.Response;
import com.iexceed.appzillonbanking.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.core.utils.CommonUtils;


@Service
public class ExternalAPIService {

	private static final Logger logger = LogManager.getLogger(ExternalAPIService.class);

	@Autowired
	private TbUacoQRDtlsRepo tbUacoQRDtlsRepo;

	@Autowired
	private UPIPaymentService upiPaymentService;

	@Autowired
	private TbUacoMaitriRetryRepo tbUacoMaitriRetryRepo;

	public Response qrPaymentCB(QRPaymentCBRequest qrPaymentCBRequest) {
		ResponseHeader responseHeader = new ResponseHeader();
		try {
			logger.debug("Request to qrPaymentCB::{}", qrPaymentCBRequest);
			if (!CommonUtils.checkStringNullOrEmpty(qrPaymentCBRequest.getBillNumber())) {
				Optional<TbUacoQRDtls> tbUacoQRDtlsOpt = tbUacoQRDtlsRepo
						.findByBillNumber(qrPaymentCBRequest.getBillNumber());
				logger.debug(tbUacoQRDtlsOpt);
				if (tbUacoQRDtlsOpt.isPresent()) {
					TbUacoQRDtls qrDtls = tbUacoQRDtlsOpt.get();

					if (null != qrPaymentCBRequest.getTransactionstatus()) {
						qrDtls.setStatus(qrPaymentCBRequest.getTransactionstatus().toUpperCase());
					} else {
						qrDtls.setStatus(qrPaymentCBRequest.getTransactionstatus());
					}

					tbUacoQRDtlsRepo.save(qrDtls);
					if ("SUCCESS".equalsIgnoreCase(qrPaymentCBRequest.getTransactionstatus())) {

						try {
							ObjectMapper mapper = new ObjectMapper();
							JsonNode payloadArray = mapper.readTree(qrDtls.getPayload());

							JsonNode loanNode = payloadArray.get(0);

							String loanId = loanNode.get("id").asText();
							String dueAmt = loanNode.get("dueAmt").asText();
							String userId = loanNode.has("userId")
									? loanNode.get("userId").asText()
									: "SYSTEM";
							String uniqueIdentifier = generateUniqueIdentifier(qrDtls.getCustomerId(), getMeetingDate(qrDtls.getPayload()));
							LoanDetails loanDetails = LoanDetails.builder()
									.loanId(loanId)
									.loanDue(dueAmt)
									.loanCollectionAmt(qrPaymentCBRequest.getAmount())
									.build();
							CusDetails cusDetails = CusDetails.builder()
									.customerId(qrDtls.getCustomerId())
									.cusCollectionAmt(qrPaymentCBRequest.getAmount())
									.cusFlag(getCusFlag(qrDtls.getPayload()))
									.upiFlag("MAITRI")
									.loanDetails(Collections.singletonList(loanDetails))
									.build();
							MaitriUPIRequestFields requestFields = MaitriUPIRequestFields.builder()
											.uniqueIdentifier(uniqueIdentifier)
											.branchCode(qrDtls.getBranchId())
											.customerDetails(Collections.singletonList(cusDetails))
											.build();

							MaitriUPIRequest maitriRequest = MaitriUPIRequest.builder()
									.appId("APZCBO")
									.userId(userId)
									.interfaceName(UPIPaymentService.MAITRI_UPI_COLLECTION_INTF)
									.requestObj(requestFields)
									.build();

							logger.debug("Upi Maitri Request :: {}", maitriRequest);
							Header header = new Header();
							upiPaymentService
									.maitriUpiCollection(maitriRequest, header)
									.subscribe(maitriResponse -> {
												try {
													logger.debug("Upi Maitri Response :: {}", maitriResponse);
													String responseJson = maitriResponse.getResponseBody().getResponseObj();
													JsonNode rootNode = mapper.readTree(responseJson);
													String maitriStatus = rootNode.path("header").path("status").asText();
													logger.debug("Maitri API Status :: {}", maitriStatus);
													qrDtls.setMaitriStatus(maitriStatus);
													tbUacoQRDtlsRepo.save(qrDtls);

													if (!"success".equalsIgnoreCase(maitriStatus)) {
														TbUacoMaitriRetry retryEntity =
																TbUacoMaitriRetry.builder()
																		.customerId(qrDtls.getCustomerId())
																		.billNumber(qrDtls.getBillNumber())
																		.uniqueIdentifier(uniqueIdentifier)
																		.maitriStatus(maitriStatus)
																		.schedulerStatus("PENDING")
																		.retryCount(0)
																		.requestPayload(mapper.writeValueAsString(maitriRequest))
																		.responsePayload((responseJson))
																		.createTs(new Timestamp(System.currentTimeMillis()))
																		.updateTs(new Timestamp(System.currentTimeMillis()))
																		.build();
														tbUacoMaitriRetryRepo.save(retryEntity);
														logger.debug("Saved Maitri retry entry for bill number :: {}", qrDtls.getBillNumber());
													}
												} catch (Exception e) {
													logger.error("Error while updating Maitri status", e);
												}
											},
											error -> {
												logger.error("Error while calling Maitri API", error);
												try {
													qrDtls.setMaitriStatus(error.getMessage());
													tbUacoQRDtlsRepo.save(qrDtls);

													TbUacoMaitriRetry retryEntity =
															TbUacoMaitriRetry.builder()
																	.customerId(qrDtls.getCustomerId())
																	.billNumber(qrDtls.getBillNumber())
																	.uniqueIdentifier(uniqueIdentifier)
																	.maitriStatus(error.getMessage())
																	.schedulerStatus("PENDING")
																	.retryCount(0)
																	.requestPayload(mapper.writeValueAsString(maitriRequest))
																	.createTs(new Timestamp(System.currentTimeMillis()))
																	.updateTs(new Timestamp(System.currentTimeMillis()))
																	.build();
													tbUacoMaitriRetryRepo.save(retryEntity);
													logger.debug("Saved Maitri retry entry after API failure for bill number :: {}", qrDtls.getBillNumber());
												} catch (Exception ex) {
													logger.error("Error while saving error status", ex);
												}
											}
									);
						} catch (Exception e) {
							logger.error("Error while calling Maitri API", e);
						}
					}
					CommonUtils.generateHeaderForSuccess(responseHeader);
				} else {
					CommonUtils.generateHeaderForFailure(responseHeader,
							"No record found for the matching bill Number");
					responseHeader.setResponseCode("NR-BILLNUM");
				}
			} else {
				CommonUtils.generateHeaderForFailure(responseHeader, "Invalid Request");
				responseHeader.setResponseCode("INVL-BILLNUM");
			}
		} catch (Exception ex) {
			logger.error(CommonConstants.EXCEP_OCCURED, ex);
			CommonUtils.generateHeaderForGenericError(responseHeader);
		}
		return Response.builder().responseHeader(responseHeader).responseBody(null).build();
	}

	private String getCusFlag(String payload) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode payloadArray = mapper.readTree(payload);

			String totalDueAmt = "0";
			String totalUpiAmt = "0";

			for (JsonNode loanNode : payloadArray) {
				String dueAmt =
						loanNode.has("dueAmt")
								? loanNode.get("dueAmt").asText()
								: "0";
				String upiAmt =
						loanNode.has("upiAmt")
								? loanNode.get("upiAmt").asText()
								: "0";
				totalDueAmt = String.valueOf(
						Double.parseDouble(totalDueAmt)
								+ Double.parseDouble(dueAmt));
				totalUpiAmt = String.valueOf(
						Double.parseDouble(totalUpiAmt)
								+ Double.parseDouble(upiAmt));
			}
			logger.debug("Total Due Amount :: {}", totalDueAmt);
			logger.debug("Total UPI Amount :: {}", totalUpiAmt);

			if (Double.parseDouble(totalUpiAmt)
					>= Double.parseDouble(totalDueAmt)) {
				return "FULL";
			}
			return "PARTIAL";
		} catch (Exception e) {
			logger.error("Error while calculating cusFlag", e);
			return "PARTIAL";
		}
	}

	private String getMeetingDate(String payload){
		try{
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(payload);
			if (rootNode.has("meetingDate")
					&& !rootNode.get("meetingDate").isNull()) {
				return rootNode.get("meetingDate")
						.asText()
						.replace("-", "");
			}
		} catch (Exception e) {
			logger.error("Error extracting meetingDate from payload", e);
		}
			return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
	}

	private String generateUniqueIdentifier(String customerId, String payload) {

		String meetingDate = getMeetingDate(payload);
		LocalDate today = LocalDate.now();
		Timestamp startDate = Timestamp.valueOf(today.atStartOfDay());
		Timestamp endDate = Timestamp.valueOf(today.plusDays(1).atStartOfDay());
		long todayCount =
				tbUacoQRDtlsRepo.countByCustomerIdAndCreateTsBetween(customerId, startDate, endDate);
		long sequenceNumber = todayCount + 1;
		return "C-"
				+ customerId + "-"
				+ sequenceNumber + "-"
				+ meetingDate.replace("-", "");
	}
}
