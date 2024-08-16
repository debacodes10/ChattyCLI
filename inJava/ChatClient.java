import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ChatClient(String host, int port, String username) throws IOException {
        this.socket = new Socket(host, port);
        this.username = username;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);

        // Send username to server
        out.println(username);

        // Start a thread to read messages from the server
        new Thread(new IncomingReader()).start();
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter your username: ");
        String username = consoleReader.readLine();

        ChatClient client = new ChatClient("127.0.0.1", 12345, username);
        System.out.println("Enter 'exit' to leave the chat");

        String message;
        while ((message = consoleReader.readLine()) != null) {
            if (message.equalsIgnoreCase("exit")) {
                client.socket.close();
                break;
            }
            client.sendMessage(message);
        }
    }
}
