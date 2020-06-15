package com.assignment.distributedfilesharingapp.common.handlers;

import com.assignment.distributedfilesharingapp.common.TimeOutManager;
import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import com.assignment.distributedfilesharingapp.model.CommandTypes;
import com.assignment.distributedfilesharingapp.model.Neighbour;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
@Component
public class PingRequestHandler implements AbstractRequestHandler, AbstractResponseHandler {

    private BlockingQueue<ChannelMessage> channelOut;
    private RoutingTable routingTable;
    private TimeOutManager timeoutManager;

    @Value("${app.commands.bping-format}")
    private String bPingFormat;

    @Value("${app.commands.message-format}")
    private String messageFormat;

    @Value("${app.commands.pong-format}")
    private String pongFormat;

    @Value("${app.node.max-neighbours}")
    private Integer maxNeighbours;

    @Value("${app.node.min-neighbours}")
    private Integer minNeighbours;

    @Value("${app.node.bping-hop-limit}")
    private Integer bpingHopLimit;

    @Value("${app.node.bpong-format}")
    private String bPongFormat;

    @Override
    public void sendRequest(ChannelMessage message) {
        try {
            log.info("adding message to blocking queue with address {} port {} and the message {}", message.getAddress(), message.getPort(), message.getMessage());
            channelOut.put(message);
        } catch (InterruptedException e) {
            log.error("an error occurred while adding the message.", e);
        }
    }

    @Override
    public void handleResponse(ChannelMessage message) {
        log.info("ping received from {} port {} and the message is {}", message.getAddress(), message.getPort(), message.getMessage());
        String[] messageSplitArray = message.getMessage().split(" ");
        // if command type id BPING
        if (messageSplitArray[1].equals(CommandTypes.BPING.name())) {
            // if the given message is sent by a neighbour pass it to other neighbours.
            if (this.routingTable.isANeighbour(message.getAddress(), message.getPort())) {
                if (Integer.parseInt(messageSplitArray[4]) > 0) {
                    forwardBootstrapPing(message.getAddress(), message.getPort(), Integer.parseInt(messageSplitArray[4]) - 1);
                }
            } else {
                // check are we able to add a neighbour to the node
                if (routingTable.getNeighboursCount() < maxNeighbours) {
                    // sending a bpong request
                    String payload = String.format(bPongFormat, this.routingTable.getAddress(), this.routingTable.getPort());
                    String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
                    ChannelMessage outGoingMsg = new ChannelMessage(messageSplitArray[2], Integer.parseInt(messageSplitArray[3]), rawMessage);
                    this.sendRequest(outGoingMsg);
                    log.info("sending a bpong request {}", payload);
                } else {
                    //otherwise send it to the neighbours
                    if (Integer.parseInt(messageSplitArray[4]) > 0) {
                        forwardBootstrapPing(message.getAddress(), message.getPort(), Integer.parseInt(messageSplitArray[4]) - 1);
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
            if (this.routingTable.addNeighbour(messageSplitArray[2], Integer.parseInt(messageSplitArray[3]), message.getPort()) != 0) {
                String payload = String.format(pongFormat, this.routingTable.getAddress(), this.routingTable.getPort());
                String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
                ChannelMessage outGoingMsg = new ChannelMessage(messageSplitArray[2], Integer.parseInt(messageSplitArray[3]), rawMessage);
                this.sendRequest(outGoingMsg);
            }
        }

    }

    private void sendBootstrapPing(String address, int port) {
        List<Neighbour> otherNeighbours = routingTable.getOtherNeighbours(address, port);
        String payload = String.format(bPingFormat, this.routingTable.getAddress(), this.routingTable.getPort(), bpingHopLimit);
        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
        otherNeighbours.forEach(neighbour -> {
            ChannelMessage message = new ChannelMessage(neighbour.getAddress(), neighbour.getPort(), rawMessage);
            sendRequest(message);
        });
    }

    private void forwardBootstrapPing(String address, Integer port, int hops) {
        List<Neighbour> otherNeighbours = routingTable.getOtherNeighbours(address, port);
        String payload = String.format(bPingFormat, address, port, hops);
        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
        otherNeighbours.forEach(neighbour -> {
            ChannelMessage message = new ChannelMessage(neighbour.getAddress(), neighbour.getPort(), rawMessage);
            sendRequest(message);
        });
    }

    @Override
    public void init(RoutingTable routingTable, BlockingQueue<ChannelMessage> channelMessageBlockingQueue, TimeOutManager timeoutManager) {
        this.channelOut = channelMessageBlockingQueue;
        this.routingTable = routingTable;
        this.timeoutManager = timeoutManager;
    }
}
