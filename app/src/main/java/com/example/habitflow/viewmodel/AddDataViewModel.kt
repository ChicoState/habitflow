package com.example.habitflow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.habitflow.model.Habit
import com.example.habitflow.model.UserData
import com.example.habitflow.repository.DataRepository
import com.github.mikephil.charting.data.Entry
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

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

    suspend fun saveData(value: Float?, didCompleteHabit: Boolean) {
        if (value == null) return
        val userDataId = this.userDataId
        val timestamp = Timestamp.now()
        val entry = mapOf(
            "value" to value,
            "timestamp" to timestamp
        )
        userDataId?.let {
            dataRepository.updateUserData(it, entry, didCompleteHabit)
        }
    }

    suspend fun timeBasedDataSaver(days: Int, hours: Int, minutes: Int, seconds: Int) {
        val value =  calculateTimeBasedValue(days, hours, minutes, seconds)
        saveData(value, didCompleteHabit = true) // Always assume
    }

    suspend fun timeBasedDataUpdater(days: Int, hours: Int, minutes: Int, seconds: Int) {
        val value = calculateTimeBasedValue(days, hours, minutes, seconds)
        updateLastEntryY(value, didCompleteHabit = true)
    }

    private fun calculateTimeBasedValue(days: Int, hours: Int, minutes: Int, seconds: Int): Float {
        val totalSeconds = (days * 86400) + (hours * 3600) + (minutes * 60) + seconds
        return totalSeconds.toFloat()
    }

    suspend fun binaryDataSaver(completed: Boolean) {
        val value = if (completed) 1f else 0f
        saveData(value, didCompleteHabit = completed)
    }

    suspend fun binaryDataUpdater(completed: Boolean) {
        val value = if (completed) 1f else 0f
        updateLastEntryY(value, completed)
    }

    fun getLastEntryY(): Float? {
        return userData?.userData?.lastOrNull()?.y
    }

    suspend fun updateLastEntryY(newY: Float, didCompleteHabit: Boolean) {
        val userDataId = this.userDataId
        val userData = this.userData

        if (userDataId != null && userData != null && userData.userData.isNotEmpty()) {
            val currentTimestamp = Timestamp.now()

            val snapshot = FirebaseFirestore.getInstance()
                .collection("userData").document(userDataId).get().await()

            val data = (snapshot.get("data") as? List<Map<String, Any>>)?.toMutableList() ?: return

            val updatedEntry = data.last().toMutableMap().apply {
                this["value"] = newY
                this["timestamp"] = currentTimestamp
            }
            data[data.lastIndex] = updatedEntry

            val entries = data.mapNotNull { entry ->
                val ts = (entry["timestamp"] as? Timestamp)?.toDate()?.time?.toFloat()
                val value = (entry["value"] as? Number)?.toFloat()
                if (ts != null && value != null) Entry(ts, value) else null
            }

            val tempUserData = userData.copy(userData = entries)

            val newStreak = tempUserData.streak

            updatedEntry["streak"] = newStreak
            data[data.lastIndex] = updatedEntry

            snapshot.reference.update(
                "data", data,
                "lastUpdated", currentTimestamp
            ).addOnSuccessListener {
                Log.d("EntryDebug", "Updated last entry with streak: $newStreak")
            }.addOnFailureListener { e ->
                Log.e("EntryDebug", "Failed to update last entry: ${e.message}")
            }
        } else {
            Log.e("UpdateEntry", "Missing userData or userDataId or empty list")
        }
    }

    fun retrieveHabit(): Habit? {
        return habit
    }

    fun retrieveUserData(): UserData? {
        return userData
    }
}
