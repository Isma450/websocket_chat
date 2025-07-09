#!/bin/bash

# Script de compilation et lancement pour l'application de chat avec structure de packages

echo "=== APPLICATION DE CHAT CLIENT-SERVEUR ==="
echo "Structure organisée avec packages Java"
echo
echo "1. Compiler le projet"
echo "2. Lancer le serveur"
echo "3. Lancer le client"
echo "4. Nettoyer les fichiers compilés"
echo "5. Afficher la structure du projet"
echo "6. Quitter"
echo

read -p "Votre choix (1-6): " choice

case $choice in
    1)
        echo "Compilation du projet avec structure de packages..."
        mkdir -p bin
        javac -d bin -cp . src/**/*.java
        if [ $? -eq 0 ]; then
            echo "Compilation réussie!"
            echo "Fichiers compilés dans le dossier bin/"
        else
            echo "Erreur de compilation!"
        fi
        ;;
    2)
        echo "Lancement du serveur de chat..."
        if [ ! -d "bin" ]; then
            echo "Projet non compilé. Compilation en cours..."
            mkdir -p bin
            javac -d bin -cp . src/**/*.java
        fi
        java -cp bin src.server.ChatServer
        ;;
    3)
        echo "Lancement du client de chat..."
        if [ ! -d "bin" ]; then
            echo "Projet non compilé. Compilation en cours..."
            mkdir -p bin
            javac -d bin -cp . src/**/*.java
        fi
        java -cp bin src.client.ChatClientGUI
        ;;
    4)
        echo "Nettoyage des fichiers compilés..."
        rm -rf bin/*
        echo "Fichiers compilés supprimés!"
        ;;
    5)
        echo "Structure du projet:"
        echo "socket_GUI/"
        echo "├── chat/"
        echo "│   ├── model/          # Classes de données (User, Message, ChatGroup, Conference)"
        echo "│   ├── server/         # Serveur et gestionnaire de clients"
        echo "│   └── client/         # Interface client"
        echo "├── bin/                # Fichiers compilés"
        echo "├── data/               # Fichiers de données et configuration"
        echo "├── docs/               # Documentation"
        echo "└── scripts/            # Scripts de lancement"
        echo
        ;;
    6)
        echo "Au revoir!"
        exit 0
        ;;
    *)
        echo "Choix invalide!"
        ;;
esac
