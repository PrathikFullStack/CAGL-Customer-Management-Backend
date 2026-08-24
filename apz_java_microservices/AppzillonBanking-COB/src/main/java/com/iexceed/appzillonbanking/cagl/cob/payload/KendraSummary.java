package com.iexceed.appzillonbanking.cagl.cob.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KendraSummary {
    private String kendraId;
    private String kendraName;
    private String status;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss.SSS")
    private LocalDateTime createdTs;
}