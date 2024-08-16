import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private ServerSocket serverSocket;
    private ConcurrentHashMap<Socket, String> clients = new ConcurrentHashMap<>();
    private boolean running = true;

    public ChatServer(String host, int port) throws IOException {
        this.serverSocket = new ServerSocket();
        this.serverSocket.bind(new InetSocketAddress(host, port));
        System.out.println("Server started on " + host + ":" + port);
    }

    public void broadcast(String message, Socket sender) {
        for (Map.Entry<Socket, String> client : clients.entrySet()) {
            if (client.getKey() != sender) {
                try {
                    PrintWriter out = new PrintWriter(client.getKey().getOutputStream(), true);
                    out.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        client.getKey().close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    clients.remove(client.getKey());
                }
            }
        }
    }

    public void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            String username = in.readLine();
            synchronized (clients) {
                if (clients.containsValue(username)) {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println("Username already taken. Disconnecting...");
                    client.close();
                    return;
                } else {
                    clients.put(client, username);
                }
            }

            String welcomeMessage = username + " has joined the chat!";
            broadcast(welcomeMessage, client);
            System.out.println(welcomeMessage);

            String message;
            while ((message = in.readLine()) != null) {
                String formattedMessage = username + ": " + message;
                broadcast(formattedMessage, client);
                System.out.println(formattedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                String username = clients.get(client);
                String goodbyeMessage = username + " has left the chat!";
                System.out.println(goodbyeMessage);
                broadcast(goodbyeMessage, client);
                clients.remove(client);
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        System.out.println("Server running...");
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (IOException e) {
                if (!running) {
                    System.out.println("Server shut down.");
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    public void shutdown() {
        running = false;
        try {
            serverSocket.close();
            for (Socket client : clients.keySet()) {
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer("127.0.0.1", 12345);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.shutdown();
        }));

        server.run();
    }
}
