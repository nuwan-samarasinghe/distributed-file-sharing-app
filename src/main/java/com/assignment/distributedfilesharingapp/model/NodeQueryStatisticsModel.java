package com.assignment.distributedfilesharingapp.model;

import lombok.Data;

@Data
public class NodeQueryStatisticsModel {

    private int forwardedCount = 0;
    private int receivedCount = 0;
    private int answeredCount = 0;

    private String nodeInfo;

    public NodeQueryStatisticsModel(String address, Integer port) {
        this.nodeInfo = address + ":" + port;
    }

    public void increaseForwardedCount() {
        this.forwardedCount++;
    }

    public void increaseReceivedCount() {
        this.receivedCount++;
    }

    public void increaseAnsweredCount() {
        this.answeredCount++;
    }
}
