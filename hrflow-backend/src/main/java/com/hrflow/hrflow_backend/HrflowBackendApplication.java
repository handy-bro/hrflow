package com.hrflow.hrflow_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HrflowBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrflowBackendApplication.class, args);
	}

}
