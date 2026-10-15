
package client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread readerThread;

    public void connect(String username, Consumer<String> listener)
            throws IOException {

        socket = new Socket("localhost", 5000);

        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        out = new PrintWriter(socket.getOutputStream(), true);

        out.println(username);

        String response = in.readLine();

        if (response == null || response.startsWith("ERROR|")) {
            socket.close();

            throw new IOException(response == null
                    ? "Server closed the connection."
                    : response.substring("ERROR|".length()));
        }

        listener.accept(response);

        readerThread = new Thread(() -> {
            try {
                String line;

                while ((line = in.readLine()) != null) {
                    listener.accept(line);
                }

            } catch (IOException e) {
                if (socket != null && !socket.isClosed()) {
                    listener.accept("SYSTEM|Disconnected from server.");
                }
            }
        });

        readerThread.setDaemon(true);
        readerThread.start();
    }

    // Create a password-protected chat room.
    public void createRoom() {
        send("CREATE_ROOM");
    }

    // Join a room using its ID and password.
    public void joinRoom(String roomId, String password) {
        send("JOIN_ROOM|" + roomId + "|" + password);
    }

    // Send a message to the current room only.
    public void sendRoomMessage(String text) {
        send("ROOM_MSG|" + text);
    }
    // Request deletion of your own message.
    public void deleteRoomMessage(String messageId) {
        send("DELETE_MSG|" + messageId);
}

     public void sendTypingStatus() {
        send("TYPING");
    }

    // Leave the current room.
    public void leaveRoom() {
        send("LEAVE_ROOM");
    }

    private void send(String message) {
        if (out != null && socket != null && !socket.isClosed()) {
            out.println(message);
        }
    }

    public void disconnect() {
        send("LOGOUT");

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
            // Connection may already be closed.
        }
    }
}
