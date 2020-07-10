package com.assignment.distributedfilesharingapp.model;



public enum MessageType {
    JOIN("JOIN"),
    JOINOK("JOINOK"),
    REG("REG"),
    REGOK("REGOK"),
    UNROK("UNROK"),
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
