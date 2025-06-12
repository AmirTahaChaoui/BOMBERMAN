# 🎮 Super Bomberman JavaFX

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

Un remake moderne du célèbre jeu Bomberman développé en Java avec JavaFX, incluant plusieurs modes de jeu et fonctionnalités avancées.

![Game Screenshot](docs/screenshots/game-preview.png)

## 🌟 Fonctionnalités

### 🎯 Modes de Jeu
- **🔥 Mode Classique** - Le Bomberman traditionnel avec duel 1v1
- **🚩 Capture The Flag** - Mode CTF inédit avec drapeaux et respawn
- **🗺️ Éditeur de Cartes** - Créez vos propres niveaux personnalisés

### 🎨 Système de Thèmes
- **3 thèmes visuels** différents (Classique, Moderne, Rétro)
- **Interface adaptive** qui s'adapte au thème choisi
- **Assets graphiques** de haute qualité

### 👤 Système d'Utilisateurs
- **Comptes utilisateurs** avec authentification
- **Statistiques de jeu** (parties jouées, victoires, ratio)
- **Profils personnalisables** avec avatars
- **Sauvegarde persistante** des données

### 🎵 Audio & Effets
- **Musique de fond** immersive
- **Effets sonores** pour toutes les actions
- **Sons spécialisés** pour chaque mode de jeu
- **Contrôle du volume** intégré

### 🗺️ Gestionnaire de Cartes
- **Cartes prédéfinies** variées
- **Éditeur visuel** intuitif pour créer des cartes
- **Import/Export** de cartes personnalisées
- **Validation automatique** de la jouabilité

## 🚀 Installation

### Prérequis
- Java 17 ou supérieur
- JavaFX 17 ou supérieur
- Git (pour cloner le repository)

### Étapes d'installation

1. **Cloner le repository**
```bash
git clone https://github.com/adam-KUROPATWA-BUTTE/super-bomberman-javafx.git
cd super-bomberman-javafx
```

2. **Configurer JavaFX**
```bash
# Télécharger JavaFX SDK depuis https://openjfx.io/
# Extraire dans un dossier (ex: C:/javafx-sdk-17/)
```

3. **Compiler et exécuter**
```bash
# Avec votre IDE préféré (IntelliJ IDEA, Eclipse, VS Code)
# Ou en ligne de commande :
javac --module-path "path/to/javafx/lib" --add-modules javafx.controls,javafx.fxml,javafx.media *.java
java --module-path "path/to/javafx/lib" --add-modules javafx.controls,javafx.fxml,javafx.media Main
```

## 🎮 Comment Jouer

### Contrôles

#### Joueur 1
- **ZQSD** - Déplacement
- **Espace** - Placer une bombe

#### Joueur 2  
- **OKML** - Déplacement
- **Shift** - Placer une bombe

#### Contrôles Généraux
- **Échap** - Menu de pause
- **Alt + F4** - Quitter le jeu

### Mode Classique
- Détruisez votre adversaire avec des bombes
- Collectez des bonus pour améliorer vos capacités
- Premier à éliminer l'autre gagne

### Mode Capture The Flag
- Capturez le drapeau ennemi et ramenez-le à votre base
- Premier à 3 captures gagne
- Les joueurs respawn après élimination

## 🛠️ Architecture du Projet

```
src/
├── com/bomberman/
│   ├── controller/          # Contrôleurs JavaFX
│   │   ├── MenuController.java
│   │   ├── GameController.java
│   │   ├── CaptureTheFlagController.java
│   │   ├── MapEditorController.java
│   │   └── UserManager.java
│   ├── model/              # Modèles de données
│   │   ├── GameBoard.java
│   │   ├── Player.java
│   │   ├── Bomb.java
│   │   ├── Flag.java
│   │   ├── User.java
│   │   └── CustomMap.java
│   └── Main.java           # Point d'entrée
resources/
├── fxml/                   # Fichiers FXML
├── css/                    # Feuilles de style
├── images/                 # Assets graphiques
├── Sound/                  # Effets sonores
└── data/                   # Données de sauvegarde
```

## 🎨 Captures d'Écran

| Menu Principal | Jeu Classique | Mode CTF |
|:---:|:---:|:---:|
| ![Menu](docs/screenshots/menu.png) | ![Classic](docs/screenshots/classic.png) | ![CTF](docs/screenshots/ctf.png) |

