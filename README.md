# Streaming Garbage ♻️

**Streaming Garbage** est un projet d'analyse en temps réel et de classification automatique des déchets conçu avec **Apache Spark Structured Streaming** et **Scala**. Le système s'appuie sur une architecture Producteur/Consommateur pour traiter des flux de données et classifier les objets dans six catégories de tri sélectif distinctes.

---

## 📋 Table des Matières
1. [Fonctionnalités](#-fonctionnalités)
2. [Structure du Projet](#-structure-du-projet)
3. [Prérequis](prerequis)
4. [Installation et Configuration](#-installation-et-configuration)

---

## ✨ Fonctionnalités
* **Producer (Producteur) :** Simule ou capture un flux continu de données concernant les objets jetés et les envoie vers un système de messagerie (ex: Apache Kafka ou un flux de fichiers).
* **Consumer / Classifier (Consommateur) :** Utilise Spark Structured Streaming pour consommer le flux de données en temps réel, appliquer un modèle de classification et distribuer les résultats.
* **Classification Multi-classes :** Répartition automatique des déchets dans 6 flux de tri optimisés.

---

## 📂 Structure du Projet

Voici l'organisation des fichiers et dossiers du projet :

```text
.
├── app/
│   ├── pages/
│   │   ├── 1_📊_Vue_generale.py
│   │   ├── 2_🎯_Qualite_modele.py
│   │   ├── 3_🖼️_Analyse_images.py
│   │   └── 4_⚡_Performance_pipeline.py
│   ├── utils/
│   │   ├── __init__.py
│   │   ├── data_loader.py
│   │   └── metrics.py
│   └── home.py
├── data/
│   ├── input/
│   └── source/
│       ├── test/
│       |   ├── biodegradable/
│       |   ├── cardboard/
│       |   ├── glass/
│       |   ├── metal/
│       |   ├── paper/
│       |   └── plastic/
│       └── train/
│           ├── biodegradable/
│           ├── cardboard/
│           ├── glass/
│           ├── metal/
│           ├── paper/
│           └── plastic/
├── project/
│   ├── project/
│   ├── target/
│   ├── build.properties
│   └── plugins.sbt
├── src/
│   ├── main/
│   │   ├── resources/
│   │   │   └── app.properties
│   │   └── scala/
│   │       └── example/
│   │           └── sparkscala/
│   │               ├── Producer.scala
│   │               └── Test.scala
│   └── test/
├── target/
├── .gitignore
├── .travis.yml
└── build.sbt

```

<a id="prerequis"></a>
## 🛠️ Prérequis
Avant de lancer le projet, assurez-vous d'avoir installé :

-  Java SDK (Version 8 ou 11 recommandée pour Spark)

-  Scala (Compatible avec la version définie dans votre build.sbt)

-  SBT (Simple Build Tool)

-  Apache Spark (Si exécuté en dehors du mode local[*])

-  Python (pour le streamlit) avec packages :
    - pandas
    - plotly
    - streamlit
    - streamlit_autorefresh
    - utils

## 🚀 Installation et Configuration
Cloner le projet :

```Bash
git clone [https://github.com/votre-utilisateur/streaming-garbage.git](https://github.com/votre-utilisateur/streaming-garbage.git)
cd streaming-garbage
```
Configurer les propriétés :
Modifiez le fichier src/main/resources/app.properties pour adapter les chemins d'accès aux données (data/) et les configurations de votre cluster Spark.

Compiler et lancer le projet :

```Bash
sbt clean compile
sbt run
```

Il faut lancer deux fois - une pour le producer, et une autre pour le consumer

