package com.assignment.distributedfilesharingapp.service;

import com.assignment.distributedfilesharingapp.common.IpPortConverter;
import com.assignment.distributedfilesharingapp.exception.FileSharingException;
import com.assignment.distributedfilesharingapp.model.CommandTypes;
import com.assignment.distributedfilesharingapp.model.NeighbourNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class BootstrapServerService {

    @Value("${app.commands.register-format}")
    private String registerFormat;

    @Value("${app.commands.message-format}")
    private String messageFormat;

    @Value("${app.bootstrap-server.ip-address}")
    private String ipAddress;

    @Value("${app.bootstrap-server.port}")
    private Integer port;

    @Value("${app.bootstrap-server.timeout}")
    private Integer timeout;

    private final DatagramSocket datagramSocket;

    private final IpPortConverter ipPortConverter;

    public BootstrapServerService(IpPortConverter ipPortConverter) throws SocketException {
        this.datagramSocket = new DatagramSocket();
        this.ipPortConverter = ipPortConverter;
    }

    //register node
    public List<InetSocketAddress> register(String userName, String ipAddress, int port) throws IOException {
        String request = String.format(registerFormat, ipAddress, port, userName);
        request = String.format(messageFormat, request.length() + 5, request);
        return processBootstrapResponse(sendOrReceive(request));
    }

    private List<InetSocketAddress> processBootstrapResponse(String sendOrReceive) {

        if (!sendOrReceive.contains(CommandTypes.REGOK.name())) {
            log.error(" {} not received", CommandTypes.REGOK.name());
            throw new FileSharingException(CommandTypes.REGOK.name() + " not received");
        }
        String[] splitResponse = sendOrReceive.split(" ");
        List<InetSocketAddress> neighbourNodes = ipPortConverter.getStringToIPPortConverter().apply(splitResponse);

        if (neighbourNodes.size() == 0) {
            if (Objects.equals(splitResponse[2], "9999")) {
                log.error("Failed. There are errors in your command");
            } else if (Objects.equals(splitResponse[2], "9998")) {
                log.error("Failed, already registered to you, unRegister first");
            } else if (Objects.equals(splitResponse[2], "9997")) {
                log.error("Failed, registered to another user, try a different IP and port");
            } else if (Objects.equals(splitResponse[2], "9996")) {
                log.error("Failed, can’t register. BS full");
            } else if (Objects.equals(splitResponse[2], "0")) {
                log.info("Successful - No other nodes in the network");
                return Collections.emptyList();
            } else {
                throw new FileSharingException("Invalid status code.");
            }
            return null;
        } else {
            log.info("No of nodes found : 1");
            return neighbourNodes;
        }
    }

    private String sendOrReceive(String request) throws IOException {
        log.info("initiate send request for BS : {}", request);
        DatagramPacket sendingPacket = new DatagramPacket(request.getBytes(), request.length(), InetAddress.getByName(ipAddress), port);
        datagramSocket.setSoTimeout(timeout);
        datagramSocket.send(sendingPacket);
        byte[] buffer = new byte[65536];
        DatagramPacket received = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(received);
        String response = new String(received.getData(), 0, received.getLength());
        log.info("received the response from BS : {}", response);
        return response;
    }

}
