package com.assignment.distributedfilesharingapp.common;

import com.assignment.distributedfilesharingapp.common.handlers.PingRequestHandler;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class PingTimeoutCallback implements TimeOutCallback {

    private final Map<String, Integer> pingFailureCount;
    private final RoutingTable routingTable;
    private final Integer pingRetry;
    private final Integer minNeighbours;
    private final PingRequestHandler pingRequestHandler;

    public PingTimeoutCallback(
            Map<String, Integer> pingFailureCount,
            RoutingTable routingTable,
            Integer pingRetry,
            Integer minNeighbours,
            PingRequestHandler pingRequestHandler) {
        this.pingFailureCount = pingFailureCount;
        this.routingTable = routingTable;
        this.pingRetry = pingRetry;
        this.minNeighbours = minNeighbours;
        this.pingRequestHandler = pingRequestHandler;
    }

    @Override
    public void onTimeout(String messageId) {
        pingFailureCount.put(messageId, pingFailureCount.get(messageId) + 1);
        if (pingFailureCount.get(messageId) >= pingRetry) {
            log.info("neighbour lost :{}", messageId);
            routingTable.removeNeighbour(messageId.split(":")[1], Integer.valueOf(messageId.split(":")[2]));
        }
        if (routingTable.getNeighboursCount() < minNeighbours) {
            pingRequestHandler.sendBootstrapPing(messageId.split(":")[1], Integer.parseInt(messageId.split(":")[2]));
        }
    }

    @Override
    public void onResponse(String messageId) {
        pingFailureCount.put(messageId, 0);
    }
}
