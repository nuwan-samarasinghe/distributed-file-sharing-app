package com.assignment.distributedfilesharingapp.common.strategy;

import com.assignment.distributedfilesharingapp.common.MessageBrokerThread;
import com.assignment.distributedfilesharingapp.model.MessageType;
import com.assignment.distributedfilesharingapp.model.NodeQueryStatisticsModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageHandelingFactory {

    private final HeartBeatHandlingStrategy heartBeatHandlingStrategy;

    private final FileSearchMessageHandlingStrategy fileSearchMessageHandlingStrategy;

    private final QueryMessageHandlingStrategy queryMessageHandlingStrategy;

    public MessageHandelingFactory(
            HeartBeatHandlingStrategy heartBeatHandlingStrategy,
            FileSearchMessageHandlingStrategy fileSearchMessageHandlingStrategy,
            QueryMessageHandlingStrategy queryMessageHandlingStrategy) {
        this.heartBeatHandlingStrategy = heartBeatHandlingStrategy;
        this.fileSearchMessageHandlingStrategy = fileSearchMessageHandlingStrategy;
        this.queryMessageHandlingStrategy = queryMessageHandlingStrategy;
    }

    public MessageHandlingStrategy getMessageHandlingStrategy(MessageType messageType, MessageBrokerThread messageBroker) {
        switch (messageType) {
            case JOIN:
                //statisticsModel.increaseJoinOkCount();
                this.heartBeatHandlingStrategy.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return this.heartBeatHandlingStrategy;
            case LEAVE:
                //statisticsModel.increaseLeaveCount();
                this.heartBeatHandlingStrategy.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return this.heartBeatHandlingStrategy;
            case JOINOK:
                //statisticsModel.increaseJoinSendCount();
                this.heartBeatHandlingStrategy.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return this.heartBeatHandlingStrategy;
            case SER:
                //statisticsModel.increaseFileSearchCount();
                fileSearchMessageHandlingStrategy.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return fileSearchMessageHandlingStrategy;
            case SEROK:
                //statisticsModel.increaseFileSearchOkCount();
                queryMessageHandlingStrategy.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return queryMessageHandlingStrategy;
            default:
                log.info("Factory received unknown messageType : {}", messageType);
                return null;
        }
    }
}
