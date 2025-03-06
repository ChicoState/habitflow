@file:OptIn(ExperimentalMaterial3Api::class)
// This is important for the pop-up dialogue

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

                    navController.navigate("home?goodHabit=${good}") // Navigate back to Home
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save Habit")
        }

        if (showNameErrorDialog) {
            BasicAlertDialog(
                onDismissRequest = {
                    // Do absolutely nothing
                },
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Missing Habit Name", style = MaterialTheme.typography.titleLarge)
                        Text("You must either enter a habit name or exit this screen.")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            // Button to exit the habit screen.
                            TextButton(onClick = {
                                // For example, navigate back to home
                                navController.navigate("home")
                            }) {
                                Text("Exit")
                            }
                            // Button to dismiss the dialog so the user can enter a name.
                            TextButton(onClick = { showNameErrorDialog = false }) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }

        // Does technically allow the user to continue, need to add functionality to skip desc or return
        if (showDescErrorDialog) {
            BasicAlertDialog(
                onDismissRequest = { showDescErrorDialog = false }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Missing Habit Description",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text("You haven't provided a description. You can continue without one.")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showDescErrorDialog = false }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }
}
