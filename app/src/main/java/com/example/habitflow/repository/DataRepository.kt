package com.example.habitflow.repository

import android.util.Log
import com.example.habitflow.model.UserData
import com.github.mikephil.charting.data.Entry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

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

    suspend fun updateUserData(userDataId: String, newEntry: Map<String, Any>, didCompleteHabit: Boolean) {
        val db = getDatabase()
        val userDataRef = db.collection("userData").document(userDataId)

        val snapshot = userDataRef.get().await()
        val data = (snapshot.get("data") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()

        val lastStreak = data.lastOrNull()?.get("streak") as? Long ?: 0L
        val newStreak = if (didCompleteHabit) lastStreak + 1 else 0
        val enrichedEntry = newEntry.toMutableMap()
        enrichedEntry["streak"] = newStreak

        data.add(enrichedEntry)

        userDataRef.update(
            "data", data,
            "lastUpdated", Timestamp.now()
        ).addOnSuccessListener {
            Log.d("DataRepository", "Full entry with streak added successfully.")
        }.addOnFailureListener { exception ->
            Log.e("DataRepository", "Error writing entry with streak: ${exception.message}")
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

                        val updatedEntryMap = mapOf(
                            "timestamp" to Timestamp.now(),
                            "value" to updatedEntry.y
                        )

                        entries[lastIndex] = updatedEntryMap

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
            (item as? Map<*, *>)?.let { map ->
                val timestamp = map["timestamp"] as? Timestamp
                val value = (map["value"] as? Number)?.toFloat()

                if (timestamp != null && value != null) {
                    Entry(timestamp.toDate().time.toFloat(), value)
                } else {
                    null
                }
            }
        } ?: emptyList()

        val streakFromEntry = (data?.get("data") as? List<Map<String, Any>>)
            ?.lastOrNull()
            ?.get("streak") as? Number ?: 1

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


    private fun calculateTimeDifferences(entries: List<Entry>): List<Entry> {
        if (entries.isEmpty()) return emptyList()

        val firstTimestamp = entries.first().x

        return entries.map { entry ->
            val timeDifference = (entry.x - firstTimestamp) / 60
            Entry(timeDifference, entry.y)
        }
    }
}