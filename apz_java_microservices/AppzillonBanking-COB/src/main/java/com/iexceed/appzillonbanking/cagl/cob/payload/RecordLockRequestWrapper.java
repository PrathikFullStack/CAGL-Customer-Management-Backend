package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordLockRequestWrapper {

    private RecordLockRequestFields requestObj;
}