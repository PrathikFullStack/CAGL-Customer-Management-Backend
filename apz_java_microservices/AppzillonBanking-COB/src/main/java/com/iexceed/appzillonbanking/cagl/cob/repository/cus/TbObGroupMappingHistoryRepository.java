package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObGroupMappingHistory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface TbObGroupMappingHistoryRepository extends JpaRepository<TbObGroupMappingHistory, String> {

    @Query(value = """
            SELECT *
            FROM tb_ob_group_mapping_history
            WHERE application_id = :applicationId
            ORDER BY created_ts DESC
            LIMIT 1
            """, nativeQuery = true)
    TbObGroupMappingHistory findLatestApplication(
            @Param("applicationId") String applicationId);



    @Query(value = """
            SELECT h.*
            FROM tb_ob_group_mapping_history h
            INNER JOIN
            (
                SELECT
                    application_id,
                    MAX(created_ts) AS created_ts
                FROM tb_ob_group_mapping_history
                GROUP BY application_id
            ) latest
                ON h.application_id = latest.application_id
               AND h.created_ts = latest.created_ts
            WHERE
                CASE
                    WHEN h.to_group_id IS NOT NULL
                    THEN h.to_group_id
                    ELSE h.from_group_id
                END = :groupId
            """, nativeQuery = true)
    List<TbObGroupMappingHistory> findCurrentApplicationsByGroup(
            @Param("groupId") String groupId);



    @Query(value = """
            SELECT COUNT(*)
            FROM
            (
                SELECT
                    h.application_id,
                    CASE
                        WHEN h.to_group_id IS NOT NULL
                        THEN h.to_group_id
                        ELSE h.from_group_id
                    END AS current_group
                FROM tb_ob_group_mapping_history h
                INNER JOIN
                (
                    SELECT
                        application_id,
                        MAX(created_ts) AS created_ts
                    FROM tb_ob_group_mapping_history
                    GROUP BY application_id
                ) latest
                    ON latest.application_id = h.application_id
                   AND latest.created_ts = h.created_ts
            ) x
            WHERE x.current_group = :groupId
            """, nativeQuery = true)
    Integer getCurrentGroupCount(
            @Param("groupId") String groupId);



    @Query(value = """
            SELECT COUNT(*)
            FROM
            (
                SELECT
                    h.application_id,
                    CASE
                        WHEN h.to_kendra_id IS NOT NULL
                        THEN h.to_kendra_id
                        ELSE h.from_kendra_id
                    END AS current_kendra
                FROM tb_ob_group_mapping_history h
                INNER JOIN
                (
                    SELECT
                        application_id,
                        MAX(created_ts) AS created_ts
                    FROM tb_ob_group_mapping_history
                    GROUP BY application_id
                ) latest
                    ON latest.application_id = h.application_id
                   AND latest.created_ts = h.created_ts
            ) x
            WHERE x.current_kendra = :kendraId
            """, nativeQuery = true)
    Integer getCurrentKendraCount(
            @Param("kendraId") String kendraId);



    @Query(value = """
            SELECT current_member_count
            FROM tb_ob_group_mapping_history
            WHERE
                CASE
                    WHEN to_group_id IS NOT NULL
                    THEN to_group_id
                    ELSE from_group_id
                END = :groupId
            ORDER BY created_ts DESC
            LIMIT 1
            """, nativeQuery = true)
    Integer getLatestMemberCount(
            @Param("groupId") String groupId);

}