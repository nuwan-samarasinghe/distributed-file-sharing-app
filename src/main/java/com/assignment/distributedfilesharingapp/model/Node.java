package com.assignment.distributedfilesharingapp.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;

@Slf4j
@Data
public class Node {
    public static final String IP_FORMAT = "8.8.8.8";
    private String userName;
    private String ipAddress;
    private Integer port;

    public Node() {
        // get host ip address
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(IP_FORMAT), 10002);
            this.ipAddress = socket.getLocalAddress().getHostAddress();
            log.info("generated node ip is : {}", this.ipAddress);
        } catch (Exception e) {
            log.error("Could not find host address", e);
        }

        // generate port number
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            this.port = socket.getLocalPort();
            log.info("generated node port is : {}", this.port);
        } catch (IOException e) {
            log.error("Getting free port failed", e);
        }
    }

}
