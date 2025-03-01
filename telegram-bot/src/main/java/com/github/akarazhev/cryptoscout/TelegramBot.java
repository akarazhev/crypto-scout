package com.github.akarazhev.cryptoscout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TelegramBot {

    public static void main(String[] args) {
        SpringApplication.run(TelegramBot.class, args);
    }
}
