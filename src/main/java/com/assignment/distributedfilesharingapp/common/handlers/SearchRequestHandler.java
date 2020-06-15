package com.assignment.distributedfilesharingapp.common.handlers;

import com.assignment.distributedfilesharingapp.common.FileManager;
import com.assignment.distributedfilesharingapp.common.StringEncoderDecoder;
import com.assignment.distributedfilesharingapp.common.TimeOutManager;
import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import com.assignment.distributedfilesharingapp.model.RoutingTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SearchRequestHandler implements AbstractResponseHandler, AbstractRequestHandler {

    private RoutingTable routingTable;
    private BlockingQueue<ChannelMessage> channelOut;
    private TimeOutManager timeoutManager;

    private final FileManager fileManager;

    @Value("${app.commands.query-format}")
    private String queryFormat;

    @Value("${app.node.hop-count}")
    private Integer hopCount;

    @Value("${app.commands.message-format}")
    private String messageFormat;

    @Value("${app.commands.query-hit-format}")
    private String queryHitFormat;


    public SearchRequestHandler(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void doSearch(String keyword) {
        String payload = String.format(queryFormat, this.routingTable.getAddress(), this.routingTable.getPort(), StringEncoderDecoder.encode(keyword), hopCount);
        log.info("search for the given key word : {}", payload);
        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
        ChannelMessage initialMessage = new ChannelMessage(this.routingTable.getAddress(), this.routingTable.getPort(), rawMessage);
        this.handleResponse(initialMessage);
    }

    @Override
    public void sendRequest(ChannelMessage message) {
        try {
            log.info("adding message to blocking queue with address {} port {} and the message {}", message.getAddress(), message.getPort(), message.getMessage());
            channelOut.put(message);
        } catch (InterruptedException e) {
            log.error("an error occurred while adding the message.", e);
        }
    }

    @Override
    public void handleResponse(ChannelMessage message) {
        log.info("received message: {} from:{} port:{}", message.getMessage(), message.getAddress(), message.getPort());
        String[] splitMessage = message.getMessage().split(" ");
        String address = splitMessage[2].trim();
        int port = Integer.parseInt(splitMessage[3].trim());
        String fileName = StringEncoderDecoder.decode(splitMessage[4].trim());
        int hops = Integer.parseInt(splitMessage[5].trim());
        //search file in the current node
        Set<String> resultSet = fileManager.searchForFile(fileName);
        if (!resultSet.isEmpty()) {
            String fileNamesString = resultSet
                    .stream()
                    .map(s -> StringEncoderDecoder.encode(s) + " ")
                    .collect(Collectors.joining("", "", ""));
            String payload = String.format(queryHitFormat, resultSet.size(), routingTable.getAddress(), routingTable.getPort(), hopCount - hops, fileNamesString);
            log.info("requesting the file {}", payload);
            String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
            ChannelMessage queryHitMessage = new ChannelMessage(address, port, rawMessage);
            this.sendRequest(queryHitMessage);
        }
        //if the hop count is greater than zero send the message to all neighbours again
        if (hops > 0) {
            //skip sending search query to the same node again
            this.routingTable
                    .getNeighbours()
                    .stream()
                    .filter(neighbour -> !neighbour.getAddress().equals(message.getAddress()) || !Objects.equals(neighbour.getClientPort(), message.getPort()))
                    .forEach(neighbour -> {
                        String payload = String.format(queryFormat, address, port, StringEncoderDecoder.encode(fileName), hops - 1);
                        log.info("send request to neighbours {}", payload);
                        String rawMessage = String.format(messageFormat, payload.length() + 5, payload);
                        ChannelMessage queryMessage = new ChannelMessage(neighbour.getAddress(), neighbour.getPort(), rawMessage);
                        this.sendRequest(queryMessage);
                    });
        }
    }

    @Override
    public void init(RoutingTable routingTable, BlockingQueue<ChannelMessage> channelMessageBlockingQueue, TimeOutManager timeoutManager) {
        this.routingTable = routingTable;
        this.channelOut = channelMessageBlockingQueue;
        this.timeoutManager = timeoutManager;
    }
}
