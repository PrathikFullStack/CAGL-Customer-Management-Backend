package com.iexceed.appzillonbanking.cagl.cob.domain.spec;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardListRequestFields;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tile-scope and GLOBAL/LOCAL/FILTER search predicates for {@link TbObKendra}, mirroring
 * {@link OnboardingSpecification}'s approach for the Kendra-backed dashboard tiles (e.g. "New
 * Kendras/Groups", CHT "pending for T24 activation"). Kendra carries kendraId/kendraName/branchId/
 * district/village/state directly, so FILTER fields map straight to columns -- no subquery needed.
 */
public class KendraSpecification {

    private KendraSpecification() {
    }

    public static Specification<TbObKendra> statusEquals(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<TbObKendra> statusIn(List<String> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<TbObKendra> kendraIdIn(List<String> kendraIds) {
        return (root, query, cb) -> root.get("kendraId").in(kendraIds);
    }

    public static Specification<TbObKendra> branchIdEquals(String branchId) {
        return (root, query, cb) -> cb.equal(root.get("branchId"), branchId);
    }

    public static Specification<TbObKendra> branchIdIn(List<String> branchIds) {
        return (root, query, cb) -> root.get("branchId").in(branchIds);
    }

    public static Specification<TbObKendra> t24RefNoIsNull() {
        return (root, query, cb) -> cb.isNull(root.get("t24RefNo"));
    }

    public static Specification<TbObKendra> buildSearchSpecification(DashboardListRequestFields fields) {
        String searchType = fields.getSearchType();
        if (searchType == null || searchType.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        if ("FILTER".equalsIgnoreCase(searchType)) {
            return buildFilterSpecification(fields);
        }
        return buildFreeTextSpecification(fields);
    }

    private static Specification<TbObKendra> buildFilterSpecification(DashboardListRequestFields fields) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Object searchValue = fields.getSearchValue();
            if (searchValue instanceof List) {
                List<?> searchList = (List<?>) searchValue;
                if (!searchList.isEmpty() && searchList.get(0) instanceof Map) {
                    Map<?, ?> searchMap = (Map<?, ?>) searchList.get(0);
                    for (Map.Entry<?, ?> entry : searchMap.entrySet()) {
                        String field = OnboardingSpecification.normalizeFieldKey(String.valueOf(entry.getKey()));
                        List<String> values = OnboardingSpecification.toStringValues(entry.getValue());
                        if (values == null || values.isEmpty()) {
                            continue;
                        }
                        switch (field) {
                            case "KENDRAID":
                                predicates.add(root.get("kendraId").in(values));
                                break;
                            case "KENDRANAME":
                                predicates.add(root.get("kendraName").in(values));
                                break;
                            case "BRANCHID":
                            case "BRANCHNAME":
                                predicates.add(root.get("branchId").in(values));
                                break;
                            case "REGIONDIVISION":
                                predicates.add(root.get("district").in(values));
                                break;
                            case "AREA":
                                predicates.add(root.get("village").in(values));
                                break;
                            case "STATE":
                                List<Integer> stateCodes = OnboardingSpecification.toIntegerValues(values);
                                predicates.add(stateCodes.isEmpty() ? cb.disjunction() : root.get("state").in(stateCodes));
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Covers both LOCAL and GLOBAL search -- Kendra has no tileType-specific field set to branch on. */
    private static Specification<TbObKendra> buildFreeTextSpecification(DashboardListRequestFields fields) {
        return (root, query, cb) -> {
            Object rawValue = fields.getSearchValue();
            if (!(rawValue instanceof String) || ((String) rawValue).isEmpty()) {
                return cb.conjunction();
            }
            String searchValue = (String) rawValue;
            return cb.or(
                    cb.like(root.get("kendraId"), "%" + searchValue + "%"),
                    cb.like(root.get("kendraName"), "%" + searchValue + "%"),
                    cb.like(root.get("branchId"), "%" + searchValue + "%")
            );
        };
    }
}
