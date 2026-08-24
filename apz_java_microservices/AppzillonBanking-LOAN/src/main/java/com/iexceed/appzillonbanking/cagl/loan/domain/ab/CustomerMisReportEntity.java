package com.iexceed.appzillonbanking.cagl.loan.domain.ab;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_ucob_customer_mis_report", schema = "public")
public class CustomerMisReportEntity {

	@Id
	@JsonProperty("application_id")
	@Column(name = "application_id")
	private String applicationId;
	
	@JsonProperty("appVersion")
	@Column(name = "appversion")
	private String appVersion;

	@JsonProperty("type")
	@Column(name = "type")
	private String type;

	@JsonProperty("applicationStatus")
	@Column(name = "applicationstatus")
	private String applicationStatus;

	@JsonProperty("stageId")
	@Column(name = "stageid")
	private String stageId;

	@JsonProperty("branchId")
	@Column(name = "branchid")
	private String branchId;

	@JsonProperty("kendraId")
	@Column(name = "kendraid")
	private String kendraId;

	@JsonProperty("customerId")
	@Column(name = "customer_id")
	private String customerId;

	@JsonProperty("createDate")
	@Column(name = "createdate")
	private String createDate;

	@JsonProperty("updateDate")
	@Column(name = "updatedate")
	private String updateDate;

	@JsonProperty("userRole")
	@Column(name = "userrole")
	private String userRole;

	@JsonProperty("createdBy")
	@Column(name = "createdby")
	private String createdBy;

	@JsonProperty("modifyBy")
	@Column(name = "modifyby")
	private String modifyBy;

	@JsonProperty("addInfo1")
	@Column(name = "addinfo1")
	private String addInfo1;

	@JsonProperty("remarks")
	@Column(name = "remarks")
	private String remarks;

}
