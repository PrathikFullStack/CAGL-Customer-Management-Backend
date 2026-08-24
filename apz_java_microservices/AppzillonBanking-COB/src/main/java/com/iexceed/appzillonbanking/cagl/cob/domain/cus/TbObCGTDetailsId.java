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
public class TbObCGTDetailsId implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("cgtId")
    @Column(name = "cgt_id", nullable = false)
    private String cgtId;

    @JsonProperty("groupId")
    @Column(name = "group_id", nullable = false)
    private String groupId;

    @JsonProperty("kendraId")
    @Column(name = "kendra_id", nullable = false)
    private String kendraId;

}
