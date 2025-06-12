package com.bomberman.controller;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MusicManager {
    private static MusicManager instance;
    private MediaPlayer currentPlayer;
    private List<String> musicFiles;
    private List<String> shuffledPlaylist;
    private int currentTrackIndex = 0;
    private boolean isPlaying = false;
    private boolean isShuffle = true;
    private double volume = 0.3;
    private boolean musicFailed = false; // Nouvelle variable pour arrêter sur erreur

    private MusicManager() {
        initializeMusicFiles();
        createShuffledPlaylist();
    }

    /**
     * Retourne l'instance unique du gestionnaire de musique (pattern Singleton).
     *
     * @return L'instance de {@code MusicManager}.
     */
    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    /**
     * Initialise la liste des fichiers de musique disponibles (01.mp3 à 11.mp3).
     */
    private void initializeMusicFiles() {
        musicFiles = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            musicFiles.add(String.format("%02d.mp3", i));
        }
    }

    /**
     * Crée une nouvelle playlist mélangée à partir des fichiers de musique disponibles.
     */
    private void createShuffledPlaylist() {
        shuffledPlaylist = new ArrayList<>(musicFiles);
        if (isShuffle) {
            Collections.shuffle(shuffledPlaylist, new Random());
        }
        currentTrackIndex = 0;
    }

    /**
     * Démarre la lecture de la musique de fond si aucune erreur n'est survenue
     * et que la musique n'est pas déjà en cours.
     */
    public void startBackgroundMusic() {
        if (!musicFailed && !isPlaying && !shuffledPlaylist.isEmpty()) {
            playCurrentTrack();
        }
    }

    /**
     * Arrête et libère les ressources de la musique en cours.
     */
    public void stopBackgroundMusic() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
        }
        isPlaying = false;
    }

    /**
     * Met la musique en pause si elle est en cours de lecture.
     */
    public void pauseBackgroundMusic() {
        if (currentPlayer != null && isPlaying && !musicFailed) {
            currentPlayer.pause();
            isPlaying = false;
        }
    }

    /**
     * Reprend la lecture de la musique si elle a été mise en pause.
     */
    public void resumeBackgroundMusic() {
        if (currentPlayer != null && !isPlaying && !musicFailed) {
            currentPlayer.play();
            isPlaying = true;
        }
    }

    /**
     * Joue la piste actuelle de la playlist. En cas d’erreur, la lecture est désactivée.
     */
    private void playCurrentTrack() {
        // Si la musique a déjà échoué, ne plus essayer
        if (musicFailed) {
            return;
        }

        if (currentTrackIndex >= shuffledPlaylist.size()) {
            createShuffledPlaylist();
        }

        String currentTrack = shuffledPlaylist.get(currentTrackIndex);

        URL musicUrl = getClass().getResource("/Music/" + currentTrack);

        if (musicUrl == null) {
            musicFailed = true;
            return;
        }

        System.out.println("✅ URL trouvée : " + musicUrl);

        try {
            if (currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.dispose();
            }

            Media media = new Media(musicUrl.toExternalForm());
            currentPlayer = new MediaPlayer(media);
            currentPlayer.setVolume(volume);

            currentPlayer.setOnEndOfMedia(() -> {
                if (!musicFailed) {
                    nextTrack();
                }
            });

            currentPlayer.setOnError(() -> {
                musicFailed = true;
                isPlaying = false;
            });

            currentPlayer.play();
            isPlaying = true;

        } catch (Exception e) {
            musicFailed = true;
            isPlaying = false;
        }
    }

    /**
     * Passe à la piste suivante dans la playlist mélangée.
     * Si la fin de la liste est atteinte, une nouvelle playlist est générée.
     */

    public void nextTrack() {
        if (musicFailed) return;

        currentTrackIndex++;
        if (currentTrackIndex >= shuffledPlaylist.size()) {
            createShuffledPlaylist();
        }

        if (isPlaying) {
            playCurrentTrack();
        }
    }

    /**
     * Passe à la piste précédente dans la playlist.
     * Si le début est atteint, revient à la dernière piste.
     */
    public void previousTrack() {
        if (musicFailed) return;

        currentTrackIndex--;
        if (currentTrackIndex < 0) {
            currentTrackIndex = shuffledPlaylist.size() - 1;
        }

        if (isPlaying) {
            playCurrentTrack();
        }
    }

    /**
     * Définit le volume de la musique.
     *
     * @param volume Volume entre 0.0 et 1.0.
     */
    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
        if (currentPlayer != null && !musicFailed) {
            currentPlayer.setVolume(this.volume);
        }
    }

    /**
     * Retourne le volume actuel de la musique.
     *
     * @return Volume entre 0.0 et 1.0.
     */
    public double getVolume() {
        return volume;
    }


    /**
     * Indique si le mode aléatoire est activé.
     *
     * @return {@code true} si le shuffle est activé, sinon {@code false}.
     */
    public boolean isShuffle() {
        return isShuffle;
    }

    /**
     * Indique si la musique est actuellement en cours de lecture (et sans erreur).
     *
     * @return {@code true} si une piste est en cours de lecture, sinon {@code false}.
     */
    public boolean isPlaying() {
        return isPlaying && !musicFailed;
    }

    /**
     * Retourne le nom de la piste actuellement jouée.
     *
     * @return Nom de la piste ou un message si la musique est désactivée.
     */
    public String getCurrentTrackName() {
        if (musicFailed) {
            return "Musique désactivée";
        }
        if (currentTrackIndex < shuffledPlaylist.size()) {
            return shuffledPlaylist.get(currentTrackIndex);
        }
        return "Aucune";
    }

    /**
     * Arrête la musique de fond et réinitialise l'instance du singleton.
     */
    public void shutdown() {
        stopBackgroundMusic();
        instance = null;
    }
}