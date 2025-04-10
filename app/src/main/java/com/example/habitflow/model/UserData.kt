package com.example.habitflow.model

import com.github.mikephil.charting.data.Entry
import com.google.firebase.Timestamp
import kotlin.math.abs

data class UserData (
    val userData: List<Entry> = emptyList(),
    val decreasing: Boolean = calculateDecreasing(userData),
    var lastUpdated: Timestamp = Timestamp.now(),
    val streak: Int = calculateStreak(userData),
) {
    companion object {
        fun calculateDecreasing(userData: List<Entry>): Boolean {
            if (userData.isNotEmpty()) {
                val firstY = userData.first().y
                val lastY = userData.last().y
                return firstY > lastY
            }
            return false
        }

        fun calculateStreak(userData: List<Entry>): Int {
            if (userData.isEmpty()) return 0

            var streak = 0
            val oneDayInMinutes = 1440

            for (i in userData.size - 1 downTo 1) {
                val currentX = userData[i].x
                val previousX = userData[i - 1].x

                // Calculate the difference in minutes between adjacent x values
                val timeDifferenceInMinutes = abs(currentX - previousX)

                // If the time difference between adjacent x values is less than or equal to 1 day
                if (timeDifferenceInMinutes <= oneDayInMinutes) {
                    streak++
                } else {
                    break
                }
            }
            return streak
        }
    }
}

