package src.client;

import src.model.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

/**
 * Interface graphique du client de chat.
 * Permet la connexion, l'inscription, l'envoi de messages et l'utilisation des fonctionnalites avancees.
 */
public class ChatClientGUI extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean isConnected = false;
    
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton disconnectButton;
    private JList<String> usersList;
    private DefaultListModel<String> usersModel;
    private JTabbedPane chatTabs;
    private JPanel mainChatPanel;

    public ChatClientGUI() {
        initializeGUI();
        showLoginDialog();
    }

    private void initializeGUI() {
        setTitle("Client de Chat");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        chatTabs = new JTabbedPane();
        mainChatPanel = createChatPanel();
        chatTabs.addTab("Chat Principal", mainChatPanel);
        
        usersModel = new DefaultListModel<>();
        usersList = new JList<>(usersModel);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selectedUser = usersList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.contains("(hors ligne)")) {
                        String user = selectedUser.split(" ")[0];
                        if (!user.equals(username)) {
                            openPrivateChat(user);
                        }
                    }
                }
            }
        });
        
        JScrollPane usersScrollPane = new JScrollPane(usersList);
        usersScrollPane.setPreferredSize(new Dimension(200, 0));
        usersScrollPane.setBorder(BorderFactory.createTitledBorder("Utilisateurs (Double-clic pour chat privé)"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatTabs, usersScrollPane);
        splitPane.setDividerLocation(580);
        
        JPanel bottomPanel = createBottomPanel();
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        chatArea.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        
        sendButton = new JButton("Envoyer");
        sendButton.addActionListener(e -> sendMessage());
        sendButton.setEnabled(false);
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        disconnectButton = new JButton("Déconnecter");
        disconnectButton.addActionListener(e -> disconnect());
        disconnectButton.setEnabled(false);
        
        JButton helpButton = new JButton("Aide");
        helpButton.addActionListener(e -> showHelp());
        
        JButton groupButton = new JButton("Groupes");
        groupButton.addActionListener(e -> showGroupDialog());
        
        JButton conferenceButton = new JButton("Conférences");
        conferenceButton.addActionListener(e -> showConferenceDialog());
        
        buttonPanel.add(disconnectButton);
        buttonPanel.add(helpButton);
        buttonPanel.add(groupButton);
        buttonPanel.add(conferenceButton);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Connexion au Chat", true);
        loginDialog.setSize(350, 200);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Nom d'utilisateur: "), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Mot de passe: "), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(passwordField, gbc);
        
        JButton loginButton = new JButton("Se connecter");
        JButton registerButton = new JButton("S'inscrire");
        
        ActionListener authAction = e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());
            
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(loginDialog, "Veuillez remplir tous les champs.");
                return;
            }
            
            boolean isLogin = e.getSource() == loginButton;
            if (authenticate(user, pass, isLogin)) {
                username = user;
                setTitle("Client de Chat - " + username);
                loginDialog.dispose();
                sendButton.setEnabled(true);
                disconnectButton.setEnabled(true);
                appendToChat("Connecté au serveur de chat!", Color.GREEN);
                requestUserList();
            } else {
                String message = isLogin ? "Échec de la connexion. Vérifiez vos identifiants." 
                                        : "Échec de l'inscription. Le nom d'utilisateur existe peut-être déjà.";
                JOptionPane.showMessageDialog(loginDialog, message);
            }
        };
        
        loginButton.addActionListener(authAction);
        registerButton.addActionListener(authAction);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);
        
        loginDialog.add(panel);
        loginDialog.setVisible(true);
    }

    private boolean authenticate(String username, String password, boolean isLogin) {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            String welcome = in.readLine();
            if (!"WELCOME".equals(welcome)) {
                return false;
            }
            
            String authCommand = (isLogin ? "LOGIN:" : "REGISTER:") + username + ":" + password;
            out.println(authCommand);
            
            String response = in.readLine();
            if ("LOGIN_SUCCESS".equals(response) || "REGISTER_SUCCESS".equals(response)) {
                isConnected = true;
                startMessageListener();
                return true;
            } else {
                socket.close();
                return false;
            }
        } catch (IOException e) {
            appendToChat("Erreur de connexion: " + e.getMessage(), Color.RED);
            return false;
        }
    }

    private void startMessageListener() {
        new Thread(() -> {
            try {
                String message;
                while (isConnected && (message = in.readLine()) != null) {
                    processServerMessage(message);
                }
            } catch (IOException e) {
                if (isConnected) {
                    appendToChat("Connexion perdue avec le serveur.", Color.RED);
                    disconnectButton.doClick();
                }
            }
        }).start();
    }

    private void processServerMessage(String message) {
        String[] parts = message.split(":", 4);
        String type = parts[0];
        
        switch (type) {
            case "SYSTEM":
                appendToChat("SYSTÈME: " + parts[1], Color.BLUE);
                break;
                
            case "BROADCAST":
                if (parts.length >= 4) {
                    String formattedMsg = String.format("[%s] %s: %s", parts[3], parts[1], parts[2]);
                    appendToChat(formattedMsg, Color.BLACK);
                }
                break;
                
            case "PRIVATE":
                if (parts.length >= 4) {
                    String formattedMsg = String.format("[%s] Message privé de %s: %s", parts[3], parts[1], parts[2]);
                    appendToChat(formattedMsg, Color.MAGENTA);
                    
                    openPrivateChat(parts[1]);
                    appendToPrivateChat(parts[1], formattedMsg, Color.MAGENTA);
                }
                break;
                
            case "PRIVATE_SENT":
                if (parts.length >= 4) {
                    String formattedMsg = String.format("[%s] Vous à %s: %s", parts[3], parts[1], parts[2]);
                    appendToPrivateChat(parts[1], formattedMsg, Color.DARK_GRAY);
                }
                break;
                
            case "GROUP":
                if (parts.length >= 5) {
                    String[] groupParts = message.split(":", 5);
                    String formattedMsg = String.format("[%s] [%s] %s: %s", 
                        groupParts[4], groupParts[1], groupParts[2], groupParts[3]);
                    appendToChat(formattedMsg, new Color(0, 128, 0));
                }
                break;
                
            case "CONFERENCE":
                if (parts.length >= 5) {
                    String[] confParts = message.split(":", 5);
                    String formattedMsg = String.format("[%s] [CONF:%s] %s: %s", 
                        confParts[4], confParts[1], confParts[2], confParts[3]);
                    appendToChat(formattedMsg, new Color(128, 0, 128));
                }
                break;
                
            case "USERLIST":
                updateUsersList(parts.length > 1 ? parts[1] : "");
                break;
        }
    }

    private void sendMessage() {
        if (!isConnected || messageField.getText().trim().isEmpty()) {
            return;
        }
        
        String message = messageField.getText().trim();
        out.println(message);
        
        if (!message.startsWith("/")) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String formattedMsg = String.format("[%s] %s: %s", timestamp, username, message);
            appendToChat(formattedMsg, Color.DARK_GRAY);
        }
        
        messageField.setText("");
        messageField.requestFocus();
    }

    private void disconnect() {
        if (isConnected) {
            out.println("/disconnect");
            isConnected = false;
            
            try {
                socket.close();
            } catch (IOException e) {
                // Ignorer les erreurs de fermeture
            }
            
            sendButton.setEnabled(false);
            disconnectButton.setEnabled(false);
            usersModel.clear();
            appendToChat("Déconnecté du serveur.", Color.RED);
            
            dispose();
        }
    }

    private void requestUserList() {
        if (isConnected) {
            out.println("/list");
        }
    }

    private void updateUsersList(String userListData) {
        SwingUtilities.invokeLater(() -> {
            usersModel.clear();
            if (!userListData.isEmpty()) {
                String[] users = userListData.split(";");
                for (String user : users) {
                    if (!user.trim().isEmpty()) {
                        usersModel.addElement(user.trim());
                    }
                }
            }
        });
    }

    private void openPrivateChat(String targetUser) {
        String tabTitle = "Privé: " + targetUser;
        
        for (int i = 0; i < chatTabs.getTabCount(); i++) {
            if (chatTabs.getTitleAt(i).equals(tabTitle)) {
                chatTabs.setSelectedIndex(i);
                return;
            }
        }
        
        JPanel privateChatPanel = createChatPanel();
        chatTabs.addTab(tabTitle, privateChatPanel);
        chatTabs.setSelectedIndex(chatTabs.getTabCount() - 1);
    }

    private void appendToPrivateChat(String targetUser, String message, Color color) {
        String tabTitle = "Privé: " + targetUser;
        
        for (int i = 0; i < chatTabs.getTabCount(); i++) {
            if (chatTabs.getTitleAt(i).equals(tabTitle)) {
                JPanel panel = (JPanel) chatTabs.getComponentAt(i);
                JScrollPane scrollPane = (JScrollPane) panel.getComponent(0);
                JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
                
                SwingUtilities.invokeLater(() -> {
                    textArea.append(message + "\n");
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                });
                break;
            }
        }
    }

    private void appendToChat(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void showHelp() {
        StringBuilder help = new StringBuilder();
        help.append("=== AIDE DU CLIENT DE CHAT ===\n\n");
        help.append("COMMANDES DISPONIBLES:\n");
        help.append("/w <utilisateur> <message> - Envoyer un message privé\n");
        help.append("/list - Afficher la liste des utilisateurs\n");
        help.append("/creategroup <nom> - Créer un nouveau groupe\n");
        help.append("/joingroup <nom> - Rejoindre un groupe existant\n");
        help.append("/groupmsg <groupe> <message> - Envoyer un message au groupe\n");
        help.append("/createconference <nom> - Créer une nouvelle conférence\n");
        help.append("/inviteconf <conference> <utilisateur> - Inviter un utilisateur à une conférence\n");
        help.append("/confmsg <conference> <message> - Envoyer un message à la conférence\n");
        help.append("/disconnect - Se déconnecter\n");
        help.append("/help - Afficher cette aide\n\n");
        help.append("FONCTIONNALITÉS:\n");
        help.append("• Double-cliquez sur un utilisateur en ligne pour ouvrir un chat privé\n");
        help.append("• Utilisez la touche ENTRÉE pour envoyer des messages\n");
        help.append("• Les messages sont horodatés automatiquement\n");
        help.append("• Gérez les groupes et conférences via les boutons dédiés\n");
        
        JTextArea helpArea = new JTextArea(help.toString());
        helpArea.setEditable(false);
        helpArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(helpArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Aide", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showGroupDialog() {
        String[] options = {"Créer un groupe", "Rejoindre un groupe", "Envoyer un message au groupe"};
        int choice = JOptionPane.showOptionDialog(this, "Que voulez-vous faire?", "Gestion des Groupes",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        
        switch (choice) {
            case 0:
                String groupName = JOptionPane.showInputDialog(this, "Nom du groupe à créer:");
                if (groupName != null && !groupName.trim().isEmpty()) {
                    out.println("/creategroup " + groupName.trim());
                }
                break;
            case 1:
                String groupToJoin = JOptionPane.showInputDialog(this, "Nom du groupe à rejoindre:");
                if (groupToJoin != null && !groupToJoin.trim().isEmpty()) {
                    out.println("/joingroup " + groupToJoin.trim());
                }
                break;
            case 2:
                String group = JOptionPane.showInputDialog(this, "Nom du groupe:");
                if (group != null && !group.trim().isEmpty()) {
                    String groupMessage = JOptionPane.showInputDialog(this, "Message à envoyer au groupe " + group + ":");
                    if (groupMessage != null && !groupMessage.trim().isEmpty()) {
                        out.println("/groupmsg " + group.trim() + " " + groupMessage);
                    }
                }
                break;
        }
    }

    private void showConferenceDialog() {
        String[] options = {"Créer une conférence", "Inviter à une conférence", "Envoyer un message à la conférence"};
        int choice = JOptionPane.showOptionDialog(this, "Que voulez-vous faire?", "Gestion des Conférences",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        
        switch (choice) {
            case 0:
                String confName = JOptionPane.showInputDialog(this, "Nom de la conférence à créer:");
                if (confName != null && !confName.trim().isEmpty()) {
                    out.println("/createconference " + confName.trim());
                }
                break;
            case 1:
                String conference = JOptionPane.showInputDialog(this, "Nom de la conférence:");
                if (conference != null && !conference.trim().isEmpty()) {
                    String userToInvite = JOptionPane.showInputDialog(this, "Nom d'utilisateur à inviter:");
                    if (userToInvite != null && !userToInvite.trim().isEmpty()) {
                        out.println("/inviteconf " + conference.trim() + " " + userToInvite.trim());
                    }
                }
                break;
            case 2:
                String conf = JOptionPane.showInputDialog(this, "Nom de la conférence:");
                if (conf != null && !conf.trim().isEmpty()) {
                    String confMessage = JOptionPane.showInputDialog(this, "Message à envoyer à la conférence " + conf + ":");
                    if (confMessage != null && !confMessage.trim().isEmpty()) {
                        out.println("/confmsg " + conf.trim() + " " + confMessage);
                    }
                }
                break;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI().setVisible(true));
    }
}
