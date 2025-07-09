# Makefile pour l'application de chat client-serveur

# Variables
SRCDIR = src
BINDIR = bin
DATADIR = data

# Cibles principales
.PHONY: all clean compile server client help

all: compile

# Compilation de tous les fichiers Java
compile:
	@echo "Compilation du projet..."
	@mkdir -p $(BINDIR)
	javac -d $(BINDIR) -cp . $(SRCDIR)/**/*.java
	@echo "Compilation terminée!"

# Lancement du serveur
server: compile
	@echo "Lancement du serveur..."
	java -cp $(BINDIR) src.server.ChatServer

# Lancement du client
client: compile
	@echo "Lancement du client..."
	java -cp $(BINDIR) src.client.ChatClientGUI

# Nettoyage des fichiers compilés
clean:
	@echo "Nettoyage des fichiers compilés..."
	rm -rf $(BINDIR)/*
	@echo "Nettoyage terminé!"

# Création des dossiers de données s'ils n'existent pas
setup:
	@mkdir -p $(DATADIR)
	@mkdir -p $(BINDIR)
	@echo "Structure de dossiers créée!"

# Affichage de l'aide
help:
	@echo "Makefile pour l'application de chat client-serveur"
	@echo ""
	@echo "Commandes disponibles:"
	@echo "  make compile  - Compiler le projet"
	@echo "  make server   - Lancer le serveur"
	@echo "  make client   - Lancer le client"
	@echo "  make clean    - Nettoyer les fichiers compilés"
	@echo "  make setup    - Créer la structure de dossiers"
	@echo "  make help     - Afficher cette aide"
