package com.assignment.distributedfilesharingapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChannelMessage {
    private final MessageType type;
    private final String address;
    private final Integer port;
    private final String message;
}
