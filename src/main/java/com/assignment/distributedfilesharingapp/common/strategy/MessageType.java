package com.assignment.distributedfilesharingapp.common.strategy;



public enum MessageType {

    PING("ping"),
    PONG("pong"),
    SER("search"),
    SEROK("searchok"),
    LEAVE("leave");

    private String type;

    MessageType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

}
