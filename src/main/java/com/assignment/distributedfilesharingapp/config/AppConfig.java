package com.assignment.distributedfilesharingapp.config;

import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.service.BootstrapServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class AppConfig {

    private final BootstrapServerService bootstrapServerService;

    public AppConfig(BootstrapServerService bootstrapServerService) throws IOException {
        this.bootstrapServerService = bootstrapServerService;
        Node node = new Node();
        bootstrapServerService.register("nsamarasinghe", node.getIpAddress(), node.getPort());
    }
}
