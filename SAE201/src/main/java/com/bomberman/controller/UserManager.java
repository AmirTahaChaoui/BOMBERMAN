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
            if (created) {
                System.out.println("📁 Dossier de sauvegarde créé : " + directory.getAbsolutePath());
            }
        }
        System.out.println("📍 Chemin du fichier de sauvegarde : " + new File(SAVE_FILE).getAbsolutePath());
    }

    private void loadUsers() {
        File file = new File(SAVE_FILE);

        if (!file.exists()) {
            System.out.println("📄 Aucun fichier de sauvegarde trouvé : " + SAVE_FILE);
            System.out.println("📄 Démarrage avec une base vide.");
            return;
        }

        try {
            String json = new String(Files.readAllBytes(Paths.get(SAVE_FILE)), StandardCharsets.UTF_8);

            if (json.trim().isEmpty()) {
                System.out.println("📄 Fichier vide, initialisation avec liste vide");
                users = new ArrayList<>();
                return;
            }

            Type userListType = new TypeToken<ArrayList<User>>(){}.getType();
            users = gson.fromJson(json, userListType);

            if (users == null) {
                users = new ArrayList<>();
            }

            System.out.println("✅ " + users.size() + " utilisateur(s) chargé(s) depuis " + SAVE_FILE);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des utilisateurs : " + e.getMessage());
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

            System.out.println("💾 Utilisateurs sauvegardés dans " + SAVE_FILE);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde : " + e.getMessage());
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
            System.err.println("❌ Erreur de hashage : " + e.getMessage());
            return password; // Fallback non sécurisé
        }
    }

    // ===== OPÉRATIONS UTILISATEUR =====
    public boolean createUser(String username, String password, String firstName,
                              String lastName, String avatarId) {
        System.out.println("🔍 Tentative de création utilisateur : " + username);

        // Vérifier que le username n'existe pas déjà
        if (getUserByUsername(username) != null) {
            System.out.println("❌ Nom d'utilisateur déjà pris : " + username);
            return false;
        }

        // Créer le nouvel utilisateur
        String passwordHash = hashPassword(password);
        User newUser = new User(username, passwordHash, firstName, lastName, avatarId);

        // Ajouter à la liste et sauvegarder
        users.add(newUser);
        System.out.println("👤 Utilisateur ajouté à la liste. Total : " + users.size());

        saveUsers();

        System.out.println("✅ Utilisateur créé avec succès : " + username);
        return true;
    }

    public boolean login(String username, String password) {
        System.out.println("🔍 Tentative de connexion pour : " + username);

        User user = getUserByUsername(username);

        if (user == null) {
            System.out.println("❌ Utilisateur introuvable : " + username);
            System.out.println("📋 Utilisateurs disponibles :");
            for (User u : users) {
                System.out.println("  - " + u.getUsername());
            }
            return false;
        }

        String passwordHash = hashPassword(password);
        if (!user.checkPassword(passwordHash)) {
            System.out.println("❌ Mot de passe incorrect pour : " + username);
            return false;
        }

        // Connexion réussie
        currentUser = user;
        currentUser.updateLastLogin();
        saveUsers();

        System.out.println("✅ Connexion réussie : " + username);
        return true;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("👋 Déconnexion de : " + currentUser.getUsername());
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
            System.out.println("⚠️ Aucun utilisateur connecté pour enregistrer le résultat");
            return;
        }

        currentUser.incrementGamesPlayed();
        if (won) {
            currentUser.incrementGamesWon();
        }

        saveUsers();
        System.out.println("📊 Statistiques mises à jour pour : " + currentUser.getUsername());
    }

    public void updateProfile(String firstName, String lastName, String avatarId) {
        if (currentUser == null) {
            System.out.println("⚠️ Aucun utilisateur connecté");
            return;
        }

        if (firstName != null) currentUser.setFirstName(firstName);
        if (lastName != null) currentUser.setLastName(lastName);
        if (avatarId != null) currentUser.setAvatarId(avatarId);

        saveUsers();
        System.out.println("✏️ Profil mis à jour pour : " + currentUser.getUsername());
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) return false;

        String oldHash = hashPassword(oldPassword);
        if (!currentUser.checkPassword(oldHash)) {
            System.out.println("❌ Ancien mot de passe incorrect");
            return false;
        }

        currentUser.setPasswordHash(hashPassword(newPassword));
        saveUsers();
        System.out.println("🔐 Mot de passe changé pour : " + currentUser.getUsername());
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