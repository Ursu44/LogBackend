package com.example.logsbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Alert {

    @Id
    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "timestamp_val")
    private Double timestamp;

    @Column(name = "timestamp_iso")
    private LocalDateTime timestampIso;

    @Column(name = "log_category")
    private String logCategory;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "rule_triggered")
    private Boolean ruleTriggered;

    @Column(name = "rule_score")
    private Double ruleScore;

    @Column(name = "rule_shortcut")
    private Boolean ruleShortcut;

    @ElementCollection
    @CollectionTable(
            name = "alert_rules_fired",
            joinColumns = @JoinColumn(name = "event_id")
    )

    @Column(name = "rule_name")
    private List<String> rulesFired;

    @Column(name = "stat_score")
    private Double statScore;

    @Column(name = "behavior_score")
    private Double behaviorScore;

    @Column(name = "cat_score")
    private Double catScore;

    @Column(name = "rarity")
    private Double rarity;

    @Column(name = "raw_log", length = 500)
    private String rawLog;

    @Column(name = "templateId", length = 500)
    private String templateId;

    @Column(name = "burst")
    private Double burst;

    @Column(name = "rf_score")
    private Double rfScore;

    @Column(name = "lstm_score")
    private Double lstmScore;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "failedAuth", column = @Column(name = "ctx_failed_auth")),
            @AttributeOverride(name = "sudoCount",  column = @Column(name = "ctx_sudo_count")),
            @AttributeOverride(name = "uploads",    column = @Column(name = "ctx_uploads")),
            @AttributeOverride(name = "lsass",      column = @Column(name = "ctx_lsass"))
    })
    private EntityContext entityContext;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "ruleEngine",      column = @Column(name = "sb_rule_engine")),
            @AttributeOverride(name = "isolationForest", column = @Column(name = "sb_isolation_forest")),
            @AttributeOverride(name = "randomForest",    column = @Column(name = "sb_random_forest")),
            @AttributeOverride(name = "lstm",            column = @Column(name = "sb_lstm"))
    })
    private ScoreBreakdown scoreBreakdown;

    @Column(name = "final_risk")
    private Double finalRisk;

    @Column(name = "risk_level")
    private String riskLevel;
}
