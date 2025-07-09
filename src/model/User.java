package src.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Classe representant un utilisateur du systeme de chat.
 * Gere les informations de base d'un utilisateur comme le nom d'utilisateur et le mot de passe.
 */
public class User implements Serializable {
    private final String username;
    private final String password;
    private boolean isOnline;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.isOnline = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return username + (isOnline ? " (en ligne)" : " (hors ligne)");
    }
}
