@file:OptIn(ExperimentalMaterial3Api::class)
// This is important for the pop-up BasicAlertDialog for some reason

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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.clickable


@Composable
fun AddHabitScreen(navController: NavController) {
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }  // New description field
    var isGoodHabit by remember { mutableStateOf(true) }
    var showNameErrorDialog by remember { mutableStateOf(false) }
    var showDescErrorDialog by remember { mutableStateOf(false) }
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
                if (habitName.isBlank()) { // If box is empty
                    showNameErrorDialog = true // complain to user
                    return@Button // Strictly enforce the name requirement
                } else if (habitDescription.isBlank()) {
                    showDescErrorDialog = true // warn user
                    // Not strictly enforced, user can proceed.
                } else {
                    val habits = loadHabits(sharedPreferences).toMutableList()
                    val good = if (isGoodHabit) { "good" } else { "bad" }
                    val habitEntry = "$habitName:$habitDescription:$good"
                    habits.add(habitEntry)
                    saveHabits(sharedPreferences, habits)

                    navController.navigate("home/") // Navigate back to Home
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save Habit")
        }

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
                                    val good = if (isGoodHabit) { "good" } else { "bad" }
                                    val habitEntry = "$habitName:$habitDescription:$good"
                                    habits.add(habitEntry)
                                    saveHabits(sharedPreferences, habits)

                                    navController.navigate("home/")
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
}