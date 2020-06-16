package com.assignment.distributedfilesharingapp.common.handlers;

import com.assignment.distributedfilesharingapp.common.StringEncoderDecoder;
import com.assignment.distributedfilesharingapp.common.TimeOutManager;
import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
import com.assignment.distributedfilesharingapp.model.SearchResult;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

@Slf4j
@Component
public class QueryRequestHandler implements AbstractResponseHandler {

    @Value("${app.commands.query-format}")
    private String queryFormat;

    @Value("${app.node.hop-count}")
    private Integer hopCount;

    @Value("${app.commands.message-format}")
    private String messageFormat;

    private RoutingTable routingTable;
    private BlockingQueue<ChannelMessage> channelOut;
    private TimeOutManager timeoutManager;

    @Setter
    private Map<String, SearchResult> searchResults;

    @Setter
    private Long searchInitiatedTime;

    @Override
    public void handleResponse(ChannelMessage message) {
        log.info("received message {} from: {} port:{}", message.getMessage(), message.getAddress(), message.getPort());
        String[] messageSplit = message.getMessage().split(" ");
        int filesCount = Integer.parseInt(messageSplit[2]);
        String address = messageSplit[3].trim();
        int port = Integer.parseInt(messageSplit[4].trim());
        int hops = Integer.parseInt(messageSplit[5]);
        while (filesCount > 0) {
            String fileName = StringEncoderDecoder.decode(messageSplit[6]);
            if (this.searchResults != null) {
                if (!this.searchResults.containsKey(address + ":" + port + fileName)) {
                    this.searchResults.put(
                            address + ":" + port + fileName,
                            new SearchResult(fileName, address, port, hops, (System.currentTimeMillis() - searchInitiatedTime)));
                }
            }
            filesCount--;
        }

    }

    @Override
    public void init(RoutingTable routingTable, BlockingQueue<ChannelMessage> channelMessageBlockingQueue, TimeOutManager timeoutManager) {
        this.routingTable = routingTable;
        this.channelOut = channelMessageBlockingQueue;
        this.timeoutManager = timeoutManager;
    }

    public void doSearch(String fileName) {
        String payload = String.format(queryFormat, this.routingTable.getAddress(), this.routingTable.getPort(), StringEncoderDecoder.encode(fileName), hopCount);
        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
        ChannelMessage initialMessage = new ChannelMessage(this.routingTable.getAddress(), this.routingTable.getPort(), rawMessage);
        this.handleResponse(initialMessage);
    }
}
