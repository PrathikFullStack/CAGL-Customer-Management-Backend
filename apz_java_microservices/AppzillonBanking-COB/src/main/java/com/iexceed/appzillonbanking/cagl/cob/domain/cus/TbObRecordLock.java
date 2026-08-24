package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * tb_ob_record_lock - PK: lock_id.
 * Drives the 423 LOCKED behaviour required by API 9: a row here with
 * status=ACTIVE and (lock_type=PERMANENT OR lock_expiry_ts > now) blocks
 * any user other than locked_by from viewing/editing the application.
 */
@Entity
@Table(name = "tb_ob_record_lock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbObRecordLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lock_id", nullable = false)
    private Long lockId;

    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Column(name = "locked_by", length = 20, nullable = false)
    private String lockedBy;

    /** RPC_MAKER / RPC_CHECKER */
    @Column(name = "locked_by_role", length = 20, nullable = false)
    private String lockedByRole;

    /** TIMED / PERMANENT */
    @Column(name = "lock_type", length = 15, nullable = false)
    private String lockType;

    @Column(name = "locked_at", nullable = false)
    private Instant lockedAt;

    /** NULL for PERMANENT locks */
    @Column(name = "lock_expiry_ts")
    private Instant lockExpiryTs;

    /** ACTIVE / RELEASED / EXPIRED */
    @Column(name = "status", length = 10, nullable = false)
    private String status;

    @Column(name = "released_at")
    private Instant releasedAt;
}
