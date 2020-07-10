package com.assignment.distributedfilesharingapp.config;

import com.assignment.distributedfilesharingapp.common.FileManager;
import com.assignment.distributedfilesharingapp.common.MessageBrokerThread;
import com.assignment.distributedfilesharingapp.common.strategy.*;
import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.model.SearchResult;
import com.assignment.distributedfilesharingapp.service.BootstrapServerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;

@Slf4j
@Component
public class AppConfig {

    private String nodeName;

    private final BootstrapServerService bootstrapServerService;

    private String username;

    private final HeartBeatHandlingStrategy heartBeatHandlingStrategy;

    private final LeaveMessageHandlingStrategy leaveMessageHandlingStrategy;

    private final MessageHandelingFactory messageHandelingFactory;

    private final FileSearchMessageHandlingStrategy fileSearchMessageHandlingStrategy;

    private final QueryMessageHandlingStrategy queryMessageHandlingStrategy;


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
            MessageHandelingFactory messageHandelingFactory,
            HeartBeatHandlingStrategy heartBeatHandlingStrategy,
            LeaveMessageHandlingStrategy leaveMessageHandlingStrategy,
            FileSearchMessageHandlingStrategy fileSearchMessageHandlingStrategy,
                    QueryMessageHandlingStrategy queryMessageHandlingStrategy,
            Environment environment) throws SocketException {
        this.node = node;
        this.bootstrapServerService = bootstrapServerService;
        this.heartBeatHandlingStrategy=heartBeatHandlingStrategy;
        this.leaveMessageHandlingStrategy = leaveMessageHandlingStrategy;
        this.queryMessageHandlingStrategy = queryMessageHandlingStrategy;
        this.fileSearchMessageHandlingStrategy=fileSearchMessageHandlingStrategy;
        this.messageHandelingFactory = messageHandelingFactory;
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
            //register my PC with its IP and Port(random port)
            neighbourNodes = this.bootstrapServerService.register(node.getUserName(), node.getIpAddress(), node.getPort());
            this.username = node.getUserName();
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred while registering the node in bootstrap server",e);
        }
        this.fileManager = FileManager.getInstance(this.username, this.environment.getProperty("app.common.file-name"));
        this.fileSearchMessageHandlingStrategy.setFileManager(fileManager);

        this.messageBrokerThread = new MessageBrokerThread(
                node.getIpAddress(),
                node.getPort(),
                heartBeatHandlingStrategy,
                leaveMessageHandlingStrategy,
                fileManager,
                this.environment.getProperty("app.common.r-join-message-id"),
                Integer.parseInt(Objects.requireNonNull(this.environment.getProperty("app.bootstrap-server.message-interval"))),
                messageHandelingFactory,
                this.queryMessageHandlingStrategy,
                this.fileSearchMessageHandlingStrategy,
                this.environment,
                neighbourNodes
        );
        new Thread(messageBrokerThread).start();
    }

    public void unregisterNode() {
        try {
            this.bootstrapServerService.unRegister(this.node.getUserName(), this.node.getIpAddress(), this.node.getPort());
            this.messageBrokerThread.getLeaveMessageHandlingStrategy().sendLeave();
        } catch (IOException e) {
            log.info("Un-Registering node failed", e);
        }
    }

    public void stopApplication() {
        System.exit(0);
    }

    public Map<String, SearchResult> doSearch(String fileName) {
        Map<String, SearchResult> searchResults = new HashMap<>();
        this.queryMessageHandlingStrategy.setSearchResults(searchResults);
        this.queryMessageHandlingStrategy.setSearchInitiatedTime(System.currentTimeMillis());
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
