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
            Sinks.many().multicast().onBackpressureBuffer(1000, false);

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

        LocalDateTime maxTimestamp = alertRepository
                .findTop1ByOrderByTimestampIsoDesc()
                .map(Alert::getTimestampIso)
                .orElse(LocalDateTime.now());

        log.info("=== getAlerts ===");
        log.info("riskLevel={}, category={}, entityId={}, windowMinutes={}, limit={}",
                riskLevel, category, entityId, windowMinutes, limit);
        log.info("maxTimestamp: {}", maxTimestamp);

        LocalDateTime from = windowMinutes != null
                ? maxTimestamp.minusMinutes(windowMinutes)
                : null;

        log.info("from: {}", from);

        List<Alert> results;

        if (entityId != null) {
            log.info("→ ramura entityId");
            results = alertRepository
                    .findByEntityIdOrderByTimestampIsoAsc(entityId);

        } else if (riskLevel != null && from != null) {
            log.info("→ ramura riskLevel + from");
            LocalDateTime finalFrom = from;
            results = alertRepository
                    .findByRiskLevelOrderByTimestampIsoDesc(riskLevel)
                    .stream()
                    .filter(a -> a.getTimestampIso() != null &&
                            a.getTimestampIso().isAfter(finalFrom))
                    .toList();

        } else if (riskLevel != null) {
            log.info("→ ramura riskLevel fără from");
            results = alertRepository
                    .findByRiskLevelOrderByTimestampIsoDesc(riskLevel);

        } else if (category != null && from != null) {
            log.info("→ ramura category + from");
            LocalDateTime finalFrom = from;
            results = alertRepository
                    .findByLogCategoryOrderByTimestampIsoDesc(category)
                    .stream()
                    .filter(a -> a.getTimestampIso() != null &&
                            a.getTimestampIso().isAfter(finalFrom))
                    .toList();

        } else if (category != null) {
            log.info("→ ramura category fără from");
            results = alertRepository
                    .findByLogCategoryOrderByTimestampIsoDesc(category);

        } else if (from != null) {
            log.info("→ ramura from fără riskLevel");
            results = alertRepository.findAllFromTime(from);

        } else {
            log.info("→ ramura default");
            results = alertRepository
                    .findTop100ByOrderByTimestampIsoDesc();
        }

        log.info("rezultate înainte de limit: {}", results.size());

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