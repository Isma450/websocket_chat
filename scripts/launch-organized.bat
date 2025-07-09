@echo off
title Application de Chat Client-Serveur - Structure Organisee

:menu
cls
echo =======================================
echo  APPLICATION DE CHAT CLIENT-SERVEUR
echo =======================================
echo Structure organisee avec packages Java
echo.
echo 1. Compiler le projet
echo 2. Lancer le serveur
echo 3. Lancer le client
echo 4. Nettoyer les fichiers compiles
echo 5. Afficher la structure du projet
echo 6. Quitter
echo.
set /p choice="Votre choix (1-6): "

if "%choice%"=="1" goto compile
if "%choice%"=="2" goto server
if "%choice%"=="3" goto client
if "%choice%"=="4" goto clean
if "%choice%"=="5" goto structure
if "%choice%"=="6" goto exit
echo Choix invalide!
pause
goto menu

:compile
echo Compilation du projet avec structure de packages...
if not exist "bin" mkdir bin
javac -d bin -cp . chat/**/*.java
if %errorlevel%==0 (
    echo Compilation reussie!
    echo Fichiers compiles dans le dossier bin/
) else (
    echo Erreur de compilation!
)
pause
goto menu

:server
echo Lancement du serveur de chat...
if not exist "bin" (
    echo Projet non compile. Compilation en cours...
    if not exist "bin" mkdir bin
    javac -d bin -cp . chat/**/*.java
)
)
java -cp bin chat.server.ChatServer
pause
goto menu

:client
echo Lancement du client de chat...
if not exist "bin" (
    echo Projet non compile. Compilation en cours...
    if not exist "bin" mkdir bin
    javac -d bin -cp . chat/**/*.java
)
java -cp bin chat.client.ChatClientGUI
pause
goto menu

:clean
echo Nettoyage des fichiers compiles...
if exist "bin" rmdir /s /q bin
mkdir bin
echo Fichiers compiles supprimes!
pause
goto menu

:structure
echo Structure du projet:
echo socket_GUI/
echo │   ├── src/
echo │   │   ├── model/          # Classes de donnees (User, Message, ChatGroup, Conference)
echo │   │   ├── server/         # Serveur et gestionnaire de clients
echo │   │   └── client/         # Interface client
echo │   ├── bin/                # Fichiers compiles
echo │   ├── data/               # Fichiers de donnees et configuration
echo │   ├── docs/               # Documentation
echo │   └── scripts/            # Scripts de lancement
pause
goto menu

:exit
echo Au revoir!
pause
exit
