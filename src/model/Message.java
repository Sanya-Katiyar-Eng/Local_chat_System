
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Message {
    private final String id;
    private final String sender;
    private final String recipient;
    private final String room;
    private final String content;
    private final LocalDateTime timestamp;
    private final String type;

    public Message(String sender, String recipient, String room,
                   String content, String type) {
        this.id = UUID.randomUUID().toString();
        this.sender = sender;
        this.recipient = recipient;
        this.room = room;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getRoom() {
        return room;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }
}