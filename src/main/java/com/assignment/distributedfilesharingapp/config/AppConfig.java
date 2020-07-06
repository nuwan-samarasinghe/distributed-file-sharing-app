package com.assignment.distributedfilesharingapp.config;

import com.assignment.distributedfilesharingapp.common.FileManager;
import com.assignment.distributedfilesharingapp.common.MessageBrokerThread;
import com.assignment.distributedfilesharingapp.common.handlers.*;
import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.model.SearchResult;
import com.assignment.distributedfilesharingapp.service.BootstrapServerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class AppConfig {

    private String nodeName;

    private final BootstrapServerService bootstrapServerService;

    private String username;

    private final PingRequestHandler pingRequestHandler;

    private final LeaveRequestHandler leaveRequestHandler;

    private final ResponseHandlerFactory responseHandlerFactory;

    private final SearchRequestHandler searchRequestHandler;

    private final QueryRequestHandler queryRequestHandler;

    private final Environment environment;

    @Getter
    private FileManager fileManager;

    @Getter
    private MessageBrokerThread messageBrokerThread;

    @Getter
    private Node node;

    public AppConfig(
            Node node,
            BootstrapServerService bootstrapServerService,
            PingRequestHandler pingRequestHandler,
            LeaveRequestHandler leaveRequestHandler,
            PongRequestHandler pongRequestHandler,
            SearchRequestHandler searchRequestHandler,
            QueryRequestHandler queryRequestHandler,
            Environment environment) throws SocketException {
        this.node = node;
        this.bootstrapServerService = bootstrapServerService;
        this.pingRequestHandler = pingRequestHandler;
        this.leaveRequestHandler = leaveRequestHandler;
        this.searchRequestHandler = searchRequestHandler;
        this.queryRequestHandler = queryRequestHandler;
        responseHandlerFactory = new ResponseHandlerFactory(pingRequestHandler, pongRequestHandler, searchRequestHandler, queryRequestHandler);
        this.environment = environment;
        this.nodeName = environment.getProperty("app.node.node-name");
        if (!Boolean.parseBoolean(this.environment.getProperty("app.common.enable-console"))) {
            init();
        }
    }

    private void init() throws SocketException {
        this.node = getNode();
        List<InetSocketAddress> neighbourNodes = new ArrayList<>();
        log.info("node created {}", node);
        try {
            neighbourNodes = this.bootstrapServerService.register(node.getUserName(), node.getIpAddress(), node.getPort());
            this.username = node.getUserName();
        } catch (IOException e) {
            log.error("An error occurred while registering the node in bootstrap server", e);
        }

        this.fileManager = FileManager.getInstance(this.username, this.environment.getProperty("app.common.file-name"));
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
                this.queryRequestHandler,
                this.searchRequestHandler,
                this.environment,
                neighbourNodes
        );
        new Thread(messageBrokerThread).start();
    }

    public void unregisterNode() {
        try {
            this.bootstrapServerService.unRegister(this.node.getUserName(), this.node.getIpAddress(), this.node.getPort());
            this.messageBrokerThread.getLeaveRequestHandler().sendLeave();
        } catch (IOException e) {
            log.info("Un-Registering node failed", e);
        }
    }

    public void stopApplication() {
        System.exit(0);
    }

    public Map<String, SearchResult> doSearch(String fileName) {
        Map<String, SearchResult> searchResults = new HashMap<>();
        this.queryRequestHandler.setSearchResults(searchResults);
        this.queryRequestHandler.setSearchInitiatedTime(System.currentTimeMillis());
        this.messageBrokerThread.doSearch(fileName);
        log.info("Please be patient till the file results are returned ...");
        try {
            Thread.sleep(3000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return searchResults;
    }
}
