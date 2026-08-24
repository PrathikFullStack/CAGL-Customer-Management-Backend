package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_OB_DMS_SESSION_DATA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbObDMSSessionDataEntity {

    @Id
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE")
    private Date date;

    @Column(name = "SESSION_ID")
    private String sessionId;
}