package com.example.habitflow

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.mikephil.charting.data.Entry
import org.json.JSONArray
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Delete



@Composable
fun HomeScreen(navController: NavController, goodHabit: String, isDeleting: String) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("habit_prefs", Context.MODE_PRIVATE) }
    var habits by remember { mutableStateOf(loadHabits(sharedPreferences)) }
    var selectedHabits by remember { mutableStateOf(mutableSetOf<String>()) }
    val onDeleteSelectedHabits: () -> Unit = {
        habits = habits.filterNot { selectedHabits.contains(it) } // Remove selected habits
        saveHabits(sharedPreferences, habits) // Save updated habits to SharedPreferences
        selectedHabits.clear() // Clear the selected habits list
    }
    Box(
        modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "HabitFlow",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(
                    onClick = { /* TODO: Navigation Home */ },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF00897B), // Teal
                        modifier = Modifier
                            .size(50.dp)
                            .padding(top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            //Additional label to show when user is in delete mode
            if (isDeleting == "true") {
                Spacer(modifier = Modifier.height(8.dp)) // Optional space between the two texts
                Text(text = "Select habit(s) to remove:", style = MaterialTheme.typography.headlineSmall)
            }
            // Keeps button at the bottom while scrolling through list
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(habits.filter { habit -> !selectedHabits.contains(habit) }, key = { it }) { habit ->
                    HabitItem(
                        habit = habit,
                        navController = navController,
                        goodHabit = goodHabit,
                        isDeleting = isDeleting,
                        isSelected = selectedHabits.contains(habit),
                        onSelect = { isSelected ->
                            if (isSelected) {
                                selectedHabits.add(habit)
                            } else {
                                selectedHabits.remove(habit)
                            }
                        },
                    )
                }
            }
            // If there are currently no habits, show this message
            if (habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Habits Found", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDeleting != "true") {
                        // Delete Button (Left)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center) {
                                IconButton(
                                    onClick = {
                                        if (isDeleting == "true") {
                                            navController.navigate("home/false")
                                        } else {
                                            navController.navigate("home/true")
                                        }

                                    }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFF00897B), // Teal
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(top = 4.dp)
                                    )
                                }
                            }
                            Text(
                                "Delete",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        // Add Habit button (Center)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FloatingActionButton(
                                onClick = { navController.navigate("addHabit") },
                                containerColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(8.dp),
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Habit",
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add Habit", style = MaterialTheme.typography.bodyLarge)
                        }
                        // Stats button (Right)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center

                        ) {
                            IconButton(onClick = { /* TODO: Navigation Stats */ }) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = "Stats",
                                    tint = Color(0xFF00897B),
                                    modifier = Modifier
                                        .size(50.dp)
                                        .padding(top = 4.dp)
                                )
                            }
                            Text("Stats", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (isDeleting == "true") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, // Align buttons with space between them
                            verticalAlignment = Alignment.CenterVertically // Vertically center the buttons
                        ) {
                            Button(
                                onClick = {
                                    selectedHabits.clear()
                                    navController.navigate("home/false") // Just an example, adjust as needed
                                },
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(100.dp)
                                    .padding(start = 8.dp), // Adds space between the buttons
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray) // You can style it differently if needed
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                    onClick = {
                                        if (selectedHabits.size > 0) {
                                            onDeleteSelectedHabits()
                                            navController.navigate("home/false")
                                        }
                                        else {}
                                    },
                                    modifier = Modifier
                                        //.padding(start = 8.dp)
                                        .height(50.dp)
                                        .width(200.dp)
                                        .padding(start = 8.dp), // Adds space between the buttons
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)) // Make the button red
                            ) {
                                Text("Delete Selected Habits")
                            }

                        }
                    }
            }
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

fun isFirstYGreaterThanLast(list: List<Entry>): Boolean {
    if (list.isNotEmpty()) {
        val firstY = list.first().y
        val lastY = list.last().y
        return firstY > lastY
    }
    return false
}

fun countMatchingFromEnd(list1: List<Entry>, list2: List<Entry>): Int {
    val minSize = minOf(list1.size, list2.size) // Find the smaller list size
    var count = 0

    for (i in 1..minSize) { // Loop from end to start
        if (list1[list1.size - i].y <= list2[list2.size - i].y) {
            count++
        } else {
            break // Stop counting when a mismatch occurs
        }
    }

    return count
}

