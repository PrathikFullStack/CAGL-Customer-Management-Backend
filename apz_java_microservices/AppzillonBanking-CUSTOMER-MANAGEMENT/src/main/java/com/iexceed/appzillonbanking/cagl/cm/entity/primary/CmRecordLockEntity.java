package com.iexceed.appzillonbanking.cagl.cm.entity.primary;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_cm_record_lock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmRecordLockEntity {

    @Id
    @Column(name = "lock_id", length = 20, nullable = false)
    private String lockId;

    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Column(name = "locked_by", length = 20, nullable = false)
    private String lockedBy;

    @Column(name = "locked_by_role", length = 20, nullable = false)
    private String lockedByRole;

    @Column(name = "lock_type", length = 15, nullable = false)
    private String lockType;

    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "lock_expiry_ts")
    private LocalDateTime lockExpiryTs;

    @Column(name = "status", length = 10, nullable = false)
    private String status; // ACTIVE, RELEASED, EXPIRED

    @Column(name = "released_at")
    private LocalDateTime releasedAt;
}
