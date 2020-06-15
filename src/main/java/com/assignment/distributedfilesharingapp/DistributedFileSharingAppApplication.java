package com.assignment.distributedfilesharingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.yml")
public class DistributedFileSharingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedFileSharingAppApplication.class, args);
    }

}
