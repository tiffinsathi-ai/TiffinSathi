package com.tiffin_sathi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TiffinSathiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TiffinSathiApplication.class, args);
        System.out.println("hello world");
    }
}
