package com.example.habitflow.repository

import android.util.Log
import androidx.compose.material3.Text
import com.example.habitflow.model.UserData
import com.github.mikephil.charting.data.Entry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class DataRepository {

    fun loadUserDataFromFirestore(
        userDataId: String,
        onSuccess: (UserData) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userDataRef = db.collection("userData").document(userDataId)

        userDataRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    val userData = parseUserData(data)
                    onSuccess(userData)
                } else {
                    onFailure("User data not found")
                    Log.e("DataRepository", "User data not found in loadUserDataFromFirestore")
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Error loading user data: ${exception.message}")
                Log.e("DataRepository", "Error loading user data loadUserDataFromFirestore")

            }
    }

    private fun parseUserData(data: Map<String, Any>?): UserData {
        // Fetch the 'data' field which should be a list of maps
        val rawEntries = (data?.get("data") as? List<*>)?.mapNotNull { item ->
            // For each item in the list, check if it is a map with a timestamp and value
            (item as? Map<*, *>)?.let { map ->
                val timestamp = map["timestamp"] as? Timestamp
                val value = (map["value"] as? Number)?.toFloat()

                // If both timestamp and value are present, create an Entry
                if (timestamp != null && value != null) {
                    Entry(timestamp.seconds.toFloat(), value)
                } else {
                    null // Skip invalid entries
                }
            }
        } ?: emptyList() // Return empty list if data is null or not in the expected format

        // Calculate the time differences for each entry
        val entriesWithTimeDifference = calculateTimeDifferences(rawEntries)

        // Return UserData with the calculated entries and a decreasing flag
        return UserData(userData = entriesWithTimeDifference, decreasing = isDecreasing(entriesWithTimeDifference))
    }


    // Calculate time difference in minutes from the first timestamp
    private fun calculateTimeDifferences(entries: List<Entry>): List<Entry> {
        if (entries.isEmpty()) return emptyList()

        // Get the first timestamp
        val firstTimestamp = entries.first().x

        // Map each entry to a new Entry where x is the time difference in minutes
        return entries.map { entry ->
            val timeDifference = (entry.x - firstTimestamp) / 60 // Convert seconds to minutes
            Entry(timeDifference, entry.y)
        }
    }
    private fun isDecreasing(list: List<Entry>): Boolean {
        if (list.isNotEmpty()) {
            val firstY = list.first().y
            val lastY = list.last().y
            return firstY > lastY
        }
        return false
    }
}