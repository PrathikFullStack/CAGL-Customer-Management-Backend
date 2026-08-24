package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;

import java.io.Serializable;

public class TbObGRTDetailsId implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("grtId")
    @Column(name = "grt_id", nullable = false)
    private String grtId;

    @JsonProperty("groupId")
    @Column(name = "group_id", nullable = false)
    private String groupId;

    @JsonProperty("kendraId")
    @Column(name = "kendra_id", nullable = false)
    private String kendraId;
}
