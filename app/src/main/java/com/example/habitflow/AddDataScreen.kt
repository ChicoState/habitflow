package com.example.habitflow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import com.example.habitflow.model.Habit
import com.example.habitflow.model.UserData
import com.example.habitflow.viewmodel.AddDataViewModel

@Composable
fun AddDataScreen(
    viewModel: AddDataViewModel,
    navController: NavController
) {
    val habit = viewModel.retrieveHabit()
    val userData = viewModel.retrieveUserData()
    var dataValue by remember { mutableStateOf("") }
    var days by remember { mutableIntStateOf(0) }
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(0) }
    var yes by remember { mutableStateOf(false) }
    var no by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(true) }



    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            HabitTopBar(habit = habit, navController = navController)
            userData?.let {
                if (!it.promptEntry) {
                    DataEntrySection(
                        userData = it,
                        dataValue = dataValue,
                        days = days,
                        hours = hours,
                        minutes = minutes,
                        seconds = seconds,
                        yes = yes,
                        no = no,
                        onDataValueChange = { newDataValue -> dataValue = newDataValue },
                        onDaysChange = { newDays -> days = newDays },
                        onHoursChange = { newHours -> hours = newHours },
                        onMinutesChange = { newMinutes -> minutes = newMinutes },
                        onSecondsChange = { newSeconds -> seconds = newSeconds },
                        onYesChecked = { yes = true; no = false },
                        onNoChecked = { no = true; yes = false },
                        buttonText = "Save Data",
                        onSubmitClick = {
                            when (it.trackingMethod) {
                                "numeric" -> viewModel.saveData(dataValue.toFloatOrNull())
                                "timeBased" -> viewModel.timeBasedDataSaver(days, hours, minutes, seconds)
                                "binary" -> viewModel.binaryDataSaver(yes)
                            }
                            navController.navigate("home/")
                        }
                    )
                } else {
                    if (showPrompt) {
                        UpdatePrompt(
                            currentEntry = viewModel.getLastEntryY(),
                            onConfirm = { showPrompt = false },
                            navController = navController
                        )
                    } else {
                        DataEntrySection(
                            userData = it,
                            dataValue = dataValue,
                            days = days,
                            hours = hours,
                            minutes = minutes,
                            seconds = seconds,
                            yes = yes,
                            no = no,
                            onDataValueChange = { newDataValue -> dataValue = newDataValue },
                            onDaysChange = { newDays -> days = newDays },
                            onHoursChange = { newHours -> hours = newHours },
                            onMinutesChange = { newMinutes -> minutes = newMinutes },
                            onSecondsChange = { newSeconds -> seconds = newSeconds },
                            onYesChecked = { yes = true; no = false },
                            onNoChecked = { no = true; yes = false },
                            buttonText = "Update Entry",
                            onSubmitClick = {
                                when (it.trackingMethod) {
                                    "numeric" -> viewModel.updateLastEntryY(dataValue.toFloat())
                                    "timeBased" -> viewModel.timeBasedDataUpdater(days, hours, minutes, seconds)
                                    "binary" -> viewModel.binaryDataUpdater(yes)
                                }
                                navController.navigate("home/")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTopBar(habit: Habit?, navController: NavController) {
    TopAppBar(
        title = {
            habit?.let {
                Text(
                    text = "Update Habit: ${it.name}",
                    style = MaterialTheme.typography.headlineMedium
                )
            } ?: Text("No habit data available")
        },
        navigationIcon = {
            IconButton(
                onClick = { navController.navigate("home/") },
                modifier = Modifier.size(40.dp).padding(5.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun DataEntrySection(
    userData: UserData,
    dataValue: String,
    days: Int,
    hours: Int,
    minutes: Int,
    seconds: Int,
    yes: Boolean,
    no: Boolean,
    onDataValueChange: (String) -> Unit,
    onDaysChange: (Int) -> Unit,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onYesChecked: () -> Unit,
    onNoChecked: () -> Unit,
    buttonText: String,
    onSubmitClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (userData.trackingMethod) {
                "numeric" -> NumericInput(dataValue, onDataValueChange)
                "timeBased" -> TimeInput(
                    days, hours, minutes, seconds,
                    onDaysChange = onDaysChange,
                    onHoursChange = onHoursChange,
                    onMinutesChange = onMinutesChange,
                    onSecondsChange = onSecondsChange
                )
                "binary" -> BinaryInput(
                    yes = yes,
                    no = no,
                    onYesChecked = onYesChecked,
                    onNoChecked = onNoChecked
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubmitClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun UpdatePrompt(
    currentEntry: Float?,
    onConfirm: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your current entry is $currentEntry",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Would you like to update it?",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Button(
                onClick = onConfirm
            ) {
                Text("Yes, Update")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { navController.navigate("home/") }
            ) {
                Text("No Thanks")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun NumericInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Enter Count/Quantity") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    )
}

@Composable
fun BinaryInput(
    yes: Boolean,
    no: Boolean,
    onYesChecked: () -> Unit,
    onNoChecked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = yes, onCheckedChange = { onYesChecked() })
            Text("Completed")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = no, onCheckedChange = { onNoChecked() })
            Text("Not This Time")
        }
    }
}


@Composable
fun TimeInput(
    days: Int,
    hours: Int,
    minutes: Int,
    seconds: Int,
    onDaysChange: (Int) -> Unit,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimeDropdown("Days", days, onValueChange = onDaysChange, range = 0..31)
        TimeDropdown("Hours", hours, onValueChange = onHoursChange, range = 0..23)
        TimeDropdown("Minutes", minutes, onValueChange = onMinutesChange, range = 0..59)
        TimeDropdown("Seconds", seconds, onValueChange = onSecondsChange, range = 0..59)
    }
}

@Composable
fun TimeDropdown(
    label: String,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.width(85.dp)) {
        OutlinedTextField(
            value = selectedValue.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    Modifier.clickable { expanded = true }
                )
            },
            modifier = Modifier
                .clickable { expanded = true }
                .fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            range.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.toString()) },
                    onClick = {
                        onValueChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}




