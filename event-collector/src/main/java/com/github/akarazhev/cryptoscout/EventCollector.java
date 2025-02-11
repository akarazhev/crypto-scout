package com.github.akarazhev.cryptoscout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
class EventCollector {

    public static void main(final String[] args) {
        SpringApplication.run(EventCollector.class, args);
    }
}
