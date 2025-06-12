package com.bomberman.controller;

import com.bomberman.model.CustomMap;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MapManager {
    private static MapManager instance;
    private List<CustomMap> loadedMaps;
    private static final String MAPS_FILE_PATH = "SAE201/src/main/resources/donnees/maps.json";

    private MapManager() {
        loadedMaps = new ArrayList<>();
        loadMaps();
    }

    public static MapManager getInstance() {
        if (instance == null) {
            instance = new MapManager();
        }
        return instance;
    }

    /**
     * Charge toutes les maps depuis le fichier JSON (parsing manuel)
     */
    public void loadMaps() {
        loadedMaps.clear();

        try {
            File file = new File(MAPS_FILE_PATH);
            if (!file.exists()) {
                return;
            }
            String content = new String(Files.readAllBytes(Paths.get(MAPS_FILE_PATH)));
            parseJsonContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sauvegarde une nouvelle map dans le fichier JSON
     */
    public boolean saveMap(CustomMap map) {
        try {
            // Ajouter à la liste en mémoire
            boolean mapExists = false;
            for (int i = 0; i < loadedMaps.size(); i++) {
                if (loadedMaps.get(i).getName().equals(map.getName())) {
                    loadedMaps.set(i, map);
                    mapExists = true;
                    break;
                }
            }

            if (!mapExists) {
                loadedMaps.add(map);
            }

            // Réécrire tout le fichier JSON
            writeJsonFile();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retourne la liste des noms de toutes les maps
     */
    public List<String> getMapsList() {
        List<String> mapNames = new ArrayList<>();
        for (CustomMap map : loadedMaps) {
            mapNames.add(map.getName());
        }
        return mapNames;
    }

    /**
     * Récupère une map par son nom
     */
    public CustomMap getMapByName(String name) {
        for (CustomMap map : loadedMaps) {
            if (map.getName().equals(name)) {
                return map;
            }
        }
        return null;
    }

    /**
     * Retourne toutes les maps chargées
     */
    public List<CustomMap> getAllMaps() {
        return new ArrayList<>(loadedMaps);
    }

    // === MÉTHODES PRIVÉES ===

    private void parseJsonContent(String content) {
        String[] lines = content.split("\n");
        boolean inMapArray = false;
        boolean inMap = false;
        boolean inMatrix = false;

        String currentName = "";
        String currentDescription = "";
        String currentAuthor = "";
        String currentCreated = "";
        int currentWidth = 0;
        int currentHeight = 0;
        List<int[]> currentMatrix = new ArrayList<>();

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex].trim();

            if (line.contains("\"maps\"")) {
                inMapArray = true;
                continue;
            }

            if (inMapArray && line.equals("{")) {
                inMap = true;
                continue;
            }

            if (inMap && (line.equals("}") || line.equals("},"))) {
                // Fin d'une map
                if (!currentName.isEmpty()) {
                    int[][] matrix = currentMatrix.toArray(new int[currentMatrix.size()][]);
                    CustomMap map = new CustomMap(currentName, currentDescription,
                            currentWidth, currentHeight, matrix,
                            currentAuthor, currentCreated);
                    loadedMaps.add(map);
                }

                // Reset
                currentName = "";
                currentDescription = "";
                currentAuthor = "";
                currentCreated = "";
                currentWidth = 0;
                currentHeight = 0;
                currentMatrix.clear();
                inMap = false;
                inMatrix = false;
                continue;
            }

            if (inMap) {
                if (line.contains("\"name\"")) {
                    currentName = extractStringValue(line);
                } else if (line.contains("\"description\"")) {
                    currentDescription = extractStringValue(line);
                } else if (line.contains("\"author\"")) {
                    currentAuthor = extractStringValue(line);
                } else if (line.contains("\"created\"")) {
                    currentCreated = extractStringValue(line);
                } else if (line.contains("\"width\"")) {
                    currentWidth = extractIntValue(line);
                } else if (line.contains("\"height\"")) {
                    currentHeight = extractIntValue(line);
                } else if (line.contains("\"matrix\"") && line.contains("[")) {
                    inMatrix = true;
                } else if (inMatrix) {
                    // CORRECTION MAJEURE : Détecter correctement les lignes de matrice
                    if (line.startsWith("[") && (line.endsWith("]") || line.endsWith("],"))) {
                        // C'est une ligne de matrice !
                        String matrixLine = line;

                        // Supprimer la virgule finale si elle existe
                        if (matrixLine.endsWith(",")) {
                            matrixLine = matrixLine.substring(0, matrixLine.length() - 1);
                        }

                        int[] row = parseMatrixRow(matrixLine);
                        if (row != null) {
                            currentMatrix.add(row);
                        }
                    } else if (line.equals("]") || line.equals("],")) {
                        inMatrix = false;
                    }
                }
            }
        }
    }

    private String extractStringValue(String line) {
        try {
            int start = line.indexOf("\"", line.indexOf(":")) + 1;
            int end = line.lastIndexOf("\"");
            String result = line.substring(start, end);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private int extractIntValue(String line) {
        String numberStr = line.substring(line.indexOf(":") + 1).replaceAll("[,\\s]", "");
        return Integer.parseInt(numberStr);
    }

    private int[] parseMatrixRow(String line) {
        // Supprimer les crochets
        String numbers = line.substring(1, line.length() - 1);

        // Diviser par virgule
        String[] parts = numbers.split(",");
        int[] row = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            row[i] = Integer.parseInt(parts[i].trim());
        }

        return row;
    }

    private void writeJsonFile() throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"maps\": [\n");

        for (int mapIndex = 0; mapIndex < loadedMaps.size(); mapIndex++) {
            CustomMap map = loadedMaps.get(mapIndex);

            json.append("    {\n");
            json.append("      \"name\": \"").append(map.getName()).append("\",\n");
            json.append("      \"description\": \"").append(map.getDescription()).append("\",\n");
            json.append("      \"width\": ").append(map.getWidth()).append(",\n");
            json.append("      \"height\": ").append(map.getHeight()).append(",\n");
            json.append("      \"created\": \"").append(map.getCreated()).append("\",\n");
            json.append("      \"author\": \"").append(map.getAuthor()).append("\",\n");
            json.append("      \"matrix\": [\n");

            int[][] matrix = map.getMatrix();

            // CORRECTION : Utiliser les dimensions DÉCLARÉES pour écrire la matrice complète
            int declaredWidth = map.getWidth();
            int declaredHeight = map.getHeight();

            // Écrire TOUTES les lignes selon les dimensions déclarées
            for (int i = 0; i < declaredHeight; i++) {
                json.append("        [");

                for (int j = 0; j < declaredWidth; j++) {
                    // Vérifier que nous n'accédons pas hors limites de la matrice réelle
                    if (i < matrix.length && j < matrix[i].length) {
                        json.append(matrix[i][j]);
                    } else {
                        // Si la matrice réelle est plus petite, remplir avec des 0 (cases vides)
                        json.append(0);
                    }

                    if (j < declaredWidth - 1) json.append(",");
                }

                json.append("]");
                if (i < declaredHeight - 1) json.append(",");
                json.append("\n");
            }

            json.append("      ]\n");
            json.append("    }");
            if (mapIndex < loadedMaps.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append("  ],\n");
        json.append("  \"metadata\": {\n");
        json.append("    \"version\": \"1.0\",\n");
        json.append("    \"totalMaps\": ").append(loadedMaps.size()).append(",\n");
        json.append("    \"lastUpdated\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\"\n");
        json.append("  }\n");
        json.append("}");

        File file = new File(MAPS_FILE_PATH);
        file.getParentFile().mkdirs();

        Files.write(Paths.get(MAPS_FILE_PATH), json.toString().getBytes());
    }

    public int getMapCount() {
        return loadedMaps.size();
    }
}