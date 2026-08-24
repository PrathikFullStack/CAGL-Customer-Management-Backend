package com.iexceed.appzillonbanking.cagl.cob.utils;

import com.iexceed.appzillonbanking.cagl.cob.payload.PaginationRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

public final class PaginationUtil {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final String DEFAULT_SORT_BY = "updatedTs";

    private PaginationUtil() {
        // Private constructor to prevent instantiation
    }

    public static Pageable createPageable(PaginationRequest pagination) {
        return Optional.ofNullable(pagination)
                .map(p -> PageRequest.of(
                        Optional.ofNullable(p.getPageNo()).orElse(DEFAULT_PAGE),
                        Optional.ofNullable(p.getPageSize()).orElse(DEFAULT_PAGE_SIZE),
                        Sort.by(Sort.Direction.DESC, DEFAULT_SORT_BY)
                ))
                .orElse(PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, DEFAULT_SORT_BY)));
    }
}