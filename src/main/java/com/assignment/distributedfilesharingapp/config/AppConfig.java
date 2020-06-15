package com.assignment.distributedfilesharingapp.config;

import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.service.BootstrapServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class AppConfig {

    private final BootstrapServerService bootstrapServerService;

    public AppConfig(BootstrapServerService bootstrapServerService) {
        this.bootstrapServerService = bootstrapServerService;
        init();
    }

    private void init() {
        Node node = new Node();
        List<InetSocketAddress> neighbourNodes = new ArrayList<>();
        log.info("node created {}", node);
        try {
            neighbourNodes = this.bootstrapServerService.register(node.getUserName(), node.getIpAddress(), node.getPort());
        } catch (IOException e) {
            log.error("An error occurred while registering the node in bootstrap server", e);
        }

        // send pings to the nodes
        neighbourNodes.forEach(neighbourNode -> {

        });
        
    }
}
