package com.bomberman.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe représentant un utilisateur du jeu
 * Contient toutes les informations et statistiques d'un joueur
 */
public class User {

    // ===== IDENTIFIANTS =====
    /**
     * Nom d'utilisateur unique pour la connexion
     * Exemple: "player123"
     */
    private String username;

    /**
     * Mot de passe hashé (on ne stocke jamais en clair!)
     * Utilise SHA-256 pour la sécurité
     */
    private String passwordHash;

    // ===== INFORMATIONS PERSONNELLES =====
    /**
     * Prénom du joueur
     * Exemple: "Jean"
     */
    private String firstName;

    /**
     * Nom de famille du joueur
     * Exemple: "Dupont"
     */
    private String lastName;

    // ===== STATISTIQUES DE JEU =====
    /**
     * Nombre total de parties jouées
     * S'incrémente à chaque fin de partie
     */
    private int gamesPlayed;

    /**
     * Nombre de parties gagnées
     * S'incrémente uniquement en cas de victoire
     */
    private int gamesWon;

    // ===== MÉTADONNÉES =====
    /**
     * Date et heure de création du compte
     * Format: "2024-01-15 14:30:00"
     */
    private String registrationDate;

    /**
     * Date et heure de dernière connexion
     * Se met à jour à chaque connexion
     */
    private String lastLoginDate;

    // ===== CONSTRUCTEURS =====
    /**
     * Constructeur pour un nouvel utilisateur
     * Initialise les statistiques à 0 et définit les dates
     */
    public User(String username, String passwordHash, String firstName, String lastName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gamesPlayed = 0;
        this.gamesWon = 0;

        // Définir la date d'inscription à maintenant
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.registrationDate = LocalDateTime.now().format(formatter);
        this.lastLoginDate = this.registrationDate;
    }

    /**
     * Constructeur vide pour la désérialisation JSON
     * Gson en a besoin pour recréer les objets depuis le fichier
     */
    public User() {
        // Constructeur vide nécessaire pour Gson
    }

    // ===== MÉTHODES MÉTIER =====
    /**
     * Incrémente le nombre de parties jouées
     * Appelée à la fin de chaque partie
     */
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    /**
     * Incrémente le nombre de parties gagnées
     * Appelée uniquement si le joueur gagne
     */
    public void incrementGamesWon() {
        this.gamesWon++;
    }

    /**
     * Met à jour la date de dernière connexion
     * Appelée lors de chaque connexion réussie
     */
    public void updateLastLogin() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.lastLoginDate = LocalDateTime.now().format(formatter);
    }

    /**
     * Calcule le ratio victoires/parties jouées
     * @return Le pourcentage de victoires (0-100)
     */
    public double getWinRate() {
        if (gamesPlayed == 0) return 0.0;
        return (double) gamesWon / gamesPlayed * 100;
    }

    /**
     * Vérifie si le mot de passe fourni correspond
     * @param passwordHash Le hash du mot de passe à vérifier
     * @return true si les mots de passe correspondent
     */
    public boolean checkPassword(String passwordHash) {
        if (this.passwordHash == null) {
            return passwordHash == null;
        }
        return this.passwordHash.equals(passwordHash);
    }

    // ===== GETTERS ET SETTERS =====
    // Getters - Pour récupérer les valeurs
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getGamesWon() { return gamesWon; }
    public String getRegistrationDate() { return registrationDate; }
    public String getLastLoginDate() { return lastLoginDate; }

    // Setters - Pour modifier les valeurs (certains seulement)
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // Setters pour la désérialisation JSON uniquement
    public void setUsername(String username) { this.username = username; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
    public void setLastLoginDate(String lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    /**
     * Représentation textuelle de l'utilisateur
     * Utile pour le debug
     */
    @Override
    public String toString() {
        return String.format("User{username='%s', name='%s %s', games=%d, wins=%d, winRate=%.1f%%}",
                username, firstName, lastName, gamesPlayed, gamesWon, getWinRate());
    }
}