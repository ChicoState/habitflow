package com.example.habitflow

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.collections.setOf
import com.example.habitflow.model.Habit
import com.example.habitflow.model.UserData
import com.example.habitflow.repository.DataRepository
import com.example.habitflow.viewmodel.AddDataViewModel
import com.example.habitflow.viewmodel.HabitCardViewModel
import kotlin.math.roundToInt

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(addDataViewModel: AddDataViewModel, navController: NavController, isDeleting: String) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current.applicationContext as Application
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            application = context,
            dataRepository = DataRepository(),
            habitRepository = HabitRepository,
            auth = FirebaseAuth.getInstance()
        )
    )

    val habitsMap by homeViewModel.habits.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.loadHabits()
    }

    val selectedHabits = remember { mutableStateOf(mutableSetOf<String>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Logo Image (centered)
        Image(
            painter = painterResource(id = R.drawable.habitflow_background),
            contentDescription = "HabitFlow Logo Background",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp)
                .align(Alignment.Center),
            alpha = 0.2f,
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
                items(habitsMap.entries.toList(), key = { it.key.id }) { (habit, userData) ->
                    HabitItem(
                        habit = habit,
                        userData = userData,
                        homeViewModel = homeViewModel,
                        dataViewModel = addDataViewModel,
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

            if (habitsMap.isEmpty()) {
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
                                    homeViewModel.moveToPastHabits(selectedHabits.value) {
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
                                    homeViewModel.deleteHabits(selectedHabits.value) {
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

@Composable
fun HabitItem(
    habit: Habit,
    userData: UserData?,
    homeViewModel: HomeViewModel,
    dataViewModel: AddDataViewModel,
    navController: NavController,
    isDeleting: String,
    isSelected: Boolean,
    onSelect: (Boolean) -> Unit) {

    val context = LocalContext.current
    val habitCardViewModel = remember { HabitCardViewModel(habit, userData) }

    var swipeOffset = habitCardViewModel.swipeOffset.value
    var showDeleteIcon = habitCardViewModel.showDeleteIcon.value

    var isDeleted by remember { mutableStateOf(false) }
    if (isDeleted) return // Don't show if deleted
    var isPressed by remember { mutableStateOf(isSelected) }
    val isDarkTheme = isSystemInDarkTheme()

    val backgroundColor = habit.backgroundColor
    val notificationIcon = habitCardViewModel.getNotificationIcon()
    val pressedBackgroundColor = if (isDeleting == "true" && isPressed) {
        if (isDarkTheme) Color(0xFF1E88E5).copy(alpha = 0.3f) else Color(0xFF90CAF9).copy(alpha = 0.5f)
    } else {
        backgroundColor.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        habitCardViewModel.handleHorizontalDrag(dragAmount = 0f) {
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        habitCardViewModel.handleHorizontalDrag(dragAmount = dragAmount) {
                        }
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        if (showDeleteIcon) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.LightGray)
                    .padding(start = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeleteButton(
                    habit = habit,
                    homeViewModel = homeViewModel,
                    navController = navController,
                    context = context,
                    onDeleted = {
                        isDeleted = true
                    }
                )
            }
        }
        Card(
            modifier = Modifier
                .offset { IntOffset(swipeOffset.roundToInt(), 0) }
                .fillMaxWidth()
                .clickable {
                    if (showDeleteIcon) {
                        swipeOffset = 0f
                        showDeleteIcon = false
                    } else {
                        isPressed = !isPressed
                        if (isDeleting != "true") {
                            navController.navigate("progress/${habit.id}/Overall")
                        } else {
                            onSelect(isPressed)
                        }
                    }
                },
            colors = CardDefaults.cardColors(containerColor = pressedBackgroundColor),
            shape = RoundedCornerShape(20.dp)
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
                    MainTitleDisplay(habit.name, habitCardViewModel)
                    StreakDisplay(userData)
                    ArrowDirectionDisplay(habitCardViewModel)
                }
            }
        }
        // IconButton in the upper right corner
        IconButton(
            onClick = {
                dataViewModel.setHabit(habit)
                navController.navigate("addData")
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
        ) {
            Icon(notificationIcon, contentDescription = "Notification Icon")
        }
    }
}

@Composable
fun MainTitleDisplay(habitName: String, viewModel: HabitCardViewModel) {
    val progressPercentage = viewModel.calculateDeadlineRatio()
    Column {
        Text(
            text = habitName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = progressPercentage?.let {
                "${it}% Complete!"
            } ?: "0% Complete",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun StreakDisplay(userData: UserData?) {
    val streak = userData?.streak ?: 0
    Column {
        Text(
            text = "$streak Day${if (streak != 1) "s" else ""}",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Streak 🔥",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ArrowDirectionDisplay(viewModel: HabitCardViewModel) {
    val (upOrDown, arrowColor) = viewModel.getArrowAndColor()
    Column {
        Text(
            text = buildAnnotatedString {
                pushStyle(SpanStyle(color = arrowColor, fontSize = 24.sp))
                append(upOrDown)
                pop()
            }
        )
    }
}

@Composable
fun DeleteButton(
    habit: Habit,
    homeViewModel: HomeViewModel,
    navController: NavController,
    context: Context,
    onDeleted: () -> Unit
) {
    IconButton(
        onClick = {
            homeViewModel.deleteHabits(setOf(habit.id)) {
                navController.navigate("home/false")
                Toast.makeText(context, "Habit deleted", Toast.LENGTH_SHORT).show()
                onDeleted()
            }
        }
    ) {
        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
    }
}



