# Application de Chat Client-Serveur en Java

## Description

Cette application implemente un systeme de chat client-serveur complet en Java avec des interfaces graphiques Swing. Elle permet a plusieurs utilisateurs de communiquer entre eux via differentes modalites : messages publics, messages prives, groupes et conferences.

## Fonctionnalites

### Authentification et Gestion des Utilisateurs

- Inscription de nouveaux utilisateurs avec nom d'utilisateur et mot de passe
- Connexion securisee des utilisateurs existants
- Stockage persistant des comptes utilisateurs
- Affichage des utilisateurs connectes en temps reel

### Communication

- **Messages publics** : Diffusion de messages a tous les utilisateurs connectes
- **Messages prives** : Communication directe entre deux utilisateurs
- **Groupes de discussion** : Creation et gestion de groupes avec ajout/suppression de membres
- **Conferences** : Sessions de chat moderees avec gestion des participants par le createur

### Interface Utilisateur

- **Cote Serveur** :

  - Affichage des connexions clients avec nom et adresse IP
  - Separation visuelle par client
  - Log detaille des activites
  - Statistiques en temps reel
  - Interface de gestion des utilisateurs connectes

- **Cote Client** :
  - Interface intuitive avec onglets pour conversations multiples
  - Bouton "Envoyer" reactif a la touche ENTREE
  - Bouton "Deconnecter" pour fermeture propre
  - Liste des utilisateurs avec double-clic pour chat prive
  - Horodatage automatique des messages
  - Dialogs dedies pour gestion des groupes et conferences

### Fonctionnalites Avancees

- **Stockage des messages** : Historique persistant de toutes les conversations
- **Commandes specialisees** :
  - `/w <utilisateur> <message>` : Message prive
  - `/creategroup <nom>` : Creation de groupe
  - `/joingroup <nom>` : Rejoindre un groupe
  - `/groupmsg <groupe> <message>` : Message de groupe
  - `/createconference <nom>` : Creation de conference
  - `/inviteconf <conference> <utilisateur>` : Invitation a une conference
  - `/confmsg <conference> <message>` : Message de conference
  - `/list` : Liste des utilisateurs
  - `/help` : Aide des commandes

## Architecture

### Classes Principales

#### Serveur

- **ChatServer** : Serveur principal avec interface graphique et gestion des connexions
- **ClientHandler** : Gestionnaire individuel pour chaque client connecte
- **User** : Representation d'un utilisateur avec authentification
- **Message** : Structure des messages avec horodatage et types
- **ChatGroup** : Gestion des groupes de discussion
- **Conference** : Gestion des conferences avec moderation

#### Client

- **ChatClientGUI** : Interface graphique complete du client avec toutes les fonctionnalites

## Installation et Utilisation

### Prerequis

- Java 8 ou superieur
- Acces reseau pour la communication client-serveur

### Compilation

```bash
javac *.java
```

### Lancement

#### Demarrage du Serveur

```bash
java ChatServer
```

Le serveur se lance automatiquement sur le port 12345 et affiche son interface de gestion.

#### Lancement du Client

```bash
java ChatClientGUI
```

Une boite de dialogue d'authentification apparait permettant :

- La connexion avec un compte existant
- L'inscription d'un nouveau compte

### Utilisation

#### Premiere Connexion

1. Lancer le serveur
2. Lancer un ou plusieurs clients
3. Creer un compte via "S'inscrire" ou se connecter avec un compte existant
4. Commencer a chatter

#### Chat Public

- Taper directement dans le champ de message et appuyer sur ENTREE
- Le message est diffuse a tous les utilisateurs connectes

#### Chat Prive

- Double-cliquer sur un utilisateur en ligne dans la liste
- Un nouvel onglet s'ouvre pour la conversation privee
- Ou utiliser la commande `/w <utilisateur> <message>`

#### Groupes

- Utiliser le bouton "Groupes" pour acceder aux options
- Creer un groupe, le rejoindre ou envoyer des messages

#### Conferences

- Utiliser le bouton "Conferences" pour la gestion
- Le createur devient automatiquement moderateur
- Possibilite d'inviter et retirer des participants

## Stockage des Donnees

L'application stocke automatiquement :

- **users.dat** : Base de donnees des utilisateurs enregistres
- **messages.dat** : Historique complet des messages

Ces fichiers sont crees automatiquement au premier lancement et permettent la persistance des donnees entre les sessions.

## Securite

- Mots de passe stockes en clair (pour simplification - en production, utiliser un hachage)
- Verification d'unicite des noms d'utilisateur
- Gestion des connexions multiples
- Protection contre les noms d'utilisateur vides ou invalides

## Limitations Connues

- Connexion locale uniquement (localhost) par defaut
- Pas de chiffrement des communications
- Interface en francais uniquement
- Limite theorique du nombre de connexions simultanees selon les ressources systeme

## Extension Possible

L'architecture modulaire permet facilement d'ajouter :

- Chiffrement des communications
- Interface web
- Notifications push
- Transfert de fichiers
- Emojis et formatage des messages
- Moderation avancee
- Sauvegarde dans base de donnees externe
