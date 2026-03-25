package com.food.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单幂等服务（内存版）
 */
@Service
public class IdempotencyService {

    private static class Entry {
        private final long createdAtMs;
        private final Long orderId;
        private final String status; // PROCESSING / DONE

        private Entry(long createdAtMs, Long orderId, String status) {
            this.createdAtMs = createdAtMs;
            this.orderId = orderId;
            this.status = status;
        }
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private static final long TTL_MS = 5 * 60 * 1000L;

    public Long checkDone(String key) {
        cleanupExpired();
        Entry entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if ("DONE".equals(entry.status)) {
            return entry.orderId;
        }
        return null;
    }

    public boolean tryBegin(String key) {
        cleanupExpired();
        Entry prev = store.putIfAbsent(key, new Entry(System.currentTimeMillis(), null, "PROCESSING"));
        return prev == null;
    }

    public void complete(String key, Long orderId) {
        store.put(key, new Entry(System.currentTimeMillis(), orderId, "DONE"));
    }

    public void clear(String key) {
        store.remove(key);
    }

    public boolean isProcessing(String key) {
        Entry entry = store.get(key);
        return entry != null && "PROCESSING".equals(entry.status);
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> now - e.getValue().createdAtMs > TTL_MS);
    }
}
