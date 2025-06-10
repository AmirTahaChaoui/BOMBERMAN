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
                System.err.println("❌ Fichier maps.json introuvable !");
                return;
            }

            String content = new String(Files.readAllBytes(Paths.get(MAPS_FILE_PATH)));
            parseJsonContent(content);

            System.out.println("✅ " + loadedMaps.size() + " map(s) chargée(s) avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des maps : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sauvegarde une nouvelle map dans le fichier JSON
     */
    public boolean saveMap(CustomMap map) {
        System.out.println("=== DEBUG MAPMANAGER SAVE ===");
        System.out.println("🔍 Map à sauvegarder: " + map.getName());
        System.out.println("🔍 Nombre de maps actuelles: " + loadedMaps.size());

        try {
            // Ajouter à la liste en mémoire
            boolean mapExists = false;
            System.out.println("🔍 Vérification si map existe...");

            for (int i = 0; i < loadedMaps.size(); i++) {
                System.out.println("🔍 Comparaison avec: " + loadedMaps.get(i).getName());
                if (loadedMaps.get(i).getName().equals(map.getName())) {
                    System.out.println("🔍 Map existe, remplacement à l'index " + i);
                    loadedMaps.set(i, map);
                    mapExists = true;
                    break;
                }
            }

            if (!mapExists) {
                System.out.println("🔍 Nouvelle map, ajout à la liste");
                loadedMaps.add(map);
            }

            System.out.println("🔍 Nombre de maps après ajout: " + loadedMaps.size());

            // Réécrire tout le fichier JSON
            System.out.println("🔍 Appel writeJsonFile()...");
            writeJsonFile();
            System.out.println("🔍 writeJsonFile() terminé");

            System.out.println("✅ Map '" + map.getName() + "' sauvegardée avec succès");
            return true;

        } catch (Exception e) {
            System.err.println("❌ ERREUR dans MapManager.saveMap(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            System.out.println("=== FIN DEBUG MAPMANAGER SAVE ===");
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

        System.out.println("🔍 DÉBUT PARSING JSON");

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex].trim();
            // System.out.println("📄 Ligne " + lineIndex + ": " + line); // Débug si besoin

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
                    System.out.println("🗺️ Création map: " + currentName);
                    System.out.println("   - Dimensions: " + currentWidth + "x" + currentHeight);
                    System.out.println("   - Lignes collectées: " + currentMatrix.size());

                    int[][] matrix = currentMatrix.toArray(new int[currentMatrix.size()][]);
                    CustomMap map = new CustomMap(currentName, currentDescription,
                            currentWidth, currentHeight, matrix,
                            currentAuthor, currentCreated);
                    loadedMaps.add(map);
                    System.out.println("✅ Map ajoutée: " + map.getName());
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
                    System.out.println("✅ Début matrice");
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
                            System.out.println("   Ligne " + currentMatrix.size() + " ajoutée ✅");
                        }
                    } else if (line.equals("]") || line.equals("],")) {
                        inMatrix = false;
                        System.out.println("✅ Fin matrice - " + currentMatrix.size() + " lignes collectées");
                    }
                }
            }
        }

        System.out.println("🔍 FIN PARSING - " + loadedMaps.size() + " maps chargées");
    }

    private String extractStringValue(String line) {
        try {
            System.out.println("🔍 extractStringValue: " + line);
            int start = line.indexOf("\"", line.indexOf(":")) + 1;
            int end = line.lastIndexOf("\"");
            String result = line.substring(start, end);
            System.out.println("🔍 Résultat: '" + result + "'");
            return result;
        } catch (Exception e) {
            System.err.println("❌ Erreur extractStringValue: " + line + " -> " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private int extractIntValue(String line) {
        try {
            System.out.println("🔍 extractIntValue: " + line);
            String numberStr = line.substring(line.indexOf(":") + 1).replaceAll("[,\\s]", "");
            int result = Integer.parseInt(numberStr);
            System.out.println("🔍 Résultat: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("❌ Erreur extractIntValue: " + line + " -> " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private int[] parseMatrixRow(String line) {
        try {
            // Supprimer les crochets
            String numbers = line.substring(1, line.length() - 1);

            // Diviser par virgule
            String[] parts = numbers.split(",");
            int[] row = new int[parts.length];

            for (int i = 0; i < parts.length; i++) {
                row[i] = Integer.parseInt(parts[i].trim());
            }

            return row;

        } catch (Exception e) {
            System.err.println("❌ Erreur parseMatrixRow: " + line + " -> " + e.getMessage());
            return null;
        }
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

            System.out.println("🔍 Écriture map: " + map.getName());
            System.out.println("   - Dimensions déclarées: " + declaredWidth + "x" + declaredHeight);
            System.out.println("   - Taille réelle matrice: " + matrix.length + "x" + (matrix.length > 0 ? matrix[0].length : 0));

            // Écrire TOUTES les lignes selon les dimensions déclarées
            for (int i = 0; i < declaredHeight; i++) {
                json.append("        [");

                for (int j = 0; j < declaredWidth; j++) {
                    // Vérifier que nous n'accédons pas hors limites de la matrice réelle
                    if (i < matrix.length && j < matrix[i].length) {
                        json.append(matrix[i][j]);
                    } else {
                        // Si la matrice réelle est plus petite, remplir avec des 0 (cases vides)
                        System.out.println("⚠️ Padding avec 0 à la position (" + i + "," + j + ")");
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
        System.out.println("💾 Maps sauvegardées avec dimensions correctes");
    }

    public int getMapCount() {
        return loadedMaps.size();
    }
}