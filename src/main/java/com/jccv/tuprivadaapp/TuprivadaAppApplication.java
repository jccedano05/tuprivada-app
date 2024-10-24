package com.jccv.tuprivadaapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.jccv.tuprivadaapp")
@EnableScheduling
public class TuprivadaAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(TuprivadaAppApplication.class, args);
	}

}
