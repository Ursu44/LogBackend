package com.example.logsbackend.repository;

import com.example.logsbackend.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, String> {

    List<Incident> findTop200ByOrderByCreatedAtDesc();

    List<Incident> findByEntityIdOrderByCreatedAtDesc(String entityId);

    List<Incident> findBySeverityOrderByCreatedAtDesc(String severity);

    List<Incident> findTop20BySeverityOrderByCreatedAtDesc(String severity);
    List<Incident> findTop10BySeverityOrderByCreatedAtDesc(String severity);

    @Query("SELECT i FROM Incident i JOIN i.attackTypes at " +
            "WHERE at = :attackType ORDER BY i.createdAt DESC")
    List<Incident> findByAttackType(@Param("attackType") String attackType);

    @Query("SELECT i FROM Incident i " +
            "WHERE i.createdAt >= " +
            "CAST(CURRENT_TIMESTAMP AS java.time.LocalDateTime) " +
            "- CAST(:minutes AS java.time.Duration) " +
            "ORDER BY i.createdAt DESC")
    List<Incident> findRecentIncidents(@Param("minutes") int minutes);

    @Query("SELECT i.severity, COUNT(i) FROM Incident i GROUP BY i.severity")
    List<Object[]> countBySeverity();
}