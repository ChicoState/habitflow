package com.example.habitflow.repository

import android.util.Log
import com.example.habitflow.model.UserData
import com.github.mikephil.charting.data.Entry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

class DataRepository {

    private fun getDatabase(): FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createEmptyUserData(
        type: Boolean,
        deadline: String?,
        trackingMethod: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val db = getDatabase()

        val userData = mapOf(
            "data" to emptyList<Map<String, Any>>(),
            "createDate" to Timestamp.now(),
            "deadline" to deadline,
            "lastUpdated" to Timestamp.now(),
            "type" to if (type) "good" else "bad",
            "trackingMethod" to trackingMethod
        )

        db.collection("userData")
            .add(userData)
            .addOnSuccessListener { documentReference ->
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { e ->
                onFailure("Failed to create userData: ${e.message}")
            }
    }

    fun updateUserData(userDataId: String, newData: Map<String, Any>, currentTimestamp: Timestamp) {
        val db = getDatabase()
        db.collection("userData")
            .document(userDataId)
            .update(
                "data", FieldValue.arrayUnion(newData),
                "lastUpdated", currentTimestamp
            )
            .addOnSuccessListener {
                Log.d("DataRepository", "Data successfully updated")
            }
            .addOnFailureListener { exception ->
                Log.e("DataRepository", "Error updating data: ${exception.message}")
            }
    }

    fun loadUserDataFromFirestore(
        userDataId: String,
        onComplete: (Result<UserData>) -> Unit
    ) {
        val db = getDatabase()
        val userDataRef = db.collection("userData").document(userDataId)

        userDataRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    val userData = parseUserData(data, userDataId)
                    onComplete(Result.success(userData))
                } else {
                    onComplete(Result.failure(Exception("User data not found")))
                    Log.e("DataRepository", "User data not found in loadUserDataFromFirestore")
                }
            }
            .addOnFailureListener { exception ->
                onComplete(Result.failure(Exception("Error loading user data: ${exception.message}")))
                Log.e("DataRepository", "Error loading user data loadUserDataFromFirestore")
            }
    }

    fun updateLastEntryInFirestore(userDataId: String, updatedEntry: Entry) {
        val db = FirebaseFirestore.getInstance()
        val userDataRef = db.collection("userData").document(userDataId)

        userDataRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    val entries = (data?.get("data") as? List<*>)?.toMutableList()

                    if (!entries.isNullOrEmpty()) {
                        val lastIndex = entries.lastIndex

                        // Construct new map for the updated entry
                        val updatedEntryMap = mapOf(
                            "timestamp" to Timestamp.now(),
                            "value" to updatedEntry.y
                        )

                        // Replace the last entry
                        entries[lastIndex] = updatedEntryMap

                        // Update the entire data array in Firestore
                        userDataRef.update(
                            "data", entries,
                            "lastUpdated", Timestamp.now()
                        ).addOnSuccessListener {
                            Log.d("DataRepository", "Last entry successfully updated.")
                        }.addOnFailureListener { e ->
                            Log.e("DataRepository", "Failed to update last entry: ${e.message}")
                        }
                    } else {
                        Log.e("DataRepository", "No entries to update in document: $userDataId")
                    }
                } else {
                    Log.e("DataRepository", "Document $userDataId does not exist.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("DataRepository", "Error fetching document: ${e.message}")
            }
    }


    fun deleteUserData(userDataId: String, onComplete: (Boolean) -> Unit) {
        if (userDataId.isBlank()) {
            onComplete(false)
            return
        }

        val db = getDatabase()
        val docRef = db.collection("userData").document(userDataId)

        docRef.delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onComplete(false)
            }
    }

    private fun parseUserData(data: Map<String, Any>?, userDataId: String): UserData {
        val rawEntries = (data?.get("data") as? List<*>)?.mapNotNull { item ->
            // For each item in the list, check if it is a map with a timestamp and value
            (item as? Map<*, *>)?.let { map ->
                val timestamp = map["timestamp"] as? Timestamp
                val value = (map["value"] as? Number)?.toFloat()

                // If both timestamp and value are present, create an Entry
                if (timestamp != null && value != null) {
                    Entry(timestamp.toDate().time.toFloat(), value)
                } else {
                    null
                }
            }
        } ?: emptyList()

        return UserData(
            userDataId = userDataId,
            userData = rawEntries, //calculateTimeDifferences(rawEntries),
            lastUpdated = data?.get("lastUpdated") as? Timestamp ?: Timestamp.now(),
            createDate = data?.get("createDate") as? Timestamp ?: Timestamp.now(),
            deadline = data?.get("deadline") as? String ?: "",
            type = data?.get("type") as? String ?: "",
            trackingMethod = data?.get("trackingMethod") as? String ?: ""
        )
    }


    // Calculate time difference in minutes from the first timestamp
    private fun calculateTimeDifferences(entries: List<Entry>): List<Entry> {
        if (entries.isEmpty()) return emptyList()

        // Get the first timestamp
        val firstTimestamp = entries.first().x

        // Map each entry to a new Entry where x is the time difference in minutes
        return entries.map { entry ->
            val timeDifference = (entry.x - firstTimestamp) / 60
            Entry(timeDifference, entry.y)
        }
    }
}