package src.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Classe representant un groupe de chat.
 * Permet la gestion des membres et des messages de groupe.
 */
public class ChatGroup {
    private final String name;
    private final String creator;
    private final List<String> members;
    private final List<Message> messages;

    public ChatGroup(String name, String creator) {
        this.name = name;
        this.creator = creator;
        this.members = new CopyOnWriteArrayList<>();
        this.messages = new CopyOnWriteArrayList<>();
        this.members.add(creator);
    }

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public List<String> getMembers() {
        return new ArrayList<>(members);
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void addMember(String username) {
        if (!members.contains(username)) {
            members.add(username);
        }
    }

    public void removeMember(String username) {
        if (!username.equals(creator)) {
            members.remove(username);
        }
    }

    public boolean isMember(String username) {
        return members.contains(username);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public boolean isCreator(String username) {
        return creator.equals(username);
    }

    @Override
    public String toString() {
        return name + " (" + members.size() + " membres)";
    }
}
