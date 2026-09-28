package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TbObDmsFailedUploadId implements Serializable {

    private String applicationId;
    private String docuId;
}