package com.assignment.distributedfilesharingapp.common.handlers;

import com.assignment.distributedfilesharingapp.common.TimeOutManager;
import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import com.assignment.distributedfilesharingapp.model.CommandTypes;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

@Slf4j
@Component
public class PongRequestHandler implements AbstractRequestHandler, AbstractResponseHandler {

    @Value("${app.node.max-neighbours}")
    private Integer maxNeighbours;

    @Value("${app.commands.ping-message-id-format}")
    private String pingMessageIdFormat;

    private BlockingQueue<ChannelMessage> channelOut;
    private RoutingTable routingTable;
    private TimeOutManager timeoutManager;

    @Override
    public void sendRequest(ChannelMessage message) {
        //empty method
    }

    @Override
    public void handleResponse(ChannelMessage message) {
        log.info("receiving a pong request message:{} address:{} port:{}", message.getMessage(), message.getAddress(), message.getPort());
        String[] messageSplit = message.getMessage().split(" ");
        if (messageSplit[1].equals(CommandTypes.JOINOK.name())) {
            if (routingTable.getNeighboursCount() < maxNeighbours) {
                this.routingTable.addNeighbour(messageSplit[2], Integer.parseInt(messageSplit[3].trim()), message.getPort(), maxNeighbours);
            }
        } else {
            this.timeoutManager.removeMessage(String.format(pingMessageIdFormat, messageSplit[2], Integer.parseInt(messageSplit[3].trim())));
            this.routingTable.addNeighbour(messageSplit[2], Integer.parseInt(messageSplit[3].trim()), message.getPort(), maxNeighbours);
        }
    }

    @Override
    public void init(RoutingTable routingTable, BlockingQueue<ChannelMessage> channelMessageBlockingQueue, TimeOutManager timeoutManager) {
        this.channelOut = channelMessageBlockingQueue;
        this.routingTable = routingTable;
        this.timeoutManager = timeoutManager;
    }
}
