package com.iexceed.appzillonbanking.cagl.cm.entity.primary;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CmConfigurableDataPK implements Serializable {
    private static final long serialVersionUID = 1L;
    private String appId;
    private String type;
}
