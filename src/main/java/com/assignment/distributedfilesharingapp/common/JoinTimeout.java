package com.assignment.distributedfilesharingapp.common;

import com.assignment.distributedfilesharingapp.common.strategy.HeartBeatHandlingStrategy;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class JoinTimeout implements TimeOut {

    private final Map<String, Integer> joinFailureCount;
    private final RoutingTable routingTable;
    private final Integer joinRetry;
    private final Integer minNeighbours;
    private final HeartBeatHandlingStrategy heartBeatHandlingStrategy;

    public JoinTimeout(
            Map<String, Integer> joinFailureCount,
            RoutingTable routingTable,
            Integer joinRetry,
            Integer minNeighbours,
            HeartBeatHandlingStrategy heartBeatHandlingStrategy) {
        this.joinFailureCount = joinFailureCount;
        this.routingTable = routingTable;
        this.joinRetry = joinRetry;
        this.minNeighbours = minNeighbours;
        this.heartBeatHandlingStrategy = heartBeatHandlingStrategy;
    }

    @Override
    public void onTimeout(String messageId) {
        joinFailureCount.put(messageId, joinFailureCount.get(messageId) + 1);
        if (joinFailureCount.get(messageId) >= joinRetry) {
            log.info("Neighbour seems to be not in active mode :{}", messageId);
            routingTable.removeNeighbour(messageId.split(":")[ 1 ], Integer.valueOf(messageId.split(":")[ 2 ]));
        }
        if (routingTable.getNeighboursCount() < minNeighbours) {
            heartBeatHandlingStrategy.sendBootstrapJoin(messageId.split(":")[ 1 ], Integer.parseInt(messageId.split(":")[ 2 ]));
        }
    }

    @Override
    public void onResponse(String messageId) {
        joinFailureCount.put(messageId, 0);
    }
}
