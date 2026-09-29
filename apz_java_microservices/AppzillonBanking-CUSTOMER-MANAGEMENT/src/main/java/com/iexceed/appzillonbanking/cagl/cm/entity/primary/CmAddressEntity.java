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
@Table(name = "tb_cm_address")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmAddressEntity {

    @Id
    @Column(name = "address_id", length = 20, nullable = false)
    private String addressId;

    @Column(name = "customer_id", length = 20, nullable = false)
    private String customerId;

    @Column(name = "application_id", length = 30, nullable = false)
    private String applicationId;

    @Column(name = "address_type", length = 1, nullable = false)
    private String addressType; // 'P' = Permanent, 'C' = Communication

    @Column(name = "comm_same_as_perm", length = 1)
    private String commSameAsPerm; // 'Y' / 'N'

    @Column(name = "addr_payload", columnDefinition = "jsonb", nullable = false)
    private String addrPayload;

    @Column(name = "address_proof_type", length = 30)
    private String addressProofType;

    @Column(name = "sub_type", length = 30)
    private String subType;

    @Column(name = "address_proof_doc_id", length = 20)
    private String addressProofDocId;

    @Column(name = "distance_from_branch", length = 10)
    private String distanceFromBranch;

    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;

    @Column(name = "updated_by", length = 20)
    private String updatedBy;
}
