package com.example.logsbackend.service;

import com.example.logsbackend.model.Alert;
import com.example.logsbackend.model.EntityContext;
import com.example.logsbackend.model.ScoreBreakdown;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final AlertService alertService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "ml_alerts", groupId = "spring-detector")
    public void consume(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            Alert alert   = mapToAlert(json);
            alertService.save(alert);

        } catch (Exception e) {
            log.error("Eroare la procesarea mesajului: {}", e.getMessage());
        }
    }


    private Alert mapToAlert(JsonNode json) {
        String eventId        = getText(json, "event_id");
        Double timestamp      = getDouble(json, "timestamp");
        LocalDateTime tsIso   = parseTimestamp(getText(json, "timestamp_iso"));

        String rawLog         = getText(json, "raw_log");
        String templateId     = getText(json, "template_id");
        String logCategory    = getText(json, "log_category");
        String entityId       = getText(json, "entity_id");

        Boolean ruleTriggered = getBoolean(json, "rule_triggered");
        Double  ruleScore     = getDouble(json, "rule_score");
        Boolean ruleShortcut  = getBoolean(json, "rule_shortcut");
        List<String> rulesFired = parseRulesFired(json);

        Double statScore      = getDouble(json, "stat_score");
        Double behaviorScore  = getDouble(json, "behavior_score");
        Double catScore       = getDouble(json, "cat_score");
        Double rarity         = getDouble(json, "rarity");
        Double burst          = getDouble(json, "burst");

        Double rfScore        = getDouble(json, "rf_score");
        Double lstmScore      = getDouble(json, "lstm_score");

        EntityContext entityContext = parseEntityContext(json);

        ScoreBreakdown scoreBreakdown = parseScoreBreakdown(json);

        Double finalRisk      = getDouble(json, "final_risk");
        String riskLevel      = getText(json, "risk_level");

        return Alert.builder()
                .eventId(eventId)
                .timestamp(timestamp)
                .timestampIso(tsIso)
                .rawLog(rawLog)
                .templateId(templateId)
                .logCategory(logCategory)
                .entityId(entityId)
                .ruleTriggered(ruleTriggered)
                .ruleScore(ruleScore)
                .ruleShortcut(ruleShortcut)
                .rulesFired(rulesFired)
                .statScore(statScore)
                .behaviorScore(behaviorScore)
                .catScore(catScore)
                .rarity(rarity)
                .burst(burst)
                .rfScore(rfScore)
                .lstmScore(lstmScore)
                .entityContext(entityContext)
                .scoreBreakdown(scoreBreakdown)
                .finalRisk(finalRisk)
                .riskLevel(riskLevel)
                .build();
    }

    private LocalDateTime parseTimestamp(String isoStr) {
        if (isoStr == null) return null;
        try {
            return LocalDateTime.parse(
                    isoStr,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
            );
        } catch (Exception e) {
            log.warn("Format timestamp invalid: {}", isoStr);
            return null;
        }
    }

    private List<String> parseRulesFired(JsonNode json) {
        List<String> rules = new ArrayList<>();
        JsonNode node = json.get("rules_fired");
        if (node != null && node.isArray()) {
            node.forEach(r -> rules.add(r.asText()));
        }
        return rules;
    }

    private EntityContext parseEntityContext(JsonNode json) {
        JsonNode node = json.get("entity_context");
        if (node == null) return null;
        return EntityContext.builder()
                .failedAuth(getInt(node, "failed_auth"))
                .sudoCount(getInt(node, "sudo_count"))
                .uploads(getInt(node, "uploads"))
                .lsass(getInt(node, "lsass"))
                .build();
    }

    // =====================================================
    // Helper — parsare score_breakdown obiect imbricat
    // =====================================================
    private ScoreBreakdown parseScoreBreakdown(JsonNode json) {
        JsonNode node = json.get("score_breakdown");
        if (node == null) return null;
        return ScoreBreakdown.builder()
                .ruleEngine(getDouble(node, "rule_engine"))
                .isolationForest(getDouble(node, "isolation_forest"))
                .randomForest(getDouble(node, "random_forest"))
                .lstm(getDouble(node, "lstm"))
                .build();
    }

    // =====================================================
    // Helper methods cu null-safety
    // =====================================================
    private String getText(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asText() : null;
    }

    private Double getDouble(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asDouble() : null;
    }

    private Boolean getBoolean(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asBoolean() : null;
    }

    private Integer getInt(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asInt() : null;
    }
}
