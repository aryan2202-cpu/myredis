package myredis;

import java.io.*;
import java.net.*;
import java.util.*;

public class TestClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 6379);
        OutputStream out = socket.getOutputStream();
        InputStream in = new BufferedInputStream(socket.getInputStream());

        // RPUSH mylist a
        sendAndPrint(out, in, encode("RPUSH", "mylist", "a"));
        // RPUSH mylist b
        sendAndPrint(out, in, encode("RPUSH", "mylist", "b"));
        // RPUSH mylist c
        sendAndPrint(out, in, encode("RPUSH", "mylist", "c"));
        // LPUSH mylist start
        sendAndPrint(out, in, encode("LPUSH", "mylist", "start"));
        // LLEN mylist
        sendAndPrint(out, in, encode("LLEN", "mylist"));
        // LRANGE mylist 0 -1  (get everything)
        sendAndPrint(out, in, encode("LRANGE", "mylist", "0", "-1"));
        // LPOP mylist
        sendAndPrint(out, in, encode("LPOP", "mylist"));
        // RPOP mylist
        sendAndPrint(out, in, encode("RPOP", "mylist"));
        // LRANGE mylist 0 -1 again
        sendAndPrint(out, in, encode("LRANGE", "mylist", "0", "-1"));

        socket.close();
    }

    // builds a RESP array command from parts
    private static String encode(String... parts) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(parts.length).append("\r\n");
        for (String p : parts) {
            sb.append("$").append(p.length()).append("\r\n").append(p).append("\r\n");
        }
        return sb.toString();
    }

    private static void sendAndPrint(OutputStream out, InputStream in, String resp) throws IOException {
        out.write(resp.getBytes());
        out.flush();
        System.out.println("Reply: " + readReply(in));
    }

    private static String readReply(InputStream in) throws IOException {
        int type = in.read();
        switch (type) {
            case '+': case '-': case ':': {
                return (char) type + readLine(in);
            }
            case '$': {
                int len = Integer.parseInt(readLine(in));
                if (len == -1) return "$-1 (nil)";
                byte[] buf = new byte[len];
                int read = 0;
                while (read < len) read += in.read(buf, read, len - read);
                readLine(in);
                return "$" + len + " -> " + new String(buf);
            }
            case '*': {
                int count = Integer.parseInt(readLine(in));
                List<String> items = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    items.add(readReply(in));
                }
                return "ARRAY" + items;
            }
            default:
                return "UNKNOWN type byte: " + type;
        }
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\r') { in.read(); break; }
            sb.append((char) c);
        }
        return sb.toString();
    }
}
