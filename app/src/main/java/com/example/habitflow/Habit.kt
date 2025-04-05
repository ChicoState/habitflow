package com.example.habitflow

import androidx.compose.ui.graphics.Color

data class GoalPoint(val x: Float = 0f, val y: Float = 0f)

data class Habit(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val duration: Int = 0,
    val type: String = "",
    val goalAmount: Int = 0,
    val units: String = "",
    val precision: String = "",
    val goalData: List<GoalPoint> = emptyList()
) {
    val backgroundColor: Color
        get() = if (type == "good") Color(0x40A5D6A7) else Color(0x40FF8A80)
}