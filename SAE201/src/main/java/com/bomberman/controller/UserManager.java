package com.bomberman.controller;

import com.bomberman.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire des utilisateurs (Singleton)
 * Version modifiée pour utiliser le dossier resources/donnees/
 */
public class UserManager {

    // ===== SINGLETON =====
    private static UserManager instance;

    // ===== STOCKAGE =====
    private List<User> users;
    private User currentUser;

    /**
     * Chemin du fichier de sauvegarde dans les ressources
     */
    private static final String SAVE_FILE = "SAE201/src/main/resources/donnees/users.json";

    private final Gson gson;

    // ===== CONSTRUCTEUR PRIVÉ (SINGLETON) =====
    private UserManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        this.users = new ArrayList<>();
        this.currentUser = null;

        // Créer le dossier si nécessaire
        createSaveDirectory();

        // Charger les utilisateurs existants
        loadUsers();
    }

    /**
     * Retourne l'instance unique du gestionnaire d'utilisateurs (pattern Singleton).
     *
     * @return L'instance de {@code UserManager}.
     */
    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * Crée le dossier de sauvegarde si celui-ci n'existe pas.
     */
    private void createSaveDirectory() {
        File file = new File(SAVE_FILE);
        File directory = file.getParentFile();
        if (directory != null && !directory.exists()) {
            boolean created = directory.mkdirs();
        }
    }

    /**
     * Charge la liste des utilisateurs depuis le fichier JSON.
     * Si le fichier est vide ou invalide, initialise une liste vide.
     */
    private void loadUsers() {
        File file = new File(SAVE_FILE);

        try {
            String json = new String(Files.readAllBytes(Paths.get(SAVE_FILE)), StandardCharsets.UTF_8);

            if (json.trim().isEmpty()) {
                users = new ArrayList<>();
                return;
            }

            Type userListType = new TypeToken<ArrayList<User>>(){}.getType();
            users = gson.fromJson(json, userListType);

            if (users == null) {
                users = new ArrayList<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
            users = new ArrayList<>();
        }
    }

    /**
     * Sauvegarde la liste actuelle des utilisateurs dans le fichier JSON.
     */
    private void saveUsers() {
        try {
            // Créer le dossier si nécessaire
            createSaveDirectory();

            String json = gson.toJson(users);

            // Écrire dans le fichier
            Files.write(Paths.get(SAVE_FILE), json.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Génère un hash SHA-256 sécurisé pour un mot de passe donné.
     *
     * @param password Mot de passe en clair.
     * @return Chaîne hachée (hexadécimale).
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            return password; // Fallback non sécurisé
        }
    }

    /**
     * Crée un nouvel utilisateur avec les informations fournies.
     *
     * @param username  Nom d'utilisateur unique.
     * @param password  Mot de passe (sera haché).
     * @param firstName Prénom.
     * @param lastName  Nom de famille.
     * @return {@code true} si l'utilisateur a été créé avec succès, {@code false} sinon.
     */
    public boolean createUser(String username, String password, String firstName,
                              String lastName) {

        // Vérifier que le username n'existe pas déjà
        if (getUserByUsername(username) != null) {
            return false;
        }

        // Créer le nouvel utilisateur
        String passwordHash = hashPassword(password);
        User newUser = new User(username, passwordHash, firstName, lastName);

        // Ajouter à la liste et sauvegarder
        users.add(newUser);

        saveUsers();

        return true;
    }

    /**
     * Tente de connecter un utilisateur avec les identifiants fournis.
     *
     * @param username Nom d'utilisateur.
     * @param password Mot de passe.
     * @return {@code true} si la connexion est réussie, {@code false} sinon.
     */
    public boolean login(String username, String password) {

        User user = getUserByUsername(username);

        if (user == null) {
            return false;
        }

        String passwordHash = hashPassword(password);
        if (!user.checkPassword(passwordHash)) {
            return false;
        }

        // Connexion réussie
        currentUser = user;
        currentUser.updateLastLogin();
        saveUsers();

        return true;
    }

    /**
     * Déconnecte l'utilisateur actuellement connecté.
     */

    public void logout() {
        if (currentUser != null) {
            currentUser = null;
        }
    }

    /**
     * Recherche un utilisateur par nom d'utilisateur (insensible à la casse).
     *
     * @param username Nom d'utilisateur.
     * @return L'utilisateur correspondant ou {@code null} si non trouvé.
     */
    private User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Enregistre le résultat d'une partie pour l'utilisateur connecté.
     *
     * @param won {@code true} si la partie est gagnée, {@code false} sinon.
     */
    public void recordGameResult(boolean won) {
        if (currentUser == null) {
            return;
        }

        currentUser.incrementGamesPlayed();
        if (won) {
            currentUser.incrementGamesWon();
        }

        saveUsers();
    }

    /**
     * Met à jour les informations du profil de l'utilisateur connecté.
     *
     * @param firstName Nouveau prénom (peut être {@code null}).
     * @param lastName  Nouveau nom de famille (peut être {@code null}).
     */
    public void updateProfile(String firstName, String lastName) {
        if (currentUser == null) {
            return;
        }

        if (firstName != null) currentUser.setFirstName(firstName);
        if (lastName != null) currentUser.setLastName(lastName);

        saveUsers();
    }

    /**
     * Retourne l'utilisateur actuellement connecté.
     *
     * @return Utilisateur courant, ou {@code null} s'il n'y a pas de session.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Vérifie si un utilisateur est actuellement connecté.
     *
     * @return {@code true} si connecté, sinon {@code false}.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Retourne la liste de tous les utilisateurs enregistrés.
     *
     * @return Liste d'utilisateurs.
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Retourne le nombre total d'utilisateurs enregistrés.
     *
     * @return Nombre d'utilisateurs.
     */
    public int getUserCount() {
        return users.size();
    }
}