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
import org.json.JSONObject
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import com.github.mikephil.charting.data.Entry
import androidx.compose.ui.Alignment



@Composable
fun HomeScreen(navController: NavController, goodHabit: String) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("habit_prefs", Context.MODE_PRIVATE) }
    var habits by remember { mutableStateOf(loadHabits(sharedPreferences)) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "HabitFlow", style = MaterialTheme.typography.headlineMedium)

        LazyColumn {
            items(habits) { habit ->
                HabitItem(habit, navController, goodHabit)
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


@Composable
fun HabitItem(habit: String, navController: NavController, goodHabit: String) {
    val parts = habit.split(":")
    val backgroundColor = if (parts[2] == "good") { Color(0x40A5D6A7) } else { Color(0x40FF8A80) }
    val weeklyData = listOf(Entry(1f, 15f), Entry(2f, 17f), Entry(3f, 14f), Entry(4f, 10f),
        Entry(5f, 7f), Entry(6f, 11f), Entry(7f, 5f), Entry(8f, 6f), Entry(9f, 1f), Entry(10f, 0f))
    val comparisonData = listOf(Entry(1f, 15f), Entry(2f, 13f), Entry(3f, 11f), Entry(4f, 9f),
        Entry(5f, 7f), Entry(6f, 5f), Entry(7f, 3f), Entry(8f, 2f), Entry(9f, 1f), Entry(10f, 0f))
    val streak = (countMatchingFromEnd(weeklyData, comparisonData)).toString()
    // currently working on this:
    //val progress =
        //if (isFirstYGreaterThanLast(weeklyData) && parts[2] != "good") { "\uD83D\uDC4D" }
        //else if (!isFirstYGreaterThanLast(weeklyData)) { "\uD83D\uDC4E" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                navController.navigate("progress/${parts[0]}")
            },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)

    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(0.5f)) {
                Text(text = parts[0], style = MaterialTheme.typography.titleMedium) // Habit Name
                if (parts.size > 1) {
                    Text(
                        text = parts[1],
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    ) // Habit Description
                }
            }

            Spacer(modifier = Modifier.width(16.dp))  // Adjust the width as needed

            Column(modifier = Modifier.weight(0.5f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // To space out the two columns
                    verticalAlignment = Alignment.CenterVertically // To center the items vertically within the row
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start // Centers horizontally
                    ) {
                        Text(
                            text = "$streak Day",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 1.dp, top = 11.dp)
                        )
                        Text(
                            text = "Streak \uD83D\uDD25",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f), // Take up equal space
                    verticalArrangement = Arrangement.Center, // Center content vertically
                    horizontalAlignment = Alignment.End // Align items to the right
                ) {
                    Text(text = "Next Goal: 15 Days", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}