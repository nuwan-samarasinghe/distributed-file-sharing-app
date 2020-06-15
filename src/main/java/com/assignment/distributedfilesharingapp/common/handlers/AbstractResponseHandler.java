package com.assignment.distributedfilesharingapp.common.handlers;

import com.assignment.distributedfilesharingapp.model.ChannelMessage;

public interface AbstractResponseHandler extends AbstractMessageHandler {
    void handleResponse(ChannelMessage message);
}
