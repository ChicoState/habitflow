package com.example.habitflow

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(navController: NavController, goodHabit: String = "", isDeleting: String) {
    val context = LocalContext.current
    var habits by remember { mutableStateOf<List<String>>(emptyList()) }
    val user = Firebase.auth.currentUser
    val db = Firebase.firestore
    var isLoading by remember { mutableStateOf(false) } // Loading state

    LaunchedEffect(user) {
        if (user != null) {
            loadHabitsFromFirestore(user) { fetchedHabits ->
                habits = fetchedHabits
            }
        }
    }
    val selectedHabits by remember { mutableStateOf(mutableSetOf<String>()) }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x3000008B))
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp, bottom = 20.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(
                        text = "HabitFlow",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    user?.let {
                        Text(
                            text = it.displayName ?: "User",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                IconButton(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF00897B),
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (isDeleting == "true") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Select habit(s) to remove:", style = MaterialTheme.typography.headlineSmall)
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(habits.filter { habit -> !selectedHabits.contains(habit) }, key = { it }) { habit ->
                    HabitItem(
                        habitId = habit,
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
            if (habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Habits Found", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                //.padding(horizontal = 16.dp)
        ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0x1900008B))
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
                                        navController.navigate("home/true")

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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cancel Button
                            Button(
                                onClick = {
                                    selectedHabits.clear()  // Clear selected habits
                                    navController.navigate("home/false")
                                },
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(100.dp)
                                    .padding(start = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    if (selectedHabits.isNotEmpty()) {
                                        Log.d("moveToPast", "Moving selected habits to past")
                                        moveToPastHabits(user!!, selectedHabits, db) {
                                            Log.d("moveToPast", "Move to past successful, reloading habits")
                                            // After moving the habits, reload the habits list from Firestore
                                            loadHabitsFromFirestore(user) { fetchedHabits ->
                                                habits = fetchedHabits
                                            }
                                            selectedHabits.clear()  // Clear selected habits after moving
                                            navController.navigate("home/false")  // Optionally navigate back to normal view
                                        }
                                    } else {
                                        Log.d("moveToPast", "No habits selected")
                                    }
                                },
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(200.dp)
                                    .padding(start = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)) // Red button color
                            ) {
                                Text("Move to Past Habits")
                            }
                        }
                    }
                }
        }
    }
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


///// Adding new helper functions:
fun countDaysWithLargerY(list1: List<Entry>, list2: List<Entry>): Int {
    // Find the minimum size to avoid IndexOutOfBoundsException
    val minSize = minOf(list1.size, list2.size)
    var count = 0

    for (i in 0 until minSize) { // Loop through both lists
        if (list1[i].y > list2[i].y) { // Compare the y values
            count++
        }
    }

    return count
}

fun countDaysWithSmallerY(list1: List<Entry>, list2: List<Entry>): Int {
    // Find the minimum size to avoid IndexOutOfBoundsException
    val minSize = minOf(list1.size, list2.size)
    var count = 0

    for (i in 0 until minSize) { // Loop through both lists
        if (list1[i].y < list2[i].y) { // Compare the y values for smaller values
            count++
        }
    }

    return count
}

fun countMatchingFromEndBad(list1: List<Entry>, list2: List<Entry>): Int {
    val minSize = minOf(list1.size, list2.size) // Find the smaller list size
    var count = 0

    for (i in 1..minSize) { // Loop from end to start
        if (list1[list1.size - i].y <= list2[list1.size - i].y) {
            count++
        } else {
            break // Stop counting when a mismatch occurs
        }
    }

    return count
}

fun countMatchingFromEndGood(list1: List<Entry>, list2: List<Entry>): Int {
    val minSize = minOf(list1.size, list2.size) // Find the smaller list size
    var count = 0

    for (i in 1..minSize) { // Loop from end to start
        if (list1[list1.size - i].y >= list2[list1.size - i].y) {
            count++
        } else {
            break // Stop counting when a mismatch occurs
        }
    }

    return count
}

