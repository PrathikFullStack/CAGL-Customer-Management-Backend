package com.iexceed.appzillonbanking.cagl.document.repository.ab;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.iexceed.appzillonbanking.cagl.document.domain.ab.TbUaobIncomeAssessmentHis;
import com.iexceed.appzillonbanking.cagl.document.domain.ab.TbUaobIncomeAssessmentIdHis;



public interface TbUaobIncomeAssessmentHisRepository extends CrudRepository<TbUaobIncomeAssessmentHis, TbUaobIncomeAssessmentIdHis> {

    @Query(value = "SELECT * FROM public.tb_uaob_income_assessment WHERE application_id = :applicationId AND version_no = (SELECT MAX(version_no) FROM public.tb_uaob_income_assessment WHERE application_id = :applicationId)",nativeQuery = true)
    Optional<TbUaobIncomeAssessmentHis> findByApplicationId(String applicationId);

}
