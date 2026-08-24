package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key for tb_ob_document.
 * docu_id is an index that resets PER application (1=Voter ID, 2=Aadhaar ...),
 * so uniqueness only holds in combination with application_id.
 */
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TbObDocumentId implements Serializable {
    private String applicationId;
    private String docuId;
}
