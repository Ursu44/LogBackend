package com.example.logsbackend.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityContext {
    private Integer failedAuth;
    private Integer sudoCount;
    private Integer uploads;
    private Integer lsass;
}
