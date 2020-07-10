package com.assignment.distributedfilesharingapp.model;

import lombok.Data;

@Data
public class Neighbour {
    private String address;
    private Integer port;
    private Integer pingPongs;

    public Neighbour() {
        this.pingPongs = 0;
    }

    public Neighbour(String address, Integer port) {
        this.address = address;
        this.port = port;
        this.pingPongs = 0;
    }

    public void Ping() {
        this.pingPongs++;
    }
}
