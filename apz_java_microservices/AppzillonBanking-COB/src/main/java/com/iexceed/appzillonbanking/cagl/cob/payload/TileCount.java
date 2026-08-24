package com.iexceed.appzillonbanking.cagl.cob.payload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TileCount {
    private String tileName;
    private long count;
}
