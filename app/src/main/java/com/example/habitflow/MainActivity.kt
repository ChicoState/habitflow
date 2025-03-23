package com.example.habitflow

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.habitflow.ui.theme.HabitflowTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase



class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("habitflow_prefs", Context.MODE_PRIVATE) // ✅ Load SharedPreferences

        setContent {
            val isDarkMode = remember { mutableStateOf(sharedPreferences.getBoolean("dark_mode", false)) } // ✅ Get stored value

            HabitflowTheme(darkTheme = isDarkMode.value) {  // ✅ Pass darkMode state
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentUser = remember { auth.currentUser }

                    NavHost(
                        navController,
                        startDestination = if (currentUser != null) "home/false" else "login"
                    ) {
                        // ✅ Login and Sign-Up Screens
                        composable("login") { LoginScreen(navController, auth) }
                        composable("signUp") { SignUpScreen(navController, auth) }
                        composable("profileSetup") { ProfileSetupScreen(navController) }

                        // ✅ Main App Screens
                        composable("home/{isDeleting}") { backStackEntry ->
                            val isDeletingArg = backStackEntry.arguments?.getString("isDeleting") ?: "false"
                            HomeScreen(navController = navController, goodHabit = "", isDeleting = isDeletingArg)
                        }
                        composable("addHabit") { AddHabitScreen(navController = navController, sharedPreferences = sharedPreferences) }
                        composable("progress/{habit}/{span}") { backStackEntry ->
                            val habit = backStackEntry.arguments?.getString("habit") ?: ""
                            val span = backStackEntry.arguments?.getString("span") ?: ""
                            ProgressScreen(navController = navController, habit = habit, span = span, sharedPreferences = sharedPreferences)
                        }
                        composable("settings") {
                            SettingsScreen(navController, isDarkMode, sharedPreferences) // ✅ Pass darkMode state and SharedPreferences
                        }
                    }
                }
            }
        }
    }
}

// ✅ Firebase Firestore Functions
//data class Habit(val name: String, val description: String, val type: String)

fun saveHabitsToFirestore(user: FirebaseUser, newHabits: List<String>) {
    val db = FirebaseFirestore.getInstance()
    val userDoc = db.collection("users").document(user.uid)

    userDoc.update("habits", newHabits)
        .addOnSuccessListener {
            println("Habits successfully saved to Firestore!")
        }
        .addOnFailureListener { e ->
            println("Error saving habits: $e")
        }
}

fun loadHabitsFromFirestore(user: FirebaseUser, onSuccess: (List<String>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userDoc = db.collection("users").document(user.uid)

    userDoc.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val habitsList = (document.get("habits") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                println("Fetched habits from Firestore: $habitsList")  // Debugging output
                onSuccess(habitsList)
            } else {
                println("No habit data found for user.")
                onSuccess(emptyList())
            }
        }
        .addOnFailureListener { e ->
            println("Error loading habits: ${e.message}")
            onSuccess(emptyList())
        }
}

fun getHabitFromFirestore(habitId: String, onComplete: (Habit?) -> Unit) {
    val db = Firebase.firestore
    val habitRef = db.collection("habits").document(habitId)

    habitRef.get().addOnSuccessListener { document ->
        if (document.exists()) {
            val habit = document.toObject(Habit::class.java) // Assuming you have a Habit data class
            onComplete(habit)
        } else {
            onComplete(null)
        }
    }.addOnFailureListener { exception ->
        println("Error getting habit: $exception")
        onComplete(null)
    }
}

fun moveToPastHabits(user: FirebaseUser, selectedHabits: Set<String>, db: FirebaseFirestore, onComplete: () -> Unit) {
    val userRef = db.collection("users").document(user.uid)

    // Start a batch to perform both the array updates atomically
    val batch = db.batch()

    selectedHabits.forEach { habitId ->
        // Update the user's document by moving the habit to pastHabits and removing it from habits
        batch.update(userRef,
            "habits", FieldValue.arrayRemove(habitId),  // Remove habit from habits array
            "pastHabits", FieldValue.arrayUnion(habitId)  // Add habit to pastHabits array
        )
    }

    // Commit the batch operation
    batch.commit().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("moveToPast", "Successfully moved to past habits")
            onComplete()  // Reload the habits after successful move
        } else {
            Log.e("moveToPast", "Error moving to past habits", task.exception)
        }
    }
}




