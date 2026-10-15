
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    public static final int PORT = 5000;

    static final Map<String, ClientHandler> clients =
            new ConcurrentHashMap<>();

    // Room ID -> room details
    static final Map<String, ChatRoom> rooms =
            new ConcurrentHashMap<>();

    private static final SecureRandom random = new SecureRandom();

    static class ChatRoom {
        final String roomId;
        final String password;
        final Set<String> members = ConcurrentHashMap.newKeySet();

        final Map<String, String> messageOwners =
            new ConcurrentHashMap<>();

        ChatRoom(String roomId, String password) {
            this.roomId = roomId;
            this.password = password;
        }
    }

    public static String generateRoomId() {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder id = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            id.append(characters.charAt(
                    random.nextInt(characters.length())));
        }

        return id.toString();
    }

    public static String generatePassword() {
        String characters =
                "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$";

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            password.append(characters.charAt(
                    random.nextInt(characters.length())));
        }

        return password.toString();
    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    public static void sendOnlineUsers() {
        broadcast("ONLINE|" + String.join(",", clients.keySet()));
    }

    public static void main(String[] args) {
        System.out.println("==================================");
        System.out.println("       LINKORA CHAT SERVER");
        System.out.println("==================================");

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println(
                    "Linkora server started on port " + PORT);

            while (true) {
                Socket socket = server.accept();

                System.out.println("Client connected: "
                        + socket.getRemoteSocketAddress());

                new Thread(new ClientHandler(socket)).start();
            }

        } catch (IOException e) {
            System.out.println("Server stopped: " + e.getMessage());
        }
    }
}
