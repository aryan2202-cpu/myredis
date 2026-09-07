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
            case "LPUSH" -> {
                if (command.size() < 3) {
                    RespWriter.writeError(out, "wrong number of arguments for 'lpush'");
                } else {
                    long len = Store.lpush(command.get(1), command.get(2));
                    RespWriter.writeInteger(out, len);
                }
            }
            case "RPUSH" -> {
                if (command.size() < 3) {
                    RespWriter.writeError(out, "wrong number of arguments for 'rpush'");
                } else {
                    long len = Store.rpush(command.get(1), command.get(2));
                    RespWriter.writeInteger(out, len);
                }
            }
            case "LPOP" -> {
                if (command.size() < 2) {
                    RespWriter.writeError(out, "wrong number of arguments for 'lpop'");
                } else {
                    RespWriter.writeBulkString(out, Store.lpop(command.get(1)));
                }
            }
            case "RPOP" -> {
                if (command.size() < 2) {
                    RespWriter.writeError(out, "wrong number of arguments for 'rpop'");
                } else {
                    RespWriter.writeBulkString(out, Store.rpop(command.get(1)));
                }
            }
            case "LLEN" -> {
                if (command.size() < 2) {
                    RespWriter.writeError(out, "wrong number of arguments for 'llen'");
                } else {
                    RespWriter.writeInteger(out, Store.llen(command.get(1)));
                }
            }
            case "LRANGE" -> {
                if (command.size() < 4) {
                    RespWriter.writeError(out, "wrong number of arguments for 'lrange'");
                } else {
                    int start = Integer.parseInt(command.get(2));
                    int stop = Integer.parseInt(command.get(3));
                    List<String> result = Store.lrange(command.get(1), start, stop);
                    RespWriter.writeArray(out, result);
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
