package com.plg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.plg")
public class PlgApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlgApplication.class, args);
	}

}
