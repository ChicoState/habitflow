@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.habitflow

import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitflow.viewmodel.AddHabitViewModel
import java.util.Calendar


@Composable
fun AddHabitScreen(navController: NavController, sharedPreferences: SharedPreferences) {
    val viewModel: AddHabitViewModel = viewModel()
    val darkMode = remember { sharedPreferences.getBoolean("dark_mode", false) }

    val scrollState = rememberScrollState()
    var showNameErrorDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Add a Habit",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigate("home/") },
                        modifier = Modifier
                            .size(40.dp)
                            .padding(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            AddHabitInputs(
                habitName = viewModel.habitName,
                onHabitNameChange = { viewModel.habitName = it },
                habitDescription = viewModel.habitDescription,
                onHabitDescriptionChange = { viewModel.habitDescription = it },
                endAmount = viewModel.endAmount,
                onEndAmountChange = { viewModel.endAmount = it },
                duration = viewModel.duration,
                onDurationChange = { viewModel.duration = it },
                wholeNumbers = viewModel.wholeNumbers,
                tenths = viewModel.tenths,
                hundredths = viewModel.hundredths,
                onWholeNumbersToggle = { viewModel.applyPrecision("wholeNumbers") },
                onTenthsToggle = { viewModel.applyPrecision("tenths") },
                onHundredthsToggle = { viewModel.applyPrecision("hundredths") },
                precision = viewModel.precision,
                isGoodHabit = viewModel.isGoodHabit,
                isBadHabit = viewModel.isBadHabit,
                onGoodHabitToggle = { viewModel.isGoodHabit = true; viewModel.isBadHabit = false },
                onBadHabitToggle = { viewModel.isBadHabit = true; viewModel.isGoodHabit = false },
                showDescriptionField = viewModel.showDescriptionField,
                onShowDescriptionToggle = { viewModel.showDescriptionField = it }
            )

            HabitLabelSelector(
                label = viewModel.trackingMethodLabel,
                onLabelSelected = { viewModel.trackingMethodLabel = it }
            )

            CategorySelector(
                selectedCategory = viewModel.category,
                onCategorySelected = { viewModel.category = it }
            )

            DeadlineSelector(
                currentDeadline = viewModel.deadline,
                onDeadlineChange = { viewModel.deadline = it }
            )

            ReminderOptions(
                viewModel = viewModel,
                reminders = viewModel.reminders,
                onRemindersToggle = { viewModel.reminders = it },
                hourly = viewModel.hourly,
                daily = viewModel.daily,
                weekly = viewModel.weekly,
                custom = viewModel.custom,
                onHourlySelected = {
                    viewModel.hourly = true
                    viewModel.daily = false
                    viewModel.weekly = false
                    viewModel.custom = false
                },
                onDailySelected = {
                    viewModel.daily = true
                    viewModel.hourly = false
                    viewModel.weekly = false
                    viewModel.custom = false
                },
                onWeeklySelected = {
                    viewModel.weekly = true
                    viewModel.hourly = false
                    viewModel.daily = false
                    viewModel.custom = false
                },
                onCustomSelected = {
                    viewModel.custom = true
                    viewModel.hourly = false
                    viewModel.daily = false
                    viewModel.weekly = false
                }
            )

            SaveHabitButton(
                viewModel = viewModel,
                navController = navController,
                showNameErrorDialog = { showNameErrorDialog = it }
            )

            if (showNameErrorDialog) {
                BasicAlertDialog(onDismissRequest = { showNameErrorDialog = false }) {
                    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 6.dp) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Habit Name Required", style = MaterialTheme.typography.headlineSmall)
                            Text("Please enter a name for your habit before saving.")
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { showNameErrorDialog = false }) { Text("OK") }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AddHabitInputs(
    habitName: String,
    onHabitNameChange: (String) -> Unit,
    habitDescription: String,
    onHabitDescriptionChange: (String) -> Unit,
    endAmount: Float,
    onEndAmountChange: (Float) -> Unit,
    duration: Int,
    onDurationChange: (Int) -> Unit,
    wholeNumbers: Boolean,
    tenths: Boolean,
    hundredths: Boolean,
    onWholeNumbersToggle: () -> Unit,
    onTenthsToggle: () -> Unit,
    onHundredthsToggle: () -> Unit,
    precision: String,
    isGoodHabit: Boolean,
    isBadHabit: Boolean,
    onGoodHabitToggle: () -> Unit,
    onBadHabitToggle: () -> Unit,
    showDescriptionField: Boolean,
    onShowDescriptionToggle: (Boolean) -> Unit
) {
    val endAmountText = if (endAmount == 0f) {
        ""
    } else {
        endAmount.toString()
    }
    val durationText = if (duration == 0) {
        ""
    } else {
        duration.toString()
    }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ Good/Bad checkboxes moved to top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Checkbox(checked = isGoodHabit, onCheckedChange = { onGoodHabitToggle() })
                    Text("Good Habit")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isBadHabit, onCheckedChange = { onBadHabitToggle() })
                    Text("Bad Habit")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Habit Name
            OutlinedTextField(
                value = habitName,
                onValueChange = onHabitNameChange,
                label = { Text("Habit Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            // ✅ Habit Description appears BETWEEN name field and checkbox when visible
            if (showDescriptionField) {
                OutlinedTextField(
                    value = habitDescription,
                    onValueChange = onHabitDescriptionChange,
                    label = { Text("Habit Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
            }

            // ✅ Checkbox stays under both fields
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showDescriptionField,
                    onCheckedChange = onShowDescriptionToggle
                )
                Text("Add a Description")
            }

        }
    }


@Composable
fun HabitLabelSelector(
    label: String,
    onLabelSelected: (String) -> Unit
) {
    val options = listOf("Yes/No", "Count/Quantity", "Time Spent")
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Habit Label")
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = {
                // Optional: implement explanation dialog logic here
            }) {
                Icon(Icons.Default.Info, contentDescription = "Help")
            }
        }

        Box {
            OutlinedTextField(
                value = label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Choose Label Type") },
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onLabelSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ReminderOptions(
    viewModel: AddHabitViewModel,
    reminders: Boolean,
    onRemindersToggle: (Boolean) -> Unit,
    hourly: Boolean,
    daily: Boolean,
    weekly: Boolean,
    custom: Boolean,
    onHourlySelected: () -> Unit,
    onDailySelected: () -> Unit,
    onWeeklySelected: () -> Unit,
    onCustomSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = reminders,
            onCheckedChange = onRemindersToggle
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Reminders", style = MaterialTheme.typography.bodyLarge)
    }

    if (reminders) {
        ReminderFrequencyOption("Hourly", hourly, onHourlySelected)
        ReminderFrequencyOption("Daily", daily, onDailySelected)
        ReminderFrequencyOption("Weekly", weekly, onWeeklySelected)
        ReminderFrequencyOption("Custom", custom, onCustomSelected)

        if (custom) {
            CustomReminderIntervalSelector()
        }
    }
}

@Composable
fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("General", "Health", "Productivity", "Finance", "Social")
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DeadlineSelector(
    currentDeadline: String,
    onDeadlineChange: (String) -> Unit
) {
    val months = (1..12).map { it.toString().padStart(2, '0') }
    val days = (1..31).map { it.toString().padStart(2, '0') }
    val years = (2024..2040).map { it.toString() }


    val calendar = Calendar.getInstance()
    val todayMonth = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val todayDay = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val todayYear = calendar.get(Calendar.YEAR).toString()

    var showDatePicker by remember { mutableStateOf(false) }

    // State for dropdowns
    var selectedMonth by remember {
        mutableStateOf(
            currentDeadline.takeIf { it.contains("/") }?.split("/")?.getOrNull(0)
                ?: todayMonth
        )
    }
    var selectedDay by remember {
        mutableStateOf(
            currentDeadline.takeIf { it.contains("/") }?.split("/")?.getOrNull(1)
                ?: todayDay
        )
    }
    var selectedYear by remember {
        mutableStateOf(
            currentDeadline.takeIf { it.contains("/") }?.split("/")?.getOrNull(2)
                ?: todayYear
        )
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text("Deadline", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { showDatePicker = !showDatePicker }) {
            Text(
                if (showDatePicker)
                    "Hide End Date Picker"
                else if (currentDeadline.isNotBlank())
                    "End Date: $currentDeadline"
                else
                    "Set End Date"
            )
        }

        if (showDatePicker) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DropdownField("Month", months, selectedMonth) { selectedMonth = it }
                DropdownField("Day", days, selectedDay) { selectedDay = it }
                DropdownField("Year", years, selectedYear) { selectedYear = it }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val deadline = "$selectedMonth/$selectedDay/$selectedYear"
                    onDeadlineChange(deadline)
                    showDatePicker = false
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Done")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .width(115.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 18.sp),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 150.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 14.sp) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}



@Composable
private fun ReminderFrequencyOption(
    label: String,
    checked: Boolean,
    onChecked: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onChecked() }
        )
        Text(label)
    }
}

@Composable
fun CustomReminderIntervalSelector() {
    val viewModel: AddHabitViewModel = viewModel()
    val options = listOf("Hours", "Days")

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)) {

        OutlinedTextField(
            value = viewModel.customReminderValue,
            onValueChange = { viewModel.customReminderValue = it },
            label = { Text("Reminder every...") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box {
            OutlinedTextField(
                value = viewModel.customReminderUnit,
                onValueChange = {},
                readOnly = true,
                label = { Text("Time Unit") },
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.customReminderUnit = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SaveHabitButton(
    viewModel: AddHabitViewModel,
    navController: NavController,
    showNameErrorDialog: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Button(
        onClick = {
            if (viewModel.habitName.isBlank()) {
                showNameErrorDialog(true)
                return@Button
            }

            if (viewModel.custom && viewModel.reminders) {
                val value = viewModel.customReminderValue.toIntOrNull()
                val unit = viewModel.customReminderUnit
                if (value == null || value <= 0 || (unit != "Hours" && unit != "Days")) {
                    println("Invalid custom reminder setup.")
                    return@Button
                }
            }

            viewModel.saveHabitToFirestore(
                context = context,
                onSuccess = { navController.navigate("home/") },
                onFailure = { e -> println("Error saving habit: $e") }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
    ) {
        Text("Save Habit")
    }
}
