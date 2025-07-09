package src.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Classe representant une session client connectee au serveur.
 * Gere la communication bidirectionnelle avec un client specifique.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private final BlockingQueue<String> messageQueue;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.messageQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            if (authenticate()) {
                server.addClient(username, this);
                server.log("Client connecté: " + username + " (" + socket.getInetAddress() + ")");
                
                new Thread(this::sendMessages).start();
                
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("/disconnect")) {
                        break;
                    }
                    server.processMessage(username, message);
                }
            }
        } catch (IOException e) {
            server.log("Erreur avec le client " + username + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private boolean authenticate() throws IOException {
        out.println("WELCOME");
        String response = in.readLine();
        
        if (response == null) return false;
        
        if (response.startsWith("LOGIN:")) {
            String[] parts = response.split(":", 3);
            if (parts.length == 3) {
                String user = parts[1];
                String pass = parts[2];
                if (server.authenticateUser(user, pass)) {
                    username = user;
                    out.println("LOGIN_SUCCESS");
                    return true;
                } else {
                    out.println("LOGIN_FAILED");
                    return false;
                }
            }
        } else if (response.startsWith("REGISTER:")) {
            String[] parts = response.split(":", 3);
            if (parts.length == 3) {
                String user = parts[1];
                String pass = parts[2];
                if (server.registerUser(user, pass)) {
                    username = user;
                    out.println("REGISTER_SUCCESS");
                    return true;
                } else {
                    out.println("REGISTER_FAILED");
                    return false;
                }
            }
        }
        
        out.println("AUTH_FAILED");
        return false;
    }

    private void sendMessages() {
        try {
            while (!socket.isClosed()) {
                String message = messageQueue.take();
                out.println(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void sendMessage(String message) {
        messageQueue.offer(message);
    }

    public String getUsername() {
        return username;
    }

    public String getClientInfo() {
        return username + " (" + socket.getInetAddress() + ":" + socket.getPort() + ")";
    }

    private void cleanup() {
        try {
            if (username != null) {
                server.removeClient(username);
                server.log("Client déconnecté: " + username);
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            server.log("Erreur lors de la fermeture: " + e.getMessage());
        }
    }
}
