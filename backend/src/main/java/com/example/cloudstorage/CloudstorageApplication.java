package com.example.cloudstorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudstorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudstorageApplication.class, args);
	}
}
