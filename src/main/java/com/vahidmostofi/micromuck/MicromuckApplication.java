package com.vahidmostofi.micromuck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@Configuration("application")
public class MicromuckApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicromuckApplication.class, args);
	}

}
