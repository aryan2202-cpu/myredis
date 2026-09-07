package myredis;

import java.io.*;
import java.net.*;

public class TestClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", 6379);
        OutputStream out = socket.getOutputStream();
        InputStream in = new BufferedInputStream(socket.getInputStream());

        // SET session hello EX 2   (expires in 2 seconds)
        sendAndPrint(out, in, "*5\r\n$3\r\nSET\r\n$7\r\nsession\r\n$5\r\nhello\r\n$2\r\nEX\r\n$1\r\n2\r\n");
        sendAndPrint(out, in, "*2\r\n$3\r\nGET\r\n$7\r\nsession\r\n");     // should return "hello"
        sendAndPrint(out, in, "*2\r\n$3\r\nTTL\r\n$7\r\nsession\r\n");    // should return ~2

        System.out.println("Sleeping 3 seconds so key expires...");
        Thread.sleep(3000);

        sendAndPrint(out, in, "*2\r\n$3\r\nGET\r\n$7\r\nsession\r\n");     // should return nil now
        sendAndPrint(out, in, "*2\r\n$3\r\nTTL\r\n$7\r\nsession\r\n");    // should return -2

        socket.close();
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
                String line = readLine(in);
                return (char) type + line;
            }
            case '$': {
                String lenStr = readLine(in);
                int len = Integer.parseInt(lenStr);
                if (len == -1) return "$-1 (nil)";
                byte[] buf = new byte[len];
                int read = 0;
                while (read < len) {
                    int r = in.read(buf, read, len - read);
                    read += r;
                }
                readLine(in);
                return "$" + len + " -> " + new String(buf);
            }
            default:
                return "UNKNOWN type byte: " + type;
        }
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\r') {
                in.read();
                break;
            }
            sb.append((char) c);
        }
        return sb.toString();
    }
}
