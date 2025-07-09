#!/bin/bash

# Script de lancement pour l'application de chat

echo "=== APPLICATION DE CHAT CLIENT-SERVEUR ==="
echo "1. Lancer le serveur"
echo "2. Lancer le client"
echo "3. Compiler le projet"
echo "4. Quitter"
echo

read -p "Votre choix (1-4): " choice

case $choice in
    1)
        echo "Lancement du serveur de chat..."
        java ChatServer
        ;;
    2)
        echo "Lancement du client de chat..."
        java ChatClientGUI
        ;;
    3)
        echo "Compilation du projet..."
        javac *.java
        if [ $? -eq 0 ]; then
            echo "Compilation réussie!"
        else
            echo "Erreur de compilation!"
        fi
        ;;
    4)
        echo "Au revoir!"
        exit 0
        ;;
    *)
        echo "Choix invalide!"
        ;;
esac
