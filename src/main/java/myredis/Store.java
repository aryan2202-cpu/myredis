package myredis;

import java.util.concurrent.ConcurrentHashMap;

public class Store {

    private static class Entry {
        String value;
        long expiresAt; // epoch millis, -1 means no expiry

        Entry(String value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return expiresAt != -1 && System.currentTimeMillis() >= expiresAt;
        }
    }

    private static final ConcurrentHashMap<String, Entry> data = new ConcurrentHashMap<>();

    public static void set(String key, String value) {
        data.put(key, new Entry(value, -1));
    }

    public static void setWithExpiry(String key, String value, long ttlMillis) {
        long expiresAt = System.currentTimeMillis() + ttlMillis;
        data.put(key, new Entry(value, expiresAt));
    }

    public static String get(String key) {
        Entry entry = data.get(key);
        if (entry == null) return null;
        if (entry.isExpired()) {
            data.remove(key);
            return null;
        }
        return entry.value;
    }

    public static boolean delete(String key) {
        return data.remove(key) != null;
    }

    public static boolean exists(String key) {
        return get(key) != null; // reuses expiry check
    }

    // returns true if expiry was set (key must exist)
    public static boolean expire(String key, long ttlSeconds) {
        Entry entry = data.get(key);
        if (entry == null || entry.isExpired()) return false;
        entry.expiresAt = System.currentTimeMillis() + (ttlSeconds * 1000);
        return true;
    }

    // returns remaining TTL in seconds, -1 if no expiry, -2 if key doesn't exist
    public static long ttl(String key) {
        Entry entry = data.get(key);
        if (entry == null || entry.isExpired()) return -2;
        if (entry.expiresAt == -1) return -1;
        return Math.max(0, (entry.expiresAt - System.currentTimeMillis()) / 1000);
    }
}
