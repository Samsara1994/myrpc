package com.springboot.rpcclient.circuitbreaker;

public class CircuitBreaker {
    private final int failureThreshold;
    private final long resetTimeout;
    private int failureCount;
    private long lastFailureTime;
    private boolean isOpen;

    public CircuitBreaker(int failureThreshold, long resetTimeout) {
        this.failureThreshold = failureThreshold;
        this.resetTimeout = resetTimeout;
    }

    public boolean allowRequest() {
        if (isOpen) {
            if (System.currentTimeMillis() - lastFailureTime > resetTimeout) {
                isOpen = false;
                failureCount = 0;
                return true;
            }
            return false;
        }
        return true;
    }

    public void onSuccess() {
        failureCount = 0;
    }

    public void onFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        if (failureCount >= failureThreshold) {
            isOpen = true;
        }
    }
}
