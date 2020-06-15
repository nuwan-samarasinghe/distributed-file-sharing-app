package com.assignment.distributedfilesharingapp.common.handlers;


import com.assignment.distributedfilesharingapp.model.ChannelMessage;

public interface AbstractRequestHandler extends AbstractMessageHandler {
    void sendRequest(ChannelMessage message);
}
