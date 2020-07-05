package com.assignment.distributedfilesharingapp;

import com.assignment.distributedfilesharingapp.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.Objects;

@SpringBootApplication
@PropertySource("classpath:application.yml")
public class DistributedFileSharingAppApplication {

    private final Environment environment;

    public DistributedFileSharingAppApplication(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public Node getNode() {
        return new Node(Objects.requireNonNull(environment.getProperty("app.node.node-name")));
    }

    public static void main(String[] args) {
        SpringApplication.run(DistributedFileSharingAppApplication.class, args);
    }

}
