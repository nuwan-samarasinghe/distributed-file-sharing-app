package com.assignment.distributedfilesharingapp.model;

import lombok.Data;

@Data
public class SearchResult {

    private String fileName;
    private String address;
    private int port;
    private int tcpPort;
    private int hops;
    private long timeElapsed;

    public SearchResult(String fileName, String address, Integer port, Integer hops, Long timeElapsed) {
        this.fileName = fileName;
        this.address = address;
        this.port = port;
        this.tcpPort = port + 100;
        this.hops = hops;
        this.timeElapsed = timeElapsed;
    }
}
