package myredis;

import java.io.*;
import java.util.*;

public class CommandDispatcher {

    public static void dispatch(List<String> command, OutputStream out) throws IOException {
        if (command.isEmpty()) {
            RespWriter.writeError(out, "empty command");
            return;
        }

        String cmd = command.get(0).toUpperCase();

        switch (cmd) {
            case "PING" -> {
                if (command.size() > 1) {
                    RespWriter.writeBulkString(out, command.get(1));
                } else {
                    RespWriter.writeSimpleString(out, "PONG");
                }
            }
            case "SET" -> handleSet(command, out);
            case "GET" -> {
                if (command.size() < 2) {
                    RespWriter.writeError(out, "wrong number of arguments for 'get'");
                } else {
                    RespWriter.writeBulkString(out, Store.get(command.get(1)));
                }
            }
            case "DEL" -> {
                if (command.size() < 2) {
                    RespWriter.writeError(out, "wrong number of arguments for 'del'");
                } else {
                    boolean removed = Store.delete(command.get(1));
                    RespWriter.writeInteger(out, removed ? 1 : 0);
                }
            }
            case "EXISTS" -> {
                if (command.size() < 2) {
                    RespWriter.writeError(out, "wrong number of arguments for 'exists'");
                } else {
                    boolean exists = Store.exists(command.get(1));
                    RespWriter.writeInteger(out, exists ? 1 : 0);
                }
            }
            case "EXPIRE" -> {
                if (command.size() < 3) {
                    RespWriter.writeError(out, "wrong number of arguments for 'expire'");
                } else {
                    long seconds = Long.parseLong(command.get(2));
                    boolean ok = Store.expire(command.get(1), seconds);
                    RespWriter.writeInteger(out, ok ? 1 : 0);
                }
            }
            case "TTL" -> {
                if (command.size() < 2) {
                    RespWriter.writeError(out, "wrong number of arguments for 'ttl'");
                } else {
                    long ttl = Store.ttl(command.get(1));
                    RespWriter.writeInteger(out, ttl);
                }
            }
            default -> RespWriter.writeError(out, "unknown command '" + cmd + "'");
        }
    }

    private static void handleSet(List<String> command, OutputStream out) throws IOException {
        if (command.size() < 3) {
            RespWriter.writeError(out, "wrong number of arguments for 'set'");
            return;
        }

        String key = command.get(1);
        String value = command.get(2);
        long ttlMillis = -1;

        // look for EX/PX options: SET key value EX 10  or  SET key value PX 10000
        for (int i = 3; i < command.size() - 1; i++) {
            String opt = command.get(i).toUpperCase();
            if (opt.equals("EX")) {
                ttlMillis = Long.parseLong(command.get(i + 1)) * 1000;
            } else if (opt.equals("PX")) {
                ttlMillis = Long.parseLong(command.get(i + 1));
            }
        }

        if (ttlMillis > 0) {
            Store.setWithExpiry(key, value, ttlMillis);
        } else {
            Store.set(key, value);
        }
        RespWriter.writeSimpleString(out, "OK");
    }
}
