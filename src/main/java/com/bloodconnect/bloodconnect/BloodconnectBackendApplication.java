package com.bloodconnect.bloodconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@SuppressWarnings("unused")
public class BloodconnectBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BloodconnectBackendApplication.class, args);
	}

}
