package com.example.demo.component;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class RequestContext {

    private String requestId;
    private String userId;
    private long startTime;

    public RequestContext() {
        this.startTime = System.currentTimeMillis();
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getStartTime() { return startTime; }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
}
