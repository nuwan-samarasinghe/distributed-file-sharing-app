package com.assignment.distributedfilesharingapp.config;

import com.assignment.distributedfilesharingapp.common.FileManager;
import com.assignment.distributedfilesharingapp.common.MessageBrokerThread;
import com.assignment.distributedfilesharingapp.common.handlers.*;
import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.service.BootstrapServerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Configuration
public class AppConfig {

    private String nodeName;

    private final BootstrapServerService bootstrapServerService;

    private String username;

    private final PingRequestHandler pingRequestHandler;

    private final LeaveRequestHandler leaveRequestHandler;

    private final ResponseHandlerFactory responseHandlerFactory;

    private final SearchRequestHandler searchRequestHandler;

    private final Environment environment;

    private FileManager fileManager;

    @Getter
    private MessageBrokerThread messageBrokerThread;

    @Getter
    private Node node;

    public AppConfig(
            BootstrapServerService bootstrapServerService,
            PingRequestHandler pingRequestHandler,
            LeaveRequestHandler leaveRequestHandler,
            PongRequestHandler pongRequestHandler,
            SearchRequestHandler searchRequestHandler,
            QueryRequestHandler queryRequestHandler,
            Environment environment) {
        this.bootstrapServerService = bootstrapServerService;
        this.pingRequestHandler = pingRequestHandler;
        this.leaveRequestHandler = leaveRequestHandler;
        this.searchRequestHandler = searchRequestHandler;
        responseHandlerFactory = new ResponseHandlerFactory(pingRequestHandler, pongRequestHandler, searchRequestHandler, queryRequestHandler);
        this.environment = environment;
        this.nodeName = environment.getProperty("app.bootstrap-server.node-name");
        init();
    }

    private void init() {
        this.node = new Node(nodeName);
        List<InetSocketAddress> neighbourNodes = new ArrayList<>();
        log.info("node created {}", node);
        try {
            neighbourNodes = this.bootstrapServerService.register(node.getUserName(), node.getIpAddress(), node.getPort());
            this.username = node.getUserName();
        } catch (IOException e) {
            log.error("An error occurred while registering the node in bootstrap server", e);
        }

        this.fileManager = new FileManager(this.username, this.environment.getProperty("app.common.file-name"));
        this.searchRequestHandler.setFileManager(fileManager);

        this.messageBrokerThread = new MessageBrokerThread(
                node.getIpAddress(),
                node.getPort(),
                pingRequestHandler,
                leaveRequestHandler,
                fileManager,
                this.environment.getProperty("app.common.r-ping-message-id"),
                Integer.parseInt(Objects.requireNonNull(this.environment.getProperty("app.bootstrap-server.ping-interval"))),
                responseHandlerFactory,
                this.environment);
        new Thread(messageBrokerThread).start();

        // send pings to the nodes
        neighbourNodes.forEach(neighbourNode -> {

        });

    }
}
