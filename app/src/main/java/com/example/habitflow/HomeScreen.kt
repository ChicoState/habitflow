package com.example.habitflow

import android.app.Application
import androidx.compose.foundation.Image

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habitflow.repository.HabitRepository
import com.example.habitflow.viewmodel.HomeViewModel
import com.example.habitflow.viewmodel.HomeViewModelFactory
import com.github.mikephil.charting.data.Entry
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import kotlin.math.roundToInt









@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, isDeleting: String) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current.applicationContext as Application
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            application = context,
            habitRepository = HabitRepository,
            auth = FirebaseAuth.getInstance()
        )
    )
    val habits by viewModel.habits.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHabits()
    }

    val selectedHabits = remember { mutableStateOf(mutableSetOf<String>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Logo Image (centered)
        Image(
            painter = painterResource(id = R.drawable.habitflow_background),
            contentDescription = "HabitFlow Logo Background",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp) // leave space for bottom nav
                .align(Alignment.Center),
            alpha = 0.2f, // subtle opacity
            contentScale = ContentScale.Fit
        )
        Column(modifier = Modifier.fillMaxSize()) {

            TopAppBar(
                title = {
                    Column {
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
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isDeleting == "true") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select Habits to Move:",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(habits, key = { it.id }) { habit ->
                    HabitItem(
                        habit = habit,
                        navController = navController,
                        isDeleting = isDeleting,
                        isSelected = selectedHabits.value.contains(habit.id),
                        onSelect = { isSelected: Boolean ->
                            selectedHabits.value = selectedHabits.value.toMutableSet().apply {
                                if (isSelected) add(habit.id) else remove(habit.id)
                            }
                        }
                    )
                }
            }

            if (habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Habits Found", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // BOTTOM ACTION BAR
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDeleting != "true") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            navController.navigate("home/true")
                        }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF00897B),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        Text("Edit", style = MaterialTheme.typography.bodySmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = { navController.navigate("addHabit") },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp),
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
                        Text("Add Habit", style = MaterialTheme.typography.bodyLarge)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { /* TODO: Stats */ }) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = "Stats",
                                tint = Color(0xFF00897B),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        Text("Stats", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    // Deleting mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val buttonWidth = 130.dp
                        val buttonHeight = 50.dp

                        Button(
                            onClick = {
                                selectedHabits.value.clear()
                                navController.navigate("home/false")
                            },
                            modifier = Modifier
                                .width(buttonWidth)
                                .height(buttonHeight),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Cancel", fontSize = 14.sp, color = Color.White)
                            }
                        }

                        Button(
                            onClick = {
                                if (selectedHabits.value.isNotEmpty()) {
                                    viewModel.moveToPastHabits(selectedHabits.value) {
                                        selectedHabits.value.clear()
                                        navController.navigate("home/false")
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(buttonWidth)
                                .height(buttonHeight),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Save for Later", fontSize = 14.sp, color = Color.White)
                            }
                        }

                        Button(
                            onClick = {
                                if (selectedHabits.value.isNotEmpty()) {
                                    viewModel.deleteHabits(selectedHabits.value) {
                                        selectedHabits.value.clear()
                                        navController.navigate("home/false")
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(buttonWidth)
                                .height(buttonHeight),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Delete Habits", fontSize = 14.sp, color = Color.White)
                            }
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

@Composable
fun HabitItem(habit: Habit, navController: NavController, isDeleting: String, isSelected: Boolean, onSelect: (Boolean) -> Unit) {
    val habitName = habit.name
    val habitDescription = habit.description
    val habitType = habit.type
    val backgroundColor = habit.backgroundColor

    val context = LocalContext.current
    val user = Firebase.auth.currentUser
    val db = Firebase.firestore

    val swipeOffset = remember { mutableStateOf(0f) }
    val maxSwipe = 200f
    val showDeleteIcon = remember { mutableStateOf(false) }

    var isDeleted by remember { mutableStateOf(false) }
    if (isDeleted) return // Don't show if deleted

    val userData = if (habitType == "good") listOf(
        DataLists.goodWeeklyData.ifEmpty { listOf(Entry(0f, 0f)) },
        DataLists.goodMonthlyData.ifEmpty { listOf(Entry(0f, 0f)) },
        DataLists.goodOverallData.ifEmpty { listOf(Entry(0f, 0f)) }
    ) else listOf(
        DataLists.badWeeklyData.ifEmpty { listOf(Entry(0f, 0f)) },
        DataLists.badMonthlyData.ifEmpty { listOf(Entry(0f, 0f)) },
        DataLists.badOverallData.ifEmpty { listOf(Entry(0f, 0f)) }
    )

    val comparisonData = if (habitType == "good") listOf(DataLists.goodComparisonData1, DataLists.goodComparisonData2, DataLists.goodComparisonData3) else listOf(DataLists.badComparisonData1, DataLists.badComparisonData2, DataLists.badComparisonData3)
    val progress = if (userData[2].isNotEmpty()) {
        ((userData[2].last().x) / comparisonData[2].size * 100).toInt()
    } else 0

    val streak = (countMatchingFromEnd(userData[0], comparisonData[0])).toString()

    var arrowColor = Color.Red
    var upOrDown = "nan"
    if (isFirstYGreaterThanLast(userData[2]) && habitType != "good") {
        upOrDown = "↘"; arrowColor = Color(0xFF006400)
    } else if (isFirstYGreaterThanLast(userData[2]) && habitType == "good") {
        upOrDown = "↘"; arrowColor = Color.Red
    } else {
        if (!isFirstYGreaterThanLast(userData[2]) && habitType != "good") {
            upOrDown = "↗"; arrowColor = Color.Red
        } else if (!isFirstYGreaterThanLast(userData[2]) && habitType == "good") {
            upOrDown = "↗"; arrowColor = Color(0xFF006400)
        }
    }
    val isPressed = isSelected
    val isDarkTheme = isSystemInDarkTheme()

    val pressedBackgroundColor = if (isDeleting == "true" && isPressed) {
        if (isDarkTheme) Color(0xFF1E88E5).copy(alpha = 0.3f) else Color(0xFF90CAF9).copy(alpha = 0.5f)
    } else {
        backgroundColor.copy(alpha = 0.4f)
    }

    val borderColor = if (isDeleting == "true" && isPressed) {
        if (isDarkTheme) Color(0xFF42A5F5) else Color(0xFF1E88E5)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffset.value > maxSwipe * 0.5f) {
                            showDeleteIcon.value = true
                        } else {
                            swipeOffset.value = 0f
                            showDeleteIcon.value = false
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        swipeOffset.value = (swipeOffset.value + dragAmount).coerceIn(0f, maxSwipe)
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        if (showDeleteIcon.value) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.LightGray)
                    .padding(start = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (user != null) {
                        deleteHabitsFromFirestore(user, mutableSetOf(habitId), db) {
                            Toast.makeText(context, "Habit deleted", Toast.LENGTH_SHORT).show()
                            isDeleted = true
                        }
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }

        Card(
            modifier = Modifier
                .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                .fillMaxWidth()
                .clickable {
                    if (showDeleteIcon.value) {
                        swipeOffset.value = 0f
                        showDeleteIcon.value = false
                    } else {
                        isPressed.value = !isPressed.value
                        if (isDeleting != "true") {
                            navController.navigate("progress/${habitId}/Overall")
                        } else {
                            onSelect(isPressed.value)
                        }
                    }
                },
            colors = CardDefaults.cardColors(containerColor = pressedBackgroundColor),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = pressedBackgroundColor.copy(alpha = 0.4f), shape = RoundedCornerShape(20.dp))
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
                                Spacer(modifier = Modifier.weight(1f).width(30.dp))
                                Text("$streak Day", style = MaterialTheme.typography.titleLarge)
                                Text("Streak 🔥", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.weight(1f).width(30.dp))
                            }
                            Column(
                                modifier = Modifier
                                    .weight(.5f)
                                    .padding(end = 10.dp)
                            ) {
                                Spacer(modifier = Modifier.weight(1f).width(30.dp))
                                Text(
                                    text = buildAnnotatedString {
                                        append("$progress% ")
                                        pushStyle(SpanStyle(color = arrowColor, fontSize = 24.sp))
                                        append(upOrDown)
                                        pop()
                                    },
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text("Complete", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.weight(1f).width(30.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}