package com.example.logsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableKafka
@EnableWebSocket
public class LogsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogsBackendApplication.class, args);
    }

}
