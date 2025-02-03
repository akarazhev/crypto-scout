package com.github.akarazhev.cryptoscout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CexClient {

	public static void main(String[] args) {
		SpringApplication.run(CexClient.class, args);
	}

}
