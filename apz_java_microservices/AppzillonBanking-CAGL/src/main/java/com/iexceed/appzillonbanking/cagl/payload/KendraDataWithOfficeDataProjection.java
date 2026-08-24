package com.iexceed.appzillonbanking.cagl.payload;

import com.iexceed.appzillonbanking.cagl.dto.OfficeDataDto;

public interface KendraDataWithOfficeDataProjection extends KendraDataProjection {
	
	OfficeDataDto getOfficeData();

}
