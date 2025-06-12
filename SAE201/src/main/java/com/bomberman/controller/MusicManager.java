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

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    private void initializeMusicFiles() {
        musicFiles = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
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
        if (!musicFailed && !isPlaying && !shuffledPlaylist.isEmpty()) {
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
        if (currentPlayer != null && isPlaying && !musicFailed) {
            currentPlayer.pause();
            isPlaying = false;
        }
    }

    public void resumeBackgroundMusic() {
        if (currentPlayer != null && !isPlaying && !musicFailed) {
            currentPlayer.play();
            isPlaying = true;
        }
    }

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

    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
        if (currentPlayer != null && !musicFailed) {
            currentPlayer.setVolume(this.volume);
        }
    }

    public double getVolume() {
        return volume;
    }

    public void setShuffle(boolean shuffle) {
        if (!musicFailed) {
            this.isShuffle = shuffle;
            createShuffledPlaylist();
        }
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public boolean isPlaying() {
        return isPlaying && !musicFailed;
    }

    public String getCurrentTrackName() {
        if (musicFailed) {
            return "Musique désactivée";
        }
        if (currentTrackIndex < shuffledPlaylist.size()) {
            return shuffledPlaylist.get(currentTrackIndex);
        }
        return "Aucune";
    }

    public void playSpecificTrack(int trackNumber) {
        if (musicFailed) return;

        if (trackNumber >= 1 && trackNumber <= 10) {
            String trackName = String.format("%02d.mp3", trackNumber);
            int index = shuffledPlaylist.indexOf(trackName);
            if (index != -1) {
                currentTrackIndex = index;
                playCurrentTrack();
            }
        }
    }

    public void shutdown() {
        stopBackgroundMusic();
        instance = null;
    }
}