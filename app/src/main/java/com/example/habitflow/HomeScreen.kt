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
fun HabitItem(habit: String, navController: NavController, goodHabit: String) {
    val parts = habit.split(":")
    val backgroundColor = if (parts[2] == "good") { Color(0x40A5D6A7) } else { Color(0x40FF8A80) }
    val userData = if (parts[2] == "good" )
    { listOf(DataLists.goodWeeklyData, DataLists.goodMonthlyData, DataLists.goodOverallData) }
    else { listOf(DataLists.badWeeklyData, DataLists.badMonthlyData, DataLists.badOverallData) }
    val comparisonData = if (parts[2] == "good" )
    { listOf(DataLists.goodComparisonData1, DataLists.goodComparisonData2, DataLists.goodComparisonData3) }
    else { listOf(DataLists.badComparisonData1, DataLists.badComparisonData2, DataLists.badComparisonData3) }
    val progress = calculateSizePercentage(userData[2], comparisonData[2]).toString()
    val streak = (countMatchingFromEnd(userData[0], comparisonData[0])).toString()
    val upOrDown =
        if ((isFirstYGreaterThanLast(userData[2]) && parts[2] != "good") || (!isFirstYGreaterThanLast(userData[2]) && parts[2] == "good"))
        { "↗\uFE0F" } else { "↘\uFE0F" }
    val goalLength = comparisonData[2].size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                navController.navigate("progress/${habit}")
            },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Habit Name and Description
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

            // Right Column: Streak and Progress
            Column(modifier = Modifier.weight(0.5f)) {
                // Use Row to align the two columns horizontally
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // Space between the two columns
                    verticalAlignment = Alignment.CenterVertically // Vertically center items in the row
                ) {
                    // First Column for Streak Text
                    Column(modifier = Modifier.weight(1f)) {
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

                    // Second Column for Progress Emoji (Thumbs Up / Down)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$progress%",
                            //text = "$upOrDown",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(end = 16.dp).padding(top = 12.dp)
                        )
                        Text(
                            text = "Complete",
                            //text = "$upOrDown",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
