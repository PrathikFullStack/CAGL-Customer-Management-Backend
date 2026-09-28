package com.iexceed.appzillonbanking.cagl.cob.domain.spec;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardListRequestFields;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tile-scope and GLOBAL/LOCAL/FILTER search predicates for {@link TbObGroup}, mirroring
 * {@link OnboardingSpecification}'s approach. Group only carries groupId/groupName/kendraId
 * directly -- branchId/region/area/state live on {@link TbObKendra}, so those FILTER fields go
 * through a kendraId subquery (same technique OnboardingSpecification uses for application master).
 */
public class GroupSpecification {

    private GroupSpecification() {
    }

    public static Specification<TbObGroup> statusEquals(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<TbObGroup> statusIn(List<String> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<TbObGroup> kendraIdIn(List<String> kendraIds) {
        return (root, query, cb) -> root.get("kendraId").in(kendraIds);
    }

    public static Specification<TbObGroup> t24RefNoIsNull() {
        return (root, query, cb) -> cb.isNull(root.get("t24RefNo"));
    }

    public static Specification<TbObGroup> buildSearchSpecification(DashboardListRequestFields fields) {
        String searchType = fields.getSearchType();
        if (searchType == null || searchType.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        if ("FILTER".equalsIgnoreCase(searchType)) {
            return buildFilterSpecification(fields);
        }
        return buildFreeTextSpecification(fields);
    }

    private static Specification<TbObGroup> buildFilterSpecification(DashboardListRequestFields fields) {
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
                            case "GROUPID":
                                predicates.add(root.get("groupId").in(values));
                                break;
                            case "GROUPNAME":
                                predicates.add(root.get("groupName").in(values));
                                break;
                            case "KENDRAID":
                                predicates.add(root.get("kendraId").in(values));
                                break;
                            case "BRANCHID":
                            case "BRANCHNAME":
                                predicates.add(root.get("kendraId").in(kendraIdByColumnSubquery(query, "branchId", values)));
                                break;
                            case "REGIONDIVISION":
                                predicates.add(root.get("kendraId").in(kendraIdByColumnSubquery(query, "district", values)));
                                break;
                            case "AREA":
                                predicates.add(root.get("kendraId").in(kendraIdByColumnSubquery(query, "village", values)));
                                break;
                            case "STATE":
                                List<Integer> stateCodes = OnboardingSpecification.toIntegerValues(values);
                                predicates.add(stateCodes.isEmpty()
                                        ? cb.disjunction()
                                        : root.get("kendraId").in(kendraIdByColumnSubquery(query, "state", stateCodes)));
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

    private static Subquery<String> kendraIdByColumnSubquery(jakarta.persistence.criteria.CriteriaQuery<?> query, String kendraField, List<?> values) {
        Subquery<String> kendraSubquery = query.subquery(String.class);
        Root<TbObKendra> kendraRoot = kendraSubquery.from(TbObKendra.class);
        kendraSubquery.select(kendraRoot.get("kendraId")).where(kendraRoot.get(kendraField).in(values));
        return kendraSubquery;
    }

    /** Covers both LOCAL and GLOBAL search -- Group has no tileType-specific field set to branch on. */
    private static Specification<TbObGroup> buildFreeTextSpecification(DashboardListRequestFields fields) {
        return (root, query, cb) -> {
            Object rawValue = fields.getSearchValue();
            if (!(rawValue instanceof String) || ((String) rawValue).isEmpty()) {
                return cb.conjunction();
            }
            String searchValue = (String) rawValue;
            return cb.or(
                    cb.like(root.get("groupId"), "%" + searchValue + "%"),
                    cb.like(root.get("groupName"), "%" + searchValue + "%"),
                    cb.like(root.get("kendraId"), "%" + searchValue + "%")
            );
        };
    }
}
