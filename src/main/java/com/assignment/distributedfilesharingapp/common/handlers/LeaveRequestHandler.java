package com.assignment.distributedfilesharingapp.common.handlers;

import com.assignment.distributedfilesharingapp.common.TimeOutManager;
import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

@Slf4j
@Component
public class LeaveRequestHandler implements AbstractRequestHandler {

    private RoutingTable routingTable;
    private BlockingQueue<ChannelMessage> channelOut;

    @Value("${app.commands.leave-format}")
    private String leaveMessageFormat;

    @Value("${app.commands.message-format}")
    private String messageFormat;

    public void sendLeave() {
        String payload = String.format(leaveMessageFormat, this.routingTable.getAddress(), this.routingTable.getPort());
        log.info("leave initiated with {}", payload);
        routingTable.getNeighbours()
                .forEach(neighbour ->
                        sendRequest(new ChannelMessage(neighbour.getAddress(), neighbour.getPort(),
                                String.format(messageFormat, payload.length() + 5, payload))));

    }

    @Override
    public void sendRequest(ChannelMessage message) {
        try {
            channelOut.put(message);
        } catch (InterruptedException e) {
            log.error("an error occurred while adding the message.", e);
        }
    }

    @Override
    public void init(RoutingTable routingTable, BlockingQueue<ChannelMessage> channelMessageBlockingQueue, TimeOutManager timeoutManager) {
        this.routingTable = routingTable;
        this.channelOut = channelMessageBlockingQueue;
    }

}
