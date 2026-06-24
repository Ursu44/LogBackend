package com.example.logsbackend.service;

import com.example.logsbackend.model.Incident;
import com.example.logsbackend.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;

    private final Sinks.Many<Incident> incidentSink =
            Sinks.many().multicast().onBackpressureBuffer();

    public void save(Incident incident) {
        try {
            if (incidentRepository.existsById(incident.getIncidentId())) {
                log.debug("Incident deja existent: {}", incident.getIncidentId());
                return;
            }
            incidentRepository.save(incident);
            incidentSink.tryEmitNext(incident);
            log.info("✅ Incident salvat: {} — {} — {}",
                    incident.getIncidentId(),
                    incident.getEntityId(),
                    incident.getSeverity());
        } catch (Exception e) {
            log.error("❌ Eroare la salvarea incidentului: {}", e.getMessage());
        }
    }

    public List<Incident> getRecentIncidents() {
        List<Incident> critical = incidentRepository
                .findTop20BySeverityOrderByCreatedAtDesc("CRITICAL");
        List<Incident> high = incidentRepository
                .findTop10BySeverityOrderByCreatedAtDesc("HIGH");
        List<Incident> medium = incidentRepository
                .findTop10BySeverityOrderByCreatedAtDesc("MEDIUM");

        List<Incident> all = new ArrayList<>();
        all.addAll(critical);
        all.addAll(high);
        all.addAll(medium);

        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return all;
    }

    public Optional<Incident> getIncident(String incidentId) {
        return incidentRepository.findById(incidentId);
    }

    public List<Incident> getIncidentsByEntity(String entityId) {
        return incidentRepository.findByEntityIdOrderByCreatedAtDesc(entityId);
    }

    public List<Incident> getIncidentsBySeverity(String severity) {
        return incidentRepository.findBySeverityOrderByCreatedAtDesc(severity);
    }

    public List<Incident> getIncidentsByAttackType(String attackType) {
        return incidentRepository.findByAttackType(attackType);
    }

    public Flux<Incident> getIncidentStream() {
        return incidentSink.asFlux();
    }

    public Flux<Incident> getCriticalIncidentStream() {
        return incidentSink.asFlux()
                .filter(i -> "CRITICAL".equals(i.getSeverity()) ||
                        "HIGH".equals(i.getSeverity()));
    }
}