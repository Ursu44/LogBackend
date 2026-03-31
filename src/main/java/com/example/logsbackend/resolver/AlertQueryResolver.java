package com.example.logsbackend.resolver;

import com.example.logsbackend.model.Alert;
import com.example.logsbackend.service.AlertService;
import com.example.logsbackend.service.AlertService.DashboardStats;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AlertQueryResolver {

    private final AlertService alertService;
    @QueryMapping
    public List<Alert> recentAlerts() {
        return alertService.getRecentAlerts();
    }

    @QueryMapping
    public List<Alert> alerts(
            @Argument String riskLevel,
            @Argument String category,
            @Argument String entityId,
            @Argument Integer windowMinutes,
            @Argument Integer limit
    ) {
        return alertService.getAlerts(
                riskLevel,
                category,
                entityId,
                windowMinutes,
                limit != null ? limit : 100
        );
    }

    @QueryMapping
    public Alert alert(@Argument String eventId) {
        return alertService.getAlertById(eventId);
    }

    @QueryMapping
    public DashboardStats dashboardStats(
            @Argument Integer windowMinutes
    ) {
        return alertService.getDashboardStats(
                windowMinutes != null ? windowMinutes : 30
        );
    }

    @QueryMapping
    public List<Alert> entityHistory(
            @Argument String entityId,
            @Argument Integer windowMinutes
    ) {
        return alertService.getEntityHistory(
                entityId,
                windowMinutes != null ? windowMinutes : 30
        );
    }
}