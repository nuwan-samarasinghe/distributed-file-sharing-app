package com.assignment.distributedfilesharingapp.model;

import com.assignment.distributedfilesharingapp.common.TimeOut;
import lombok.Data;

@Data
public class TimeoutCallback {
    private Long timeoutTime;
    private TimeOut callback;
    private Long timeout;

    public TimeoutCallback(Long timeout, TimeOut callback) {
        this.timeout = timeout;
        this.callback = callback;
        this.timeoutTime = System.currentTimeMillis() + timeout;
    }

    public boolean checkTimeout(String messageID) {
        if (System.currentTimeMillis() >= timeoutTime) {
            callback.onTimeout(messageID);
            return true;
        }
        return false;
    }
}
