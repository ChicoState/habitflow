package com.example.habitflow

import androidx.compose.ui.graphics.Color

data class Habit(
    val name: String = "",
    val description: String = "",
    val duration: Int = 0,
    val type: String = "",
    val frequency: Int = 0
) {
    // Define the backgroundColor as a computed property based on habit type
    val backgroundColor: Color
        get() = if (type == "good") Color(0x40A5D6A7) else Color(0x40FF8A80)
}