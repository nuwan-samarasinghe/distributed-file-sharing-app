package com.assignment.distributedfilesharingapp.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.UUID;

@Slf4j
@Data
public class Node {
    private String userName;
    private String ipAddress;
    private Integer port;

    // generate a node with ip address, username & a port
    public Node(String nodeName) {
        String uniqueID = UUID.randomUUID().toString();
        this.userName = nodeName.replace("{uniqueID}", "" + uniqueID);
        // get current hosts' ip address
        try (final DatagramSocket socket = new DatagramSocket()) {
            //set a dummy connection (no need to have reachable ip address)
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            this.ipAddress = socket.getLocalAddress().getHostAddress();
            log.info("Current IP address of your PC is : {}", this.ipAddress);
        } catch (Exception e) {
            log.error("Could not find your PC ip address", e);
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
