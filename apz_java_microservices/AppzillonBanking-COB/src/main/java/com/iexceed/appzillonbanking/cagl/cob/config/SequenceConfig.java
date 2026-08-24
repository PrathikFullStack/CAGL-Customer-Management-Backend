package com.iexceed.appzillonbanking.cagl.cob.config;

import com.iexceed.appzillonbanking.cagl.cob.payload.SequenceSyncProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SequenceSyncProperties.class)
public class SequenceConfig {
}
