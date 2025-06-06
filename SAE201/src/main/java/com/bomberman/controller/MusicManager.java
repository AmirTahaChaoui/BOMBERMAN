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
    private double volume = 0.5; // Volume par défaut (50%)

    private MusicManager() {
        initializeMusicFiles();
        createShuffledPlaylist();
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    private void initializeMusicFiles() {
        musicFiles = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            musicFiles.add(String.format("%02d.mp3", i));
        }
    }

    private void createShuffledPlaylist() {
        shuffledPlaylist = new ArrayList<>(musicFiles);
        if (isShuffle) {
            Collections.shuffle(shuffledPlaylist, new Random());
        }
        currentTrackIndex = 0;
    }

    public void startBackgroundMusic() {
        if (!isPlaying && !shuffledPlaylist.isEmpty()) {
            playCurrentTrack();
        }
    }

    public void stopBackgroundMusic() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
        }
        isPlaying = false;
    }

    public void pauseBackgroundMusic() {
        if (currentPlayer != null && isPlaying) {
            currentPlayer.pause();
            isPlaying = false;
        }
    }

    public void resumeBackgroundMusic() {
        if (currentPlayer != null && !isPlaying) {
            currentPlayer.play();
            isPlaying = true;
        }
    }

    private void playCurrentTrack() {
        if (currentTrackIndex >= shuffledPlaylist.size()) {
            // Playlist terminée, recommencer
            createShuffledPlaylist();
        }

        String currentTrack = shuffledPlaylist.get(currentTrackIndex);
        URL musicUrl = getClass().getResource("/Music/" + currentTrack);

        if (musicUrl == null) {
            System.out.println("Fichier musique non trouvé: /Music/" + currentTrack);
            // Essayer la piste suivante
            nextTrack();
            return;
        }

        try {
            // Arrêter la musique actuelle si elle existe
            if (currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.dispose();
            }

            Media media = new Media(musicUrl.toExternalForm());
            currentPlayer = new MediaPlayer(media);
            currentPlayer.setVolume(volume);

            // Quand la piste se termine, passer à la suivante
            currentPlayer.setOnEndOfMedia(() -> {
                nextTrack();
            });

            // Gérer les erreurs
            currentPlayer.setOnError(() -> {
                System.out.println("Erreur lors de la lecture de: " + currentTrack);
                nextTrack();
            });

            currentPlayer.play();
            isPlaying = true;

            System.out.println("♪ Lecture de: " + currentTrack);

        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de la musique " + currentTrack + ": " + e.getMessage());
            nextTrack();
        }
    }

    public void nextTrack() {
        currentTrackIndex++;
        if (currentTrackIndex >= shuffledPlaylist.size()) {
            // Recommencer la playlist
            createShuffledPlaylist();
        }

        if (isPlaying) {
            playCurrentTrack();
        }
    }

    public void previousTrack() {
        currentTrackIndex--;
        if (currentTrackIndex < 0) {
            currentTrackIndex = shuffledPlaylist.size() - 1;
        }

        if (isPlaying) {
            playCurrentTrack();
        }
    }

    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume)); // Entre 0 et 1
        if (currentPlayer != null) {
            currentPlayer.setVolume(this.volume);
        }
    }

    public double getVolume() {
        return volume;
    }

    public void setShuffle(boolean shuffle) {
        this.isShuffle = shuffle;
        createShuffledPlaylist();
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public String getCurrentTrackName() {
        if (currentTrackIndex < shuffledPlaylist.size()) {
            return shuffledPlaylist.get(currentTrackIndex);
        }
        return "Aucune";
    }

    public void playSpecificTrack(int trackNumber) {
        if (trackNumber >= 1 && trackNumber <= 10) {
            String trackName = String.format("%02d.mp3", trackNumber);
            int index = shuffledPlaylist.indexOf(trackName);
            if (index != -1) {
                currentTrackIndex = index;
                playCurrentTrack();
            }
        }
    }

    // Méthode pour nettoyer les ressources
    public void shutdown() {
        stopBackgroundMusic();
        instance = null;
    }
}