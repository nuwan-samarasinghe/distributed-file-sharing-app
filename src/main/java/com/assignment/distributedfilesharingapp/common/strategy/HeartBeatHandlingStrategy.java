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
        if(message.getType()==MessageType.PING){
            handlePingResponse(message);
        }

        handlePongResponse(message);
    }

    synchronized void handlePingResponse(ChannelMessage pingResponseMessage){
        // log.info("ping received from {} port {} and the message is {}", message.getAddress(), message.getPort(), message.getMessage());
        String[] messageSplitArray = pingResponseMessage.getMessage().split(" ");
        // if command type id JOIN
        if (messageSplitArray[1].equals(CommandTypes.JOIN.name())) {
            // if the given message is sent by a neighbour pass it to other neighbours.
            if (this.routingTable.isANeighbour(pingResponseMessage.getAddress(), pingResponseMessage.getPort())) {
                if (Integer.parseInt(messageSplitArray[4]) > 0) {
                    forwardBootstrapPing(pingResponseMessage.getAddress(), pingResponseMessage.getPort(), Integer.parseInt(messageSplitArray[4]) - 1);
                }
            } else {
                // check are we able to add a neighbour to the node
                if (routingTable.getNeighboursCount() < maxNeighbours) {
                    // sending a join ok request
                    String payload = String.format(joinOkFormat, this.routingTable.getAddress(), this.routingTable.getPort());
                    String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
                    ChannelMessage outGoingMsg = new ChannelMessage(MessageType.PING,messageSplitArray[2], Integer.parseInt(messageSplitArray[3]), rawMessage);
                    this.handleRequest(outGoingMsg);
                    log.info("sending a join ok request {}", payload);
                } else {
                    //otherwise send it to the neighbours
                    if (Integer.parseInt(messageSplitArray[4]) > 0) {
                        forwardBootstrapPing(pingResponseMessage.getAddress(), pingResponseMessage.getPort(), Integer.parseInt(messageSplitArray[4]) - 1);
                    }
                }
            }
        } else if (messageSplitArray[1].equals(CommandTypes.LEAVE.name())) {
            // if node leaves
            this.routingTable.removeNeighbour(messageSplitArray[2], Integer.parseInt(messageSplitArray[3]));
            if (routingTable.getNeighboursCount() <= minNeighbours) {
                sendBootstrapPing(messageSplitArray[2], Integer.parseInt(messageSplitArray[3]));
            }
        } else {
            if (this.routingTable.addNeighbour(messageSplitArray[2], Integer.parseInt(messageSplitArray[3].trim()), pingResponseMessage.getPort(), maxNeighbours) != 0) {
                String payload = String.format(pongFormat, this.routingTable.getAddress(), this.routingTable.getPort());
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
                this.routingTable.addNeighbour(messageSplit[2], Integer.parseInt(messageSplit[3].trim()), pongResponseMessage.getPort(), maxNeighbours);
            }
        } else {
            this.timeoutManager.removeMessage(String.format(pingMessageIdFormat, messageSplit[2], Integer.parseInt(messageSplit[3].trim())));
            this.routingTable.addNeighbour(messageSplit[2], Integer.parseInt(messageSplit[3].trim()), pongResponseMessage.getPort(), maxNeighbours);
        }
    }

    public void sendPing(String address, int port) {
        String payload = String.format(pingFormat, this.routingTable.getAddress(), this.routingTable.getPort());
        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
        ChannelMessage message = new ChannelMessage(MessageType.PING,address, port, rawMessage);
        this.pingFailureCount.putIfAbsent(String.format(pingMessageIdFormat, address, port), 0);
        this.timeoutManager.registerMessage(
                String.format(pingMessageIdFormat, address, port),
                pingTimeOut,
                new PingTimeoutCallback(pingFailureCount, this.routingTable, pingRetry, minNeighbours, this));
        this.handleRequest(message);
    }


    public void sendBootstrapPing(String address, int port) {
        List<Neighbour> otherNeighbours = routingTable.getOtherNeighbours(address, port);
        String payload = String.format(joinFormat, this.routingTable.getAddress(), this.routingTable.getPort(), joinHopLimit);
        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
        otherNeighbours.forEach(neighbour -> {
            ChannelMessage message = new ChannelMessage(MessageType.PING,neighbour.getAddress(), neighbour.getPort(), rawMessage);
            handleRequest(message);
        });
    }

    private void forwardBootstrapPing(String address, Integer port, int hops) {
        List<Neighbour> otherNeighbours = routingTable.getOtherNeighbours(address, port);
        String payload = String.format(joinFormat, address, port, hops);
        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
        otherNeighbours.forEach(neighbour -> {
            ChannelMessage message = new ChannelMessage(MessageType.PING,neighbour.getAddress(), neighbour.getPort(), rawMessage);
            handleRequest(message);
        });
    }
}
