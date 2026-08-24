package com.iexceed.appzillonbanking.cagl.cob.domain.cus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbObGroupMappingHistoryPK implements Serializable {

    private Long mappingId;

    private String groupId;

    private String kendraId;
}