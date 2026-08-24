package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationStage {

    @JsonProperty("DocList")
    private List<DocStatus> docList;

    @JsonProperty("status")
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocStatus {
        @JsonProperty("type")
        private String type;

        @JsonProperty("status")
        private String status;
    }
}
