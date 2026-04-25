package com.example.logsbackend.service;

import com.example.logsbackend.model.Alert;
import com.example.logsbackend.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
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
            return alert;
        }

        alertSink.tryEmitNext(alert);

        Alert saved = alertRepository.save(alert);
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
                .orElse(LocalDateTime.now(ZoneOffset.UTC));


        LocalDateTime from = windowMinutes != null
                ? maxTimestamp.minusMinutes(windowMinutes)
                : null;


        List<Alert> results;
        LocalDateTime finalFrom = from;

        if (entityId != null) {
            results = alertRepository
                    .findByEntityIdOrderByTimestampIsoAsc(entityId)
                    .stream()
                    .filter(a -> {
                        if (finalFrom != null &&
                                a.getTimestampIso() != null &&
                                !a.getTimestampIso().isAfter(finalFrom)) {
                            return false;
                        }
                        if (category != null &&
                                !category.equals(a.getLogCategory())) {
                            return false;
                        }
                        if (riskLevel != null &&
                                !riskLevel.equals(a.getRiskLevel())) {
                            return false;
                        }
                        return true;
                    })
                    .toList();

        } else if (riskLevel != null && from != null) {
            results = alertRepository
                    .findByRiskLevelOrderByTimestampIsoDesc(riskLevel)
                    .stream()
                    .filter(a -> a.getTimestampIso() != null &&
                            a.getTimestampIso().isAfter(finalFrom))
                    .filter(a -> category == null ||
                            category.equals(a.getLogCategory()))
                    .toList();

        } else if (riskLevel != null) {
            results = alertRepository
                    .findByRiskLevelOrderByTimestampIsoDesc(riskLevel)
                    .stream()
                    .filter(a -> category == null ||
                            category.equals(a.getLogCategory()))
                    .toList();

        } else if (category != null && from != null) {
            results = alertRepository
                    .findByLogCategoryOrderByTimestampIsoDesc(category)
                    .stream()
                    .filter(a -> a.getTimestampIso() != null &&
                            a.getTimestampIso().isAfter(finalFrom))
                    .toList();

        } else if (category != null) {
            results = alertRepository
                    .findByLogCategoryOrderByTimestampIsoDesc(category);

        } else if (from != null) {
            results = alertRepository.findAllFromTime(from);

        } else {
            results = alertRepository
                    .findTop100ByOrderByTimestampIsoDesc();
        }


        return results.stream().limit(limit).toList();
    }

    public Alert getAlertById(String eventId) {
        log.info("Id-ul eveniment"+eventId);
        log.info("Rezultat id "+alertRepository.findById(eventId).orElse(null));
        return alertRepository.findById(eventId).orElse(null);
    }

    public List<Alert> getEntityHistory(String entityId,
                                        int windowMinutes) {
        LocalDateTime maxTimestamp = alertRepository
                .findTop1ByOrderByTimestampIsoDesc()
                .map(Alert::getTimestampIso)
                .orElse(LocalDateTime.now(ZoneOffset.UTC));

        LocalDateTime from = maxTimestamp.minusMinutes(windowMinutes);

        return alertRepository
                .findByEntityIdOrderByTimestampIsoAsc(entityId)
                .stream()
                .filter(a -> a.getTimestampIso() != null &&
                        a.getTimestampIso().isAfter(from))
                .toList();
    }

    public DashboardStats getDashboardStats(int windowMinutes) {
        LocalDateTime maxTimestamp = alertRepository
                .findTop1ByOrderByTimestampIsoDesc()
                .map(Alert::getTimestampIso)
                .orElse(LocalDateTime.now(ZoneOffset.UTC));

        LocalDateTime from = maxTimestamp.minusMinutes(windowMinutes);

        List<Alert> recent = alertRepository.findAllFromTime(from);

        long highCount   = recent.stream()
                .filter(a -> "HIGH".equals(a.getRiskLevel())).count();
        long mediumCount = recent.stream()
                .filter(a -> "MEDIUM".equals(a.getRiskLevel())).count();
        long lowCount    = recent.stream()
                .filter(a -> "LOW".equals(a.getRiskLevel())).count();

        String oldest = recent.stream()
                .map(Alert::getTimestampIso)
                .filter(t -> t != null)
                .min(Comparator.naturalOrder())
                .map(Object::toString)
                .orElse(null);

        String newest = recent.stream()
                .map(Alert::getTimestampIso)
                .filter(t -> t != null)
                .max(Comparator.naturalOrder())
                .map(Object::toString)
                .orElse(null);

        return new DashboardStats(
                recent.size(),
                (int) highCount,
                (int) mediumCount,
                (int) lowCount,
                oldest,
                newest
        );
    }

    public record DashboardStats(
            int totalAlerts,
            int highCount,
            int mediumCount,
            int lowCount,
            String oldestAlert,
            String newestAlert
    ) {}
}