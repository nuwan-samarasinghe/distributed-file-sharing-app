package com.assignment.distributedfilesharingapp.config;

import com.assignment.distributedfilesharingapp.common.FileManager;
import com.assignment.distributedfilesharingapp.common.MessageBrokerThread;
import com.assignment.distributedfilesharingapp.common.messageProcesser.*;
import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.model.SearchResult;
import com.assignment.distributedfilesharingapp.service.BootstrapServerService;
import com.assignment.distributedfilesharingapp.service.FileService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;

@Slf4j
@Configuration
public class ApplicationConsoleConfig {

    private String nodeName;
    private final BootstrapServerService bootstrapServerService;
    private String username;
    private final HeartBeatHandlingStrategy heartBeatHandlingStrategy;
    private final LeaveMessageHandlingStrategy leaveMessageHandlingStrategy;
    private final MessageHandelingFactory messageHandelingFactory;
    private final FileSearchMessageHandlingStrategy fileSearchMessageHandlingStrategy;
    private final QueryMessageHandlingStrategy queryMessageHandlingStrategy;
    private final Environment environment;
    private final FileService fileService;
    @Getter
    private FileManager fileManager;
    @Getter
    private MessageBrokerThread messageBrokerThread;
    @Getter
    private Node node;

    public ApplicationConsoleConfig(
            Node node,
            BootstrapServerService bootstrapServerService,
            HeartBeatHandlingStrategy heartBeatHandlingStrategy,
            LeaveMessageHandlingStrategy leaveMessageHandlingStrategy,
            FileSearchMessageHandlingStrategy fileSearchMessageHandlingStrategy,
            QueryMessageHandlingStrategy queryMessageHandlingStrategy,
            FileService fileService,
            Environment environment) throws Exception {
        this.node = node;
        this.bootstrapServerService = bootstrapServerService;
        this.leaveMessageHandlingStrategy = leaveMessageHandlingStrategy;
        this.heartBeatHandlingStrategy=heartBeatHandlingStrategy;
        this.fileSearchMessageHandlingStrategy = fileSearchMessageHandlingStrategy;
        this.queryMessageHandlingStrategy = queryMessageHandlingStrategy;
        messageHandelingFactory = new MessageHandelingFactory(heartBeatHandlingStrategy, fileSearchMessageHandlingStrategy, queryMessageHandlingStrategy);
        this.environment = environment;
        this.nodeName = environment.getProperty("app.node.node-name");
        this.fileService = fileService;
        if (Boolean.parseBoolean(this.environment.getProperty("app.common.enable-console"))) {
            init();
            startExecution();
        }
    }

    private void startExecution() throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nChoose what do you want to do below : ");
            System.out.println("1) Do a search");
            System.out.println("2) Print the routing table");
            System.out.println("3) Exit the network");
            System.out.println("\nPlease enter the option : ");
            String commandOption = scanner.nextLine();

            switch (commandOption) {
                case "1":
                    System.out.println("\nEnter your search query below : ");
                    String searchQuery = scanner.nextLine();
                    if (searchQuery != null && !searchQuery.equals("")) {
                        List<SearchResult> searchResults = new LinkedList<>(doSearch(searchQuery).values());
                        if (!searchResults.isEmpty()) {
                            System.out.println("\n Following is the results \n");
                            int count = 1;
                            for (SearchResult searchResult : searchResults) {
                                System.out.println("\nOption No : " + count);
                                System.out.println("\tIp Address : " + searchResult.getAddress());
                                System.out.println("\tPort : " + searchResult.getPort());
                                System.out.println("\tTCP Port : " + searchResult.getTcpPort());
                                System.out.println("\tFile Name : " + searchResult.getFileName());
                                System.out.println("\tNo of Hops : " + searchResult.getHops());
                                System.out.println("\tTime : " + searchResult.getTimeElapsed());
                                count++;
                            }
                            while (true) {
                                try {
                                    System.out.println("\nPlease choose the file you need to download : ");
                                    String fileOption = scanner.nextLine();
                                    int option = Integer.parseInt(fileOption);
                                    if (option > searchResults.size()) {
                                        System.out.println("Please give an option within the search results...");
                                        continue;
                                    }
                                    this.fileService.startReceiveFile(searchResults.get(option - 1));
                                    break;
                                } catch (NumberFormatException e) {
                                    System.out.println("Enter a valid integer indicating " + "the file option shown above in the results...");
                                }
                            }
                        }
                    } else {
                        System.out.println("Please give a valid search query!!!");
                    }
                    break;
                case "2":
                    this.messageBrokerThread.getRoutingTable().printRoutingTable();
                    break;
                case "3":
                    // node.unRegister();
                    System.exit(0);
                default:
                    System.out.println("Please Enter a Valid Option...");
                    break;
            }
        }
    }

    public Map<String, SearchResult> doSearch(String fileName) {
        Map<String, SearchResult> searchResults = new LinkedHashMap<>();
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
}
