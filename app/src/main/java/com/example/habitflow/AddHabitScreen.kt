@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.habitflow.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habitflow.saveHabits
import com.example.habitflow.loadHabits
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.clickable

@Composable
fun AddHabitScreen(navController: NavController) {
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }
    var isGoodHabit by remember { mutableStateOf(true) }
    var showNameErrorDialog by remember { mutableStateOf(false) }
    var showDescErrorDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("habit_prefs", Context.MODE_PRIVATE) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title bar at the top
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "HabitFlow", style = MaterialTheme.typography.headlineMedium)
        }

        // Main content centered
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Add a Habit", style = MaterialTheme.typography.headlineSmall)

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

                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bad Habit ")
                    Switch(
                        checked = isGoodHabit,
                        onCheckedChange = { isGoodHabit = it }
                    )
                    Text(" Good Habit")
                }

                Button(
                    onClick = {
                        if (habitName.isBlank()) {
                            showNameErrorDialog = true
                            return@Button
                        } else {
                            val habits = loadHabits(sharedPreferences).toMutableList()
                            val good = if (isGoodHabit) "good" else "bad"
                            val habitEntry = "$habitName:$habitDescription:$good"
                            habits.add(habitEntry)
                            saveHabits(sharedPreferences, habits)

                            navController.navigate("home?goodHabit=${good}")
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Save Habit")
                }
            }
        }
    }

    // Error Dialogs (kept outside the main layout)
    if (showNameErrorDialog) {
        BasicAlertDialog(
            onDismissRequest = { showNameErrorDialog = false },
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Habit Name Required", style = MaterialTheme.typography.headlineSmall)
                    Text("Please enter a name for your habit before saving.")
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { showNameErrorDialog = false }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }

    if (showDescErrorDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDescErrorDialog = false },
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("No Description Entered", style = MaterialTheme.typography.headlineSmall)
                    Text("Are you sure you want to save without a description?")
                    Spacer(Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showDescErrorDialog = false }
                        ) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val habits = loadHabits(sharedPreferences).toMutableList()
                                val good = if (isGoodHabit) "good" else "bad"
                                val habitEntry = "$habitName:$habitDescription:$good"
                                habits.add(habitEntry)
                                saveHabits(sharedPreferences, habits)

                                navController.navigate("home?goodHabit=${good}")
                            }
                        ) {
                            Text("Save Anyway")
                        }
                    }
                }
            }
        }
    }
}
