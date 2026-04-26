package com.bloodconnect.bloodconnect.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitingService {

    private final ConcurrentHashMap<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 5;

    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        requestCounts.entrySet().removeIf(entry -> now - entry.getValue().timestamp > TimeUnit.MINUTES.toMillis(1));

        RequestInfo info = requestCounts.computeIfAbsent(key, k -> new RequestInfo(now));
        
        if (now - info.timestamp > TimeUnit.MINUTES.toMillis(1)) {
            info.timestamp = now;
            info.count.set(1);
            return true;
        }

        return info.count.incrementAndGet() <= MAX_REQUESTS_PER_MINUTE;
    }

    private static class RequestInfo {
        long timestamp;
        AtomicInteger count = new AtomicInteger(1);

        RequestInfo(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
