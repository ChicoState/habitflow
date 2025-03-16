package com.example.habitflow

import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons.AutoMirrored.Filled
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SettingsScreen(
    navController: NavController,
    isDarkMode: MutableState<Boolean>,
    sharedPreferences: SharedPreferences // ✅ Passed from MainActivity
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // ✅ Get the current Dark Mode value from SharedPreferences
    var darkMode by remember {
        mutableStateOf(sharedPreferences.getBoolean("dark_mode", false))
    }

    // Fetch user data
    LaunchedEffect(user) {
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
                name = doc.getString("name") ?: ""
                age = doc.getLong("age")?.toString() ?: "0"
                gender = doc.getString("gender") ?: ""
                email = user.email ?: ""
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Back Button
        IconButton(
            onClick = { navController.navigate("home/false") },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Filled.ArrowBack,
                contentDescription = "Back",
                tint = if(darkMode) Color.White else Color.Black
            )
        }

        Text("Settings", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ Dark Mode Toggle (Now Persists)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark Mode")
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = darkMode,
                onCheckedChange = { enabled ->
                    darkMode = enabled
                    isDarkMode.value = enabled

                    // ✅ Save to SharedPreferences
                    sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()

                    Toast.makeText(context, "Dark Mode Updated", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Edit Profile Information
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email (Read-only)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth()
        )

        // ✅ Update Profile Button
        Button(
            onClick = {
                if (user != null) {
                    val updatedData: Map<String, Any> = hashMapOf(
                        "name" to name,
                        "age" to (age.toIntOrNull() ?: 0),
                        "gender" to gender,
                        "email" to email
                    )

                    db.collection("users").document(user.uid)
                        .update(updatedData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
        ) {
            Text("Update Profile")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logout Button
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("login") { popUpTo("home") { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Delete Account Button
        Button(
            onClick = {
                user?.delete()?.addOnSuccessListener {
                    db.collection("users").document(user.uid).delete()
                    Toast.makeText(context, "Account Deleted", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") { popUpTo("home") { inclusive = true } }
                }?.addOnFailureListener {
                    Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Account", color = Color.White)
        }
    }
}