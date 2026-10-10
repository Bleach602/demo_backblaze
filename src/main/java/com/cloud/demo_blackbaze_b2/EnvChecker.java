package com.cloud.demo_blackbaze_b2;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Configuration
@PropertySource(value = "file:.env", ignoreResourceNotFound = true)
@Component
@Slf4j
public class EnvChecker {

    private final Environment environment;

    public EnvChecker(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void checkEnv() {
        log.info("================================================");
        log.info("🔍 VÉRIFICATION DES VARIABLES D'ENVIRONNEMENT");
        log.info("================================================");
        log.info("DB_URL: {}", environment.getProperty("DB_URL"));
        log.info("DB_USERNAME: {}", environment.getProperty("DB_USERNAME"));
        log.info("DB_PASSWORD: {}", mask(environment.getProperty("DB_PASSWORD")));
        log.info("================================================");
    }

    private String mask(String value) {
        if (value == null) return "null";
        return "***";
    }
}

