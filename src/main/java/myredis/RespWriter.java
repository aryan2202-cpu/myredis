package myredis;

import java.io.*;
import java.util.*;

public class RespWriter {

    public static void writeSimpleString(OutputStream out, String s) throws IOException {
        out.write(("+" + s + "\r\n").getBytes());
    }

    public static void writeError(OutputStream out, String msg) throws IOException {
        out.write(("-ERR " + msg + "\r\n").getBytes());
    }

    public static void writeInteger(OutputStream out, long value) throws IOException {
        out.write((":" + value + "\r\n").getBytes());
    }

    public static void writeBulkString(OutputStream out, String s) throws IOException {
        if (s == null) {
            out.write("$-1\r\n".getBytes());
            return;
        }
        byte[] data = s.getBytes();
        out.write(("$" + data.length + "\r\n").getBytes());
        out.write(data);
        out.write("\r\n".getBytes());
    }

    public static void writeArray(OutputStream out, List<String> items) throws IOException {
        out.write(("*" + items.size() + "\r\n").getBytes());
        for (String item : items) {
            writeBulkString(out, item);
        }
    }
}
