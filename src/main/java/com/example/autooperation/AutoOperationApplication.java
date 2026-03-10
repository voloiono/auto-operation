package com.example.autooperation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AutoOperationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoOperationApplication.class, args);
    }

}
