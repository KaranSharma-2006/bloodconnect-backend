package com.bloodconnect.bloodconnect.util;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class SearchRateLimiter {

    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW_MS = TimeUnit.MINUTES.toMillis(1);

    private final Map<Long, RateLimitInfo> userRequests = new ConcurrentHashMap<>();

    public boolean isAllowed(Long hospitalId) {
        long currentTime = System.currentTimeMillis();
        userRequests.entrySet().removeIf(entry -> currentTime - entry.getValue().lastResetTime > TIME_WINDOW_MS);

        RateLimitInfo info = userRequests.computeIfAbsent(hospitalId, k -> new RateLimitInfo(currentTime));

        if (currentTime - info.lastResetTime > TIME_WINDOW_MS) {
            info.count = 1;
            info.lastResetTime = currentTime;
            return true;
        }

        if (info.count < MAX_REQUESTS) {
            info.count++;
            return true;
        }

        return false;
    }

    private static class RateLimitInfo {
        int count;
        long lastResetTime;

        RateLimitInfo(long startTime) {
            this.count = 1;
            this.lastResetTime = startTime;
        }
    }
}
