package com.example.habitflow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.habitflow.model.Habit
import com.example.habitflow.repository.DataRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddDataViewModel(
    private val dataRepository: DataRepository = DataRepository()
    ) : ViewModel() {

    private var habit: Habit? = null

    // Set the entire habit object
    fun setHabit(habit: Habit) {
        this.habit = habit
    }
    fun saveData(value: Float) {
        val habit = retrieveHabit()
        habit?.let {
            // Check if the habit has an existing userDataId
            val userDataId = habit.userDataId
            Log.e("userDataId", "userDataId: $userDataId")
            val currentTimestamp = Timestamp.now()

            if (userDataId == "") {
                // No existing userDataId, create a new userData document
                val newData = mapOf(
                    "data" to listOf(mapOf("value" to value, "timestamp" to Timestamp.now())),
                        "lastUpdated" to currentTimestamp
                )
                // Save the new userData document
                db.collection("userData")
                    .add(newData)
                    .addOnSuccessListener { documentReference ->
                        // Once the data is saved, update the habit with the new userDataId
                        val newUserDataId = documentReference.id
                        updateHabitUserDataId(habit.id, newUserDataId)
                        habit.userDataId = newUserDataId
                    }
                    .addOnFailureListener { exception ->
                        Log.e("AddDataViewModel", "Error saving data: ${exception.message}")
                    }
            } else {
                // Create the new data pair to add to the existing list
                val newData = mapOf(
                    "value" to value,
                    "timestamp" to currentTimestamp
                )

                // Update the existing userData document by adding the new value to the array
                db.collection("userData")
                    .document(userDataId)
                    .update("data", FieldValue.arrayUnion(newData),"lastUpdated", currentTimestamp)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { exception ->
                        Log.e("AddDataViewModel", "Error adding data: ${exception.message}")
                    }
            }
        }
    }

    fun retrieveHabit(): Habit? {
        return habit
    }

    // Function to update the habit's userDataId field
    private fun updateHabitUserDataId(habitId: String, userDataId: String) {
        db.collection("habits")
            .document(habitId)
            .update("userDataId", userDataId)
            .addOnSuccessListener {
                Log.d("AddDataViewModel", "Habit updated with new userDataId: $userDataId")
            }
            .addOnFailureListener { exception ->
                Log.e("AddDataViewModel", "Error updating habit with userDataId: ${exception.message}")
            }
    }
    private val db = FirebaseFirestore.getInstance()

}
