package com.assignment.distributedfilesharingapp.model;

import lombok.Data;

@Data
public class Neighbour {
    private String address;
    private Integer port;
    private Integer tirpCount;

    public Neighbour() {
        this.tirpCount = 0;
    }

    public Neighbour(String address, Integer port) {
        this.address = address;
        this.port = port;
        this.tirpCount = 0;
    }

    public void incrementTripCount() {
        this.tirpCount++;
    }
}
