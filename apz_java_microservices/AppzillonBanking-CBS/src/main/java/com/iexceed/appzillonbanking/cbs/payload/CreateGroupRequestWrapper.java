package com.iexceed.appzillonbanking.cbs.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequestWrapper {
    
    @JsonProperty("apiRequest")	
    private CreateGroupRequest apiRequest;
    
}
