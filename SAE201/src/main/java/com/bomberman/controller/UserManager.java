package com.bomberman.controller;

import com.bomberman.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
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

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    // ===== GESTION DES FICHIERS =====
    private void createSaveDirectory() {
        File file = new File(SAVE_FILE);
        File directory = file.getParentFile();
        if (directory != null && !directory.exists()) {
            boolean created = directory.mkdirs();
        }
    }

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

    // ===== GESTION DES MOTS DE PASSE =====
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

    // ===== OPÉRATIONS UTILISATEUR =====
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

    public void logout() {
        if (currentUser != null) {
            currentUser = null;
        }
    }

    private User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    // ===== MISE À JOUR DES STATISTIQUES =====
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

    public void updateProfile(String firstName, String lastName) {
        if (currentUser == null) {
            return;
        }

        if (firstName != null) currentUser.setFirstName(firstName);
        if (lastName != null) currentUser.setLastName(lastName);

        saveUsers();
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) return false;

        String oldHash = hashPassword(oldPassword);
        if (!currentUser.checkPassword(oldHash)) {
            return false;
        }

        currentUser.setPasswordHash(hashPassword(newPassword));
        saveUsers();
        return true;
    }

    // ===== GETTERS =====
    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public int getUserCount() {
        return users.size();
    }
}