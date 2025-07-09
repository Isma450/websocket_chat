# Application de Chat Client-Serveur Java

Application de chat multi-utilisateurs avec interface graphique, organisée selon les bonnes pratiques Java.

## Structure du Projet

```
socket_GUI/
├── src/                    # Code source
│   ├── model/             # Classes de données
│   │   ├── User.java      # Gestion des utilisateurs
│   │   ├── Message.java   # Structure des messages
│   │   ├── ChatGroup.java # Groupes de discussion
│   │   └── Conference.java # Conférences modérées
│   ├── server/            # Serveur
│   │   ├── ChatServer.java    # Serveur principal avec GUI
│   │   └── ClientHandler.java # Gestionnaire de clients
│   └── client/            # Client
│       └── ChatClientGUI.java # Interface client complète
├── bin/                   # Fichiers compilés (.class)
├── data/                  # Données persistantes
│   ├── users.dat         # Base utilisateurs
│   ├── messages.dat      # Historique messages
│   └── config.properties # Configuration
├── docs/                  # Documentation
│   ├── README.md         # Documentation détaillée
│   └── GUIDE_TEST.md     # Guide de test
├── scripts/              # Scripts de lancement
│   ├── launch-organized.sh   # Script Linux/Mac
│   └── launch-organized.bat  # Script Windows
└── Makefile              # Compilation automatisée
```

## Compilation et Lancement

### Méthode 1: Makefile (recommandé)

```bash
make compile    # Compiler le projet
make server     # Lancer le serveur
make client     # Lancer le client
make clean      # Nettoyer
make help       # Aide
```

### Méthode 2: Scripts

```bash
# Linux/Mac
./scripts/launch-organized.sh

# Windows
scripts\launch-organized.bat
```

### Méthode 3: Manuelle

```bash
# Compilation
cd src
javac -d ../bin model/*.java server/*.java client/*.java

# Lancement serveur
cd bin
java src.server.ChatServer

# Lancement client
cd bin
java src.client.ChatClientGUI
```

## Fonctionnalités

### Fonctionnalités de Base (Cahier des Charges)

- **Authentification complète**: Inscription ET connexion sécurisée avec interface dédiée
- **Interface graphique Swing**: Client et serveur avec interfaces modernes
- **Gestion multi-clients**: Support de nombreux utilisateurs simultanés
- **Chat public**: Messages diffusés à tous les connectés
- **Affichage serveur**: Nom et adresse IP de chaque client connecté
- **Contrôles client**: Boutons Envoyer/Déconnecter + touche ENTER
- **Horodatage automatique**: Tous les messages sont datés

### Fonctionnalités Avancées

- **Messages privés**: Communication directe entre utilisateurs (double-clic)
- **Groupes**: Création et gestion de groupes de discussion
- **Conférences**: Sessions modérées avec gestion des participants
- **Interface à onglets**: Chat principal + onglets privés multiples
- **Persistance**: Sauvegarde automatique des utilisateurs et messages
- **Commandes étendues**: Plus de 10 commandes de chat disponibles

### Inscription des Utilisateurs - COMPLÈTEMENT IMPLÉMENTÉE

- **Interface d'inscription**: Dialog avec champs nom d'utilisateur/mot de passe
- **Validation côté client**: Contrôle des champs obligatoires
- **Traitement serveur**: Vérification d'unicité et sauvegarde
- **Persistance**: Stockage sécurisé dans `data/users.dat`
- **Feedback utilisateur**: Messages de confirmation/erreur appropriés

## Architecture

Le projet suit une architecture modulaire avec séparation claire des responsabilités:

- **Package model**: Classes de données (User, Message, ChatGroup, Conference)
- **Package server**: Logique serveur et gestion des connexions
- **Package client**: Interface utilisateur et logique client

## Démarrage Rapide

1. **Compiler**: `make compile`
2. **Serveur**: `make server` (dans un terminal)
3. **Client(s)**: `make client` (dans un autre terminal, répéter pour plusieurs clients)
4. **Créer un compte** via l'interface de connexion
5. **Commencer à chatter**!

## Commandes Chat

- `/w <user> <message>` - Message privé
- `/creategroup <nom>` - Créer un groupe
- `/joingroup <nom>` - Rejoindre un groupe
- `/groupmsg <groupe> <message>` - Message de groupe
- `/createconference <nom>` - Créer une conférence
- `/inviteconf <conf> <user>` - Inviter en conférence
- `/confmsg <conf> <message>` - Message de conférence
- `/list` - Liste des utilisateurs
- `/help` - Aide complète

## Documentation

Consultez `docs/README.md` pour la documentation complète et `docs/GUIDE_TEST.md` pour les scénarios de test.

## Technologie

- **Java 8+** avec Swing pour l'interface graphique
- **Sockets TCP** pour la communication réseau
- **Threads** pour la gestion concurrente des clients
- **Sérialisation Java** pour la persistance des données
