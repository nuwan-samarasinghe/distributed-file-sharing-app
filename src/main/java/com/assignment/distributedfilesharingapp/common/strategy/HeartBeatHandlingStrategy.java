package com.assignment.distributedfilesharingapp.common.strategy;

import com.assignment.distributedfilesharingapp.common.PingTimeoutCallback;
import com.assignment.distributedfilesharingapp.common.TimeOutManager;
import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import com.assignment.distributedfilesharingapp.model.CommandTypes;
import com.assignment.distributedfilesharingapp.model.Neighbour;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@Slf4j
@Component
public class HeartBeatHandlingStrategy implements MessageHandlingStrategy {

    private BlockingQueue<ChannelMessage> channelOut;
    private RoutingTable routingTable;
    private TimeOutManager timeoutManager;
    private final Map<String, Integer> pingFailureCount = new HashMap<>();

    @Value("${app.commands.join-format}")
    private String joinFormat;

    @Value("${app.commands.message-format}")
    private String messageFormat;

    @Value("${app.commands.pong-format}")
    private String pongFormat;

    @Value("${app.node.max-neighbours}")
    private Integer maxNeighbours;

    @Value("${app.node.min-neighbours}")
    private Integer minNeighbours;

    @Value("${app.node.join-hop-limit}")
    private Integer joinHopLimit;

    @Value("${app.commands.joinok-format}")
    private String joinOkFormat;

    @Value("${app.commands.ping-format}")
    private String pingFormat;

    @Value("${app.commands.ping-message-id-format}")
    private String pingMessageIdFormat;

    @Value("${app.bootstrap-server.ping-timeout}")
    private Integer pingTimeOut;

    @Value("${app.bootstrap-server.ping-retry}")
    private Integer pingRetry;


    @Override
    public void init(RoutingTable routingTable, BlockingQueue<ChannelMessage> channelMessageBlockingQueue, TimeOutManager timeoutManager) {
        this.channelOut = channelMessageBlockingQueue;
        this.routingTable = routingTable;
        this.timeoutManager = timeoutManager;
    }

    @Override
    public void handleRequest(ChannelMessage message) {
        try {
            channelOut.put(message);
        } catch (InterruptedException e) {
            log.error("an error occurred while adding the message.", e);
        }

    }

    @Override
    public void handleResponse(ChannelMessage message) {
        /*
        this will call when inbound que has messages with JOIN or JOINOK
        JOIN - When other node need to connect with this node
        JOINOK - When this node want to connect other node and received success response
         */
        if(message.getType()==MessageType.JOIN){
            handleIncommingMessage(message);
        }

        handlePongResponse(message);
    }

