package com.example.habitflow.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitflow.AddDataScreen
import com.example.habitflow.AddHabitScreen
import com.example.habitflow.HomeScreen
import com.example.habitflow.LoginScreen
import com.example.habitflow.ProfileSetupScreen
import com.example.habitflow.ProgressScreen
import com.example.habitflow.SettingsScreen
import com.example.habitflow.SignUpScreen
import com.example.habitflow.viewmodel.AddDataViewModel


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        enableEdgeToEdge()

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("habitflow_prefs", Context.MODE_PRIVATE)

        setContent {
            val isDarkMode = remember { mutableStateOf(sharedPreferences.getBoolean("dark_mode", false)) }

            HabitflowTheme(darkTheme = isDarkMode.value) {  // ✅ Pass darkMode state
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentUser = remember { auth.currentUser }
                    val addDataViewModel: AddDataViewModel = viewModel()

                    NavHost(
                        navController,
                        startDestination = if (currentUser != null) "home/false" else "login"
                    ) {
                        // ✅ Login and Sign-Up Screens
                        composable("login") { LoginScreen(navController) }
                        composable("signUp") { SignUpScreen(navController, auth) }
                        composable("profileSetup") { ProfileSetupScreen(navController) }

                        // ✅ Main App Screens
                        composable("home/{isDeleting}") { backStackEntry ->
                            val isDeletingArg = backStackEntry.arguments?.getString("isDeleting") ?: "false"
                            HomeScreen(addDataViewModel = addDataViewModel, navController = navController, isDeleting = isDeletingArg)
                        }
                        composable("addHabit") { AddHabitScreen(navController = navController, sharedPreferences = sharedPreferences) }
                        composable("progress/{habitId}/{span}") { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                            val span = backStackEntry.arguments?.getString("span") ?: ""
                            ProgressScreen(navController = navController, habitId = habitId, span = span, sharedPreferences = sharedPreferences)
                        }
                        composable("settings") {
                            SettingsScreen(navController, isDarkMode, sharedPreferences)
                        }
                        composable("addData") {
                            AddDataScreen(viewModel = addDataViewModel, navController = navController)
                        }
                    }
                }
            }
        }
    }
}