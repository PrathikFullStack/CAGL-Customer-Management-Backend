package com.iexceed.appzillonbanking.cagl.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gk_nominee", uniqueConstraints = { @UniqueConstraint(name = "uk_gk_nominee_cust_app_doc", columnNames = {
		"Custid", "application_id", "legaldocId" }) })
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(GkNomineeId.class)
public class GkNominee {

	@Id
	@Column(name = "Custid", length = 150)
	private String custid;
	
	@Id
	@Column(name = "application_id", length = 150)
	private String applicationId;

	@Id
	@Column(name = "legaldocId", length = 250)
	private String legaldocId;
	
	@Column(name = "nomineeName", length = 45)
    private String nomineeName;

	@Column(name = "dob", length = 15)
	private String dob;

	@Column(name = "memRelation", length = 25)
	private String memRelation;

	@Column(name = "legaldocName", length = 50)
	private String legaldocName;

	@Column(name = "mobileNum", length = 15)
	private String mobileNum;

	@Column(name = "gender", length = 10)
	private String gender;

	@Column(name = "docuNoF", length = 250)
	private String docuNoF;

	@Column(name = "docuNoB", length = 250)
	private String docuNoB;

	@Column(name = "createdAt")
	private LocalDateTime createdAt;

	@Column(name = "updatedAt")
	private LocalDateTime updatedAt;

	@Column(name = "status", length = 40)
	private String status;

}
