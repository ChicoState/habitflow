package com.example.habitflow

import android.util.Log.*
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController, auth: FirebaseAuth) {
	val context = LocalContext.current
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var isLoading by remember { mutableStateOf(false) }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.Center
	) {
		Text("HabitFlow Login", style = MaterialTheme.typography.headlineLarge)
		Spacer(modifier = Modifier.height(16.dp))

		OutlinedTextField(
			value = email,
			onValueChange = { email = it },
			label = { Text("Email") },
			modifier = Modifier.fillMaxWidth()
		)
		Spacer(modifier = Modifier.height(8.dp))

		OutlinedTextField(
			value = password,
			onValueChange = { password = it },
			label = { Text("Password") },
			modifier = Modifier.fillMaxWidth()
		)
		Spacer(modifier = Modifier.height(16.dp))

		Button(
			onClick = {
				isLoading = true
				auth.signInWithEmailAndPassword(email, password)
					.addOnCompleteListener { task ->
						isLoading = false
						if (task.isSuccessful) {
							val user: FirebaseUser? = auth.currentUser
							Toast.makeText(context, "Welcome ${user?.email}", Toast.LENGTH_SHORT).show()
							navController.navigate("home/false") // Navigate to HomeScreen
						} else {
							Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
						}
					}
			},
			modifier = Modifier.fillMaxWidth(),
			enabled = !isLoading
		) {
			Text(if (isLoading) "Logging in..." else "Login")
		}
		Spacer(modifier = Modifier.height(8.dp))

		TextButton(onClick = {
			isLoading = true

			if (email.isNullOrBlank() || password.isNullOrBlank())
				Toast.makeText(context, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()

			auth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener { task ->
					if (task.isSuccessful) {
						val user = auth.currentUser
						val userDoc = FirebaseFirestore.getInstance().collection("users").document(user!!.uid)

						val userData = hashMapOf(
							"email" to user.email,
							"name" to "",
							"age" to 0,
							"gender" to "",
							"habits" to listOf<String>()
						)

						userDoc.set(userData)
							.addOnSuccessListener {
								d("Firebase", "User profile created")
								navController.navigate("profileSetup")
							}
							.addOnFailureListener { e ->
								e("Firebase", "Error writing document", e)
							}
					} else {
						e("FirebaseAuth", "Error: ${task.exception?.message}")
						Toast.makeText(context, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
					}
				}
		}) {
			Text("No account? Sign up")
		}
	}
}
