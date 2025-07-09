package src.server;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import src.model.*;

/**
 * Classe principale du serveur de chat.
 * Gere les connexions clients, l'authentification, les messages et les fonctionnalites avancees.
 */
public class ChatServer extends JFrame {
    private static final int PORT = 12345;
    private static final String USERS_FILE = "data/users.dat";
    private static final String MESSAGES_FILE = "data/messages.dat";
    
    private JTextArea logArea;
    private JList<String> clientsList;
    private DefaultListModel<String> clientsModel;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private final List<Message> messageHistory = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, ChatGroup> groups = new ConcurrentHashMap<>();
    private final Map<String, Conference> conferences = new ConcurrentHashMap<>();

    public ChatServer() {
        initializeGUI();
        loadUsers();
        loadMessages();
        startServer();
    }

    private void initializeGUI() {
        setTitle("Serveur de Chat - Port " + PORT);
        setSize(800, 600);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(500, 400));
        
        clientsModel = new DefaultListModel<>();
        clientsList = new JList<>(clientsModel);
        clientsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane clientsScrollPane = new JScrollPane(clientsList);
        clientsScrollPane.setPreferredSize(new Dimension(250, 400));
        clientsScrollPane.setBorder(BorderFactory.createTitledBorder("Clients Connectés"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logScrollPane, clientsScrollPane);
        splitPane.setDividerLocation(500);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton clearLogButton = new JButton("Vider le Log");
        clearLogButton.addActionListener(e -> logArea.setText(""));
        
        JButton showStatsButton = new JButton("Statistiques");
        showStatsButton.addActionListener(e -> showStatistics());
        
        buttonPanel.add(clearLogButton);
        buttonPanel.add(showStatsButton);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setVisible(true);
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                isRunning = true;
                log("=== SERVEUR DE CHAT DÉMARRE ===");
                log("Écoute sur le port " + PORT);
                log("Utilisateurs enregistrés: " + users.size());
                
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        new Thread(new ClientHandler(clientSocket, this)).start();
                    } catch (IOException e) {
                        if (isRunning) {
                            log("Erreur d'acceptation client: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                log("Erreur de démarrage du serveur: " + e.getMessage());
            }
        }).start();
    }

    public synchronized void addClient(String username, ClientHandler handler) {
        connectedClients.put(username, handler);
        users.get(username).setOnline(true);
        
        SwingUtilities.invokeLater(() -> {
            clientsModel.addElement(handler.getClientInfo());
        });
        
        broadcastUserList();
        
        Message systemMessage = new Message("SYSTEM", "ALL", 
            username + " s'est connecté", Message.MessageType.SYSTEM);
        broadcastMessage(systemMessage, username);
    }

    public synchronized void removeClient(String username) {
        ClientHandler handler = connectedClients.remove(username);
        if (handler != null && users.containsKey(username)) {
            users.get(username).setOnline(false);
            
            SwingUtilities.invokeLater(() -> {
                clientsModel.removeElement(handler.getClientInfo());
            });
            
            broadcastUserList();
            
            Message systemMessage = new Message("SYSTEM", "ALL", 
                username + " s'est déconnecté", Message.MessageType.SYSTEM);
            broadcastMessage(systemMessage, username);
        }
    }

    public boolean authenticateUser(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }

    public boolean registerUser(String username, String password) {
        if (users.containsKey(username) || username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }
        
        User newUser = new User(username, password);
        users.put(username, newUser);
        saveUsers();
        log("Nouvel utilisateur enregistré: " + username);
        return true;
    }

    public void processMessage(String sender, String messageContent) {
        if (messageContent.startsWith("/")) {
            processCommand(sender, messageContent);
        } else {
            Message message = new Message(sender, "ALL", messageContent, Message.MessageType.PRIVATE);
            messageHistory.add(message);
            broadcastMessage(message, null);
            saveMessages();
        }
    }

    private void processCommand(String sender, String command) {
        String[] parts = command.split(" ", 3);
        String cmd = parts[0].toLowerCase();
        
        ClientHandler senderHandler = connectedClients.get(sender);
        if (senderHandler == null) return;

        switch (cmd) {
            case "/whisper":
            case "/w":
                if (parts.length >= 3) {
                    String recipient = parts[1];
                    String messageContent = parts[2];
                    sendPrivateMessage(sender, recipient, messageContent);
                } else {
                    senderHandler.sendMessage("SYSTEM:Format: /w <utilisateur> <message>");
                }
                break;
                
            case "/list":
                sendUserList(sender);
                break;
                
            case "/creategroup":
                if (parts.length >= 2) {
                    String groupName = parts[1];
                    createGroup(sender, groupName);
                } else {
                    senderHandler.sendMessage("SYSTEM:Format: /creategroup <nom>");
                }
                break;
                
            case "/joingroup":
                if (parts.length >= 2) {
                    String groupName = parts[1];
                    joinGroup(sender, groupName);
                } else {
                    senderHandler.sendMessage("SYSTEM:Format: /joingroup <nom>");
                }
                break;
                
            case "/groupmsg":
                if (parts.length >= 3) {
                    String groupName = parts[1];
                    String messageContent = parts[2];
                    sendGroupMessage(sender, groupName, messageContent);
                } else {
                    senderHandler.sendMessage("SYSTEM:Format: /groupmsg <groupe> <message>");
                }
                break;
                
            case "/createconference":
                if (parts.length >= 2) {
                    String conferenceName = parts[1];
                    createConference(sender, conferenceName);
                } else {
                    senderHandler.sendMessage("SYSTEM:Format: /createconference <nom>");
                }
                break;
                
            case "/inviteconf":
                if (parts.length >= 3) {
                    String conferenceName = parts[1];
                    String username = parts[2];
                    inviteToConference(sender, conferenceName, username);
                } else {
                    senderHandler.sendMessage("SYSTEM:Format: /inviteconf <conference> <utilisateur>");
                }
                break;
                
            case "/confmsg":
                if (parts.length >= 3) {
                    String conferenceName = parts[1];
                    String messageContent = parts[2];
                    sendConferenceMessage(sender, conferenceName, messageContent);
                } else {
                    senderHandler.sendMessage("SYSTEM:Format: /confmsg <conference> <message>");
                }
                break;
                
            case "/help":
                sendHelp(sender);
                break;
                
            default:
                senderHandler.sendMessage("SYSTEM:Commande inconnue. Tapez /help pour l'aide.");
        }
    }

    private void sendPrivateMessage(String sender, String recipient, String content) {
        ClientHandler recipientHandler = connectedClients.get(recipient);
        ClientHandler senderHandler = connectedClients.get(sender);
        
        if (recipientHandler != null) {
            Message message = new Message(sender, recipient, content, Message.MessageType.PRIVATE);
            messageHistory.add(message);
            
            recipientHandler.sendMessage("PRIVATE:" + sender + ":" + content + ":" + message.getFormattedTimestamp());
            senderHandler.sendMessage("PRIVATE_SENT:" + recipient + ":" + content + ":" + message.getFormattedTimestamp());
            
            log("Message privé: " + sender + " -> " + recipient);
            saveMessages();
        } else {
            senderHandler.sendMessage("SYSTEM:Utilisateur " + recipient + " non connecté.");
        }
    }

    private void createGroup(String creator, String groupName) {
        if (groups.containsKey(groupName)) {
            connectedClients.get(creator).sendMessage("SYSTEM:Le groupe " + groupName + " existe déjà.");
            return;
        }
        
        ChatGroup group = new ChatGroup(groupName, creator);
        groups.put(groupName, group);
        
        connectedClients.get(creator).sendMessage("SYSTEM:Groupe " + groupName + " créé avec succès.");
        log("Groupe créé: " + groupName + " par " + creator);
    }

    private void joinGroup(String username, String groupName) {
        ChatGroup group = groups.get(groupName);
        if (group != null) {
            group.addMember(username);
            connectedClients.get(username).sendMessage("SYSTEM:Vous avez rejoint le groupe " + groupName);
            
            for (String member : group.getMembers()) {
                ClientHandler memberHandler = connectedClients.get(member);
                if (memberHandler != null && !member.equals(username)) {
                    memberHandler.sendMessage("SYSTEM:" + username + " a rejoint le groupe " + groupName);
                }
            }
            log("Utilisateur " + username + " a rejoint le groupe " + groupName);
        } else {
            connectedClients.get(username).sendMessage("SYSTEM:Groupe " + groupName + " introuvable.");
        }
    }

    private void sendGroupMessage(String sender, String groupName, String content) {
        ChatGroup group = groups.get(groupName);
        if (group != null && group.isMember(sender)) {
            Message message = new Message(sender, groupName, content, Message.MessageType.GROUP);
            group.addMessage(message);
            messageHistory.add(message);
            
            for (String member : group.getMembers()) {
                ClientHandler memberHandler = connectedClients.get(member);
                if (memberHandler != null) {
                    memberHandler.sendMessage("GROUP:" + groupName + ":" + sender + ":" + content + ":" + message.getFormattedTimestamp());
                }
            }
            
            log("Message de groupe [" + groupName + "]: " + sender);
            saveMessages();
        } else {
            connectedClients.get(sender).sendMessage("SYSTEM:Vous n'êtes pas membre du groupe " + groupName);
        }
    }

    private void createConference(String moderator, String conferenceName) {
        if (conferences.containsKey(conferenceName)) {
            connectedClients.get(moderator).sendMessage("SYSTEM:La conférence " + conferenceName + " existe déjà.");
            return;
        }
        
        Conference conference = new Conference(conferenceName, moderator);
        conferences.put(conferenceName, conference);
        
        connectedClients.get(moderator).sendMessage("SYSTEM:Conférence " + conferenceName + " créée. Vous êtes le modérateur.");
        log("Conférence créée: " + conferenceName + " par " + moderator);
    }

    private void inviteToConference(String moderator, String conferenceName, String username) {
        Conference conference = conferences.get(conferenceName);
        if (conference != null && conference.isModerator(moderator)) {
            if (connectedClients.containsKey(username)) {
                conference.addParticipant(username);
                
                connectedClients.get(username).sendMessage("SYSTEM:Vous avez été invité à la conférence " + conferenceName);
                connectedClients.get(moderator).sendMessage("SYSTEM:" + username + " a été ajouté à la conférence.");
                
                log("Utilisateur " + username + " invité à la conférence " + conferenceName);
            } else {
                connectedClients.get(moderator).sendMessage("SYSTEM:Utilisateur " + username + " non connecté.");
            }
        } else {
            connectedClients.get(moderator).sendMessage("SYSTEM:Vous n'êtes pas modérateur de cette conférence ou elle n'existe pas.");
        }
    }

    private void sendConferenceMessage(String sender, String conferenceName, String content) {
        Conference conference = conferences.get(conferenceName);
        if (conference != null && conference.isParticipant(sender) && conference.isActive()) {
            Message message = new Message(sender, conferenceName, content, Message.MessageType.CONFERENCE);
            conference.addMessage(message);
            messageHistory.add(message);
            
            for (String participant : conference.getParticipants()) {
                ClientHandler participantHandler = connectedClients.get(participant);
                if (participantHandler != null) {
                    participantHandler.sendMessage("CONFERENCE:" + conferenceName + ":" + sender + ":" + content + ":" + message.getFormattedTimestamp());
                }
            }
            
            log("Message de conférence [" + conferenceName + "]: " + sender);
            saveMessages();
        } else {
            connectedClients.get(sender).sendMessage("SYSTEM:Vous n'participez pas à cette conférence ou elle est inactive.");
        }
    }

    private void sendUserList(String requester) {
        StringBuilder userList = new StringBuilder("USERLIST:");
        for (User user : users.values()) {
            userList.append(user.toString()).append(";");
        }
        connectedClients.get(requester).sendMessage(userList.toString());
    }

    private void sendHelp(String username) {
        ClientHandler handler = connectedClients.get(username);
        handler.sendMessage("SYSTEM:=== COMMANDES DISPONIBLES ===");
        handler.sendMessage("SYSTEM:/w <utilisateur> <message> - Message privé");
        handler.sendMessage("SYSTEM:/list - Liste des utilisateurs");
        handler.sendMessage("SYSTEM:/creategroup <nom> - Créer un groupe");
        handler.sendMessage("SYSTEM:/joingroup <nom> - Rejoindre un groupe");
        handler.sendMessage("SYSTEM:/groupmsg <groupe> <message> - Message de groupe");
        handler.sendMessage("SYSTEM:/createconference <nom> - Créer une conférence");
        handler.sendMessage("SYSTEM:/inviteconf <conference> <utilisateur> - Inviter à une conférence");
        handler.sendMessage("SYSTEM:/confmsg <conference> <message> - Message de conférence");
        handler.sendMessage("SYSTEM:/disconnect - Se déconnecter");
        handler.sendMessage("SYSTEM:/help - Afficher cette aide");
    }

    private void broadcastMessage(Message message, String excludeUser) {
        String formattedMessage = "BROADCAST:" + message.getSender() + ":" + message.getContent() + ":" + message.getFormattedTimestamp();
        
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (excludeUser == null || !entry.getKey().equals(excludeUser)) {
                entry.getValue().sendMessage(formattedMessage);
            }
        }
        
        log("[" + message.getFormattedTimestamp() + "] " + message.getSender() + ": " + message.getContent());
    }

    private void broadcastUserList() {
        StringBuilder userList = new StringBuilder("USERLIST:");
        for (User user : users.values()) {
            userList.append(user.toString()).append(";");
        }
        
        String listMessage = userList.toString();
        for (ClientHandler handler : connectedClients.values()) {
            handler.sendMessage(listMessage);
        }
    }

    private void showStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== STATISTIQUES DU SERVEUR ===\n");
        stats.append("Utilisateurs enregistrés: ").append(users.size()).append("\n");
        stats.append("Clients connectés: ").append(connectedClients.size()).append("\n");
        stats.append("Messages total: ").append(messageHistory.size()).append("\n");
        stats.append("Groupes créés: ").append(groups.size()).append("\n");
        stats.append("Conférences créées: ").append(conferences.size()).append("\n");
        stats.append("\n=== UTILISATEURS CONNECTÉS ===\n");
        
        for (String username : connectedClients.keySet()) {
            stats.append("- ").append(username).append("\n");
        }
        
        JTextArea statsArea = new JTextArea(stats.toString());
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(statsArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Statistiques du Serveur", JOptionPane.INFORMATION_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            Map<String, User> loadedUsers = (Map<String, User>) ois.readObject();
            users.putAll(loadedUsers);
            log("Utilisateurs chargés: " + users.size());
        } catch (FileNotFoundException e) {
            log("Fichier utilisateurs non trouvé, démarrage avec base vide.");
        } catch (IOException | ClassNotFoundException e) {
            log("Erreur lors du chargement des utilisateurs: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(new HashMap<>(users));
        } catch (IOException e) {
            log("Erreur lors de la sauvegarde des utilisateurs: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadMessages() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MESSAGES_FILE))) {
            List<Message> loadedMessages = (List<Message>) ois.readObject();
            messageHistory.addAll(loadedMessages);
            log("Messages chargés: " + messageHistory.size());
        } catch (FileNotFoundException e) {
            log("Fichier messages non trouvé, démarrage avec historique vide.");
        } catch (IOException | ClassNotFoundException e) {
            log("Erreur lors du chargement des messages: " + e.getMessage());
        }
    }

    private void saveMessages() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MESSAGES_FILE))) {
            oos.writeObject(new ArrayList<>(messageHistory));
        } catch (IOException e) {
            log("Erreur lors de la sauvegarde des messages: " + e.getMessage());
        }
    }

    public void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logMessage = "[" + timestamp + "] " + message;
        
        SwingUtilities.invokeLater(() -> {
            logArea.append(logMessage + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void shutdown() {
        log("Arrêt du serveur...");
        isRunning = false;
        
        for (ClientHandler handler : connectedClients.values()) {
            handler.sendMessage("SYSTEM:Le serveur va s'arrêter.");
        }
        
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Erreur lors de la fermeture du serveur: " + e.getMessage());
        }
        
        saveUsers();
        saveMessages();
        
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatServer());
    }
}
