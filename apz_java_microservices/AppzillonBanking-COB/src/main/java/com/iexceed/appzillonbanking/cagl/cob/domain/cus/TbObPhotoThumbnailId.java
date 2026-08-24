package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObPhotoThumbnailId implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("applicationId")
    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @JsonProperty("docuId")
    @Column(name = "docu_id", nullable = false)
    private String docuId;
    
    @JsonProperty("docuType")
    @Column(name = "docu_Type", nullable = false)
    private String docuType;;
}