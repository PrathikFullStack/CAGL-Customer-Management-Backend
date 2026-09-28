package com.iexceed.appzillonbanking.cagl.cob.payload;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.iexceed.appzillonbanking.cagl.cob.payload.ActiveLoanDtls;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanDtls {

    @JsonProperty("caglAmt")
    private String caglAmt;

    @JsonProperty("cbAmt")
    private String cbAmt;

    @JsonProperty("custVintageInterestRate")
    private String custVintageInterestRate;

    @JsonProperty("insurancePercentage")
    private String insurancePercentage;

    @JsonProperty("product")
    private String product;

    @JsonProperty("productId")
    private String productId;

    @JsonProperty("productType")
    private String productType;

    @JsonProperty("shortDesc")
    private String shortDesc;

    @JsonProperty("spouseInsurance")
    private String spouseInsurance;

    @JsonProperty("term")
    private String term;

    @JsonProperty("loanMode")
    private String loanMode;

    @JsonProperty("loanStatus")
    private String loanStatus;

    @JsonProperty("chargeAndBreakupDtls")
    private ChargeAndBreakupDtls chargeAndBreakupDtls;

    @JsonProperty("insurDtls")
    private InsuranceDetails insurDtls;

    @JsonProperty("disburseMode")
    private IdDescPair disburseMode;

    @JsonProperty("nomineeDtls")
    private NomineeDtls nomineeDtls;

    @JsonProperty("purpose")
    private PurposeDtls purpose;

    @JsonProperty("activeLoanDtls")
    private List<ActiveLoanDtls> activeLoanDtls;

    @JsonProperty("repayFrequency")
    private IdDescPair repayFrequency;

    @JsonProperty("interestRate")
    private Double interestRate;

    @JsonProperty("installmentDetails")
    private String installmentDetails;
}