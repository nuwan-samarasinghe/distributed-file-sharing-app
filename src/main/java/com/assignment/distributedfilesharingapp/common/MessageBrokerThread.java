package com.assignment.distributedfilesharingapp.common;

import com.assignment.distributedfilesharingapp.common.handlers.*;
import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import com.assignment.distributedfilesharingapp.model.Neighbour;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
import com.assignment.distributedfilesharingapp.service.BootstrapServerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MessageBrokerThread implements Runnable {

    private final String address;
    private final Integer port;
    @Getter
    private final RoutingTable routingTable;
    private final PingRequestHandler pingRequestHandler;
    @Getter
    private final LeaveRequestHandler leaveRequestHandler;
    private final BlockingQueue<ChannelMessage> channelIn;
    @Getter
    private final LinkedBlockingQueue<ChannelMessage> channelOut;
    @Getter
    private final TimeOutManager timeoutManager;
    private final FileManager fileManager;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final MessageReceiver server;
    private final MessageSender client;
    private final QueryRequestHandler queryRequestHandler;
    private final SearchRequestHandler searchRequestHandler;

    public MessageBrokerThread(
            String address,
            Integer port,
            PingRequestHandler pingRequestHandler,
            LeaveRequestHandler leaveRequestHandler,
            FileManager fileManager,
            String rPingMessageId,
            Integer pingInterval,
            ResponseHandlerFactory responseHandlerFactory,
            QueryRequestHandler queryRequestHandler,
            SearchRequestHandler searchRequestHandler,
            Environment environment,
            List<InetSocketAddress> neighbourNodes) throws SocketException {

        this.address = address;
        this.port = port;
        this.routingTable = new RoutingTable(address, port);
        this.channelIn = new LinkedBlockingQueue<>();
        this.channelOut = new LinkedBlockingQueue<>();
        this.pingRequestHandler = pingRequestHandler;
        this.leaveRequestHandler = leaveRequestHandler;
        this.fileManager = fileManager;
        timeoutManager = new TimeOutManager(environment);
        this.queryRequestHandler = queryRequestHandler;
        this.searchRequestHandler = searchRequestHandler;
        this.pingRequestHandler.init(routingTable, channelOut, timeoutManager);
        this.leaveRequestHandler.init(routingTable, channelOut, timeoutManager);
        this.queryRequestHandler.init(routingTable, channelOut, timeoutManager);
        this.searchRequestHandler.init(routingTable, channelOut, timeoutManager);
        this.responseHandlerFactory = responseHandlerFactory;

        DatagramSocket socket = new DatagramSocket(this.port);
        this.server = new MessageReceiver(channelIn, socket);
        this.client = new MessageSender(channelOut, new DatagramSocket());

        neighbourNodes.forEach(neighbourNode -> routingTable.addNeighbour(
                neighbourNode.getAddress().getHostAddress(),
                neighbourNode.getPort(),
                port,
                Integer.parseInt(Objects.requireNonNull(environment.getProperty("app.node.max-neighbours")))));
        log.info("adding initial nodes {}", routingTable.getNeighbours());

        log.info("starting the server");
        timeoutManager.registerMessage(rPingMessageId, pingInterval, new TimeOutCallback() {
            @Override
            public void onTimeout(String messageId) {
                sendRoutinePing();
            }

            @Override
            public void onResponse(String messageId) {
                // empty method
            }
        });
    }

    private void sendRoutinePing() {
        List<Neighbour> neighbours = routingTable.getNeighbours();
        log.info("send routing ping {}", neighbours);
        neighbours.forEach(neighbour -> this.pingRequestHandler.sendPing(neighbour.getAddress(), neighbour.getPort()));
    }

    @Override
    public void run() {
        this.server.start();
        this.client.start();
        this.process();
    }

    private void process() {
        do {
            try {
                timeoutManager.checkForTimeout();
                ChannelMessage message = channelIn.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    log.info("found the message {}", message.getMessage());
                    AbstractResponseHandler abstractResponseHandler = responseHandlerFactory.getResponseHandler(message.getMessage().split(" ")[1], this);
                    if (abstractResponseHandler != null) {
                        abstractResponseHandler.handleResponse(message);
                    }
                }
            } catch (InterruptedException e) {
                log.error("an error occurred while processing the message", e);
            }
        } while (true);
    }

    public void doSearch(String fileName) {
        this.searchRequestHandler.doSearch(fileName);
    }
}
