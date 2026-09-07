package myredis;

import java.io.*;
import java.util.*;

public class RespParser {

    public static List<String> parseCommand(InputStream in) throws IOException {
        int firstByte = in.read();
        if (firstByte == -1) return null;
        if (firstByte != '*') {
            throw new IOException("Expected '*' but got: " + (char) firstByte);
        }

        int numArgs = Integer.parseInt(readLine(in));
        List<String> args = new ArrayList<>();

        for (int i = 0; i < numArgs; i++) {
            int typeByte = in.read();
            if (typeByte != '$') {
                throw new IOException("Expected '$' but got: " + (char) typeByte);
            }
            int len = Integer.parseInt(readLine(in));
            byte[] buf = new byte[len];
            int read = 0;
            while (read < len) {
                int r = in.read(buf, read, len - read);
                if (r == -1) throw new IOException("Unexpected end of stream");
                read += r;
            }
            readLine(in);
            args.add(new String(buf));
        }

        return args;
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
