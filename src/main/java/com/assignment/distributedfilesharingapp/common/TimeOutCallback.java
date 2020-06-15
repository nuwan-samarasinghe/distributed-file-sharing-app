package com.assignment.distributedfilesharingapp.common;

public interface TimeOutCallback {
    void onTimeout(String messageId);

    void onResponse(String messageId);
}
