@file:OptIn(ExperimentalMaterial3Api::class)
// This is important for the pop-up BasicAlertDialog for some reason

package com.example.habitflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import android.content.SharedPreferences


@Composable
fun AddHabitScreen(navController: NavController, sharedPreferences: SharedPreferences) {
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }  // New description field
    var isGoodHabit by remember { mutableStateOf(true) }
    var showNameErrorDialog by remember { mutableStateOf(false) }
    var showDescErrorDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isBadHabit by remember { mutableStateOf(false) }

    // ✅ Get the current Dark Mode value from SharedPreferences
    var darkMode by remember {
        mutableStateOf(sharedPreferences.getBoolean("dark_mode", false))
    }

    Column(modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x3000008B))
                .padding(horizontal = 20.dp)
                .padding(top = 30.dp, bottom = 20.dp)
        ) {
            IconButton(
                onClick = { navController.navigate("home/") }, // Navigate to "home/"
                modifier = Modifier
                    .size(40.dp) // Increase the size of the icon button for the bubble effect
                    .padding(5.dp)
                    .align(Alignment.TopStart)

            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if(darkMode) Color.White else Color.Black
                )
            }
            Text(
                text = "Add a Habit",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Center)

            )
        }

        OutlinedTextField(
            value = habitName,
            onValueChange = { habitName = it },
            label = { Text("Habit Name") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 10.dp)
        )

        OutlinedTextField(
            value = habitDescription,
            onValueChange = { habitDescription = it },
            label = { Text("Habit Description") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 10.dp)
        )

        Row(modifier = Modifier.padding(vertical = 8.dp).padding(horizontal = 16.dp)) {
            Checkbox(
                checked = isGoodHabit,
                onCheckedChange = {
                    // When "Good Habit" is checked, uncheck "Bad Habit"
                    isGoodHabit = true
                    isBadHabit = false
                }
            )
            Text("Good Habit")
            Checkbox(
                checked = isBadHabit,
                onCheckedChange = {
                    // When "Bad Habit" is checked, uncheck "Good Habit"
                    isBadHabit = true
                    isGoodHabit = false
                }
            )
            Text("Bad Habit")

        }

        Button(
            onClick = {
                if (habitName.isBlank()) {
                    showNameErrorDialog = true
                    return@Button
                }

                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val db = FirebaseFirestore.getInstance()
                    val userDoc = db.collection("users").document(user.uid)

                    userDoc.get().addOnSuccessListener { document ->
                        val existingHabits = (document.get("habits") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                        val habitType = if (isGoodHabit) "good" else "bad"
                        val habitEntry = "$habitName:$habitDescription:$habitType"

                        existingHabits.add(habitEntry)

                        userDoc.update("habits", existingHabits)
                            .addOnSuccessListener {
                                navController.navigate("home/")
                            }
                            .addOnFailureListener { e ->
                                println("Error updating habits: ${e.message}")
                            }
                    }
                }
            },
            modifier = Modifier.padding(top = 16.dp).padding(horizontal = 16.dp)
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
                                    val user = FirebaseAuth.getInstance().currentUser
                                    if (user != null) {
                                        loadHabitsFromFirestore(user) { habits ->
                                            val updatedHabits = habits.toMutableList()
                                            val habitType = if (isGoodHabit) "good" else "bad"
                                            val habitEntry = "$habitName:$habitDescription:$habitType"

                                            updatedHabits.add(habitEntry)

                                            saveHabitsToFirestore(user, updatedHabits) // Save to Firestore
                                            navController.navigate("home/")
                                        }
                                    }
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