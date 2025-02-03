package com.github.akarazhev.cryptoscout;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
class Scheduler {

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void perform() {
        // Your task logic goes here
        System.out.println("Scheduled task executed at: " + new java.util.Date());
    }
}
