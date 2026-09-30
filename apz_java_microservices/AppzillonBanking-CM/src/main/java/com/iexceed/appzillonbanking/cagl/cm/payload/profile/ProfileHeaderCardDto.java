package com.iexceed.appzillonbanking.cagl.cm.payload.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileHeaderCardDto {
    private String customerId;
    private String customerName;
    private String mobileNumber;
    private String dob;
    private String gender;
    private String maritalStatus;
    private String memberPhotoDmsId;
    private Integer profileCompletionPercentage;
    private String customerStatus;
    private String customerVintage;
    private Integer pendingUpdatesCount;
    private Integer inProgressUpdatesCount;
    private boolean kycRenewalRequired;
    private String kycRenewalDueDate;
}
