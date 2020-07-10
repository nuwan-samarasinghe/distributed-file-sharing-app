package com.assignment.distributedfilesharingapp.common.strategy;



public enum MessageType {
    JOIN("JOIN"),
    JOINOK("JOINOK"),
    PING("PING"),
    PONG("PONG"),
    SER("SER"),
    SEROK("SEROK"),
    LEAVE("LEAVE");

    private String type;

    MessageType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }



}
