package com.example.habitflow

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitflow.viewmodel.AuthViewModel
import com.example.habitflow.viewmodel.ProfileSetupViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileSetupScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance().currentUser
    val viewModel: ProfileSetupViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile Setup", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = viewModel.age,
            onValueChange = { viewModel.age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = viewModel.gender,
            onValueChange = { viewModel.gender = it },
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                auth?.uid?.let { uid ->
                    viewModel.validateAndSave(
                        uid = uid,
                        onSuccess = {
                            Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home/false")
                        },
                        onFailure = {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                } ?: run {
                    viewModel.error = "User not authenticated."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        viewModel.error?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
