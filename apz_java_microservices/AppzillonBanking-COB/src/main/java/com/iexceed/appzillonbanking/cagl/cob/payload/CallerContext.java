package com.iexceed.appzillonbanking.cagl.cob.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Identity of the caller, resolved by the API gateway / auth filter and
 * passed downstream via headers. All 11 roles (KM, BM, AM, RPC Maker,
 * RPC Checker, etc.) call this endpoint, so role is not restricted here -
 * role-based field masking, if any, is applied at the gateway/BFF layer.
 */
@Getter
@AllArgsConstructor
public class CallerContext {
    private final String userId;
    private final String userName;
    private final String userRole;
}
