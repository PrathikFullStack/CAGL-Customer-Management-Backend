package com.iexceed.appzillonbanking.cagl.cm.entity.primary;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CmConfigurableDataPK implements Serializable {
    private static final long serialVersionUID = 1L;
    private String appId;
    private String type;
}
