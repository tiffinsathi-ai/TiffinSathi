package com.tiffin_sathi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TiffinSathiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TiffinSathiApplication.class, args);
        System.out.println("hello world");
	}

}
