package com.assignment.distributedfilesharingapp.model;

import lombok.Data;

@Data
public class Neighbour {
    private String address;
    private Integer port;
    private Integer pingPongs;
    private Integer clientPort;

    public Neighbour() {
        this.pingPongs = 0;
    }

    public Neighbour(String address, Integer port, Integer clientPort) {
        this.address = address;
        this.port = port;
        this.pingPongs = 0;
        this.clientPort = clientPort;
    }

    public void Ping() {
        this.pingPongs++;
    }
}
