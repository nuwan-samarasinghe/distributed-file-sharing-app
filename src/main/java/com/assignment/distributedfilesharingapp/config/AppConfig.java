package com.assignment.distributedfilesharingapp.config;

import com.assignment.distributedfilesharingapp.common.FileManager;
import com.assignment.distributedfilesharingapp.common.MessageBrokerThread;
import com.assignment.distributedfilesharingapp.common.handlers.*;
import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.service.BootstrapServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class AppConfig {

    @Value("${app.common.r-ping-message-id}")
    private String rPingMessageId;

    @Value("${app.bootstrap-server.ping-interval}")
    private Integer pingInterval;

    @Value("${app.bootstrap-server.node-name}")
    private String nodeName;

    private final BootstrapServerService bootstrapServerService;

    private String username;

    private final PingRequestHandler pingRequestHandler;

    private final LeaveRequestHandler leaveRequestHandler;

    private final ResponseHandlerFactory responseHandlerFactory;

    public AppConfig(
            BootstrapServerService bootstrapServerService,
            PingRequestHandler pingRequestHandler,
            LeaveRequestHandler leaveRequestHandler,
            PongRequestHandler pongRequestHandler,
            SearchRequestHandler searchRequestHandler,
            QueryRequestHandler queryRequestHandler) {
        this.bootstrapServerService = bootstrapServerService;
        init();
        this.pingRequestHandler = pingRequestHandler;
        this.leaveRequestHandler = leaveRequestHandler;
        searchRequestHandler.setFileManager(getFileManager());
        responseHandlerFactory = new ResponseHandlerFactory(pingRequestHandler, pongRequestHandler, searchRequestHandler, queryRequestHandler);
    }

    private void init() {
        Node node = new Node(nodeName);
        List<InetSocketAddress> neighbourNodes = new ArrayList<>();
        log.info("node created {}", node);
        try {
            neighbourNodes = this.bootstrapServerService.register(node.getUserName(), node.getIpAddress(), node.getPort());
            this.username = node.getUserName();
        } catch (IOException e) {
            log.error("An error occurred while registering the node in bootstrap server", e);
        }

        MessageBrokerThread messageBrokerThread = new MessageBrokerThread(
                node.getIpAddress(),
                node.getPort(),
                pingRequestHandler,
                leaveRequestHandler,
                getFileManager(),
                rPingMessageId,
                pingInterval,
                responseHandlerFactory);
        new Thread(messageBrokerThread).start();

        // send pings to the nodes
        neighbourNodes.forEach(neighbourNode -> {

        });

    }

    @Bean
    public FileManager getFileManager() {
        return new FileManager(this.username);
    }
}
