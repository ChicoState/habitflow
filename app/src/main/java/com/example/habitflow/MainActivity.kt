package com.example.habitflow

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.habitflow.ui.AddHabitScreen
import com.example.habitflow.ui.theme.HabitflowTheme
import androidx.navigation.navArgument
import androidx.navigation.NavType




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitflowTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    //val sharedPreferences = remember { context.getSharedPreferences("habit_prefs", Context.MODE_PRIVATE) }

                    // Navigation between Home and Add Habit screens
                    NavHost(navController, startDestination = "home/false") {
                        composable("home/{isDeleting}") { backStackEntry ->
                            var isDeletingArg = backStackEntry.arguments?.getString("isDeleting") ?: "false"
                            HomeScreen(navController, goodHabit = "",  isDeleting = isDeletingArg)
                        }
                        composable("addHabit") { AddHabitScreen(navController) }
                        composable("progress/{habit}/{span}") { backStackEntry ->
                            val habit = backStackEntry.arguments?.getString("habit") ?: ""
                            val span = backStackEntry.arguments?.getString("span") ?: ""
                            ProgressScreen(navController, habit = habit, span = span)
                        }
                    }
                }
            }
        }
    }
}