fun calculateSizePercentage(list1: List<Any>, list2: List<Any>): Int {
    val size1 = list1.size
    val size2 = list2.size

    // Calculate the smaller list size and the larger list size
    val smallerSize = if (size1 < size2) size1 else size2
    val largerSize = if (size1 > size2) size1 else size2

    // Calculate the percentage and return as an integer
    return ((smallerSize.toFloat() / largerSize) * 100).toInt()
}


@Composable
fun HabitItem(habit: String, navController: NavController, goodHabit: String, isDeleting: String, isSelected: Boolean, onSelect: (Boolean) -> Unit) {
    val parts = habit.split(":")
    var backgroundColor = if (parts[2] == "good") { Color(0x40A5D6A7) } else { Color(0x40FF8A80) }
    val userData = if (parts[2] == "good" )
    { listOf(DataLists.goodWeeklyData, DataLists.goodMonthlyData, DataLists.goodOverallData) }
    else { listOf(DataLists.badWeeklyData, DataLists.badMonthlyData, DataLists.badOverallData) }
    val comparisonData = if (parts[2] == "good" )
    { listOf(DataLists.goodComparisonData1, DataLists.goodComparisonData2, DataLists.goodComparisonData3) }
    else { listOf(DataLists.badComparisonData1, DataLists.badComparisonData2, DataLists.badComparisonData3) }
    val progress = ((userData[2][userData[2].size-1].x) / comparisonData[2].size * 100).toInt()
    val streak = (countMatchingFromEnd(userData[0], comparisonData[0])).toString()
    var arrowColor = Color.Red
    var upOrDown = "nan"
    if (isFirstYGreaterThanLast(userData[2]) && parts[2] != "good") {
        upOrDown = "↘"
        arrowColor = Color(0xFF006400)
    } else if (isFirstYGreaterThanLast(userData[2]) && parts[2] == "good") {
        upOrDown = "↘"
        arrowColor = Color.Red
    } else if (!isFirstYGreaterThanLast(userData[2]) && parts[2] != "good") {
        upOrDown = "↗"
        arrowColor = Color.Red
    } else if (!isFirstYGreaterThanLast(userData[2]) && parts[2] == "good") {
        upOrDown = "↗"
        arrowColor = Color(0xFF006400)
    }

    val isPressed = remember { mutableStateOf(isSelected) }
    val pressedBackgroundColor = if (isDeleting == "true" && isPressed.value) {
        backgroundColor.copy(
            red = backgroundColor.red * 0.2f,   // reduce the red component
            green = backgroundColor.green * 0.2f,  // reduce the green component
            blue = backgroundColor.blue * 0.2f,    // reduce the blue component
            alpha = 0.1f
        )
    } else {
        backgroundColor.copy(alpha = 0.4f)  // Normal color when not pressed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(
                onClick = {
                    isPressed.value = !isPressed.value
                    if (isDeleting != "true") {
                        navController.navigate("progress/${habit}/Overall")
                    }
                    else {
                        onSelect(isPressed.value) // Update selected habits list
                    }
                },
            ),
        colors = CardDefaults.cardColors(containerColor = pressedBackgroundColor),
        shape = RoundedCornerShape(20.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = pressedBackgroundColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(0.4f)) {
                    Text(
                        text = parts[0],
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) // Habit Name
                    if (parts.size > 1) {
                        Text(
                            text = parts[1],
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right Column: Streak and Progress
                Column(modifier = Modifier.weight(0.6f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // First Column for Streak test
                        Column(
                            modifier = Modifier
                                .weight(.5f)
                                .padding(start = 10.dp)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .width(30.dp)
                            )
                            Text(
                                text = "$streak Day",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = "Streak \uD83D\uDD25",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .width(30.dp)
                            )
                        }
                        // Second Column for Progress Emoji (Thumbs Up / Thumbs Down)
                        Column(
                            modifier = Modifier
                                .weight(.5f)
                                .padding(end = 10.dp)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .width(30.dp)
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("$progress% ")
                                    pushStyle(SpanStyle(color = arrowColor, fontSize = 24.sp))
                                    append(upOrDown)
                                    pop()
                                },
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = "Complete",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .width(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

