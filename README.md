# 📱 HabitFlow

HabitFlow is an Android application that helps users build good habits and break bad ones. Users can create, track, and visualize progress on habits using customizable options like habit types, reminder frequencies, and progress goals.

---

## ⚙️ Tech Stack

- **Kotlin + Jetpack Compose**: For modern, declarative UI.
- **MVVM Architecture**: Separates responsibilities cleanly.
- **Firebase Firestore**: Stores user and habit data.
- **WorkManager**: Schedules reminder notifications.
- **MPAndroidChart**: Renders progress graphs.
- **Material 3**: Provides modern UI styling.

---

## 🧠 Architecture

HabitFlow uses **MVVM (Model-View-ViewModel)** architecture:

### 1. **Model**
Represents the app's data:
- `NewHabit.kt`: Defines the structure of a habit.
- Repository classes (`HabitRepository`, `AuthRepository`): Handle all Firestore and authentication logic.

### 2. **ViewModel**
Provides state & logic to the UI:
- ViewModels like `AddHabitViewModel`, `HomeViewModel`, `ProgressViewModel`, etc. hold all the logic for loading, updating, and saving habits.
- ViewModels are injected with repository instances, keeping Firebase code out of the Composables.

### 3. **View (UI Layer)**
Composables render UI and observe state from ViewModels:
- `AddHabitScreen.kt`, `HomeScreen.kt`, `ProgressScreen.kt`, etc. render UI based on state passed from ViewModels.
- UI never directly accesses Firebase — all interactions go through the ViewModel layer.

---

## 📱 App Flow

1. **Sign Up / Login**: Users authenticate using Firebase Auth.
2. **Home Screen**: Lists all current habits. Users can:
    - Tap a habit to view progress
    - Enter delete/edit mode
3. **Add Habit**: Users fill out a form to create a habit with:
    - Name, optional description
    - Type (good/bad), category, tracking method
    - Goal and duration
    - Optional reminders (daily, weekly, custom)
4. **Progress Screen**: Shows visualized progress vs. goal using charts.
5. **Settings**: Users can manage preferences like dark mode.

---

## 🚀 Features

- MVVM architecture with ViewModel/Repository split
- Firebase Firestore integration
- Track habits by quantity, time, or binary success
- Custom streak tracking and graphs
- Reminder scheduling with WorkManager
- Dark mode support via SharedPreferences
- Clean Jetpack Compose UI

---

## 📁 Project Structure
```
com.example.habitflow
├── model/
│   └── NewHabit.kt
├── repository/
│   ├── HabitRepository.kt
│   └── AuthRepository.kt
├── viewmodel/
│   ├── AddHabitViewModel.kt
│   ├── HomeViewModel.kt
│   └── ProgressViewModel.kt
├── ui/
│   ├── HomeScreen.kt
│   ├── AddHabitScreen.kt
│   └── ProgressScreen.kt
```

---

## 👥 Contributors

| Name             | GitHub Username                                      |
|------------------|------------------------------------------------------|
| Nick Kaplan      | [@NickK21](https://github.com/NickK21)               | 
| Halin Gailey     | [@hgailey](https://github.com/hgailey)               |
| Patrick Guevarra | [@pvguevarra](https://github.com/pvguevarra)         | 
| Matthew Munoz    | [@esvkat15](https://github.com/esvkat15)             |
| Drew Mortenson   | [@DrewsCodeLife](https://github.com/DrewsCodeLife)   |
| Allan Constanza  | [@AllanConstanza](https://github.com/AllanConstanza) |

---
