package com.example.habitflow

import androidx.compose.ui.graphics.Color
import com.example.habitflow.model.NewHabit
import com.example.habitflow.model.UserData
import com.google.firebase.Timestamp
import org.junit.Test
import com.github.mikephil.charting.data.Entry

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

class NewHabitTest {
    @Test
    fun emptyValuesConstructorTest() {
        val o = NewHabit(
            id = "",
            name = "",
            description = "",
            duration = 0,
            goalAmount = 0f,
            precision = "",
            goalData = emptyList(),
            remindersEnabled = false,
            reminderFrequency = null,
            category = "", "",
            customReminderValue = null,
            customReminderUnit = null,
            notificationTriggered = false,
            userDataId = ""
        )
        assertEquals("", o.id)
    }
}

class UserDataTest {
    @Test
    fun emptyValuesConstructorTest() {
        val o = UserData(
            userDataId = "",
            type = "",
            userData = emptyList(),
            lastUpdated = Timestamp.now(),
            streak = 0,
            createDate = Timestamp.now(),
            deadline = null,
            trackingMethod = ""
        )
        assertEquals("", o.userDataId)
    }

    @Test
    fun nonEmptyValuesConstructorTest() {
        val o = UserData(
            userDataId = "abc",
            type = "good",
            userData = listOf(Entry(1f, 1f), Entry(2f, 1f)),
            lastUpdated = Timestamp.now(),
            createDate = Timestamp.now(),
            deadline = null,
            trackingMethod = "binary"
        )
        assertEquals("abc", o.userDataId)
        assertEquals(1, o.streak)
        assertEquals(null, o.deadlineAsTimestamp)
        assertEquals(Color(0x40A5D6A7), o.backgroundColor)
        assertEquals(0f, o.progressPercentage)
        assertEquals(true, o.promptEntry)
    }
}