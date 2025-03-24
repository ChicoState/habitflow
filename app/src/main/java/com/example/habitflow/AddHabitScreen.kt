@file:OptIn(ExperimentalMaterial3Api::class)
// This is important for the pop-up BasicAlertDialog for some reason

package com.example.habitflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.firestore.FieldValue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp


@Composable
fun AddHabitScreen(navController: NavController, sharedPreferences: SharedPreferences) {
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }  // New description field
    var isGoodHabit by remember { mutableStateOf(false) }
    var showNameErrorDialog by remember { mutableStateOf(false) }
    var showDescErrorDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isBadHabit by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf(0) }
    var frequency by remember { mutableStateOf(0) }
    var reminders by remember { mutableStateOf(false) }
    var texts by remember { mutableStateOf(false) }
    var hourly by remember { mutableStateOf(false) }
    var daily by remember { mutableStateOf(false) }
    var weekly by remember { mutableStateOf(false) }
    var custom by remember { mutableStateOf(false) }
    var startAmount by remember { mutableStateOf(0f) }
    var endAmount by remember { mutableStateOf(0f) }
    var trainingType by remember { mutableStateOf("") }
    var steadyIncline by remember { mutableStateOf(false) }
    var stepIntervals by remember { mutableStateOf(false) }
    var burstIntervals by remember { mutableStateOf(false) }
    var stepCount by remember { mutableStateOf(0) }
    var burstCount by remember { mutableStateOf(0) }
    var units by remember { mutableStateOf("") }  // New description field
    var wholeNumbers by remember { mutableStateOf(false) }
    var tenths by remember { mutableStateOf(false) }
    var hundredths by remember { mutableStateOf(false) }
    var precision by remember { mutableStateOf("") }  // New description field


    // ✅ Get the current Dark Mode value from SharedPreferences
    var darkMode by remember {
        mutableStateOf(sharedPreferences.getBoolean("dark_mode", false))
    }
    val scrollState = rememberScrollState()
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
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
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            var startAmountText by remember { mutableStateOf("") }
            var endAmountText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = startAmountText,
                onValueChange = {
                    startAmountText = it
                    startAmount = startAmountText.toFloatOrNull() ?: 0f // Convert to float, or set to 0 if empty
                },
                label = { Text("Start Amount") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number // Set keyboard type to number
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            OutlinedTextField(
                value = endAmountText,
                onValueChange = {
                    endAmountText = it
                    endAmount = endAmountText.toFloatOrNull() ?: 0f // Convert to float, or set to 0 if empty
                },
                label = { Text("Goal Amount") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number // Set keyboard type to number
                ),
                modifier = Modifier
                    .weight(1f)
            )
        }
            var durationText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = durationText,
                onValueChange = { newValue ->
                    newValue.toIntOrNull()?.let {
                        if (it in 1..1000) {
                            durationText = newValue
                            duration = it
                        }
                    } },
                label = { Text("Number of days to reach goal") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number // Set keyboard type to number
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 10.dp)
            )
        var frequencyText by remember { mutableStateOf("") }
        OutlinedTextField(
            value = frequencyText,
            onValueChange = { newValue ->
                newValue.toIntOrNull()?.let {
                    if (it in 1..1000) {
                        frequencyText = newValue
                        frequency = it
                    }
                } },
            label = { Text("Check in every (Enter Value) days") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number // Set keyboard type to number
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 10.dp)
        )
        OutlinedTextField(
            value = units,
            onValueChange = { units = it },
            label = { Text("Units (Ex: Miles)") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 10.dp)
        )
        Row(
            modifier = Modifier
                .padding(top = 8.dp).
                padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Precision: ",
            modifier = Modifier.weight(.20f)
            )
            Box(modifier = Modifier
                .weight(.38f)
                .padding(end = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = wholeNumbers,
                        onCheckedChange = {
                            // When "Good Habit" is checked, uncheck "Bad Habit"
                            wholeNumbers = true
                            tenths = false
                            hundredths = false
                            precision = "wholeNumbers"
                        }
                    )
                    Text(
                        text = "Whole Numbers",
                        fontSize = 12.sp // Adjust the font size to your desired value
                    )
                }
            }
            Box(modifier = Modifier
                .weight(.20f)
                .padding(end = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = tenths,
                        onCheckedChange = {
                            // When "Bad Habit" is checked, uncheck "Good Habit"
                            tenths = true
                            wholeNumbers = false
                            hundredths = false
                            precision = "tenths"
                        }
                    )
                    Text(
                        text = "0.1",
                        fontSize = 12.sp // Adjust the font size to your desired value
                    )
                }
            }
            Box(modifier = Modifier
                .weight(.20f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hundredths,
                        onCheckedChange = {
                            // When "Bad Habit" is checked, uncheck "Good Habit"
                            tenths = false
                            wholeNumbers = false
                            hundredths = true
                            precision = "hundredths"
                        }
                    )
                    Text(
                        text = "0.01",
                        fontSize = 12.sp // Adjust the font size to your desired value
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(top = 10.dp).
                padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = isGoodHabit,
                onCheckedChange = {
                    // When "Good Habit" is checked, uncheck "Bad Habit"
                    isGoodHabit = true
                    isBadHabit = false
                }
            )
            Text("Good Habit")
            Spacer(modifier = Modifier.width(12.dp))
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
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp).
                padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("How would you like to work towards your goal?")
        }
        var steadyInclineSelected by remember { mutableStateOf(false) }
        var stepIntervalsSelected by remember { mutableStateOf(false) }
        var burstIntervalsSelected by remember { mutableStateOf(false) }

        val selectedColor = Color(0xFF00897B) // Teal for selected
        val unselectedColor = Color(0xFFB0BEC5).copy(alpha = 0.6f) // Grey for unselected

        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, // Align the text and buttons vertically centered
            horizontalArrangement = Arrangement.Start
        ) {
            // "Steady Incline" Button
            val straightLineImage: Painter = painterResource(id = R.drawable.straightlineup) // Assuming steadyinclineup.png is in res/drawable
            Button(
                onClick = {
                    steadyInclineSelected = true
                    stepIntervalsSelected = false
                    burstIntervalsSelected = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (steadyInclineSelected) selectedColor else unselectedColor
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp) // Optional rounded corners
            ) {
                Box(
                    contentAlignment = Alignment.Center // Align content (text) in the center
                    //modifier = Modifier.fillMaxSize() // Make Box take the full size of the button
                ) {
                    Image(
                        painter = straightLineImage,
                        contentDescription = "Steady Incline Image",  // Provide an accessible content description
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    )
                    Text(
                        text = "Steady Incline",
                        modifier = Modifier.fillMaxWidth(), // Ensure text takes full width for centering
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }
            // "Step Intervals" Button
            val stepIntervalsImage: Painter = painterResource(id = R.drawable.stepsup)
            Button(
                onClick = {
                    steadyInclineSelected = false
                    stepIntervalsSelected = true
                    burstIntervalsSelected = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (stepIntervalsSelected) selectedColor else unselectedColor
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp) // Optional rounded corners
            ) {
                Box(
                    contentAlignment = Alignment.Center // Align content (text) in the center
                    //modifier = Modifier.fillMaxSize() // Make Box take the full size of the button
                ) {
                    Image(
                        painter = stepIntervalsImage,
                        contentDescription = "Step Intervals Image",  // Provide an accessible content description
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    )
                    Text(
                        text = "Step Intervals",
                        modifier = Modifier.fillMaxWidth(), // Ensure text takes full width for centering
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }

            // "Burst Intervals" Button
            val burstIntervalsImage: Painter = painterResource(id = R.drawable.burstsup)
            Button(
                onClick = {
                    steadyInclineSelected = false
                    stepIntervalsSelected = false
                    burstIntervalsSelected = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (burstIntervalsSelected) selectedColor else unselectedColor
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp) // Optional rounded corners
            ) {
                Box(
                    contentAlignment = Alignment.Center // Align content (text) in the center
                    //modifier = Modifier.fillMaxSize() // Make Box take the full size of the button
                ) {
                    Image(
                        painter = burstIntervalsImage,
                        contentDescription = "Burst Intervals Image",  // Provide an accessible content description
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    )
                    Text(
                        text = "Burst Intervals",
                        modifier = Modifier.fillMaxWidth(), // Ensure text takes full width for centering
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }
        }
        if (stepIntervalsSelected) {
            var stepCountText by remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "How many steps would you like to take in reaching your goal?",
                    modifier = Modifier.weight(.65f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = stepCountText,
                    onValueChange = {
                        stepCountText = it
                        stepCount = stepCountText.toIntOrNull() ?: 0 // Convert to float, or set to 0 if empty
                    },
                    label = { Text(
                        text = "1 - Duration/2",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number // Set keyboard type to number
                    ),
                    modifier = Modifier
                        .weight(.35f)
                        .padding(start = .16.dp)
                )
            }

        }
        if (burstIntervalsSelected) {
            var burstCountText by remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "How many bursts would you like to have in reaching your goal?",
                    modifier = Modifier.weight(.66f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = burstCountText,
                    onValueChange = {
                        burstCountText = it
                        burstCount = burstCountText.toIntOrNull() ?: 0 // Convert to float, or set to 0 if empty
                    },
                    label = { Text(
                        text = "1 - Duration/2",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number // Set keyboard type to number
                    ),
                    modifier = Modifier
                        .weight(.34f)
                        .padding(start = .16.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp).
                padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = reminders,
                onCheckedChange = {
                    // When "Bad Habit" is checked, uncheck "Good Habit"
                    reminders = it
                }
            )
            Text("Reminders")
        }
        if (reminders) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = hourly,
                    onCheckedChange = {
                        // When "Bad Habit" is checked, uncheck "Good Habit"
                        hourly = it
                        daily = false
                        weekly = false
                        custom = false
                    }
                )
                Text("Hourly")
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = daily,
                    onCheckedChange = {
                        // When "Bad Habit" is checked, uncheck "Good Habit"
                        daily = it
                        hourly = false
                        weekly = false
                        custom = false
                    }
                )
                Text("Daily")
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = weekly,
                    onCheckedChange = {
                        // When "Bad Habit" is checked, uncheck "Good Habit"
                        weekly = it
                        hourly = false
                        daily = false
                        custom = false
                    }
                )
                Text("Weekly")
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically, // Align the text and checkbox vertically centered
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = custom,
                    onCheckedChange = {
                        // When "Bad Habit" is checked, uncheck "Good Habit"
                        custom = it
                        hourly = false
                        daily = false
                        weekly = false
                    }
                )
                Text("Custom")
            }
        }
        Button(
            onClick = {
                if (habitName.isBlank()) {
                    showNameErrorDialog = true
                    return@Button
                }
                val user = FirebaseAuth.getInstance().currentUser
                trainingType =
                    if (stepIntervalsSelected) { "stepIntervals" }
                    else if (burstIntervalsSelected) { "burstIntervals" }
                    else { "steadyIncline" }
                val goalData = generateGoalDataForFirestore(duration, startAmount, endAmount, trainingType, stepCount, burstCount, precision)
                if (user != null) {
                    val db = FirebaseFirestore.getInstance()
                    val habitData = hashMapOf(
                        "name" to habitName,
                        "description" to habitDescription,
                        "type" to if (isGoodHabit) "good" else "bad",
                        "duration" to duration, // Convert to integer or default to 0
                        "frequency" to frequency, // Convert to integer or default to 0
                        "startAmount" to startAmount,
                        "goalAmount" to endAmount,
                        "goalData" to goalData,
                        "trainingType" to trainingType,
                        "stepCount" to stepCount,
                        "burstCount" to burstCount,
                        "units" to units,
                        "precision" to precision
                    )

                    // Step 1: Create the habit in the 'habits' collection
                    db.collection("habits")
                        .add(habitData)
                        .addOnSuccessListener { documentReference ->
                            println("Habit created successfully. Document ID: ${documentReference.id}")
                            // Step 2: Add the habit ID to the current user's document
                            val userDoc = db.collection("users").document(user.uid)
                            userDoc.update("habits", FieldValue.arrayUnion(documentReference.id))
                                .addOnSuccessListener {
                                    // Navigate back to home screen
                                    navController.navigate("home/")
                                }
                                .addOnFailureListener { e ->
                                    println("Error updating habits: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            println("Error creating habit: ${e.message}")
                        }
                    }
                },
                /*val user = FirebaseAuth.getInstance().currentUser
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
            },*/
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally)
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

@Composable
fun CircularCheckbox(checked: Boolean, onCheckedChange: () -> Unit) {

}


