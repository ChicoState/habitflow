package com.example.habitflow.repository

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore

class ReminderWorker(
	context: Context,
	params: WorkerParameters
) : Worker(context, params) {

	override fun doWork(): Result {
		val habitName = inputData.getString("habitName") ?: return Result.failure()
		val habitId = inputData.getString("habitId") ?: return Result.failure() // Fetch the habit ID

		ReminderNotificationHelper.showReminderNotification(applicationContext, habitName, habitId)

		updateNotificationFlagInFirestore(habitId)

		return Result.success()
	}
}

private fun updateNotificationFlagInFirestore(habitId: String) {
	val db = getDatabase()
	val habitRef = db.collection("habits").document(habitId)

	habitRef.update("notificationTriggered", true)
		.addOnSuccessListener {
			// Successfully updated the flag
			Log.d("ReminderWorker", "Notification triggered flag updated for habitId: $habitId")
		}
		.addOnFailureListener { e ->
			// Handle the failure
			Log.e("ReminderWorker", "Error updating notification flag for habitId: $habitId", e)
		}
}

private fun getDatabase(): FirebaseFirestore = FirebaseFirestore.getInstance()

