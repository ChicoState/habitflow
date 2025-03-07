package com.example.habitflow

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight



@Composable
fun HomeScreen(navController: NavController, goodHabit: String) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("habit_prefs", Context.MODE_PRIVATE) }
    var habits by remember { mutableStateOf(loadHabits(sharedPreferences)) }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "HabitFlow", style = MaterialTheme.typography.headlineMedium)
            }

            // Keeps button at the bottom while scrolling through list
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(habits) { habit ->
                    HabitItem(habit, navController, goodHabit)
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = {navController.navigate("addHabit")},
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit",modifier = Modifier.size(40.dp), tint = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Add Habit", style = MaterialTheme.typography.bodyLarge)
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
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = backgroundColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(0.5f)) {
                    Text(
                        text = parts[0],
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) // Habit Name
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // First Column for Streak test
                        Column(modifier = Modifier.weight(1f)) {
                            Spacer(modifier = Modifier.weight(1f).width(30.dp))  // Adjust the width as needed
                            Text(
                                text = "$streak Day",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier//.padding(bottom = 1.dp, top = 4.dp)
                            )
                            Text(
                                text = "Streak \uD83D\uDD25",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.weight(1f).width(30.dp))  // Adjust the width as needed
                        }
                        // Second Column for Progress Emoji (Thumbs Up / Thumbs Down)
                        Column(
                            modifier = Modifier.weight(1f).padding(end = 10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "$progress%",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(end = 20.dp)
                                    .padding(top = 4.dp)
                            )
                            Text(
                                text = "Complete",
                                style = MaterialTheme.typography.bodyMedium
                                //modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


