package com.example.habitflow.viewmodel

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.example.habitflow.model.Habit
import com.example.habitflow.model.UserData
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HabitCardViewModel(
    private val habit: Habit,
    private val userData: UserData?
) : ViewModel() {

    private var maxSwipe = 200f
    var swipeOffset = mutableFloatStateOf(0f)
    var showDeleteIcon = mutableStateOf(false)

    fun calculateDeadlineRatio(): Int? {

        val lastUpdated = userData?.lastUpdated ?: return null
        val createDate = habit.createDate

        // Convert deadline string (MM/dd/yyyy) to a Timestamp
        val deadlineTimestamp = habit.deadline?.let { parseDeadlineToTimestamp(it) }
        if (deadlineTimestamp == null) return null

        val timeDifferenceFromCreateDate = lastUpdated.seconds - createDate.seconds
        val timeDifferenceToDeadline = deadlineTimestamp.seconds - createDate.seconds

        if (timeDifferenceToDeadline == 0L) return null

        val ratio = timeDifferenceFromCreateDate.toFloat() / timeDifferenceToDeadline.toFloat()
        val roundedRatio = (ratio * 100).toInt()

        return roundedRatio
    }

    // Helper function to parse the deadline string to a Timestamp
    private fun parseDeadlineToTimestamp(deadline: String): Timestamp? {
        return try {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val parsedDate = dateFormat.parse(deadline)
            parsedDate?.let {
                val calendar = Calendar.getInstance()
                calendar.time = it
                // Set the time to midnight (00:00:00) for consistency
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // Return as Timestamp
                Timestamp(calendar.time)
            }
        } catch (e: Exception) {
            Log.e("HabitCardViewModel", "Error parsing deadline: ${e.message}")
            null
        }
    }

    fun getArrowAndColor(): Pair<String, Color> {
        // Default values for upOrDown and arrowColor
        var upOrDown = "↗"
        var arrowColor = Color.Red

        // Check if userData is not null
        userData?.let {
            if (it.decreasing && habit.type != "good") {
                upOrDown = "↘"
                arrowColor = Color(0xFF006400) // Green color
            } else if (it.decreasing && habit.type == "good") {
                upOrDown = "↘"
                arrowColor = Color.Red
            } else if (!it.decreasing && habit.type != "good") {
                upOrDown = "↗"
                arrowColor = Color.Red
            } else if (!it.decreasing && habit.type == "good") {
                upOrDown = "↗"
                arrowColor = Color(0xFF006400)
            }
        }
        return Pair(upOrDown, arrowColor)
    }

    fun handleHorizontalDrag(
        dragAmount: Float,
        onDragEnd: () -> Unit
    ) {
        swipeOffset.floatValue = (swipeOffset.floatValue + dragAmount).coerceIn(0f, maxSwipe)
        if (swipeOffset.floatValue > maxSwipe * 0.5f) {
            showDeleteIcon.value = true
        } else {
            showDeleteIcon.value = false
        }
        onDragEnd()
    }

    fun getNotificationIcon(): ImageVector {
        return when (habit.notificationTriggered) {
            true -> Icons.Default.Add // Plus sign icon for triggered
            false -> Icons.Default.Check // Checkmark icon for not triggered
            null -> Icons.Default.Check // Default icon for null (or any other neutral icon you want to use)
        }
    }
}