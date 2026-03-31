package com.example.logsbackend.service;

import com.example.logsbackend.model.Alert;
import com.example.logsbackend.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;

    private final Sinks.Many<Alert> alertSink =
            Sinks.many().multicast().onBackpressureBuffer();

    public Alert save(Alert alert) {
        if (alertRepository.existsById(alert.getEventId())) {
            log.debug("Alertă duplicat ignorată: {}", alert.getEventId());
            return alert;
        }

        alertSink.tryEmitNext(alert);

        if ("LOW".equals(alert.getRiskLevel())) {
            log.debug("LOW alert — skip PostgreSQL: {}", alert.getEventId());
            return alert;
        }

        Alert saved = alertRepository.save(alert);
        System.out.println("✅ Alert salvat: " + saved.getEventId() +
                " [" + saved.getRiskLevel() + "] entity=" +
                saved.getEntityId());
        return saved;
    }

    public Flux<Alert> getAlertStream() {
        return alertSink.asFlux();
    }

    public Flux<Alert> getHighAlertStream() {
        return alertSink.asFlux()
                .filter(a -> "HIGH".equals(a.getRiskLevel()));
    }

    public List<Alert> getRecentAlerts() {
        return alertRepository.findTop100ByOrderByTimestampIsoDesc();
    }

    public List<Alert> getAlerts(String riskLevel, String category,
                                 String entityId, Integer windowMinutes,
                                 int limit) {
        List<Alert> results;

        if (entityId != null) {
            results = alertRepository
                    .findByEntityIdOrderByTimestampIsoAsc(entityId);
        } else if (riskLevel != null && windowMinutes != null) {
            LocalDateTime from = LocalDateTime.now()
                    .minusMinutes(windowMinutes);
            results = alertRepository
                    .findByRiskLevelAndTimeRange(riskLevel, from);
        } else if (riskLevel != null) {
            results = alertRepository
                    .findByRiskLevelOrderByTimestampIsoDesc(riskLevel);
        } else if (category != null) {
            results = alertRepository
                    .findByLogCategoryOrderByTimestampIsoDesc(category);
        } else if (windowMinutes != null) {
            LocalDateTime from = LocalDateTime.now()
                    .minusMinutes(windowMinutes);
            results = alertRepository.findAllFromTime(from);
        } else {
            results = alertRepository
                    .findTop100ByOrderByTimestampIsoDesc();
        }

        return results.stream().limit(limit).toList();
    }

    public Alert getAlertById(String eventId) {
        return alertRepository.findById(eventId).orElse(null);
    }

    public List<Alert> getEntityHistory(String entityId,
                                        int windowMinutes) {
        LocalDateTime from = LocalDateTime.now()
                .minusMinutes(windowMinutes);

        return alertRepository
                .findByEntityIdOrderByTimestampIsoAsc(entityId)
                .stream()
                .filter(a -> a.getTimestampIso() != null &&
                        a.getTimestampIso().isAfter(from))
                .toList();
    }

    public DashboardStats getDashboardStats(int windowMinutes) {
        LocalDateTime from = LocalDateTime.now()
                .minusMinutes(windowMinutes);

        List<Alert> recent = alertRepository.findAllFromTime(from);

        long highCount   = recent.stream()
                .filter(a -> "HIGH".equals(a.getRiskLevel())).count();
        long mediumCount = recent.stream()
                .filter(a -> "MEDIUM".equals(a.getRiskLevel())).count();
        long lowCount    = recent.stream()
                .filter(a -> "LOW".equals(a.getRiskLevel())).count();

        return new DashboardStats(
                recent.size(),
                (int) highCount,
                (int) mediumCount,
                (int) lowCount
        );
    }

    public record DashboardStats(
            int totalAlerts,
            int highCount,
            int mediumCount,
            int lowCount
    ) {}
}