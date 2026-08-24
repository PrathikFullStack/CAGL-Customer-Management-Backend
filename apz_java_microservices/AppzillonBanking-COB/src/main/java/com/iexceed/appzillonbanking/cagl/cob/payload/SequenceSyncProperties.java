package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.sequences")
public class SequenceSyncProperties {

    private List<SeqConfig> items;

    @Setter
    @Getter
    public static class SeqConfig {
        private String sequenceName;
        private String table;
        private String column;
        private long floor;
    }
}