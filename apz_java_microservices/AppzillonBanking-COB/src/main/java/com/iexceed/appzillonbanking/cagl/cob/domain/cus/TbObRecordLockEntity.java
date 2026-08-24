package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_ob_record_lock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObRecordLockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lock_id", nullable = false)
    @JsonProperty("lockId")
    private Long lockId;

    @Column(name = "application_id", nullable = false)
    @JsonProperty("applicationId")
    private String applicationId;

    @Column(name = "locked_by", nullable = false)
    @JsonProperty("lockedBy")
    private String lockedBy;

    @Column(name = "locked_by_role", nullable = false)
    @JsonProperty("lockedByRole")
    private String lockedByRole;

    @Column(name = "lock_type", nullable = false)
    @JsonProperty("lockType")
    private String lockType;

    @Column(name = "locked_at", nullable = false)
    @JsonProperty("lockedAt")
    private Long lockedAt;

    @Column(name = "lock_expiry_ts", nullable = true)
    @JsonProperty("lockExpiryTs")
    private Long lockExpiryTs;

    @Column(name = "status", nullable = false)
    @JsonProperty("status")
    private String status;

    @Column(name = "released_at", nullable = true)
    @JsonProperty("releasedAt")
    private Long releasedAt;
}