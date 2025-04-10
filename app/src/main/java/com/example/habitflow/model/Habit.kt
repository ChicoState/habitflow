package com.example.habitflow.model
import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

data class GoalPoint(val x: Float = 0f, val y: Float = 0f)

data class Habit(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val duration: Int = 0,
    val type: String = "",
    val goalAmount: Int = 0,
    val units: String = "",
    val precision: String = "",
    val goalData: List<GoalPoint> = emptyList(),
    var notificationTriggered: Boolean? = true,
    val createDate: Timestamp,
    val deadline: String? = null,
    var userDataId: String = ""
) {
    val backgroundColor: Color
        get() = if (type == "good") Color(0x40A5D6A7) else Color(0x40FF8A80)
}