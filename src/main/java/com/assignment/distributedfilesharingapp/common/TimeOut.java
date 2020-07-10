package com.assignment.distributedfilesharingapp.common;

public interface TimeOut {
    void onTimeout(String messageId);

    void onResponse(String messageId);
}
