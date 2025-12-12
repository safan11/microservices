package com.tcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class Orderservice2Application {

	public static void main(String[] args) {
		SpringApplication.run(Orderservice2Application.class, args);
	}
	
	

}
