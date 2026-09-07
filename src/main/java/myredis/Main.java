package myredis;

import java.io.*;
import java.net.*;
import java.util.*;

public class Main {
    private static final String DUMP_FILE = "dump.myrdb";

    public static void main(String[] args) throws IOException {
        Store.load(DUMP_FILE);
        System.out.println("Loaded data from " + DUMP_FILE + " (if it existed)");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Store.save(DUMP_FILE);
                System.out.println("Saved data to " + DUMP_FILE + " on shutdown");
            } catch (IOException e) {
                System.out.println("Failed to save on shutdown: " + e.getMessage());
            }
        }));

        int port = 6379;
        ServerSocket serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        System.out.println("Server listening on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private static void handleClient(Socket socket) {
        try (socket;
             InputStream in = new BufferedInputStream(socket.getInputStream());
             OutputStream out = socket.getOutputStream()) {

            while (true) {
                List<String> command = RespParser.parseCommand(in);
                if (command == null) break;
                System.out.println("Received command: " + command);
                CommandDispatcher.dispatch(command, out);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
