@echo off
title Application de Chat Client-Serveur

:menu
cls
echo =======================================
echo  APPLICATION DE CHAT CLIENT-SERVEUR
echo =======================================
echo.
echo 1. Lancer le serveur
echo 2. Lancer le client
echo 3. Compiler le projet
echo 4. Quitter
echo.
set /p choice="Votre choix (1-4): "

if "%choice%"=="1" goto server
if "%choice%"=="2" goto client
if "%choice%"=="3" goto compile
if "%choice%"=="4" goto exit
echo Choix invalide!
pause
goto menu

:server
echo Lancement du serveur de chat...
java ChatServer
pause
goto menu

:client
echo Lancement du client de chat...
java ChatClientGUI
pause
goto menu

:compile
echo Compilation du projet...
javac *.java
if %errorlevel%==0 (
    echo Compilation reussie!
) else (
    echo Erreur de compilation!
)
pause
goto menu

:exit
echo Au revoir!
pause
exit
