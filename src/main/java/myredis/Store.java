package myredis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

public class Store {

    private static class Entry {
        String value;
        long expiresAt;

        Entry(String value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return expiresAt != -1 && System.currentTimeMillis() >= expiresAt;
        }
    }

    private static final ConcurrentHashMap<String, Entry> data = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LinkedList<String>> lists = new ConcurrentHashMap<>();

    // ---- STRING OPS ----

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
        boolean removed = data.remove(key) != null;
        removed |= lists.remove(key) != null;
        return removed;
    }

    public static boolean exists(String key) {
        return get(key) != null || lists.containsKey(key);
    }

    public static boolean expire(String key, long ttlSeconds) {
        Entry entry = data.get(key);
        if (entry == null || entry.isExpired()) return false;
        entry.expiresAt = System.currentTimeMillis() + (ttlSeconds * 1000);
        return true;
    }

    public static long ttl(String key) {
        Entry entry = data.get(key);
        if (entry == null || entry.isExpired()) return -2;
        if (entry.expiresAt == -1) return -1;
        return Math.max(0, (entry.expiresAt - System.currentTimeMillis()) / 1000);
    }

    // ---- LIST OPS ----

    public static synchronized long lpush(String key, String value) {
        LinkedList<String> list = lists.computeIfAbsent(key, k -> new LinkedList<>());
        list.addFirst(value);
        return list.size();
    }

    public static synchronized long rpush(String key, String value) {
        LinkedList<String> list = lists.computeIfAbsent(key, k -> new LinkedList<>());
        list.addLast(value);
        return list.size();
    }

    public static synchronized String lpop(String key) {
        LinkedList<String> list = lists.get(key);
        if (list == null || list.isEmpty()) return null;
        String val = list.removeFirst();
        if (list.isEmpty()) lists.remove(key);
        return val;
    }

    public static synchronized String rpop(String key) {
        LinkedList<String> list = lists.get(key);
        if (list == null || list.isEmpty()) return null;
        String val = list.removeLast();
        if (list.isEmpty()) lists.remove(key);
        return val;
    }

    public static long llen(String key) {
        LinkedList<String> list = lists.get(key);
        return list == null ? 0 : list.size();
    }

    // supports negative indices like Redis: -1 = last element
    public static synchronized List<String> lrange(String key, int start, int stop) {
        LinkedList<String> list = lists.get(key);
        if (list == null || list.isEmpty()) return Collections.emptyList();

        int size = list.size();
        if (start < 0) start = Math.max(size + start, 0);
        if (stop < 0) stop = size + stop;
        stop = Math.min(stop, size - 1);

        if (start > stop || start >= size) return Collections.emptyList();

        List<String> result = new ArrayList<>();
        for (int i = start; i <= stop; i++) {
            result.add(list.get(i));
        }
        return result;
    }
}
