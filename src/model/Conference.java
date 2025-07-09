package src.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Classe representant une conference (chat entre plusieurs utilisateurs).
 * Permet a un moderateur de gerer les participants d'une conference.
 */
public class Conference {
    private final String name;
    private final String moderator;
    private final List<String> participants;
    private final List<Message> messages;
    private boolean isActive;

    public Conference(String name, String moderator) {
        this.name = name;
        this.moderator = moderator;
        this.participants = new CopyOnWriteArrayList<>();
        this.messages = new CopyOnWriteArrayList<>();
        this.participants.add(moderator);
        this.isActive = true;
    }

    public String getName() {
        return name;
    }

    public String getModerator() {
        return moderator;
    }

    public List<String> getParticipants() {
        return new ArrayList<>(participants);
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void addParticipant(String username) {
        if (!participants.contains(username) && isActive) {
            participants.add(username);
        }
    }

    public void removeParticipant(String username) {
        if (!username.equals(moderator) && isActive) {
            participants.remove(username);
        }
    }

    public boolean isParticipant(String username) {
        return participants.contains(username);
    }

    public boolean isModerator(String username) {
        return moderator.equals(username);
    }

    public void addMessage(Message message) {
        if (isActive) {
            messages.add(message);
        }
    }

    @Override
    public String toString() {
        return name + " (Modérateur: " + moderator + ", " + participants.size() + " participants)";
    }
}
