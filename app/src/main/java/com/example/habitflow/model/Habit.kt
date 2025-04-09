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
    val totalDays: Float
        get() {
            // Get the createDate in milliseconds
            val createDateMillis = createDate.seconds * 1000 // Convert Firebase Timestamp (seconds) to milliseconds
            val createDateObject = Date(createDateMillis)

            // Parse the deadline if it's not null
            val deadlineDate = deadline?.let {
                try {
                    val dateFormat = SimpleDateFormat("MM/dd/yyyy") // Format to match deadline format
                    // Set time to midnight (00:00:00.000)
                    dateFormat.isLenient = false // Prevent lenient parsing
                    val deadlineParsed = dateFormat.parse(it)
                    // If parsed successfully, we reset the time to midnight
                    deadlineParsed?.let { parsedDate ->
                        val calendar = Calendar.getInstance()
                        calendar.time = parsedDate
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        return@let calendar.time
                    }
                } catch (e: Exception) {
                    null // If parsing fails, return null
                }
            }

            // If deadline is null or invalid, return 0
            if (deadlineDate == null) {
                return 0f
            }

            // Calculate the difference in milliseconds between the deadline and createDate
            val differenceInMillis = deadlineDate.time - createDateObject.time

            // Convert milliseconds to days and return as a Float value
            return differenceInMillis.toFloat() / (1000 * 60 * 60 * 24) // Convert milliseconds to days
        }
}