fun convertToDates(entries: List<Entry>, startDate: String): List<String> {
    // Define the SimpleDateFormat to parse the startDate and format the resulting date
    val sdf = SimpleDateFormat("d/M/yy", Locale.US)

    // Parse the startDate to a Date object
    val baseDate = sdf.parse(startDate)

    // Convert each entry's x (which represents the number of days offset from startDate) to a date
    val calendar = Calendar.getInstance()
    calendar.time = baseDate ?: Date() /* Use default date if null */

    return entries.map { entry ->
        // Add the x value (days) to the calendar
        calendar.add(Calendar.DAY_OF_MONTH, entry.x.toInt())

        // Return the new date formatted as a string
        sdf.format(calendar.time)
    }
}

fun compareLists(list1: List<Entry>, list2: List<Entry>): List<Entry> {
    val resultList = mutableListOf<Entry>()

    // Iterate through the indices of both lists
    for (i in list1.indices) {
        // Ensure both lists have the same index length and valid entry
        if (i < list2.size) {
            val entry1 = list1[i]
            val entry2 = list2[i]

            // Check if the y components are equal for the same x index
            if (entry1.y == entry2.y) {
                resultList.add(entry1) // Add the entry from list1 (or list2, they have the same y value)
            }
        }
    }

    return resultList
}

///////

@Composable
fun HabitItem(habitId: String, navController: NavController, goodHabit: String, isDeleting: String, isSelected: Boolean, onSelect: (Boolean) -> Unit) {
    var habitName: String = ""
    var habitDescription: String = ""
    var habitType: String = ""
    var habit by remember { mutableStateOf<Habit?>(null) }
    var backgroundColor: Color = Color.Transparent

    LaunchedEffect(habitId) {
        getHabitFromFirestore(habitId) { fetchedHabit ->
            habit = fetchedHabit // Store the fetched habit in the state
        }
    }
    habit?.let { habitNonNull ->
        habitName = habitNonNull.name
        habitDescription = habitNonNull.description
        habitType = habitNonNull.type
        backgroundColor = habitNonNull.backgroundColor
    }

    val userData = if (habitType == "good")
        listOf(
            DataLists.goodWeeklyData.ifEmpty { listOf(Entry(0f, 0f)) },
            DataLists.goodMonthlyData.ifEmpty { listOf(Entry(0f, 0f)) },
            DataLists.goodOverallData.ifEmpty { listOf(Entry(0f, 0f)) }
        )
    else
        listOf(
            DataLists.badWeeklyData.ifEmpty { listOf(Entry(0f, 0f)) },
            DataLists.badMonthlyData.ifEmpty { listOf(Entry(0f, 0f)) },
            DataLists.badOverallData.ifEmpty { listOf(Entry(0f, 0f)) }
        )

    val comparisonData = if (habitType == "good") listOf(DataLists.goodComparisonData1, DataLists.goodComparisonData2, DataLists.goodComparisonData3) else listOf(DataLists.badComparisonData1, DataLists.badComparisonData2, DataLists.badComparisonData3)
    val progress = if (userData.size > 2 && userData[2].isNotEmpty() && comparisonData.size > 2) {
        ((userData[2].last().x) / comparisonData[2].size * 100).toInt()
    } else {
        0
    }
    val streak = (countMatchingFromEnd(userData[0], comparisonData[0])).toString()
    var arrowColor = Color.Red
    var upOrDown = "nan"
    if (isFirstYGreaterThanLast(userData[2]) && habitType != "good") {
        upOrDown = "↘"
        arrowColor = Color(0xFF006400)
    } else if (isFirstYGreaterThanLast(userData[2]) && habitType == "good") {
        upOrDown = "↘"
        arrowColor = Color.Red
    } else {
        if (!isFirstYGreaterThanLast(userData[2]) && habitType != "good") {
            upOrDown = "↗"
            arrowColor = Color.Red
        } else {
            if (!isFirstYGreaterThanLast(userData[2]) && habitType == "good") {
                upOrDown = "↗"
                arrowColor = Color(0xFF006400)
            }
        }
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
            .padding(horizontal = 16.dp)
            .clickable(
                onClick = {
                    isPressed.value = !isPressed.value
                    if (isDeleting != "true") {
                        navController.navigate("progress/a:b:c/Overall")
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
                        text = habitName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = habitDescription,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(0.6f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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