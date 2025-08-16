package ru.practicum.ewm.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "ru/practicum/ewm/dto")
@EnableJpaRepositories(basePackages = "ru.practicum.ewm.server.repository")
public class EWMStatisticServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EWMStatisticServiceApp.class);
    }
}
