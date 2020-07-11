package com.assignment.distributedfilesharingapp.common;

import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.service.BootstrapServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Slf4j
@Component
public class ShutDownConfig {

    private final Node node;

    private final BootstrapServerService bootstrapServerService;

    public ShutDownConfig(BootstrapServerService bootstrapServerService, Node node) {
        this.bootstrapServerService = bootstrapServerService;
        this.node = node;
    }

    @PreDestroy
    public void initDestroy() {
        log.info("shut down init {}", node);
        try {
            log.info("un registration init done {}", bootstrapServerService.unRegister(node.getUserName(), node.getIpAddress(), node.getPort()));
        } catch (IOException e) {
            log.error("error occurred while shutting down the node", e);
        }
    }
}
