package com.example.habitflow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.habitflow.model.Habit
import com.example.habitflow.model.UserData
import com.example.habitflow.repository.DataRepository
import com.github.mikephil.charting.data.Entry
import com.google.firebase.Timestamp

class AddDataViewModel(
    private val dataRepository: DataRepository = DataRepository()
    ) : ViewModel() {

    private var habit: Habit? = null
    private var userData: UserData? = null
    private var userDataId: String? = null

    fun setHabit(habit: Habit) {
        this.habit = habit
    }
    fun setUserData(userData: UserData) {
        this.userData = userData
        this.userDataId = userData.userDataId
    }

    fun saveData(value: Float?) {
        if (value == null) return
        val userDataId = this.userDataId
        userDataId?.let {
            val currentTimestamp = Timestamp.now()
            val newData = mapOf(
                "value" to value,
                "timestamp" to currentTimestamp
            )
            dataRepository.updateUserData(userDataId, newData, currentTimestamp)
        }
    }

    fun timeBasedDataSaver(days: Int, hours: Int, minutes: Int, seconds: Int) {
        val value =  calculateTimeBasedValue(days, hours, minutes, seconds)
        saveData(value)
    }

    fun timeBasedDataUpdater(days: Int, hours: Int, minutes: Int, seconds: Int) {
        val value = calculateTimeBasedValue(days, hours, minutes, seconds)
        updateLastEntryY(value)
    }

    // Helper function for time based data for saveData() and updateLastEntryY()
    private fun calculateTimeBasedValue(days: Int, hours: Int, minutes: Int, seconds: Int): Float {
        val totalSeconds = (days * 86400) + (hours * 3600) + (minutes * 60) + seconds
        return totalSeconds.toFloat()
    }

    fun binaryDataSaver(completed: Boolean) {
        val value = calculateBinaryValue(completed)
        saveData(value)
    }

    fun binaryDataUpdater(completed: Boolean) {
        val value = calculateBinaryValue(completed)
        updateLastEntryY(value)
    }

    // Helper function for binary based data for saveData() and updateLastEntryY()
    private fun calculateBinaryValue(completed: Boolean): Float {
        val valueToAdd = if (completed) 1f else -1f
        val lastY = userData?.userData?.lastOrNull()?.y ?: 0f

        val updatedY = (lastY + valueToAdd).coerceAtLeast(0f)
        return updatedY
    }

    fun getLastEntryY(): Float? {
        return userData?.userData?.lastOrNull()?.y
    }

    fun updateLastEntryY(newY: Float) {
        val userDataId = this.userDataId
        val userData = this.userData

        if (userDataId != null && userData != null && userData.userData.isNotEmpty()) {
            val lastIndex = userData.userData.lastIndex
            val lastEntry = userData.userData[lastIndex]
            val updatedEntry = Entry(lastEntry.x, newY)

            val updatedList = userData.userData.toMutableList().apply {
                this[lastIndex] = updatedEntry
            }

            userData.userData = updatedList

            dataRepository.updateLastEntryInFirestore(userDataId, updatedEntry)
        } else {
            Log.e("UpdateEntry", "Could not update last entry. Missing userData or userDataId or entries empty.")
        }
    }

    fun retrieveHabit(): Habit? {
        return habit
    }

    fun retrieveUserData(): UserData? {
        return userData
    }
}
