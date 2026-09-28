package com.iexceed.appzillonbanking.cagl.cob.domain.spec;

import com.iexceed.appzillonbanking.cagl.cob.constants.DashboardConstants;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObCustomer;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObKendra;
import com.iexceed.appzillonbanking.cagl.cob.payload.DashboardListRequestFields;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class OnboardingSpecification {

    private OnboardingSpecification() {
    }

    /**
     * For Global Search  Based On KM, Branch, RPC, RPC_TL, AM, RPC_HO, RPC_IN, RPC_CHT, RPC_BST
     */
    public static Specification<TbObApplicationMaster> buildRoleScopeSpecification(DashboardListRequestFields fields) {
        return (root, query, criteriaBuilder) -> {
            String userRole = fields.getUserRole();
            if ("KM".equalsIgnoreCase(userRole)) {
                List<String> kendraIds = fields.getKendraIds();
                return (kendraIds == null || kendraIds.isEmpty())
                        ? criteriaBuilder.disjunction()
                        : root.get("kendraId").in(kendraIds);
            }
            if ("BM".equalsIgnoreCase(userRole)) {
                List<String> branchIds = fields.getBranchIds();
                return (branchIds == null || branchIds.isEmpty())
                        ? criteriaBuilder.disjunction()
                        : root.get("branchId").in(branchIds);
            }
            if ("AM".equalsIgnoreCase(userRole)) {
                String branchId = fields.getBranchId();
                return branchId == null
                        ? criteriaBuilder.disjunction()
                        : criteriaBuilder.equal(root.get("branchId"), branchId);
            }
            if (DashboardConstants.isRpcRole(userRole) || DashboardConstants.isRpcTlRole(userRole)) {
                List<String> branchIds = fields.getBranchIds();
                return (branchIds == null || branchIds.isEmpty())
                        ? criteriaBuilder.disjunction()
                        : root.get("branchId").in(branchIds);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<TbObApplicationMaster> buildSearchSpecification(DashboardListRequestFields fields) {
        String searchType = fields.getSearchType();
        if (searchType == null || searchType.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        if ("FILTER".equalsIgnoreCase(searchType)) {
            return buildFilterSpecification(fields);
        }
        if ("LOCAL".equalsIgnoreCase(searchType)) {
            return buildLocalSearchSpecification(fields);
        }
        return buildGlobalSearchSpecification(fields);
    }

    public static Specification<TbObApplicationMaster> buildGlobalSearchSpecification(DashboardListRequestFields fields) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Object searchValue = fields.getSearchValue();
            List<String> searchFields = fields.getSearchFields();
            if (searchValue instanceof List<?> valueList && searchFields != null && !searchFields.isEmpty()) {
                int count = Math.min(searchFields.size(), valueList.size());
                for (int i = 0; i < count; i++) {
                    Object rawValue = valueList.get(i);
                    String value = rawValue == null ? null : String.valueOf(rawValue).trim();
                    if (value == null || value.isEmpty()) {
                        continue;
                    }
                    Predicate fieldPredicate = buildGlobalFieldPredicate(root, query, criteriaBuilder,
                            normalizeFieldKey(searchFields.get(i)), value);
                    if (fieldPredicate != null) {
                        predicates.add(fieldPredicate);
                    }
                }
            } else if (searchValue instanceof String) {
                String searchString = ((String) searchValue).trim();
                if (!searchString.isEmpty()) {
                    String likeValue = "%" + searchString + "%";
                    assert query != null;
                    Subquery<String> groupSubquery = query.subquery(String.class);
                    Root<TbObGroup> groupRoot = groupSubquery.from(TbObGroup.class);
                    groupSubquery.select(groupRoot.get("groupId"))
                            .where(criteriaBuilder.like(groupRoot.get("groupName"), likeValue));

                    Subquery<String> kycSubquery = query.subquery(String.class);
                    Root<TbObCustomer> customerRoot = kycSubquery.from(TbObCustomer.class);
                    kycSubquery.select(customerRoot.get("applicationId"))
                            .where(criteriaBuilder.like(customerRoot.get("primaryKycId"), likeValue));

                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.like(root.get("createdBy"), likeValue),
                            criteriaBuilder.like(root.get("kmName"), likeValue),
                            criteriaBuilder.like(root.get("branchId"), likeValue),
                            criteriaBuilder.like(root.get("branchName"), likeValue),
                            criteriaBuilder.like(root.get("applicationId"), likeValue),
                            criteriaBuilder.like(root.get("customerId"), likeValue),
                            criteriaBuilder.like(root.get("customerName"), likeValue),
                            criteriaBuilder.like(root.get("kendraId"), likeValue),
                            criteriaBuilder.like(root.get("kendraName"), likeValue),
                            criteriaBuilder.like(root.get("groupId"), likeValue),
                            root.get("groupId").in(groupSubquery),
                            root.get("applicationId").in(kycSubquery),
                            criteriaBuilder.like(root.get("mobileNumber"), likeValue)
                    ));
                }
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Maps a single configurable GLOBAL search field (e.g. "customerName", "mobileNumber") to a LIKE/subquery
     * predicate against one value.
     */
    private static Predicate buildGlobalFieldPredicate(Root<TbObApplicationMaster> root, CriteriaQuery<?> query,
                                                         jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                                         String normalizedField, String value) {
        String likeValue = "%" + value + "%";
        switch (normalizedField) {
            case "KMID":
            case "KMNAME":
                return criteriaBuilder.or(
                        criteriaBuilder.like(root.get("createdBy"), likeValue),
                        criteriaBuilder.like(root.get("kmName"), likeValue));
            case "BRANCHID":
            case "BRANCHNAME":
                return criteriaBuilder.or(
                        criteriaBuilder.like(root.get("branchId"), likeValue),
                        criteriaBuilder.like(root.get("branchName"), likeValue));
            case "CUSTOMERID":
            case "MEMBERID":

                return criteriaBuilder.like(root.get("customerId"), likeValue);
            case "MBDFID":
            case "APPLICATIONID":
                return criteriaBuilder.like(root.get("applicationId"), likeValue);
            case "CUSTOMERNAME":
                return criteriaBuilder.like(root.get("customerName"), likeValue);
            case "KENDRAID":
                return criteriaBuilder.like(root.get("kendraId"), likeValue);
            case "KENDRANAME":
                return criteriaBuilder.like(root.get("kendraName"), likeValue);
            case "GROUPID":
                return criteriaBuilder.like(root.get("groupId"), likeValue);
            case "GROUPNAME": {
                assert query != null;
                Subquery<String> groupSubquery = query.subquery(String.class);
                Root<TbObGroup> groupRoot = groupSubquery.from(TbObGroup.class);
                groupSubquery.select(groupRoot.get("groupId"))
                        .where(criteriaBuilder.like(groupRoot.get("groupName"), likeValue));
                return root.get("groupId").in(groupSubquery);
            }
            case "VALIDATEDKYCID":
            case "KYCID":
            case "MEMBERVOTERID": {
                assert query != null;
                Subquery<String> kycSubquery = query.subquery(String.class);
                Root<TbObCustomer> customerRoot = kycSubquery.from(TbObCustomer.class);
                kycSubquery.select(customerRoot.get("applicationId"))
                        .where(criteriaBuilder.like(customerRoot.get("primaryKycId"), likeValue));
                return root.get("applicationId").in(kycSubquery);
            }
            case "REGISTEREDPHONENUMBER":
            case "MOBILENUMBER":
            case "PHONENUMBER":
            case "MOBILENO":
                return criteriaBuilder.like(root.get("mobileNumber"), likeValue);
            default:
                return null;
        }
    }

    public static Specification<TbObApplicationMaster> buildFilterSpecification(DashboardListRequestFields fields) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Object searchValue = fields.getSearchValue();
            if (searchValue instanceof List<?> searchList) {
                if (!searchList.isEmpty() && searchList.get(0) instanceof Map<?, ?> searchMap) {
                    for (Map.Entry<?, ?> entry : searchMap.entrySet()) {
                        String field = normalizeFieldKey(String.valueOf(entry.getKey()));
                        List<String> values = toStringValues(entry.getValue());

                        if (!values.isEmpty() && isFilterFieldAllowedForRole(fields.getUserRole(), field)) {
                            switch (field) {
                                case "KMID":
                                case "KMNAME":
                                    predicates.add(criteriaBuilder.or(
                                            root.get("createdBy").in(values),
                                            root.get("kmName").in(values)
                                    ));
                                    break;
                                case "BRANCHID":
                                case "BRANCHNAME":
                                    predicates.add(criteriaBuilder.or(
                                            root.get("branchId").in(values),
                                            root.get("branchName").in(values)
                                    ));
                                    break;
                                case "CUSTOMERID":
                                case "MEMBERID":
                                    predicates.add(root.get("customerId").in(values));
                                    break;
                                case "MBDFID":
                                    predicates.add(root.get("applicationId").in(values));
                                    break;
                                case "CUSTOMERNAME":
                                    predicates.add(root.get("customerName").in(values));
                                    break;
                                case "KENDRAID":
                                    predicates.add(root.get("kendraId").in(values));
                                    break;
                                case "KENDRANAME":
                                    predicates.add(root.get("kendraName").in(values));
                                    break;
                                case "GROUPID":
                                    predicates.add(root.get("groupId").in(values));
                                    break;
                                case "REGIONDIVISION":
                                    assert query != null;
                                    predicates.add(root.get("kendraId")
                                            .in(buildKendraIdSubquery(query, "district", values)));
                                    break;
                                case "AREA":
                                    assert query != null;
                                    predicates.add(root.get("kendraId")
                                            .in(buildKendraIdSubquery(query, "village", values)));
                                    break;
                                case "STATE":
                                    List<Integer> stateCodes = toIntegerValues(values);
                                    assert query != null;
                                    predicates.add(stateCodes.isEmpty()
                                            ? criteriaBuilder.disjunction()
                                            : root.get("kendraId")
                                                    .in(buildKendraIdSubquery(query, "state", stateCodes)));
                                    break;
                                case "GROUPNAME":
                                    assert query != null;
                                    Subquery<String> groupSubquery = query.subquery(String.class);
                                    Root<TbObGroup> groupRoot = groupSubquery.from(TbObGroup.class);
                                    groupSubquery.select(groupRoot.get("groupId"))
                                            .where(groupRoot.get("groupName").in(values));
                                    predicates.add(root.get("groupId").in(groupSubquery));
                                    break;
                                case "VALIDATEDKYCID":
                                case "MEMBERVOTERID":
                                    assert query != null;
                                    Subquery<String> kycSubquery = query.subquery(String.class);
                                    Root<TbObCustomer> customerRoot = kycSubquery.from(TbObCustomer.class);
                                    kycSubquery.select(customerRoot.get("applicationId"))
                                            .where(customerRoot.get("primaryKycId").in(values));
                                    predicates.add(root.get("applicationId").in(kycSubquery));
                                    break;
                                case "REGISTEREDPHONENUMBER":
                                    predicates.add(root.get("mobileNumber").in(values));
                                    break;
                                case "KMSUBMISSIONDATE":
                                case "KMSUBMISSIONDATETIME":
                                    predicates.add(buildDateMatchPredicate(root.get("createdTs"), criteriaBuilder, values));
                                    break;
                                case "RPCPROCESSEDDATE":
                                case "RPCPROCESSEDDATETIME":
                                    predicates.add(buildDateMatchPredicate(root.get("updatedTs"), criteriaBuilder, values));
                                    break;
                                case "CURRENTSTAGE":
                                    predicates.add(criteriaBuilder.or(
                                            root.get("stage").in(values),
                                            root.get("status").in(values)
                                    ));
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<TbObApplicationMaster> buildLocalSearchSpecification(DashboardListRequestFields fields) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Object rawValue = fields.getSearchValue();
            String searchValue = rawValue instanceof String ? ((String) rawValue).trim() : null;
            String tileType = fields.getTileType();

            if (searchValue != null && !searchValue.isEmpty() && tileType != null) {
                String likeValue = "%" + searchValue + "%";
                switch (tileType) {
                    case "Leads":
                         predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("mobileNumber"), likeValue),
                                criteriaBuilder.like(root.get("createdTs"), likeValue),
                                criteriaBuilder.like(root.get("channelType"), likeValue)
                        ));
                        break;
                    case DashboardConstants.TILE_DRAFTS_MBDF:
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                getCustomerName(root, criteriaBuilder, likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("kendraName"), likeValue),
                                criteriaBuilder.like(root.get("groupId"), likeValue),
                                criteriaBuilder.like(root.get("stage"), likeValue),
                                criteriaBuilder.like(root.get("status"), likeValue)
                        ));
                        break;
                    case "BRE_Fail":
                    case "BRE_FAIL":
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("kendraName"), likeValue),
                                criteriaBuilder.like(root.get("stage"), likeValue),
                                criteriaBuilder.like(root.get("status"), likeValue)
                        ));
                        break;
                    case DashboardConstants.TILE_PENDING_WITH_RPC:
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("kendraName"), likeValue),
                                criteriaBuilder.like(root.get("branchId"), likeValue),
                                criteriaBuilder.like(root.get("stage"), likeValue),
                                criteriaBuilder.like(root.get("status"), likeValue)
                        ));
                        break;
                    case DashboardConstants.TILE_INPUT_LOAN_DETAILS:
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("kendraName"), likeValue),
                                criteriaBuilder.like(root.get("groupId"), likeValue)
                        ));
                        break;
                    case DashboardConstants.TILE_ONHOLD:
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("kendraName"), likeValue),
                                criteriaBuilder.like(root.get("remarks"), likeValue)
                        ));
                        break;
                    case DashboardConstants.TILE_CGT:
                    case DashboardConstants.TILE_PENDING_FOR_GRT:
                         predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("groupId"), likeValue),
                                criteriaBuilder.like(root.get("groupName"), likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("kendraName"), likeValue)
                        ));
                        break;
                    case "RE_INTERVIEW":
                    case "REINTERVIEW":
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("kendraName"), likeValue)
                        ));
                        break;
                    case DashboardConstants.TILE_ACTIVATED_REJECTED:
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("status"), likeValue),
                                criteriaBuilder.like(root.get("remarks"), likeValue)
                        ));
                        break;
                    case DashboardConstants.TILE_PENDING_FOR_ACTIVATION:
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("remarks"), likeValue)
                        ));
                        break;
                    case DashboardConstants.TILE_MAKERS_POOL:
                    case DashboardConstants.TILE_CHECKERS_POOL:
                    case DashboardConstants.TILE_CLEARED_BY_USER:
                    case DashboardConstants.TILE_GREEN_CHANNEL:
                    case DashboardConstants.TILE_CLEARED_CASES:
                    case DashboardConstants.TILE_AML_HO_POOL:
                           predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("branchId"), likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("kendraName"), likeValue),
                                criteriaBuilder.like(root.get("groupId"), likeValue),
                                criteriaBuilder.like(root.get("createdTs"), likeValue),
                                criteriaBuilder.like(root.get("updatedTs"), likeValue),
                                criteriaBuilder.like(root.get("stage"), likeValue),
                                criteriaBuilder.like(root.get("status"), likeValue)
                        ));
                        break;
                    case DashboardConstants.TILE_CRT:
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("kendraId"), likeValue),
                                criteriaBuilder.like(root.get("kendraName"), likeValue),
                                criteriaBuilder.like(root.get("status"), likeValue)
                        ));
                        break;
                    default:
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), likeValue),
                                criteriaBuilder.like(root.get("customerName"), likeValue),
                                criteriaBuilder.like(root.get("kendraName"), likeValue),
                                criteriaBuilder.like(root.get("groupId"), likeValue),
                                criteriaBuilder.like(root.get("branchId"), likeValue),
                                criteriaBuilder.like(root.get("status"), likeValue)
                        ));
                        break;
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate getCustomerName(Root<TbObApplicationMaster> root, CriteriaBuilder criteriaBuilder, String likeValue) {
        return criteriaBuilder.like(root.get("customerName"), likeValue);
    }

    /**
     * Normalizes a FILTER field key so both underscore-uppercase ("KENDRA_ID") and bare camelCase
     * ("kendraId") client shapes hit the same switch branch.
     */
    static String normalizeFieldKey(String field) {
        return field == null ? "" : field.replace("_", "").toUpperCase();
    }


    private static final Set<String> RPC_GEO_FIELDS = Set.of("STATE", "REGIONDIVISION", "AREA", "KENDRAID", "BRANCHID");
    private static final Set<String> RPC_HO_FIELDS = Set.of("REGIONDIVISION");
    private static final Set<String> RPC_REGULAR_FIELDS = Set.of(
            "BRANCHID", "MEMBERID", "MEMBERVOTERID", "GROUPID", "KENDRAID",
            "KMSUBMISSIONDATE", "KMSUBMISSIONDATETIME", "RPCPROCESSEDDATE", "RPCPROCESSEDDATETIME", "CURRENTSTAGE"
    );

    static boolean isFilterFieldAllowedForRole(String userRole, String normalizedField) {
        String normalizedRole = userRole == null ? null : userRole.trim().toUpperCase().replaceAll("\\s+", "");
        if ("RPCIN".equals(normalizedRole) || "RPCTL".equals(normalizedRole) || "RPCAMLHO".equals(normalizedRole)) {
            return RPC_GEO_FIELDS.contains(normalizedField);
        }
        if ("RPCHO".equals(normalizedRole)) {
            return RPC_HO_FIELDS.contains(normalizedField);
        }
        if (DashboardConstants.isRpcRole(normalizedRole)) {
            // Remaining RPC_ROLES members (RPC, RPCCHT, RPCBST) -- the regular Maker/Checker flow.
            return RPC_REGULAR_FIELDS.contains(normalizedField);
        }
        return true;
    }

    static Predicate buildDateMatchPredicate(Path<LocalDateTime> path, jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder, List<String> values) {
        List<Predicate> dayPredicates = new ArrayList<>();
        for (String value : values) {
            try {
                LocalDate date = LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDateTime startOfDay = date.atStartOfDay();
                dayPredicates.add(criteriaBuilder.between(path, startOfDay, startOfDay.plusDays(1)));
            } catch (Exception ignored) {
            }
        }
        return dayPredicates.isEmpty() ? criteriaBuilder.disjunction() : criteriaBuilder.or(dayPredicates.toArray(new Predicate[0]));
    }


    static Subquery<String> buildKendraIdSubquery(CriteriaQuery<?> query, String kendraField, List<?> values) {
        assert query != null;
        Subquery<String> kendraSubquery = query.subquery(String.class);
        Root<TbObKendra> kendraRoot = kendraSubquery.from(TbObKendra.class);
        kendraSubquery.select(kendraRoot.get("kendraId")).where(kendraRoot.get(kendraField).in(values));
        return kendraSubquery;
    }


    static List<String> toStringValues(Object rawValue) {
        if (rawValue instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) rawValue) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        if (rawValue == null) {
            return List.of();
        }
        return List.of(String.valueOf(rawValue));
    }

    static List<Integer> toIntegerValues(List<String> values) {
        List<Integer> result = new ArrayList<>();
        for (String value : values) {
            try {
                result.add(Integer.valueOf(value.trim()));
            } catch (NumberFormatException ex) {
                // Non-numeric state code from the UI -- ignore rather than fail the whole search.
            }
        }
        return result;
    }


    public static Specification<TbObApplicationMaster> stageEquals(String stage) {
        return (root, query, cb) -> cb.equal(root.get("stage"), stage);
    }

    public static Specification<TbObApplicationMaster> stageIn(List<String> stages) {
        return (root, query, cb) -> root.get("stage").in(stages);
    }

    public static Specification<TbObApplicationMaster> kendraIdIn(List<String> kendraIds) {
        return (root, query, cb) -> root.get("kendraId").in(kendraIds);
    }

    public static Specification<TbObApplicationMaster> groupIdIn(List<String> groupIds) {
        return (root, query, cb) -> (groupIds == null || groupIds.isEmpty())
                ? cb.disjunction()
                : root.get("groupId").in(groupIds);
    }

    public static Specification<TbObApplicationMaster> branchIdEquals(String branchId) {
        return (root, query, cb) -> cb.equal(root.get("branchId"), branchId);
    }

    public static Specification<TbObApplicationMaster> branchIdIn(List<String> branchIds) {
        return (root, query, cb) -> root.get("branchId").in(branchIds);
    }

    public static Specification<TbObApplicationMaster> recordTypeEquals(String recordType) {
        return (root, query, cb) -> cb.equal(root.get("recordType"), recordType);
    }

    public static Specification<TbObApplicationMaster> recordTypeIn(List<String> recordTypes) {
        return (root, query, cb) -> root.get("recordType").in(recordTypes);
    }

    public static Specification<TbObApplicationMaster> wfStageEquals(String wfStage) {
        return (root, query, cb) -> cb.equal(root.get("wfStage"), wfStage);
    }

    public static Specification<TbObApplicationMaster> channelTypeEquals(String channelType) {
        return (root, query, cb) -> cb.equal(root.get("channelType"), channelType);
    }

    public static Specification<TbObApplicationMaster> createdTsAfter(LocalDateTime after) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdTs"), after);
    }

    public static Specification<TbObApplicationMaster> createdTsBefore(LocalDateTime before) {
        return (root, query, cb) -> cb.lessThan(root.get("createdTs"), before);
    }

    /**
     * Case ageing is measured from the application's last activity — updatedTs, falling back to
     * createdTs for rows that have never been touched since creation.
     */
    private static Expression<LocalDateTime> lastActivityTs(Root<TbObApplicationMaster> root, CriteriaBuilder cb) {
        return cb.coalesce(root.<LocalDateTime>get("updatedTs"), root.<LocalDateTime>get("createdTs"));
    }

    public static Specification<TbObApplicationMaster> lastActivityBetween(LocalDateTime fromInclusive,
                                                                          LocalDateTime toExclusive) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(lastActivityTs(root, cb), fromInclusive),
                cb.lessThan(lastActivityTs(root, cb), toExclusive));
    }

    public static Specification<TbObApplicationMaster> lastActivityBefore(LocalDateTime before) {
        return (root, query, cb) -> cb.lessThan(lastActivityTs(root, cb), before);
    }

    public static Specification<TbObApplicationMaster> updatedByEquals(String updatedBy) {
        return (root, query, cb) -> cb.equal(root.get("updatedBy"), updatedBy);
    }

    public static Specification<TbObApplicationMaster> updatedTsAfter(LocalDateTime after) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("updatedTs"), after);
    }

    /**
     * (stage = value OR status = value) -- the GRT / GRTAPPROVED pattern used across
     * findPendingForGrt* and findPendingForActivation*.
     */
    public static Specification<TbObApplicationMaster> grtStyle(String value) {
        return (root, query, cb) -> cb.or(cb.equal(root.get("status"), value), cb.equal(root.get("stage"), value));
    }

    /** (stage = value OR status = value) -- same shape as {@link #grtStyle}, named for reinterview call sites. */
    public static Specification<TbObApplicationMaster> stageOrStatusEquals(String value) {
        return grtStyle(value);
    }

    /** wfStage = 'T24_ACTIVATED'. */
    public static Specification<TbObApplicationMaster> activatedSpec() {
        return (root, query, cb) -> cb.equal(root.get("wfStage"), "T24_ACTIVATED");
    }

    /** status = 'REJECTED' OR stage IN rejectStages. */
    public static Specification<TbObApplicationMaster> rejectedSpec(List<String> rejectStages) {
        return (root, query, cb) -> cb.or(cb.equal(root.get("status"), "REJECTED"), root.get("stage").in(rejectStages));
    }

    /** wfStage = 'T24_ACTIVATED' OR status = 'REJECTED' OR stage IN rejectStages. */
    public static Specification<TbObApplicationMaster> activatedOrRejectedSpec(List<String> rejectStages) {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("wfStage"), "T24_ACTIVATED"),
                cb.equal(root.get("status"), "REJECTED"),
                root.get("stage").in(rejectStages)
        );
    }


    public static Specification<TbObApplicationMaster> rpcPoolAllSpec(String pendingStage, String pendingWfStage) {
        return (root, query, cb) -> cb.or(
                cb.and(cb.equal(root.get("stage"), pendingStage), cb.equal(root.get("wfStage"), pendingWfStage)),
                cb.and(cb.equal(root.get("stage"), "ONHOLD"), cb.equal(root.get("wfStage"), "RPCONHOLD"))
        );
    }
}
