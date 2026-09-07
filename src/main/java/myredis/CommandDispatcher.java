package myredis;

import java.io.*;
import java.util.*;

public class CommandDispatcher {

    public static void dispatch(List<String> command, OutputStream out) throws IOException {
        if (command.isEmpty()) { RespWriter.writeError(out, "empty command"); return; }
        String cmd = command.get(0).toUpperCase();

        switch (cmd) {
            case "PING" -> {
                if (command.size() > 1) RespWriter.writeBulkString(out, command.get(1));
                else RespWriter.writeSimpleString(out, "PONG");
            }
            case "SET" -> handleSet(command, out);
            case "GET" -> {
                if (command.size() < 2) RespWriter.writeError(out, "wrong number of arguments for 'get'");
                else RespWriter.writeBulkString(out, Store.get(command.get(1)));
            }
            case "DEL" -> {
                if (command.size() < 2) RespWriter.writeError(out, "wrong number of arguments for 'del'");
                else RespWriter.writeInteger(out, Store.delete(command.get(1)) ? 1 : 0);
            }
            case "EXISTS" -> {
                if (command.size() < 2) RespWriter.writeError(out, "wrong number of arguments for 'exists'");
                else RespWriter.writeInteger(out, Store.exists(command.get(1)) ? 1 : 0);
            }
            case "EXPIRE" -> {
                if (command.size() < 3) RespWriter.writeError(out, "wrong number of arguments for 'expire'");
                else RespWriter.writeInteger(out, Store.expire(command.get(1), Long.parseLong(command.get(2))) ? 1 : 0);
            }
            case "TTL" -> {
                if (command.size() < 2) RespWriter.writeError(out, "wrong number of arguments for 'ttl'");
                else RespWriter.writeInteger(out, Store.ttl(command.get(1)));
            }
            case "LPUSH" -> {
                if (command.size() < 3) RespWriter.writeError(out, "wrong number of arguments for 'lpush'");
                else RespWriter.writeInteger(out, Store.lpush(command.get(1), command.get(2)));
            }
            case "RPUSH" -> {
                if (command.size() < 3) RespWriter.writeError(out, "wrong number of arguments for 'rpush'");
                else RespWriter.writeInteger(out, Store.rpush(command.get(1), command.get(2)));
            }
            case "LPOP" -> {
                if (command.size() < 2) RespWriter.writeError(out, "wrong number of arguments for 'lpop'");
                else RespWriter.writeBulkString(out, Store.lpop(command.get(1)));
            }
            case "RPOP" -> {
                if (command.size() < 2) RespWriter.writeError(out, "wrong number of arguments for 'rpop'");
                else RespWriter.writeBulkString(out, Store.rpop(command.get(1)));
            }
            case "LLEN" -> {
                if (command.size() < 2) RespWriter.writeError(out, "wrong number of arguments for 'llen'");
                else RespWriter.writeInteger(out, Store.llen(command.get(1)));
            }
            case "LRANGE" -> {
                if (command.size() < 4) RespWriter.writeError(out, "wrong number of arguments for 'lrange'");
                else RespWriter.writeArray(out, Store.lrange(command.get(1), Integer.parseInt(command.get(2)), Integer.parseInt(command.get(3))));
            }
            case "HSET" -> {
                if (command.size() < 4) RespWriter.writeError(out, "wrong number of arguments for 'hset'");
                else RespWriter.writeInteger(out, Store.hset(command.get(1), command.get(2), command.get(3)));
            }
            case "HGET" -> {
                if (command.size() < 3) RespWriter.writeError(out, "wrong number of arguments for 'hget'");
                else RespWriter.writeBulkString(out, Store.hget(command.get(1), command.get(2)));
            }
            case "HDEL" -> {
                if (command.size() < 3) RespWriter.writeError(out, "wrong number of arguments for 'hdel'");
                else RespWriter.writeInteger(out, Store.hdel(command.get(1), command.get(2)) ? 1 : 0);
            }
            case "HEXISTS" -> {
                if (command.size() < 3) RespWriter.writeError(out, "wrong number of arguments for 'hexists'");
                else RespWriter.writeInteger(out, Store.hexists(command.get(1), command.get(2)) ? 1 : 0);
            }
            case "HGETALL" -> {
                if (command.size() < 2) RespWriter.writeError(out, "wrong number of arguments for 'hgetall'");
                else RespWriter.writeArray(out, Store.hgetall(command.get(1)));
            }
            case "HLEN" -> {
                if (command.size() < 2) RespWriter.writeError(out, "wrong number of arguments for 'hlen'");
                else RespWriter.writeInteger(out, Store.hlen(command.get(1)));
            }
            default -> RespWriter.writeError(out, "unknown command '" + cmd + "'");
        }
    }

    private static void handleSet(List<String> command, OutputStream out) throws IOException {
        if (command.size() < 3) { RespWriter.writeError(out, "wrong number of arguments for 'set'"); return; }
        String key = command.get(1), value = command.get(2);
        long ttlMillis = -1;
        for (int i = 3; i < command.size() - 1; i++) {
            String opt = command.get(i).toUpperCase();
            if (opt.equals("EX")) ttlMillis = Long.parseLong(command.get(i + 1)) * 1000;
            else if (opt.equals("PX")) ttlMillis = Long.parseLong(command.get(i + 1));
        }
        if (ttlMillis > 0) Store.setWithExpiry(key, value, ttlMillis);
        else Store.set(key, value);
        RespWriter.writeSimpleString(out, "OK");
    }
}
