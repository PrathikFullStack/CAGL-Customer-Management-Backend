package com.iexceed.appzillonbanking.cagl.loan.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AMLReportRequestFields {
    private String applicationId;
    private String loanId;
}
