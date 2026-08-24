package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import java.io.Serializable;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbObOtherDocumentPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "docu_id")
    private String docuId;

//    @Column(name = "id")
//    private String id;

    @Column(name = "category")
    private String category;
    
    
}