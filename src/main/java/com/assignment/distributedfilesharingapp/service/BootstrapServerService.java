package com.assignment.distributedfilesharingapp.service;

import com.assignment.distributedfilesharingapp.common.IpPortConverter;
import com.assignment.distributedfilesharingapp.exception.MessageExchangeException;
import com.assignment.distributedfilesharingapp.model.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.util.*;

@Slf4j
@Service
public class BootstrapServerService {

    @Value("${app.commands.register-format}")
    private String registerFormat;

    @Value("${app.commands.message-format}")
    private String messageFormat;

    @Value("${app.commands.un-register-format}")
    private String unRegisterFormat;

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

    /**
     * to register a node in Bootstrap server
     *
     * @param userName  username of the server
     * @param ipAddress ip-address of the server
     * @param port      application running port of the server
     * @return after registration it will give the another two registered neighbour nodes.
     */
    public List<InetSocketAddress> register(String userName, String ipAddress, int port) throws IOException {
        String request = String.format(registerFormat, ipAddress, port, userName);
        request = String.format(messageFormat, request.length() + 5, request);
        return processBootstrapRegisterResponse(registerOrUnregister(request));
    }

    /**
     * un register a node in Bootstrap server
     *
     * @param userName  username of the server
     * @param ipAddress ip-address of the server
     * @param port      application running port of the server
     * @return after registration it will give a boolean value whether it removed or not.
     */
    public boolean unRegister(String userName, String ipAddress, int port) throws IOException {
        String request = String.format(unRegisterFormat, ipAddress, port, userName);
        request = String.format(messageFormat, request.length() + 5, request);
        return processBootstrapServerUnregisterResponse(registerOrUnregister(request));
    }

    private boolean processBootstrapServerUnregisterResponse(String response) {
        String[] strings = response.split(" ");
        if (!response.contains(MessageType.REGOK.name())) {
            throw new IllegalStateException("Registration not successful with response :"+strings);
        }
        if (Objects.equals(strings[2], "0")) {
            log.info("Successfully unregistered");
            return true;
        } else if (Objects.equals(strings[2], "9999")) {
            log.info("Error while un-registering. IP and port may not be in the registry or command is incorrect");
            return false;
        } else {
            return false;
        }
    }

    private List<InetSocketAddress> processBootstrapRegisterResponse(String sendOrReceive) {

        if (!sendOrReceive.contains(MessageType.REGOK.name())) {
            log.error(" {} not received", MessageType.REGOK.name());
            throw new MessageExchangeException(MessageType.REGOK.name() + " not received");
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
                throw new MessageExchangeException("Invalid status code.");
            }
            return null;
        } else {
            log.info("No of nodes found : 1");
            return neighbourNodes;
        }
    }

    private String registerOrUnregister(String request) throws IOException {
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
