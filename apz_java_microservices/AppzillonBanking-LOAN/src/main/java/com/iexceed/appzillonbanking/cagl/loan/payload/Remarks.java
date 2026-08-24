package com.iexceed.appzillonbanking.cagl.loan.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Remarks {

	@JsonProperty("dob")
	private String dob;	

	@JsonProperty("memRelation")
	private String memRelation;

	@JsonProperty("remarks")
	private String remarks;

	@JsonProperty("earningmemCount")
	private Integer earningmemCount;

}
