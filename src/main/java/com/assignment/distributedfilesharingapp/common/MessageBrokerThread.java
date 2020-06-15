package com.assignment.distributedfilesharingapp.common;

import com.assignment.distributedfilesharingapp.common.handlers.AbstractResponseHandler;
import com.assignment.distributedfilesharingapp.common.handlers.LeaveRequestHandler;
import com.assignment.distributedfilesharingapp.common.handlers.PingRequestHandler;
import com.assignment.distributedfilesharingapp.common.handlers.ResponseHandlerFactory;
import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import com.assignment.distributedfilesharingapp.model.Neighbour;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.List;
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
    private final LeaveRequestHandler leaveRequestHandler;
    private final BlockingQueue<ChannelMessage> channelIn;
    @Getter
    private final LinkedBlockingQueue<ChannelMessage> channelOut;
    @Getter
    private final TimeOutManager timeoutManager;
    private final FileManager fileManager;
    private final ResponseHandlerFactory responseHandlerFactory;

    public MessageBrokerThread(
            String address,
            Integer port,
            PingRequestHandler pingRequestHandler,
            LeaveRequestHandler leaveRequestHandler,
            FileManager fileManager,
            String rPingMessageId,
            Integer pingInterval,
            ResponseHandlerFactory responseHandlerFactory,
            Environment environment) {

        this.address = address;
        this.port = port;
        this.routingTable = new RoutingTable(address, port);
        this.channelIn = new LinkedBlockingQueue<>();
        this.channelOut = new LinkedBlockingQueue<>();
        this.pingRequestHandler = pingRequestHandler;
        this.leaveRequestHandler = leaveRequestHandler;
        this.fileManager = fileManager;
        timeoutManager = new TimeOutManager(environment);
        this.pingRequestHandler.init(routingTable, channelOut, timeoutManager);
        this.leaveRequestHandler.init(routingTable, channelOut, timeoutManager);
        this.responseHandlerFactory = responseHandlerFactory;

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
        neighbours.forEach(neighbour -> this.pingRequestHandler.sendPing(neighbour.getAddress(), neighbour.getPort()));
    }

    @Override
    public void run() {
        this.process();
    }

    private void process() {
        do {
            try {
                ChannelMessage message = channelIn.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    AbstractResponseHandler abstractResponseHandler = responseHandlerFactory.getResponseHandler(message.getMessage().split(" ")[1], this);
                    if (abstractResponseHandler != null) {
                        abstractResponseHandler.handleResponse(message);
                    }
                }
                timeoutManager.checkForTimeout();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
    }
}
