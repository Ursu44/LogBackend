package com.example.logsbackend.resolver;

import com.example.logsbackend.model.Alert;
import com.example.logsbackend.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class AlertSubscriptionResolver {

    private final AlertService alertService;

    @SubscriptionMapping
    public Flux<Alert> newAlert() {
        return alertService.getAlertStream();
    }

    @SubscriptionMapping
    public Flux<Alert> newHighAlert() {
        return alertService.getHighAlertStream();
    }
}