package com.iexceed.appzillonbanking.cagl.cob.domain.spec;

import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SearchType {
    // LIKE searches for character-based fields
    APPLICATION_ID("applicationId", SearchType::like),
    CUSTOMER_NAME("customerName", SearchType::like),
    MOBILE_NUMBER("mobileNo", SearchType::like), // Special handling for JOIN is in buildPredicate
    KENDRA_NAME("kendraName", SearchType::like),
    BRANCH_ID("branchId", SearchType::like),
    KM_NAME("kmName", SearchType::like),
    KM_ID("createdBy", SearchType::like),
    STATUS("status", SearchType::like),

    // EQUALS searches for numeric ID fields
    CUSTOMER_ID("customerId", SearchType::equal),
    KENDRA_ID("kendraId", SearchType::equalLong),
    GROUP_ID("groupId", SearchType::equalLong),

    // DATE search
    MBDF_ENTRY_DATE("createdTs", SearchType::dateEquals);


    private final String fieldName;
    private final TriFunction<CriteriaBuilder, Path<?>, String, Predicate> predicateBuilder;

    SearchType(String fieldName, TriFunction<CriteriaBuilder, Path<?>, String, Predicate> predicateBuilder) {
        this.fieldName = fieldName;
        this.predicateBuilder = predicateBuilder;
    }

    public Predicate buildPredicate(Root<TbObApplicationMaster> root, CriteriaBuilder cb, String searchValue) {
        // Special handling for mobile number, which requires a JOIN.
        if (this == MOBILE_NUMBER) {
            Path<String> mobileNoPath = root.join("customer", JoinType.LEFT).get(this.fieldName);
            return this.predicateBuilder.apply(cb, mobileNoPath, searchValue);
        }
        return this.predicateBuilder.apply(cb, root.get(this.fieldName), searchValue);
    }

    public Predicate buildInClause(Root<TbObApplicationMaster> root, CriteriaBuilder cb, List<String> values) {
        return root.get(this.fieldName).in(parseValuesForInClause(values));
    }

    public static SearchType from(String text) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(text))
                .findFirst()
                .orElse(null);
    }

    // --- Predicate building functions ---
    private static Predicate like(CriteriaBuilder cb, Path<?> path, String value) {
        return cb.like(cb.lower(path.as(String.class)), "%" + value.toLowerCase() + "%");
    }

    private static Predicate equal(CriteriaBuilder cb, Path<?> path, String value) {
        return cb.equal(path, value);
    }

    private static Predicate equalLong(CriteriaBuilder cb, Path<?> path, String value) {
        try {
            return cb.equal(path, Long.parseLong(value));
        } catch (NumberFormatException e) {
            return cb.disjunction(); // Invalid number format, find nothing
        }
    }

    private static Predicate dateEquals(CriteriaBuilder cb, Path<?> path, String value) {
        try {
            // Assuming the date is passed in 'yyyy-MM-dd' format
            LocalDate searchDate = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDateTime startOfDay = searchDate.atStartOfDay();
            LocalDateTime endOfDay = searchDate.plusDays(1).atStartOfDay();
            return cb.between(path.as(LocalDateTime.class), startOfDay, endOfDay);
        } catch (Exception e) {
            return cb.disjunction(); // Invalid date format, find nothing
        }
    }

    // --- Value Parsers for IN clauses ---
    private List<?> parseValuesForInClause(List<String> values) {
        if (this == BRANCH_ID || this == KENDRA_ID) {
            return values.stream()
                    .map(v -> v.contains("/") ? v.substring(v.lastIndexOf('/') + 1) : v)
                    .collect(Collectors.toList());
        }
        return values;
    }
}