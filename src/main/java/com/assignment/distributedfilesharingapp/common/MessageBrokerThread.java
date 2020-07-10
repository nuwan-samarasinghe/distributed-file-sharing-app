package com.assignment.distributedfilesharingapp.common;

import com.assignment.distributedfilesharingapp.common.strategy.*;
import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import com.assignment.distributedfilesharingapp.model.Neighbour;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
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
    private final HeartBeatHandlingStrategy heartBeatHandlingStrategy;
    @Getter
    private final LeaveMessageHandlingStrategy leaveMessageHandlingStrategy;
    private final BlockingQueue<ChannelMessage> channelIn;
    @Getter
    private final LinkedBlockingQueue<ChannelMessage> channelOut;
    @Getter
    private final TimeOutManager timeoutManager;
    private final FileManager fileManager;
    private final MessageHandelingFactory messageHandelingFactory;
    private final MessageReceiver server;
    private final MessageSender client;
    private final QueryMessageHandlingStrategy queryMessageHandlingStrategy;
    private final FileSearchMessageHandlingStrategy fileSearchMessageHandlingStrategy;

    public MessageBrokerThread(
            String address,
            Integer port,
            HeartBeatHandlingStrategy heartBeatHandlingStrategy,
            LeaveMessageHandlingStrategy leaveMessageHandlingStrategy,
            FileManager fileManager,
            String rPingMessageId,
            Integer pingInterval,
            MessageHandelingFactory messageHandelingFactory,
            QueryMessageHandlingStrategy queryMessageHandlingStrategy,
            FileSearchMessageHandlingStrategy fileSearchMessageHandlingStrategy,
            Environment environment,
            List<InetSocketAddress> neighbourNodes) throws SocketException {

        this.address = address;
        this.port = port;
        this.routingTable = new RoutingTable(address,port);
        this.channelIn = new LinkedBlockingQueue<>();
        this.channelOut = new LinkedBlockingQueue<>();
        this.heartBeatHandlingStrategy=heartBeatHandlingStrategy;
        this.leaveMessageHandlingStrategy = leaveMessageHandlingStrategy;
        this.fileManager = fileManager;
        timeoutManager = new TimeOutManager(environment);
        this.queryMessageHandlingStrategy = queryMessageHandlingStrategy;
        this.fileSearchMessageHandlingStrategy = fileSearchMessageHandlingStrategy;
        this.heartBeatHandlingStrategy.init(routingTable, channelOut, timeoutManager);
        this.leaveMessageHandlingStrategy.init(routingTable, channelOut, timeoutManager);
        this.queryMessageHandlingStrategy.init(routingTable, channelOut, timeoutManager);
        this.fileSearchMessageHandlingStrategy.init(routingTable, channelOut, timeoutManager);
        this.messageHandelingFactory = messageHandelingFactory;

        DatagramSocket socket = new DatagramSocket(this.port);
        this.server = new MessageReceiver(channelIn, socket);
        this.client = new MessageSender(channelOut, new DatagramSocket());

        neighbourNodes.forEach(neighbourNode -> routingTable.addNeighbour(
                neighbourNode.getAddress().getHostAddress(),
                neighbourNode.getPort(),
                Integer.parseInt(Objects.requireNonNull(environment.getProperty("app.node.max-neighbours")))));
        log.info("adding initial nodes {}", routingTable.getNeighbours());

        log.info("starting the server");
        timeoutManager.registerMessage(rPingMessageId, pingInterval, new TimeOutCallback() {
            @Override
            public void onTimeout(String messageId) {
                sendRoutineJoin();
            }

            @Override
            public void onResponse(String messageId) {
                // empty method
            }
        });
    }

    private void sendRoutineJoin() {
        List<Neighbour> neighbours = routingTable.getNeighbours();
        neighbours.forEach(neighbour -> this.heartBeatHandlingStrategy.sendJoin(neighbour.getAddress(), neighbour.getPort()));
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
                    //extract necessary message handling strategy by giving correct message type
                    MessageHandlingStrategy messageHandlingStrategy = messageHandelingFactory.getMessageHandlingStrategy(message.getType(), this);
                    if (messageHandlingStrategy != null) {
                        messageHandlingStrategy.handleResponse(message);
                    }
                }
            } catch (InterruptedException e) {
                log.error("an error occurred while processing the message", e);
            }
        } while (true);
    }

    public void doSearch(String fileName) {
        this.fileSearchMessageHandlingStrategy.doSearch(fileName);
    }
}
