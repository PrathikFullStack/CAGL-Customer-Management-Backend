package com.iexceed.appzillonbanking.cagl.payload;

import java.util.List;
import com.iexceed.appzillonbanking.cagl.entity.DigitalCollection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FetchMahiResponseBody {
    private List<ResponseObject> responseObj;
	
	private List<DigitalCollection> collectionsObj;
}