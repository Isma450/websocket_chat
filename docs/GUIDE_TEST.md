# Guide de Test de l'Application

## Test Rapide

### 1. Démarrage

```bash
# Terminal 1 - Lancement du serveur
java ChatServer

# Terminal 2 - Lancement du premier client
java ChatClientGUI

# Terminal 3 - Lancement du deuxième client
java ChatClientGUI
```

### 2. Inscription et Connexion

- Client 1 : S'inscrire avec "Alice" / "password123"
- Client 2 : S'inscrire avec "Bob" / "password456"

### 3. Tests de Fonctionnalités

#### Chat Public

- Alice : "Bonjour tout le monde!"
- Bob : "Salut Alice!"

#### Message Privé

- Alice : "/w Bob Comment ça va?"
- Bob : "/w Alice Très bien, merci!"

#### Groupe

- Alice : "/creategroup Projet"
- Bob : "/joingroup Projet"
- Alice : "/groupmsg Projet Réunion demain à 14h"

#### Conférence

- Alice : "/createconference Reunion_Equipe"
- Alice : "/inviteconf Reunion_Equipe Bob"
- Bob : "/confmsg Reunion_Equipe Parfait, je serai là!"

#### Autres Commandes

- "/list" : Voir tous les utilisateurs
- "/help" : Aide complète

### 4. Interface Graphique

- Double-clic sur "Bob (en ligne)" pour ouvrir chat privé
- Utilisation des boutons "Groupes" et "Conférences"
- Touche ENTRÉE pour envoyer les messages
- Bouton "Déconnecter" pour fermer proprement

### 5. Persistance

- Fermer tous les clients
- Relancer : les comptes sont sauvegardés
- L'historique des messages est conservé
