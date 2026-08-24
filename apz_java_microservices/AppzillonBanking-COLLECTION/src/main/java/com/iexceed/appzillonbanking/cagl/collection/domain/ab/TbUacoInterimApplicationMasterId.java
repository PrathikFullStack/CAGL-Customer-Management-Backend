package com.iexceed.appzillonbanking.cagl.collection.domain.ab;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbUacoInterimApplicationMasterId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "app_id", nullable = false)
    private String appId;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Column(name = "latest_version_no", nullable = false)
    private int latestVersionNo;

    @Column(name = "kendra_id", nullable = false)
    private String kendraId;
}
