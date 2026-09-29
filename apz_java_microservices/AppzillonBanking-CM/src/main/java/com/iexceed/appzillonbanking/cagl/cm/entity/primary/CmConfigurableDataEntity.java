package com.iexceed.appzillonbanking.cagl.cm.entity.primary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_cm_configurable_data")
@IdClass(CmConfigurableDataPK.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmConfigurableDataEntity {

    @Id
    @Column(name = "app_id", length = 20, nullable = false)
    private String appId;

    @Id
    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "json_payload", columnDefinition = "jsonb", nullable = false)
    private String jsonPayload;
}
