package com.example.habitflow

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("habit_prefs", Context.MODE_PRIVATE) }

    var habits by remember { mutableStateOf(loadHabits(sharedPreferences)) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "HabitFlow", style = MaterialTheme.typography.headlineMedium)

        LazyColumn {
            items(habits) { habit ->
                HabitItem(habit)
            }
        }

        Button(
            onClick = {
                navController.navigate("addHabit")
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Add Habit")
        }
    }
}

// Function to load habits from SharedPreferences
fun loadHabits(sharedPreferences: SharedPreferences): List<String> {
    val jsonString = sharedPreferences.getString("habits", "[]") ?: "[]"
    val jsonArray = JSONArray(jsonString)
    return List(jsonArray.length()) { jsonArray.getString(it) }
}

// Function to save habits to SharedPreferences
fun saveHabits(sharedPreferences: SharedPreferences, habits: List<String>) {
    val editor = sharedPreferences.edit()
    editor.putString("habits", JSONArray(habits).toString())
    editor.apply()
}

@Composable
fun HabitItem(habit: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val parts = habit.split(":")
            Text(text = parts[0], style = MaterialTheme.typography.titleMedium) // Habit Name
            if (parts.size > 1) {
                Text(text = parts[1], style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp)) // Habit Description
            }
        }
    }
}