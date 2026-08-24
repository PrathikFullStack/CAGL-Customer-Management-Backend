package com.iexceed.appzillonbanking.cagl.cob.domain.spec;

import com.iexceed.appzillonbanking.cagl.cob.constants.DashboardConstants;
import com.iexceed.appzillonbanking.cagl.cob.domain.ab.TbObApplicationMaster;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroup;
import com.iexceed.appzillonbanking.cagl.cob.payload.SearchRequestPayload;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OnboardingSpecification {

    public static Specification<TbObApplicationMaster> buildSearchSpecification(SearchRequestPayload fields) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Object searchValue = fields.getSearchValue();

            if (searchValue instanceof String) {
                String searchString = (String) searchValue;
                if (!searchString.isEmpty()) {
                    Subquery<String> groupSubquery = query.subquery(String.class);
                    Root<TbObGroup> groupRoot = groupSubquery.from(TbObGroup.class);
                    groupSubquery.select(groupRoot.get("groupId"))
                            .where(criteriaBuilder.like(groupRoot.get("groupName"), "%" + searchString + "%"));

                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.like(root.get("createdBy"), "%" + searchString + "%"),
                            criteriaBuilder.like(root.get("branchId"), "%" + searchString + "%"),
                            criteriaBuilder.like(root.get("applicationId"), "%" + searchString + "%"),
                            criteriaBuilder.like(root.get("customerName"), "%" + searchString + "%"),
                            criteriaBuilder.like(root.get("kendraId"), "%" + searchString + "%"),
                            criteriaBuilder.like(root.get("kendraName"), "%" + searchString + "%"),
                            criteriaBuilder.like(root.get("groupId"), "%" + searchString + "%"),
                            root.get("groupId").in(groupSubquery),
                          //  criteriaBuilder.like(root.get("voterId"), "%" + searchString + "%"),
                            criteriaBuilder.like(root.get("mobileNumber"), "%" + searchString + "%")
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<TbObApplicationMaster> buildFilterSpecification(SearchRequestPayload fields) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Object searchValue = fields.getSearchValue();
            if (searchValue instanceof List) {
                List<?> searchList = (List<?>) searchValue;
                if (!searchList.isEmpty() && searchList.get(0) instanceof Map) {
                    Map<String, List<String>> searchMap = (Map<String, List<String>>) searchList.get(0);
                    for (Map.Entry<String, List<String>> entry : searchMap.entrySet()) {
                        String field = entry.getKey();
                        List<String> values = entry.getValue();

                        if (values != null && !values.isEmpty()) {
                            switch (field) {
                                case "KM_ID":
                                case "KM_NAME":
                                    predicates.add(root.get("createdBy").in(values));
                                    break;
                                case "BRANCH_ID":
                                case "BRANCH_NAME":
                                    predicates.add(root.get("branchId").in(values));
                                    break;
                                case "CUSTOMER_ID":
                                case "MBDF_ID":
                                    predicates.add(root.get("applicationId").in(values));
                                    break;
                                case "CUSTOMER_NAME":
                                    predicates.add(root.get("customerName").in(values));
                                    break;
                                case "KENDRA_ID":
                                    predicates.add(root.get("kendraId").in(values));
                                    break;
                                case "KENDRA_NAME":
                                    predicates.add(root.get("kendraName").in(values));
                                    break;
                                case "GROUP_ID":
                                    predicates.add(root.get("groupId").in(values));
                                    break;
                                case "GROUP_NAME":
                                    Subquery<String> groupSubquery = query.subquery(String.class);
                                    Root<TbObGroup> groupRoot = groupSubquery.from(TbObGroup.class);
                                    groupSubquery.select(groupRoot.get("groupId"))
                                            .where(groupRoot.get("groupName").in(values));
                                    predicates.add(root.get("groupId").in(groupSubquery));
                                    break;
                                case "VALIDATED_KYC_ID":
                                  //  predicates.add(root.get("voterId").in(values));
                                    break;
                                case "REGISTERED_PHONE_NUMBER":
                                    predicates.add(root.get("mobileNumber").in(values));
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

    public static Specification<TbObApplicationMaster> buildLocalSearchSpecification(SearchRequestPayload fields) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String searchValue = (String) fields.getSearchValue();
            String tileType = fields.getTileType();

            if (searchValue != null && !searchValue.isEmpty() && tileType != null) {
                switch (tileType) {
                    case "Leads":
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("applicationId"), "%" + searchValue + "%"),
                                criteriaBuilder.like(root.get("customerName"), "%" + searchValue + "%"),
                                criteriaBuilder.like(root.get("mobileNumber"), "%" + searchValue + "%"),
                                criteriaBuilder.like(root.get("createdTs"), "%" + searchValue + "%"),
                                criteriaBuilder.like(root.get("channelType"), "%" + searchValue + "%")
                        ));
                        break;
                    default:
                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(root.get("customerName"), "%" + searchValue + "%"),
                                criteriaBuilder.like(root.get("kendraName"), "%" + searchValue + "%"),
                                criteriaBuilder.like(root.get("groupId"), "%" + searchValue + "%")
                        ));
                        break;
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}