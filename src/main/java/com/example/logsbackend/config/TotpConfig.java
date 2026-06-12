
package com.example.logsbackend.config;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.secret.*;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.context.annotation.*;

@Configuration
public class TotpConfig {

    @Bean
    public SecretGenerator secretGenerator() {
        return new DefaultSecretGenerator(32);
    }

    @Bean
    public TimeProvider timeProvider() {
        return new SystemTimeProvider();
    }

    @Bean
    public CodeGenerator codeGenerator() {
        return new DefaultCodeGenerator(
                HashingAlgorithm.SHA1, 6);
    }

    @Bean
    public CodeVerifier codeVerifier(
            TimeProvider  timeProvider,
            CodeGenerator codeGenerator) {

        DefaultCodeVerifier verifier =
                new DefaultCodeVerifier(
                        codeGenerator, timeProvider);

        verifier.setTimePeriod(30);
        verifier.setAllowedTimePeriodDiscrepancy(1);

        return verifier;
    }
}