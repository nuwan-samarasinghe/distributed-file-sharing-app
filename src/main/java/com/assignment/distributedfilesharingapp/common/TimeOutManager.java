package com.assignment.distributedfilesharingapp.common;

import com.assignment.distributedfilesharingapp.model.TimeoutCallbackMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TimeOutManager {

    private final Environment environment;

    private final Map<String, TimeoutCallbackMap> requests = new ConcurrentHashMap<>();

    public TimeOutManager(Environment environment) {
        this.environment = environment;
    }

    public void registerMessage(String messageId, long timeout, TimeOutCallback callback) {
        requests.put(messageId, new TimeoutCallbackMap(timeout, callback));
    }

    public void removeMessage(String messageId) {
        log.info("RegisteringResponse : " + messageId);
        requests.remove(messageId);
    }

    public void checkForTimeout() {
        requests.keySet().forEach(requestString -> {
            if (requests.get(requestString).checkTimeout(requestString)) {
                if (requestString.equals(this.environment.getProperty("app.common.r-ping-message-id"))) {
                    requests.get(requestString).setTimeoutTime(requests.get(requestString).getTimeoutTime()
                            + requests.get(requestString).getTimeout());
                } else {
                    requests.remove(requestString);
                }
            }
        });
    }
}
