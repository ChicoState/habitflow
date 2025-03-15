package com.example.habitflow

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(navController: NavController, auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val errorMessage by remember { mutableStateOf<String?>(null) }

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
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user != null) {
                            val db = FirebaseFirestore.getInstance()
                            val userDoc = db.collection("users").document(user.uid)

                            val userData = hashMapOf(
                                "email" to (user.email ?: ""),
                                "name" to "",
                                "age" to 0,
                                "gender" to ""
                            )

                            userDoc.set(userData)
                                .addOnSuccessListener {
                                    println("User profile created successfully")
                                    navController.navigate("profileSetup")
                                }
                                .addOnFailureListener { e ->
                                    println("Firestore error: ${e.message}")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Sign-Up Error: ${e.localizedMessage}")
                    }
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