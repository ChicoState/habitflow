package com.example.habitflow.model

data class GoalPoint(val x: Float = 0f, val y: Float = 0f)

data class Habit(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val duration: Int = 0,
    val goalAmount: Float = 0f,
    val units: String = "",
    val precision: String = "",
    val goalData: List<GoalPoint> = emptyList(),
    var notificationTriggered: Boolean? = false,
    var userDataId: String = ""
)