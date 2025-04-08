package com.example.habitflow

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.habitflow.viewmodel.AddDataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDataScreen(
    viewModel: AddDataViewModel,
    navController: NavController,
    sharedPreferences: SharedPreferences
) {
    // Access the entire habit object from ViewModel
    val habit = viewModel.retrieveHabit()
    var dataValue by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    habit?.let {
                        Text(
                            text = "Update Habit: ${it.name}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    } ?: run {
                    // If habit is null, show an error
                    Text(text = "No habit data available")
                }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // TextField for entering the float value
                    OutlinedTextField(
                        value = dataValue,
                        onValueChange = { newValue ->
                            dataValue = newValue // Update the state when text changes
                        },
                        label = { Text("Enter Data") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button to submit data
                    Button(
                        onClick = {
                            val floatValue = dataValue.toFloatOrNull() // Convert string to float

                            if (floatValue != null) {
                                // If valid, save data
                                viewModel.saveData(floatValue)
                            } else {
                                Log.e("SaveData", "Invalid float value entered")
                            }
                            navController.navigate("home/")
                                  },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Data")
                    }
                }
            }
        }
    }
}
