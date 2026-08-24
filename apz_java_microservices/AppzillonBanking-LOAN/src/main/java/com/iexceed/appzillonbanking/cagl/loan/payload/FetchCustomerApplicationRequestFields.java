package com.iexceed.appzillonbanking.cagl.loan.payload;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FetchCustomerApplicationRequestFields {
	
	@JsonProperty("userRole")
	private String userRole;
	
	@JsonProperty("branchId")
	private List<String> branchId;
	
	@JsonProperty("kendraId")
	private List<String> kendraId;
	
	@JsonProperty("applicationId")
	private String applicationId;

	@JsonProperty("loanApplicationId")
	private String loanApplicationId;

}
