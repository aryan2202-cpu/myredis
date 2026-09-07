package myredis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

public class Store {

    private static class Entry {
        String value;
        long expiresAt;
        Entry(String value, long expiresAt) { this.value = value; this.expiresAt = expiresAt; }
        boolean isExpired() { return expiresAt != -1 && System.currentTimeMillis() >= expiresAt; }
    }

    private static final ConcurrentHashMap<String, Entry> data = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LinkedList<String>> lists = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Map<String, String>> hashes = new ConcurrentHashMap<>();

    public static void set(String key, String value) { data.put(key, new Entry(value, -1)); }
    public static void setWithExpiry(String key, String value, long ttlMillis) {
        data.put(key, new Entry(value, System.currentTimeMillis() + ttlMillis));
    }
    public static String get(String key) {
        Entry entry = data.get(key);
        if (entry == null) return null;
        if (entry.isExpired()) { data.remove(key); return null; }
        return entry.value;
    }
    public static boolean delete(String key) {
        boolean removed = data.remove(key) != null;
        removed |= lists.remove(key) != null;
        removed |= hashes.remove(key) != null;
        return removed;
    }
    public static boolean exists(String key) {
        return get(key) != null || lists.containsKey(key) || hashes.containsKey(key);
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

    public static synchronized long lpush(String key, String value) {
        LinkedList<String> list = lists.computeIfAbsent(key, k -> new LinkedList<>());
        list.addFirst(value); return list.size();
    }
    public static synchronized long rpush(String key, String value) {
        LinkedList<String> list = lists.computeIfAbsent(key, k -> new LinkedList<>());
        list.addLast(value); return list.size();
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
    public static synchronized List<String> lrange(String key, int start, int stop) {
        LinkedList<String> list = lists.get(key);
        if (list == null || list.isEmpty()) return Collections.emptyList();
        int size = list.size();
        if (start < 0) start = Math.max(size + start, 0);
        if (stop < 0) stop = size + stop;
        stop = Math.min(stop, size - 1);
        if (start > stop || start >= size) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (int i = start; i <= stop; i++) result.add(list.get(i));
        return result;
    }

    // ---- HASH OPS ----
    public static synchronized long hset(String key, String field, String value) {
        Map<String, String> hash = hashes.computeIfAbsent(key, k -> new LinkedHashMap<>());
        boolean isNew = !hash.containsKey(field);
        hash.put(field, value);
        return isNew ? 1 : 0;
    }
    public static String hget(String key, String field) {
        Map<String, String> hash = hashes.get(key);
        return hash == null ? null : hash.get(field);
    }
    public static synchronized boolean hdel(String key, String field) {
        Map<String, String> hash = hashes.get(key);
        if (hash == null) return false;
        boolean removed = hash.remove(field) != null;
        if (hash.isEmpty()) hashes.remove(key);
        return removed;
    }
    public static boolean hexists(String key, String field) {
        Map<String, String> hash = hashes.get(key);
        return hash != null && hash.containsKey(field);
    }
    public static List<String> hgetall(String key) {
        Map<String, String> hash = hashes.get(key);
        if (hash == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String> e : hash.entrySet()) {
            result.add(e.getKey());
            result.add(e.getValue());
        }
        return result;
    }
    public static long hlen(String key) {
        Map<String, String> hash = hashes.get(key);
        return hash == null ? 0 : hash.size();
    }
}
