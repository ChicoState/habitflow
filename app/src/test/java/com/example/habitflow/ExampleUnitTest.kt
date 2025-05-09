package com.example.habitflow

import androidx.compose.ui.graphics.Color
import com.example.habitflow.model.GoalPoint
import com.example.habitflow.model.Habit
import com.example.habitflow.model.NewHabit
import com.example.habitflow.model.User
import com.example.habitflow.model.UserData
import com.google.firebase.Timestamp
import org.junit.Test
import com.github.mikephil.charting.data.Entry
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HabitTest {

    @Test
    fun defaultHabit_hasCorrectDefaults() {
        val habit = Habit()

        assertEquals("", habit.id)
        assertEquals("", habit.name)
        assertEquals("", habit.description)
        assertEquals(0, habit.duration)
        assertEquals(0f, habit.goalAmount)
        assertEquals("", habit.units)
        assertEquals("", habit.precision)
        assertTrue(habit.goalData.isEmpty())
        assertEquals(false, habit.notificationTriggered)
        assertEquals("", habit.userDataId)
    }

    @Test
    fun habit_withCustomValues_hasCorrectValues() {
        val goalPoints = listOf(GoalPoint(1f, 2f), GoalPoint(3f, 4f))
        val habit = Habit(
            id = "abc123",
            name = "Exercise",
            description = "Daily exercise routine",
            duration = 30,
            goalAmount = 10.5f,
            units = "minutes",
            precision = "day",
            goalData = goalPoints,
            notificationTriggered = true,
            userDataId = "user456"
        )
        assertEquals("abc123", habit.id)
        assertEquals("Exercise", habit.name)
        assertEquals("Daily exercise routine", habit.description)
        assertEquals(30, habit.duration)
        assertEquals(10.5f, habit.goalAmount)
        assertEquals("minutes", habit.units)
        assertEquals("day", habit.precision)
        assertEquals(goalPoints, habit.goalData)
        assertEquals(true, habit.notificationTriggered)
        assertEquals("user456", habit.userDataId)
    }

    @Test
    fun habit_idAndNotificationTriggered_areMutable() {
        val habit = Habit()
        habit.id = "newId"
        habit.notificationTriggered = true

        assertEquals("newId", habit.id)
        assertEquals(true, habit.notificationTriggered)
    }

    @Test
    fun habit_valFields_areImmutable() {
        val habit = Habit(name = "Read")
        assertEquals("Read", habit.name)
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

    @Test
    fun newHabit_hasCorrectDefaultValues() {
        val habit = NewHabit()

        assertEquals("", habit.id)
        assertEquals("", habit.name)
        assertEquals("", habit.description)
        assertEquals(0, habit.duration)
        assertEquals(0f, habit.goalAmount)
        assertEquals("", habit.precision)
        assertTrue(habit.goalData.isEmpty())
        assertEquals(false, habit.remindersEnabled)
        assertNull(habit.reminderFrequency)
        assertEquals("", habit.category)
        assertEquals("", habit.frequency)
        assertNull(habit.customReminderValue)
        assertNull(habit.customReminderUnit)
        assertEquals(false, habit.notificationTriggered)
        assertEquals("", habit.userDataId)
    }

    @Test
    fun newHabit_withCustomValues_hasCorrectProperties() {
        val goalData = listOf(
            mapOf("x" to 1f, "y" to 2f),
            mapOf("x" to 3f, "y" to 4f)
        )

        val habit = NewHabit(
            id = "habit123",
            name = "Meditation",
            description = "Daily morning meditation",
            duration = 20,
            goalAmount = 15.0f,
            precision = "day",
            goalData = goalData,
            remindersEnabled = true,
            reminderFrequency = "daily",
            category = "Wellness",
            frequency = "Daily",
            customReminderValue = "2",
            customReminderUnit = "hours",
            notificationTriggered = true,
            userDataId = "user789"
        )

        assertEquals("habit123", habit.id)
        assertEquals("Meditation", habit.name)
        assertEquals("Daily morning meditation", habit.description)
        assertEquals(20, habit.duration)
        assertEquals(15.0f, habit.goalAmount)
        assertEquals("day", habit.precision)
        assertEquals(goalData, habit.goalData)
        assertTrue(habit.remindersEnabled)
        assertEquals("daily", habit.reminderFrequency)
        assertEquals("Wellness", habit.category)
        assertEquals("Daily", habit.frequency)
        assertEquals("2", habit.customReminderValue)
        assertEquals("hours", habit.customReminderUnit)
        assertEquals(true, habit.notificationTriggered)
        assertEquals("user789", habit.userDataId)
    }

    @Test
    fun newHabit_mutableFieldsCanBeModified() {
        val habit = NewHabit()
        habit.id = "newId"
        habit.notificationTriggered = true
        habit.userDataId = "newUserId"

        assertEquals("newId", habit.id)
        assertEquals(true, habit.notificationTriggered)
        assertEquals("newUserId", habit.userDataId)
    }

    @Test
    fun newHabit_immutableFieldsRemainUnchanged() {
        val habit = NewHabit(name = "Yoga")
        assertEquals("Yoga", habit.name)
    }
}

class UserDataTest {

    private fun timestampFrom(dateStr: String): Timestamp {
        val cal = Calendar.getInstance().apply {
            time = SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(dateStr)!!
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(cal.time)
    }

    @Test
    fun emptyValuesConstructor() {
        val o = UserData(
            userDataId = "",
            type = "",
            userData = emptyList(),
            lastUpdated = Timestamp.now(),
            createDate = Timestamp.now(),
            deadline = null,
            trackingMethod = ""
        )
        assertEquals("", o.userDataId)
    }

    @Test
    fun userDataStreakCalculation() {
        val o = UserData(
            userDataId = "abc",
            type = "good",
            userData = listOf(
                Entry(1f, 1f),
                Entry(2f, 1f)),
            lastUpdated = Timestamp.now(),
            createDate = Timestamp.now(),
            deadline = null,
            trackingMethod = "binary"
        )
        assertEquals(1, o.streak)
        assertEquals("abc", o.userDataId)
        assertEquals(null, o.deadlineAsTimestamp)
        assertEquals(Color(0x40A5D6A7), o.backgroundColor)
        assertEquals(null, o.progressPercentage)
        assertEquals(true, o.promptEntry)
    }

    @Test
    fun calculateDeadlineRatio_returnsCorrectPercentage() {
        val createDate = timestampFrom("01/01/2024")
        val lastUpdated = timestampFrom("01/11/2024")
        val deadline = timestampFrom("01/21/2024")

        val ratio = UserData.calculateDeadlineRatio(createDate, lastUpdated, deadline)
        assertEquals(50.0f, ratio)
    }

    @Test
    fun promptForDataEntry_returnsTrueForToday() {
        val today = Timestamp.now()
        val entry = Entry(System.currentTimeMillis().toFloat(), 1f)

        assertTrue(UserData.promptForDataEntry(today, listOf(entry)))
    }

    @Test
    fun promptForDataEntry_returnsFalseWhenEmpty() {
        val today = Timestamp.now()
        assertFalse(UserData.promptForDataEntry(today, emptyList()))
    }

    @Test
    fun getColorBasedOnType_returnsExpectedColor() {
        val goodColor = UserData.getColorBasedOnType("good")
        val badColor = UserData.getColorBasedOnType("bad")
        val neutralColor = UserData.getColorBasedOnType("neutral")

        assertEquals(Color(0x40A5D6A7), goodColor)
        assertEquals(Color(0x40FF8A80), badColor)
        assertEquals(Color.Gray, neutralColor)
    }

    @Test
    fun calculateDecreasing_returnsTrueWhenYDecreases() {
        val data = listOf(Entry(0f, 5f), Entry(1f, 3f))
        val user = UserData(
            createDate = Timestamp.now(),
            userData = data
        )
        assertEquals(true, user.calculateDecreasing(user.userData))
    }

    @Test
    fun calculateIncreasing_returnsTrueWhenYIncreases() {
        val data = listOf(Entry(0f, 2f), Entry(1f, 4f))
        val user = UserData(
            createDate = Timestamp.now(),
            userData = data
        )
        assertEquals(true, user.calculateIncreasing(user.userData))
    }

    @Test
    fun firstAndLastMatch_returnsTrueWhenClose() {
        val data = listOf(Entry(0f, 2f), Entry(1f, 2.00001f))
        val user = UserData(
            createDate = Timestamp.now(),
            userData = data
        )
        assertTrue(user.firstAndLastMatch())
    }

    @Test
    fun streak_calculatesConsecutiveDaysCorrectly() {
        val now = Calendar.getInstance()
        val day1 = now.clone() as Calendar
        val day2 = now.clone() as Calendar
        val day3 = now.clone() as Calendar

        day1.add(Calendar.DAY_OF_YEAR, -2)
        day2.add(Calendar.DAY_OF_YEAR, -1)

        val data = listOf(
            Entry(day1.timeInMillis.toFloat(), 1f),
            Entry(day2.timeInMillis.toFloat(), 1f),
            Entry(day3.timeInMillis.toFloat(), 1f),
        )

        val user = UserData(
            createDate = Timestamp.now(),
            userData = data
        )
        assertEquals(3, user.streak)
    }

    @Test
    fun trendDrawable_returnsNeutralForSinglePoint() {
        val user = UserData(
            type = "bad",
            createDate = Timestamp.now(),
            userData = listOf(Entry(0f, 1f))
        )

        assertEquals(R.drawable.neutral, user.trendDrawable)
    }

    @Test
    fun calculateDeadlineRatio_returnsNullWhenDeadlineIsNull() {
        val createDate = Timestamp.now()
        val lastUpdated = Timestamp.now()
        val ratio = UserData.calculateDeadlineRatio(createDate, lastUpdated, null)
        assertNull(ratio)
    }

    @Test
    fun calculateDeadlineRatio_returnsZeroWhenDeadlineBeforeCreateDate() {
        val createDate = timestampFrom("01/10/2024")
        val lastUpdated = timestampFrom("01/15/2024")
        val deadline = timestampFrom("01/05/2024")

        val ratio = UserData.calculateDeadlineRatio(createDate, lastUpdated, deadline)
        assertEquals(0f, ratio)
    }

    @Test
    fun calculateDecreasing_returnsNullWhenYValuesEqual() {
        val data = listOf(Entry(0f, 3f), Entry(1f, 3f))
        val user = UserData(createDate = Timestamp.now(), userData = data)
        assertNull(user.calculateDecreasing(user.userData))
    }

    @Test
    fun calculateIncreasing_returnsNullWhenYValuesEqual() {
        val data = listOf(Entry(0f, 5f), Entry(1f, 5f))
        val user = UserData(createDate = Timestamp.now(), userData = data)
        assertNull(user.calculateIncreasing(user.userData))
    }

    @Test
    fun firstAndLastMatch_returnsFalseWhenFarApart() {
        val data = listOf(Entry(0f, 1f), Entry(1f, 2f))
        val user = UserData(createDate = Timestamp.now(), userData = data)
        assertFalse(user.firstAndLastMatch())
    }

    @Test
    fun calculateStreak_returnsZeroForEmptyData() {
        val user = UserData(createDate = Timestamp.now(), userData = emptyList())
        assertEquals(0, user.streak)
    }

    @Test
    fun calculateStreak_returnsOneForSingleEntry() {
        val user = UserData(createDate = Timestamp.now(), userData = listOf(Entry(0f, 1f)))
        assertEquals(1, user.streak)
    }

    @Test
    fun trendDrawable_returnsBadDecreaseForGoodType() {
        val data = listOf(Entry(0f, 10f), Entry(1f, 5f))
        val user = UserData(type = "good", createDate = Timestamp.now(), userData = data)
        assertEquals(R.drawable.bad_decrease, user.trendDrawable)
    }

    @Test
    fun trendDrawable_returnsGoodIncreaseForGoodType() {
        val data = listOf(Entry(0f, 5f), Entry(1f, 10f))
        val user = UserData(type = "good", createDate = Timestamp.now(), userData = data)
        assertEquals(R.drawable.good_increase, user.trendDrawable)
    }

    @Test
    fun trendDrawable_returnsBadIncreaseForBadType() {
        val data = listOf(Entry(0f, 3f), Entry(1f, 6f))
        val user = UserData(type = "bad", createDate = Timestamp.now(), userData = data)
        assertEquals(R.drawable.bad_increase, user.trendDrawable)
    }

    @Test
    fun trendDrawable_returnsGoodDecreaseForBadType() {
        val data = listOf(Entry(0f, 5f), Entry(1f, 2f))
        val user = UserData(type = "bad", createDate = Timestamp.now(), userData = data)
        assertEquals(R.drawable.good_decrease, user.trendDrawable)
    }
}

class UserTest {

    @Test
    fun defaultUser_hasCorrectDefaults() {
        val user = User()

        assertEquals("", user.name)
        assertEquals(0, user.age)
        assertEquals("", user.gender)
        assertEquals("", user.email)
        assertTrue(user.habits.isEmpty())
        assertTrue(user.pastHabits.isEmpty())
    }

    @Test
    fun user_withCustomValues_hasCorrectProperties() {
        val currentHabits = listOf("Exercise", "Meditate")
        val oldHabits = listOf("Smoke", "Drink")

        val user = User(
            name = "Alex",
            age = 25,
            gender = "Non-binary",
            email = "alex@example.com",
            habits = currentHabits,
            pastHabits = oldHabits
        )

        assertEquals("Alex", user.name)
        assertEquals(25, user.age)
        assertEquals("Non-binary", user.gender)
        assertEquals("alex@example.com", user.email)
        assertEquals(currentHabits, user.habits)
        assertEquals(oldHabits, user.pastHabits)
    }

    @Test
    fun users_withSameValues_areEqual() {
        val user1 = User(name = "Jordan", age = 30)
        val user2 = User(name = "Jordan", age = 30)

        assertEquals(user1, user2)
        assertEquals(user1.hashCode(), user2.hashCode())
    }

    @Test
    fun user_lists_canContainValues() {
        val user = User(
            name = "Maya",
            habits = listOf("Read", "Yoga"),
            pastHabits = listOf("Junk Food")
        )

        assertTrue(user.habits.contains("Yoga"))
        assertTrue(user.pastHabits.contains("Junk Food"))
    }

    @Test
    fun user_lists_areImmutable() {
        val user = User(habits = listOf("Run"))
        try {
            (user.habits as MutableList).add("Sleep")
            fail("Expected UnsupportedOperationException")
        } catch (e: UnsupportedOperationException) {
        }
    }
}