    synchronized void handleIncommingMessage(ChannelMessage inboundMessage){

        String[] messageSplitArray = inboundMessage.getMessage().split(" ");
        MessageType messageType=MessageType.valueOf(messageSplitArray[1]);

        // if new node need to connect/join with our node
        if (MessageType.JOIN==messageType) {
            // if the given message is sent by a neighbour pass it to other neighbours.
            if (this.routingTable.isANeighbour(inboundMessage.getAddress(), inboundMessage.getPort())) {
                if (Integer.parseInt(messageSplitArray[4]) > 0) {
                    forwardBootstrapJoin(inboundMessage.getAddress(), inboundMessage.getPort(), Integer.parseInt(messageSplitArray[4]) - 1);
                }
            } else {
                // check are we able to add a neighbour to the node
                if (routingTable.getNeighboursCount() < maxNeighbours) {
                    // sending a join ok request
                    String payload = String.format(joinOkFormat, this.routingTable.getNodeIp(), this.routingTable.getNodePort());
                    //format 3digit string to 4digit
                    String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
                    ChannelMessage outGoingMsg = new ChannelMessage(MessageType.JOINOK,messageSplitArray[2], Integer.parseInt(messageSplitArray[3]), rawMessage);
                    this.handleRequest(outGoingMsg);
                    log.info("sending a join ok request {}", payload);
                } else {
                    //otherwise send it to the neighbours
                    if (Integer.parseInt(messageSplitArray[4]) > 0) {
                        forwardBootstrapJoin(inboundMessage.getAddress(), inboundMessage.getPort(), Integer.parseInt(messageSplitArray[4]) - 1);
                    }
                }
            }
        } else if (messageSplitArray[1].equals(CommandTypes.LEAVE.name())) {
            // if node leaves
            this.routingTable.removeNeighbour(messageSplitArray[2], Integer.parseInt(messageSplitArray[3]));
            if (routingTable.getNeighboursCount() <= minNeighbours) {
                sendBootstrapJoin(messageSplitArray[2], Integer.parseInt(messageSplitArray[3]));
            }
        } else {
            if (this.routingTable.addNeighbour(messageSplitArray[2], Integer.parseInt(messageSplitArray[3].trim()), maxNeighbours) != 0) {
                String payload = String.format(pongFormat, this.routingTable.getNodeIp(), this.routingTable.getNodePort());
                String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
                ChannelMessage outGoingMsg = new ChannelMessage(MessageType.PING,messageSplitArray[2], Integer.parseInt(messageSplitArray[3].trim()), rawMessage);
                this.handleRequest(outGoingMsg);
            }
        }

    }

    synchronized void handlePongResponse(ChannelMessage pongResponseMessage){
        // log.info("receiving a pong request message:{} address:{} port:{}", message.getMessage(), message.getAddress(), message.getPort());
        String[] messageSplit = pongResponseMessage.getMessage().split(" ");
        if (messageSplit[1].equals(CommandTypes.JOINOK.name())) {
            if (routingTable.getNeighboursCount() < maxNeighbours) {
                this.routingTable.addNeighbour(messageSplit[2], Integer.parseInt(messageSplit[3].trim()), maxNeighbours);
            }
        } else {
            this.timeoutManager.removeMessage(String.format(pingMessageIdFormat, messageSplit[2], Integer.parseInt(messageSplit[3].trim())));
            this.routingTable.addNeighbour(messageSplit[2], Integer.parseInt(messageSplit[3].trim()), maxNeighbours);
        }
    }

    public void sendJoin(String address, int port) {
        String payload = String.format(joinFormat, this.routingTable.getNodeIp(), this.routingTable.getNodePort());
        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
        ChannelMessage message = new ChannelMessage(MessageType.JOIN,address, port, rawMessage);
        this.pingFailureCount.putIfAbsent(String.format(pingMessageIdFormat, address, port), 0);
        this.timeoutManager.registerMessage(
                String.format(pingMessageIdFormat, address, port),
                pingTimeOut,
                new PingTimeoutCallback(pingFailureCount, this.routingTable, pingRetry, minNeighbours, this));
        this.handleRequest(message);
    }


    public void sendBootstrapJoin(String address, int port) {
        List<Neighbour> otherNeighbours = routingTable.getOtherNeighbours(address, port);
        String payload = String.format(joinFormat, this.routingTable.getNodeIp(), this.routingTable.getNodePort(), joinHopLimit);
        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
        otherNeighbours.forEach(neighbour -> {
            ChannelMessage message = new ChannelMessage(MessageType.JOIN,neighbour.getAddress(), neighbour.getPort(), rawMessage);
            handleRequest(message);
        });
    }

    private void forwardBootstrapJoin(String address, Integer port, int hops) {
        List<Neighbour> otherNeighbours = routingTable.getOtherNeighbours(address, port);
        String payload = String.format(joinFormat, address, port, hops);
        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
        otherNeighbours.forEach(neighbour -> {
            ChannelMessage message = new ChannelMessage(MessageType.PING,neighbour.getAddress(), neighbour.getPort(), rawMessage);
            handleRequest(message);
        });
    }
}