| Éditeur de Cartes | Profil Utilisateur | Thèmes |
|:---:|:---:|:---:|
| ![Editor](docs/screenshots/editor.png) | ![Profile](docs/screenshots/profile.png) | ![Themes](docs/screenshots/themes.png) |

## 🏆 Fonctionnalités Avancées

### Système de Bonus
- **💣 Bombe +** - Augmente le nombre de bombes
- **🔥 Portée +** - Augmente la portée d'explosion
- **⚡ Vitesse +** - Augmente la vitesse de déplacement

### Intelligence Artificielle (Futur)
- [ ] IA pour mode solo
- [ ] Différents niveaux de difficulté
- [ ] Stratégies avancées

### Multijoueur en Ligne (Futur)
- [ ] Partie en ligne 2-4 joueurs
- [ ] Classements globaux
- [ ] Tournois communautaires

## 🤝 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. Fork le projet
2. Créez votre branche (`git checkout -b feature/AmazingFeature`)
3. Commitez vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

### Standards de Code
- Suivre les conventions Java
- Commenter le code complexe
- Tester les nouvelles fonctionnalités
- Respecter l'architecture MVC

## 📋 Roadmap

### Version 1.0 ✅
- [x] Mode de jeu classique
- [x] Système d'utilisateurs
- [x] Éditeur de cartes
- [x] Thèmes multiples

### Version 1.1 ✅
- [x] Mode Capture The Flag
- [x] Effets sonores
- [x] Statistiques avancées
- [x] Interface améliorée

### Version 2.0 (En cours)
- [ ] Mode multijoueur en ligne
- [ ] Intelligence artificielle
- [ ] Power-ups avancés
- [ ] Animations améliorées

### Version 3.0 (Futur)
- [ ] Mode Battle Royale (4+ joueurs)
- [ ] Éditeur de thèmes
- [ ] Support mobile
- [ ] Steam Workshop

## 🐛 Bugs Connus

- ⚠️ Parfois les drapeaux peuvent disparaître après explosion (corrigé en v1.1.2)
- ⚠️ Rare désynchronisation audio sur certains systèmes
- ⚠️ L'éditeur de cartes peut planter avec des cartes très grandes (>50x50)

## 📝 Changelog

### v1.1.0 (2025-06-12)
- ✨ Ajout du mode Capture The Flag
- 🔊 Système audio complet
- 🚩 Mécaniques de drapeaux et respawn
- 🐛 Corrections diverses

### v1.0.0 (2025-06-01)
- 🎮 Version initiale
- 🔥 Mode classique fonctionnel
- 👤 Système d'utilisateurs
- 🗺️ Éditeur de cartes

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👥 Équipe

- **Adam KUROPATWA-BUTTE** - *Développeur* - [@adam-KUROPATWA-BUTTE](https://github.com/adam-KUROPATWA-BUTTE)

- **Simon EL KASSOUF** - *Développeur* - [@Simon-ElKassouf](https://github.com/Simon-ElKassouf)
  
- **Theo GHEUX** - *Développeur* - [@TheoGHEUX](https://github.com/TheoGHEUX)
  
- **Amir TAHA CHAOUI** - *Développeur* - [@AmirTahaChaoui](https://github.com/AmirTahaChaoui)



## 🙏 Remerciements

- Hudson Soft pour le Bomberman original
- La communauté JavaFX pour les ressources
- [OpenGameArt](https://opengameart.org/) pour certains assets
- Tous les testeurs et contributeurs

## 📞 Support

- **Issues** : [GitHub Issues](https://github.com/adam-KUROPATWA-BUTTE/super-bomberman-javafx/issues)
- **Email** : adam.kuropatwa@example.com
- **Discord** : SuperBomberman#1234

---

⭐ **N'oubliez pas de mettre une étoile si le projet vous plaît !** ⭐

[![GitHub stars](https://img.shields.io/github/stars/adam-KUROPATWA-BUTTE/super-bomberman-javafx.svg?style=social&label=Star)](https://github.com/adam-KUROPATWA-BUTTE/super-bomberman-javafx)
[![GitHub forks](https://img.shields.io/github/forks/adam-KUROPATWA-BUTTE/super-bomberman-javafx.svg?style=social&label=Fork)](https://github.com/adam-KUROPATWA-BUTTE/super-bomberman-javafx/fork)
