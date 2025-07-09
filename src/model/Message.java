package src.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe representant un message dans le systeme de chat.
 * Contient l'expediteur, le destinataire, le contenu et l'horodatage du message.
 */
public class Message implements Serializable {
    private final String sender;
    private final String receiver;
    private final String content;
    private final LocalDateTime timestamp;
    private final MessageType type;

    public enum MessageType {
        PRIVATE, GROUP, CONFERENCE, SYSTEM
    }

    public Message(String sender, String receiver, String content, MessageType type) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getFormattedTimestamp(), sender, content);
    }
}
