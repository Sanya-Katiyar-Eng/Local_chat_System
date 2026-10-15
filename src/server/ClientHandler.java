
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private volatile String currentRoom;

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm");

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(
                    socket.getOutputStream(), true);

            username = in.readLine();

            if (username == null
                    || !username.matches("[A-Za-z0-9_]{3,20}")) {
                sendMessage("ERROR|Invalid username.");
                return;
            }

            if (ChatServer.clients.putIfAbsent(username, this) != null) {
                sendMessage("ERROR|Username is already in use.");
                return;
            }

            ChatServer.sendOnlineUsers();
            sendMessage("SYSTEM|Welcome to Linkora, " + username + "!");

            String line;

            while ((line = in.readLine()) != null) {
                if ("LOGOUT".equals(line)) {
                    break;
                }

                processCommand(line);
            }

        } catch (IOException e) {
            System.out.println("Connection ended: " + username);

        } finally {
            leaveRoom();

            if (username != null) {
                ChatServer.clients.remove(username, this);
                ChatServer.sendOnlineUsers();
            }

            try {
                socket.close();
            } catch (IOException ignored) {
                // Connection already closed.
            }
        }
    }

    private void processCommand(String line) {

        // Create a private room.
        if ("CREATE_ROOM".equals(line)) {
            leaveRoom();

            String roomId;

            do {
                roomId = ChatServer.generateRoomId();
            } while (ChatServer.rooms.containsKey(roomId));

            String password = ChatServer.generatePassword();

            ChatServer.ChatRoom room =
                    new ChatServer.ChatRoom(roomId, password);

            room.members.add(username);
            ChatServer.rooms.put(roomId, room);
            currentRoom = roomId;

            sendMessage("ROOM_CREATED|" + roomId + "|" + password);
            return;
        }

        // Join an existing room.
        if (line.startsWith("JOIN_ROOM|")) {
            String[] parts = line.split("\\|", 3);

            if (parts.length != 3) {
                sendMessage("SYSTEM|Enter the Room ID and password.");
                return;
            }

            String roomId = parts[1].trim();
            String password = parts[2];

            ChatServer.ChatRoom room = ChatServer.rooms.get(roomId);

            if (room == null || !room.password.equals(password)) {
                sendMessage("SYSTEM|Invalid Room ID or password.");
                return;
            }

            leaveRoom();

            room.members.add(username);
            currentRoom = roomId;

            sendMessage("ROOM_JOINED|" + roomId);

            sendRoom(roomId,
                    "SYSTEM|" + username + " joined the room.");
            return;
        }

        // Typing indicator.
        if ("TYPING".equals(line)) {
            ChatServer.ChatRoom room = getCurrentRoom();

            if (room != null) {
                sendRoom(currentRoom, "TYPING|" + username);
            }
            return;
        }

        // Delete a message for everyone in the room.
        if (line.startsWith("DELETE_MSG|")) {
            String[] parts = line.split("\\|", 2);

            if (parts.length != 2 || currentRoom == null) {
                sendMessage("SYSTEM|Unable to delete this message.");
                return;
            }

            String messageId = parts[1];
            ChatServer.ChatRoom room = getCurrentRoom();

            if (room == null) {
                sendMessage("SYSTEM|You are not a member of this room.");
                return;
            }

            String owner = room.messageOwners.get(messageId);

            if (owner == null) {
                sendMessage(
                        "SYSTEM|Message was not found or was already deleted.");
                return;
            }

            if (!owner.equals(username)) {
                sendMessage(
                        "SYSTEM|You can delete only your own messages.");
                return;
            }

            // Only the actual sender can delete this message.
            if (room.messageOwners.remove(messageId, username)) {
                sendRoom(currentRoom, "MESSAGE_DELETED|" + messageId);
            }
            return;
        }

        // Send a message. Messages are not saved to disk or a database.
        if (line.startsWith("ROOM_MSG|")) {
            if (currentRoom == null) {
                sendMessage("SYSTEM|Create or join a room first.");
                return;
            }

            String text = line.substring("ROOM_MSG|".length()).trim();

            if (text.isEmpty()) {
                return;
            }

            ChatServer.ChatRoom room = getCurrentRoom();

            if (room == null) {
                sendMessage("SYSTEM|You are not a member of this room.");
                return;
            }

            String messageId = UUID.randomUUID().toString();
            String time = LocalTime.now().format(TIME);

            // Keep ownership only in server memory for deletion checks.
            room.messageOwners.put(messageId, username);

            String message = "ROOM_MESSAGE|"
                    + messageId + "|"
                    + username + "|"
                    + time + "|"
                    + text;

            sendRoom(currentRoom, message);
            return;
        }

        // Leave the current room.
        if ("LEAVE_ROOM".equals(line)) {
            leaveRoom();
            sendMessage("SYSTEM|You left the room.");
            return;
        }

        if (line.startsWith("MSG|")
                || line.startsWith("JOIN|")
                || line.startsWith("PRIVATE|")
                || "USERS".equals(line)) {
            sendMessage(
                    "SYSTEM|Create or join a private room to chat.");
        }
    }

    private ChatServer.ChatRoom getCurrentRoom() {
        if (currentRoom == null) {
            return null;
        }

        ChatServer.ChatRoom room = ChatServer.rooms.get(currentRoom);

        if (room == null || !room.members.contains(username)) {
            currentRoom = null;
            return null;
        }

        return room;
    }

    private void sendRoom(String roomId, String message) {
        ChatServer.ChatRoom room = ChatServer.rooms.get(roomId);

        if (room == null) {
            return;
        }

        for (String member : room.members) {
            ClientHandler client = ChatServer.clients.get(member);

            if (client != null) {
                client.sendMessage(message);
            }
        }
    }

    private void leaveRoom() {
        String oldRoom = currentRoom;

        if (oldRoom == null || username == null) {
            currentRoom = null;
            return;
        }

        ChatServer.ChatRoom room = ChatServer.rooms.get(oldRoom);

        if (room != null && room.members.remove(username)) {
            sendRoom(oldRoom,
                    "SYSTEM|" + username + " left the room.");
        }

        currentRoom = null;
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}







