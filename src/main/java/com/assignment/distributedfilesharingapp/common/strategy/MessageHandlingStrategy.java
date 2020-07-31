package com.assignment.distributedfilesharingapp.common.strategy;

import com.assignment.distributedfilesharingapp.common.TimeOutManager;
import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import com.assignment.distributedfilesharingapp.model.NodeQueryStatisticsModel;
import com.assignment.distributedfilesharingapp.model.RoutingTable;

import java.util.concurrent.BlockingQueue;

public interface MessageHandlingStrategy {

    void init(RoutingTable routingTable, BlockingQueue<ChannelMessage> channelMessageBlockingQueue, TimeOutManager timeoutManager);

    void handleRequest(ChannelMessage message);

    void handleResponse(NodeQueryStatisticsModel statisticsModel, ChannelMessage message);
}
