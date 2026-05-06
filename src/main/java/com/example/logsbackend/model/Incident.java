package com.example.logsbackend.model;

import com.example.logsbackend.config.JsonbType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "incidents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @Column(name = "incident_id", nullable = false, unique = true)
    private String incidentId;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_time")
    private String endTime;

    @Column(name = "duration_sec")
    private Double durationSec;

    @ElementCollection
    @CollectionTable(
            name = "incident_attack_types",
            joinColumns = @JoinColumn(name = "incident_id")
    )
    @Column(name = "attack_type")
    private List<String> attackTypes;

    @ElementCollection
    @CollectionTable(
            name = "incident_mitre_tactics",
            joinColumns = @JoinColumn(name = "incident_id")
    )
    @Column(name = "mitre_tactic")
    private List<String> mitreTactics;

    @Column(name = "apt_pattern", length = 500)
    private String aptPattern;

    @Column(name = "severity")
    private String severity;

    @Column(name = "multi_stage")
    private Boolean multiStage;

    @Column(name = "root_cause", length = 1000)
    private String rootCause;

    @Column(name = "root_cause_ts")
    private String rootCauseTs;

    @ElementCollection
    @CollectionTable(
            name = "incident_root_cause_rules",
            joinColumns = @JoinColumn(name = "incident_id")
    )
    @Column(name = "rule_name")
    private List<String> rootCauseRules;

    @Column(name = "root_cause_confidence")
    private Double rootCauseConfidence;

    @Column(name = "total_events")
    private Integer totalEvents;

    @Column(name = "high_events")
    private Integer highEvents;

    @Column(name = "medium_events")
    private Integer mediumEvents;

    @Column(name = "peak_score")
    private Double peakScore;

    @Column(name = "avg_confidence")
    private Double avgConfidence;

    @Column(name = "max_confidence")
    private Double maxConfidence;

    @Column(name = "global_uncertainty")
    private Double globalUncertainty;

    @Type(JsonbType.class)
    @Column(name = "timeline", columnDefinition = "jsonb")
    private String timelineJson;

    @ElementCollection
    @CollectionTable(
            name = "incident_event_ids",
            joinColumns = @JoinColumn(name = "incident_id")
    )
    @Column(name = "event_id")
    private List<String> eventIds;
}