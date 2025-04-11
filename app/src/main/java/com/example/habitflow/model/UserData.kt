package com.example.habitflow.model

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.habitflow.R
import com.github.mikephil.charting.data.Entry
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

data class UserData (
    val userDataId: String = "",
    val type: String = "",
    var userData: List<Entry> = emptyList(),
    var lastUpdated: Timestamp = Timestamp.now(),
    val streak: Int = calculateStreak(userData),
    val createDate: Timestamp,
    val deadline: String? = null,
    val deadlineAsTimestamp: Timestamp? = parseDeadlineToTimestamp(deadline),
    val backgroundColor: Color = getColorBasedOnType(type),
    val progressPercentage: Float = calculateDeadlineRatio(createDate, lastUpdated, deadlineAsTimestamp),
    val promptEntry: Boolean = promptForDataEntry(lastUpdated, userData),
    val trackingMethod: String = ""
) {
    val trendDrawable: Int
        get() {
            val decreasing = calculateDecreasing(userData)
            return when {
                decreasing == null -> R.drawable.neutral
                decreasing && type != "good" -> R.drawable.good_decrease
                decreasing && type == "good" -> R.drawable.bad_decrease
                !decreasing && type != "good" -> R.drawable.bad_increase
                else -> R.drawable.good_increase
            }
        }

    companion object {

        // Helper function to decreasing value
        private fun calculateDecreasing(userData: List<Entry>): Boolean? {
            if (userData.size <= 1) {
                return null
            }
            val firstY = userData.first().y
            val lastY = userData.last().y
            if (firstY == lastY) return null
            return firstY > lastY
        }

        @SuppressLint("DefaultLocale")
        fun calculateDeadlineRatio(
            createDate: Timestamp,
            lastUpdated: Timestamp,
            deadlineAsTimestamp: Timestamp?
        ): Float {
            val deadline = deadlineAsTimestamp ?: return 0f

            val timeDifferenceFromCreateDate = lastUpdated.seconds - createDate.seconds
            val timeDifferenceToDeadline = deadline.seconds - createDate.seconds

            if (timeDifferenceToDeadline <= 0) return 0f

            val ratio = timeDifferenceFromCreateDate.toFloat() / timeDifferenceToDeadline.toFloat()
            val roundedRatio = (ratio * 100)

            return String.format("%.1f", roundedRatio).toFloat()
        }

        fun getColorBasedOnType(habitType: String): Color {
            return when (habitType) {
                "good" -> Color(0x40A5D6A7)
                "bad" -> Color(0x40FF8A80)
                else -> Color.Gray
            }
        }

        private fun parseDeadlineToTimestamp(deadline: String?): Timestamp? {
            if (deadline.isNullOrBlank()) return null

            return try {
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                val parsedDate = dateFormat.parse(deadline) ?: return null

                // Set to midnight for consistency
                Calendar.getInstance().apply {
                    time = parsedDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.let { Timestamp(it.time) }

            } catch (e: Exception) {
                Log.e("HabitCardViewModel", "Error parsing deadline: ${e.message}")
                null
            }
        }

        fun promptForDataEntry(lastUpdated: Timestamp, userData: List<Entry>): Boolean {
            if (userData.isEmpty()) return false
            val lastUpdatedDate = Calendar.getInstance().apply {
                time = lastUpdated.toDate()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            return lastUpdatedDate.time == today.time
        }


        fun calculateStreak(userData: List<Entry>): Int {
            if (userData.isEmpty()) return 0

            if (userData.size == 1) return 1
            var streak = 1

            for (i in userData.size - 1 downTo 1) {
                val currentX = userData[i].x
                val previousX = userData[i - 1].x

                // Calculate the difference in minutes between adjacent x values
                val timeDifferenceInMinutes = abs(currentX - previousX)

                if (timeDifferenceInMinutes in 720.0..1440.0) {
                    streak++
                } else {
                    break
                }
            }
            return streak
        }
    }
}

