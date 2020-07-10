package com.assignment.distributedfilesharingapp.common.strategy;

import com.assignment.distributedfilesharingapp.common.MessageBrokerThread;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageHandelingFactory {

    private final HeartBeatHandlingStrategy heartBeatHandlingStrategy;

    private final FileSearchMessageHandlingStrategy fileSearchMessageHandlingStrategy;

    private final QueryMessageHandlingStrategy queryMessageHandlingStrategy;

    public MessageHandelingFactory(
            HeartBeatHandlingStrategy heartBeatHandlingStrategy,
            FileSearchMessageHandlingStrategy fileSearchMessageHandlingStrategy,
            QueryMessageHandlingStrategy queryMessageHandlingStrategy) {
        this.heartBeatHandlingStrategy=heartBeatHandlingStrategy;
        this.fileSearchMessageHandlingStrategy = fileSearchMessageHandlingStrategy;
        this.queryMessageHandlingStrategy = queryMessageHandlingStrategy;
    }

    public MessageHandlingStrategy getResponseHandler(String keyword, MessageBrokerThread messageBroker) {
        switch (keyword) {
            case "PING":
            case "JOIN":

            case "LEAVE":
                this.heartBeatHandlingStrategy.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return this.heartBeatHandlingStrategy;
            case "PONG":
            case "JOINOK":
                this.heartBeatHandlingStrategy.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return this.heartBeatHandlingStrategy;
            case "SER":
                fileSearchMessageHandlingStrategy.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return fileSearchMessageHandlingStrategy;
            case "SEROK":
                queryMessageHandlingStrategy.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return queryMessageHandlingStrategy;
            default:
                log.info("Unknown keyword received in Response Handler : {}", keyword);
                return null;
        }
    }
}
