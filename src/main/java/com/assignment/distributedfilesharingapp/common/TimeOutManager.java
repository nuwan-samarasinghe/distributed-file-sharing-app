package com.assignment.distributedfilesharingapp.common;

import com.assignment.distributedfilesharingapp.model.TimeoutCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TimeOutManager {

    private final Environment environment;

    private final Map<String, TimeoutCallback> timeoutCallbackMap = new ConcurrentHashMap<>();

    public TimeOutManager(Environment environment) {
        this.environment = environment;
    }

    public void registerMessage(String messageId, long timeout, TimeOut callback) {
        timeoutCallbackMap.put(messageId, new TimeoutCallback(timeout, callback));
    }

    public void removeMessage(String messageId) {
        timeoutCallbackMap.remove(messageId);
    }

    public void checkForTimeout() {
        timeoutCallbackMap.keySet().forEach(requestString -> {
            if (timeoutCallbackMap.get(requestString).checkTimeout(requestString)) {
                //when we are in recursive join message, we resend join requests and dabbled the timeout time
                if (requestString.equals(this.environment.getProperty("app.common.r-join-message-id"))) {
                    timeoutCallbackMap.get(requestString).setTimeoutTime(timeoutCallbackMap.get(requestString).getTimeoutTime() + timeoutCallbackMap.get(requestString).getTimeout());
                } else {
                    timeoutCallbackMap.remove(requestString);
                }
            }
        });
    }
}
