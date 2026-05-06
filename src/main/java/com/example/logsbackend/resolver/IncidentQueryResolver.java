package com.example.logsbackend.resolver;

import com.example.logsbackend.model.Incident;
import com.example.logsbackend.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IncidentQueryResolver {

    private final IncidentService incidentService;

    @QueryMapping
    public List<Incident> recentIncidents() {
        return incidentService.getRecentIncidents();
    }

    @QueryMapping
    public Incident incident(@Argument String incidentId) {
        return incidentService.getIncident(incidentId).orElse(null);
    }

    @QueryMapping
    public List<Incident> incidentsByEntity(@Argument String entityId) {
        return incidentService.getIncidentsByEntity(entityId);
    }

    @QueryMapping
    public List<Incident> incidentsBySeverity(@Argument String severity) {
        return incidentService.getIncidentsBySeverity(severity);
    }

    @QueryMapping
    public List<Incident> incidentsByAttackType(@Argument String attackType) {
        return incidentService.getIncidentsByAttackType(attackType);
    }

    @SubscriptionMapping
    public Flux<Incident> newIncident() {
        return incidentService.getIncidentStream();
    }

    @SubscriptionMapping
    public Flux<Incident> newCriticalIncident() {
        return incidentService.getCriticalIncidentStream();
    }
}