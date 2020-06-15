package com.assignment.distributedfilesharingapp.common.handlers;

import com.assignment.distributedfilesharingapp.common.MessageBrokerThread;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseHandlerFactory {

    private final PingRequestHandler pingRequestHandler;

    private final PongRequestHandler pongRequestHandler;

    private final SearchRequestHandler searchRequestHandler;

    private final QueryRequestHandler queryRequestHandler;

    public ResponseHandlerFactory(
            PingRequestHandler pingRequestHandler,
            PongRequestHandler pongRequestHandler,
            SearchRequestHandler searchRequestHandler,
            QueryRequestHandler queryRequestHandler) {
        this.pingRequestHandler = pingRequestHandler;
        this.pongRequestHandler = pongRequestHandler;
        this.searchRequestHandler = searchRequestHandler;
        this.queryRequestHandler = queryRequestHandler;
    }

    public AbstractResponseHandler getResponseHandler(String keyword, MessageBrokerThread messageBroker) {
        switch (keyword) {
            case "PING":
            case "BPING":

            case "LEAVE":
                this.pingRequestHandler.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return this.pingRequestHandler;
            case "PONG":
            case "BPONG":
                this.pongRequestHandler.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return this.pongRequestHandler;
            case "SER":
                searchRequestHandler.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return searchRequestHandler;
            case "SEROK":
                queryRequestHandler.init(messageBroker.getRoutingTable(), messageBroker.getChannelOut(), messageBroker.getTimeoutManager());
                return queryRequestHandler;
            default:
                log.info("Unknown keyword received in Response Handler : {}", keyword);
                return null;
        }
    }
}
