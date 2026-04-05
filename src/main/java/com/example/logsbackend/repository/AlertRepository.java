package com.example.logsbackend.repository;

import com.example.logsbackend.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    List<Alert> findByRiskLevelOrderByTimestampIsoDesc(String riskLevel);

    List<Alert> findByLogCategoryOrderByTimestampIsoDesc(String logCategory);

    List<Alert> findByEntityIdOrderByTimestampIsoAsc(String entityId);

    List<Alert> findTop100ByOrderByTimestampIsoDesc();

    @Query("SELECT a FROM Alert a WHERE a.timestampIso >= :from " +
            "ORDER BY a.timestampIso DESC")
    List<Alert> findAllFromTime(@Param("from") LocalDateTime from);

    @Query("SELECT a FROM Alert a WHERE a.riskLevel = :riskLevel " +
            "AND a.timestampIso >= :from ORDER BY a.timestampIso DESC")
    List<Alert> findByRiskLevelAndTimeRange(
            @Param("riskLevel") String riskLevel,
            @Param("from") LocalDateTime from
    );

    long countByRiskLevel(String riskLevel);

    @Query("SELECT a.entityId, COUNT(a) as cnt FROM Alert a " +
            "WHERE a.riskLevel = 'HIGH' " +
            "GROUP BY a.entityId ORDER BY cnt DESC")
    List<Object[]> findTopEntitiesByHighAlerts();

    Optional<Alert> findTop1ByOrderByTimestampIsoDesc();

    @Query(value = "SELECT rule_name, COUNT(*) as cnt " +
            "FROM alert_rules_fired " +
            "GROUP BY rule_name ORDER BY cnt DESC LIMIT 10",
            nativeQuery = true)
    List<Object[]> findTopRulesFired();
}
