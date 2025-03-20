package com.example.habitflow

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController, auth: FirebaseAuth) {
	val context = LocalContext.current
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var isPasswordVisible by remember { mutableStateOf(false) }
	var isLoading by remember { mutableStateOf(false) }
	var errorMessage by remember { mutableStateOf("") }

	Column(
		modifier = Modifier.fillMaxSize()
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "HabitFlow",
				style = MaterialTheme.typography.headlineLarge,
				color = MaterialTheme.colorScheme.primary
			)
		}

		Spacer(modifier = Modifier.height(16.dp))

		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(32.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				// Title
				Text(
					text = "Sign In",
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.primary
				)

				Spacer(modifier = Modifier.height(16.dp))

				// Email Input Field
				OutlinedTextField(
					value = email,
					onValueChange = { email = it },
					label = { Text("Email") },
					leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email Icon") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true
				)

				Spacer(modifier = Modifier.height(12.dp))

				// Password Input Field
				OutlinedTextField(
					value = password,
					onValueChange = { password = it },
					label = { Text("Password") },
					leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password Icon") },
					trailingIcon = {
						IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
							Icon(
								imageVector = Icons.Filled.Lock, // Keeping Lock icon
								contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password"
							)
						}
					},
					visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
					modifier = Modifier.fillMaxWidth(),
					singleLine = true
				)

				Spacer(modifier = Modifier.height(8.dp))

				if (errorMessage.isNotEmpty()) {
					Text(errorMessage, color = Color.Red, fontSize = 14.sp)
					Spacer(modifier = Modifier.height(8.dp))
				}

				// Login Button
				Button(
					onClick = {
						isLoading = true
						errorMessage = "" // Clear previous errors

						if (email.isBlank() || password.isBlank()) {
							errorMessage = "Email and password cannot be empty"
							isLoading = false
							return@Button
						}

						auth.signInWithEmailAndPassword(email, password)
							.addOnCompleteListener { task ->
								isLoading = false
								if (task.isSuccessful) {
									Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
									navController.navigate("home/false")
								} else {
									errorMessage = "Login Failed. Check your credentials."
								}
							}
					},
					modifier = Modifier
						.fillMaxWidth()
						.shadow(4.dp, RoundedCornerShape(12.dp)),
					shape = RoundedCornerShape(12.dp),
					colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
				) {
					if (isLoading) {
						CircularProgressIndicator(
							color = Color.White,
							strokeWidth = 2.dp,
							modifier = Modifier.size(20.dp)
						)
					} else {
						Text("Sign In", fontSize = 18.sp)
					}
				}

				Spacer(modifier = Modifier.height(12.dp))

				// Sign Up Link
				TextButton(onClick = { navController.navigate("signUp") }) {
					Text("Don't have an account? Sign Up", color = MaterialTheme.colorScheme.secondary)
				}
			}
		}
	}
}



