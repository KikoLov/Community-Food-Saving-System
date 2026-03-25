package com.food.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单内存滑动窗口限流服务
 */
@Service
public class RateLimitService {

    private final Map<String, ArrayDeque<Long>> windows = new ConcurrentHashMap<>();

    public boolean allow(String key, int maxRequests, int windowSeconds) {
        long now = System.currentTimeMillis();
        long threshold = now - windowSeconds * 1000L;
        ArrayDeque<Long> queue = windows.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst() < threshold) {
                queue.pollFirst();
            }
            if (queue.size() >= maxRequests) {
                return false;
            }
            queue.addLast(now);
            return true;
        }
    }
}
