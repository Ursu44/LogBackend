package com.example.logsbackend.service;

import com.example.logsbackend.model.Incident;
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
public class IncidentKafkaConsumer {

    private final IncidentService incidentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
            topics = "correlated_incidents",
            groupId = "spring-incident-consumer"
    )
    public void consume(String message) {
        try {
            JsonNode json     = objectMapper.readTree(message);
            Incident incident = mapToIncident(json);
            incidentService.save(incident);

        } catch (Exception e) {
            log.error("Eroare la procesarea incidentului: {}", e.getMessage());
        }
    }

    private Incident mapToIncident(JsonNode json) {

        String incidentId = getText(json, "incident_id");
        String entityId   = getText(json, "entity_id");
        LocalDateTime createdAt = parseTimestamp(getText(json, "created_at"));
        String startTime  = getText(json, "start_time");
        String endTime    = getText(json, "end_time");
        Double durationSec = getDouble(json, "duration_sec");

        List<String> attackTypes  = parseStringList(json, "attack_types");
        List<String> mitreTactics = parseStringList(json, "mitre_tactics");
        String aptPattern  = getText(json, "apt_pattern");
        String severity    = getText(json, "severity");
        Boolean multiStage = getBoolean(json, "multi_stage");

        String rootCause           = getText(json, "root_cause");
        String rootCauseTs         = getText(json, "root_cause_ts");
        List<String> rootCauseRules = parseStringList(json, "root_cause_rules");
        Double rootCauseConfidence  = getDouble(json, "root_cause_confidence");

        Integer totalEvents  = getInt(json, "total_events");
        Integer highEvents   = getInt(json, "high_events");
        Integer mediumEvents = getInt(json, "medium_events");
        Double peakScore     = getDouble(json, "peak_score");

        Double avgConfidence    = getDouble(json, "avg_confidence");
        Double maxConfidence    = getDouble(json, "max_confidence");
        Double globalUncertainty = getDouble(json, "global_uncertainty");

        String timelineJson = null;
        JsonNode timelineNode = json.get("timeline");
        if (timelineNode != null && !timelineNode.isNull()) {
            timelineJson = timelineNode.toString();
        }

        List<String> eventIds = parseStringList(json, "event_ids");

        return Incident.builder()
                .incidentId(incidentId)
                .entityId(entityId)
                .createdAt(createdAt)
                .startTime(startTime)
                .endTime(endTime)
                .durationSec(durationSec)
                .attackTypes(attackTypes)
                .mitreTactics(mitreTactics)
                .aptPattern(aptPattern)
                .severity(severity)
                .multiStage(multiStage)
                .rootCause(rootCause)
                .rootCauseTs(rootCauseTs)
                .rootCauseRules(rootCauseRules)
                .rootCauseConfidence(rootCauseConfidence)
                .totalEvents(totalEvents)
                .highEvents(highEvents)
                .mediumEvents(mediumEvents)
                .peakScore(peakScore)
                .avgConfidence(avgConfidence)
                .maxConfidence(maxConfidence)
                .globalUncertainty(globalUncertainty)
                .timelineJson(timelineJson)
                .eventIds(eventIds)
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

    private List<String> parseStringList(JsonNode json, String field) {
        List<String> list = new ArrayList<>();
        JsonNode node = json.get(field);
        if (node != null && node.isArray()) {
            node.forEach(item -> {
                if (!item.isNull()) {
                    list.add(item.asText());
                }
            });
        }
        return list;
    }

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