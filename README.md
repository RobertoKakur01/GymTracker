# 🏋️ GymTracker – Persönliche Android Trainings-App

**GymTracker** ist eine native Android-App, die ich entwickelt habe, weil ich mir eine Trainings-App gewünscht habe, die wirklich zu meinem Trainingsalltag passt.

Ich habe viele Fitness-Apps ausprobiert, aber keine davon hat mir klar und langfristig gezeigt, wie sich mein Trainingsfortschritt tatsächlich entwickelt. Viele Apps sind überladen, unflexibel oder lenken vom eigentlichen Training ab.

Deshalb habe ich beschlossen, meine eigene App zu bauen – fokussiert auf **Struktur**, **Fortschritt** und **Übersicht**.

---



---

## 🎯 Motivation

Beim Training ist mir besonders wichtig zu sehen, ob ich mich wirklich verbessere.

GymTracker ist aus einem sehr persönlichen Bedarf entstanden:

- Ich wollte meine Workouts **schnell und strukturiert dokumentieren**
- Ich wollte mit **Trainingsvorlagen** arbeiten, statt jedes Mal alles neu einzugeben
- Ich wollte meinen **Fortschritt visuell nachvollziehen**
- Ich wollte eine App, die ich **selbst regelmäßig benutze**

Gleichzeitig habe ich das Projekt genutzt, um moderne Android-Architektur praxisnah umzusetzen.

---

## ✨ Funktionen

### 📋 Trainingsvorlagen

- Erstellung und Verwaltung von Templates
- Mehrere Übungen pro Vorlage
- Wiederverwendbar für langfristige Trainingsplanung

### 🏃 Trainingstracking

- Erfassung von:
  - Sätzen
  - Wiederholungen
  - Gewicht
- Trainingshistorie nach Tagen

### 📊 Fortschritt & Statistiken

- Übungsbezogene Verlaufshistorie
- Diagramme zur Progressions-Analyse
- Optimierte SQL-Abfragen für Auswertungen

### 💾 Offline & lokal

- Lokale Speicherung mit **Room**
- Keine Accounts, keine Cloud-Abhängigkeit
- Fokus auf Performance & Kontrolle

---

## 🧱 Architektur

Die App folgt dem **MVVM-Architekturpattern** mit klarer Trennung der Verantwortlichkeiten:

```text
Jetpack Compose UI
        ↓
   ViewModel
(StateFlow / MutableStateFlow)
        ↓
      DAO
        ↓
   Room Database
```

### Architekturentscheidungen

- Unidirectional Data Flow
- Keine Datenbanklogik in der UI
- ViewModel kapselt State & Business-Logik
- Room übernimmt Persistenz, Relations & Queries

---

## 📁 Projektstruktur (vereinfacht)

```text
com.example.gymtracker
├─ data/
│  ├─ Entities.kt                  // Room Entities
│  ├─ Relations.kt                 // @Relation / @Embedded
│  ├─ GymDao.kt                    // SQL Queries & CRUD
│  ├─ GymDatabase.kt               // Room Database
│  ├─ TemplateEntity.kt
│  ├─ TemplateExerciseEntity.kt
│  └─ ExerciseHistoryRow.kt        // SQL Projection (Charts)
│
├─ ui.theme/
│  ├─ Color.kt
│  ├─ Theme.kt
│  └─ Type.kt
│
├─ ChartModels.kt                  // UI-Modelle für Charts
├─ Charts.kt                       // Chart-Composables
├─ GymViewModel.kt                 // State & Business-Logik
└─ MainActivity.kt                 // Einstiegspunkt der App
```

---

## 🛠 Tech Stack

- Kotlin
- Jetpack Compose
- MVVM
- Room (SQLite)
- Kotlin Coroutines
- StateFlow / MutableStateFlow
- Custom Charts mit Compose
- Gradle

