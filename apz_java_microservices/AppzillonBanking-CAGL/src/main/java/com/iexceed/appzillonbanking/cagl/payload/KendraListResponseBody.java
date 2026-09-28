package com.iexceed.appzillonbanking.cagl.payload;

import java.util.List;

import com.iexceed.appzillonbanking.cagl.dto.KendraNameIdDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KendraListResponseBody {

	private List<KendraNameIdDto> kendraList;

}
