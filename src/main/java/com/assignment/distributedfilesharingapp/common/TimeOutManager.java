package com.assignment.distributedfilesharingapp.common;

import com.assignment.distributedfilesharingapp.model.TimeoutCallbackMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TimeOutManager {

    @Value("${app.common.r-ping-message-id}")
    private String rPingMessageId;

    private final Map<String, TimeoutCallbackMap> requests = new HashMap<>();

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
                if (requestString.equals(rPingMessageId)) {
                    requests.get(requestString).setTimeoutTime(requests.get(requestString).getTimeoutTime()
                            + requests.get(requestString).getTimeout());
                } else {
                    requests.remove(requestString);
                }
            }
        });
    }
}
