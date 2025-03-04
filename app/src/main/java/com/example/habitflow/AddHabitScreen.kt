package com.example.habitflow.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habitflow.saveHabits
import com.example.habitflow.loadHabits

@Composable
fun AddHabitScreen(navController: NavController) {
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }  // New description field
    var isGoodHabit by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("habit_prefs", Context.MODE_PRIVATE) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Add a Habit", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = habitName,
            onValueChange = { habitName = it },
            label = { Text("Habit Name") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        OutlinedTextField(
            value = habitDescription,
            onValueChange = { habitDescription = it },
            label = { Text("Habit Description") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Good Habit")
            Switch(
                checked = isGoodHabit,
                onCheckedChange = { isGoodHabit = it }
            )
            Text("Bad Habit")
        }

        Button(
            onClick = {
                val habits = loadHabits(sharedPreferences).toMutableList()
                val habitEntry = if (isGoodHabit) "👍 $habitName: $habitDescription" else "👎 $habitName: $habitDescription"
                habits.add(habitEntry)
                saveHabits(sharedPreferences, habits)

                navController.navigate("home") // Navigate back to Home
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save Habit")
        }
    }
}