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
import com.github.mikephil.charting.data.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.floor
import kotlin.math.roundToInt


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
                        composable("progress/{habitId}/{span}") { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                            val span = backStackEntry.arguments?.getString("span") ?: ""
                            ProgressScreen(navController = navController, habitId = habitId, span = span, sharedPreferences = sharedPreferences)
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

fun deleteHabitsFromFirestore(user: FirebaseUser, selectedHabits: Set<String>, db: FirebaseFirestore, onComplete: () -> Unit) {
    val userRef = db.collection("users").document(user.uid)

    // Start a batch to perform both the deletion operations atomically
    val batch = db.batch()

    selectedHabits.forEach { habitId ->
        // Remove habit from the user's habits array
        batch.update(userRef, "habits", FieldValue.arrayRemove(habitId))

        // Also, delete the corresponding habit document from the "habits" collection
        val habitRef = db.collection("habits").document(habitId)
        batch.delete(habitRef)
    }

    // Commit the batch operation
    batch.commit().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("deleteHabits", "Successfully deleted habits and documents")
            onComplete()  // Reload the habits after successful deletion
        } else {
            Log.e("deleteHabits", "Error deleting habits and documents", task.exception)
        }
    }
}

/*fun generateGoalDataForFirestore(duration: Int, start: Float, end: Float): List<Map<String, Any>> {
    val goalData = mutableListOf<Map<String, Any>>()

    // Calculate the step for the y-values
    val step = (end - start) / (duration - 1)  // The step size between each entry

    // Generate goal data and store it as a list of maps
    for (i in 0 until duration) {
        val x = (i).toFloat()  // X values starting from 1f up to duration
        val y = start + (i * step)  // Calculate Y-value based on the step

        // Add each entry as a map (Firestore-compatible format)
        val entryMap = mapOf(
            "x" to x,
            "y" to y
        )
        goalData.add(entryMap)
    }

    return goalData
}*/

fun generateGoalDataForFirestore(
    duration: Int,
    start: Float,
    end: Float,
    trendType: String,
    stepCount: Int,
    burstCount: Int,
    precision: String
): List<Map<String, Any>> {
    val goalData = mutableListOf<Map<String, Any>>()
    when (trendType) {
        "steadyIncline" -> {
            // Steady Incline: Gradually increase or decrease
            val step = (end - start) / (duration - 1)  // The step size between each entry

            for (i in 0 until duration) {
                val x = (i).toFloat()  // X values starting from 1f up to duration
                val y = start + (i * step)  // Calculate Y-value based on the step

                // Add each entry as a map (Firestore-compatible format)
                val entryMap = mapOf(
                    "x" to x,
                    "y" to y
                )
                goalData.add(entryMap)
            }
        }

        "stepIntervals" -> {
            // Step Intervals: Increase for a set number of steps, then stay constant, then increase again
            val xStepSize = duration.toFloat() / stepCount.toFloat() // Step size for each interval
            val yStepSize = (2 * (end - start)) / stepCount.toFloat()
            val incrementBy = yStepSize / xStepSize
            var yValue = start
            var yTemp = start
            var i = 0
            var changing = true

            while (i < duration) {
                for (j in 0..(xStepSize.toInt() / 2)) {
                    val x = (i).toFloat()
                    if (changing) {
                        yValue += incrementBy
                    }
                    if (precision == "tenths") {
                        yTemp = (yValue * 10).roundToInt() / 10f
                    }
                    else if (precision == "hundredths") {
                        yTemp = (yValue * 100).roundToInt() / 100f
                    }
                    else {
                        yTemp = yValue.roundToInt().toFloat()
                    }
                    if ((end < start && yTemp < end) || (end > start && yTemp > end)) {
                        yTemp = end
                    }
                    if (x <= duration) {
                        val entryMap = mapOf(
                            "x" to x,
                            "y" to yTemp
                        )
                        goalData.add(entryMap)
                    }
                    i++
                }
                changing = !changing
            }
        }

        "burstIntervals" -> {
            // Burst Intervals: Increase and decrease rapidly
            val stepSize = (end - start) / (duration / 4)  // Step size for each burst
            var yValue = start
            var increasing = true  // Flag to switch between increasing and decreasing bursts

            for (i in 0 until duration) {
                val x = (i).toFloat()

                // Burst intervals: Alternate between increasing and decreasing the value
                if (increasing) {
                    yValue += stepSize
                } else {
                    yValue -= stepSize
                }

                // After a burst, reverse the direction
                if (i % (duration / 4) == 0) {
                    increasing = !increasing
                }

                // Add each entry as a map (Firestore-compatible format)
                val entryMap = mapOf(
                    "x" to x,
                    "y" to yValue
                )
                goalData.add(entryMap)
            }
        }

        else -> {
            // Default behavior for unrecognized trend types (you could also throw an exception)
            throw IllegalArgumentException("Invalid trend type: $trendType")
        }
    }

    return goalData
}








