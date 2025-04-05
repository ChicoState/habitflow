package com.example.habitflow

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habitflow.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignUpScreen(navController: NavController, auth: FirebaseAuth) {
    val viewModel: AuthViewModel = viewModel()
    val signUpState by viewModel.signUpState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(signUpState) {
        signUpState?.let { result ->
            result.onSuccess {
                navController.navigate("profileSetup")
            }.onFailure {
                errorMessage = it.localizedMessage
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = {
                errorMessage = null
                viewModel.signUp(email, password)
            }
        ) {
            Text("Sign Up")
        }

        if (errorMessage != null) {
            Text(errorMessage!!, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Log in")
        }
    }
}