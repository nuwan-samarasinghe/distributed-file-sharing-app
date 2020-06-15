package com.assignment.distributedfilesharingapp.model;

import com.assignment.distributedfilesharingapp.common.TimeOutCallback;
import lombok.Data;

@Data
public class TimeoutCallbackMap {
    private Long timeoutTime;
    private TimeOutCallback callback;
    private Long timeout;

    public TimeoutCallbackMap(Long timeout, TimeOutCallback callback) {
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
