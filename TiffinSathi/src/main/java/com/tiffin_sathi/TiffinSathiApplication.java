package com.tiffin_sathi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TiffinSathiApplication {
<<<<<<< HEAD
    public static void main(String[] args) {
        SpringApplication.run(TiffinSathiApplication.class, args);
        System.out.println("hello world");
    }
=======

	public static void main(String[] args) {
		SpringApplication.run(TiffinSathiApplication.class, args);
        System.out.println("hello world");
	}

>>>>>>> 3c958865420043907b62593e108c3e6b062ee0dc
}
