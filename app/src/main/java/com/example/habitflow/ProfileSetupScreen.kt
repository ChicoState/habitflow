@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.habitflow

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileSetupScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
        )

        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") }
        )

        Button(
            onClick = {
                val userData = mutableMapOf<String, Any>(
                    "name" to name,
                    "age" to (age.toIntOrNull() ?: 0),  // Explicit conversion
                    "gender" to gender
                )
                if (user != null) {
                    db.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            println("User data saved successfully.")
                            navController.navigate("home/false")  // ✅ Navigate only after success
                        }
                        .addOnFailureListener { e ->
                            println("Error saving profile data: ${e.message}")
                        }
                }
            }
        ) {
            Text("Save & Continue")
        }
    }
}