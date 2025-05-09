package com.example.habitflow.repository

import android.content.Context
import androidx.work.*
import com.example.habitflow.model.GoalPoint
import com.example.habitflow.model.Habit
import com.example.habitflow.model.NewHabit
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

object HabitRepository {

	private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

	// Replace the current getDatabase function with this
	private fun getDatabase(): FirebaseFirestore = firestore

	// Add this function to allow tests to set a mock Firestore
	fun setFirestore(mock: FirebaseFirestore) {
		firestore = mock
	}
	private fun updateUserHabitList(
		user: FirebaseUser,
		newHabits: List<String>,
		onSuccess: () -> Unit,
		onFailure: (String) -> Unit
	) {
		val db = getDatabase()
		val userDoc = db.collection("users").document(user.uid)
		userDoc.update("habits", newHabits)
			.addOnSuccessListener { onSuccess() }
			.addOnFailureListener { e -> onFailure(e.message ?: "Error saving habits") }
	}

	fun loadHabitsFromFirestore(
		user: FirebaseUser,
		onSuccess: (List<String>) -> Unit,
		onFailure: (String) -> Unit
	) {
		val db = getDatabase()
		val userDoc = db.collection("users").document(user.uid)
		userDoc.get()
			.addOnSuccessListener { document ->
				val habitsList = (document.get("habits") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
				onSuccess(habitsList)
			}
			.addOnFailureListener { e -> onFailure(e.message ?: "Error loading habits") }
	}

	fun getHabitFromFirestore(habitId: String, onComplete: (Habit?) -> Unit) {
		val db = getDatabase()
		val habitRef = db.collection("habits").document(habitId)
		habitRef.get()
			.addOnSuccessListener { document ->
				if (!document.exists()) {
					onComplete(null)
					return@addOnSuccessListener
				}
				val data = document.data ?: run {
					onComplete(null)
					return@addOnSuccessListener
				}

				val habit = Habit(
					id = document.id,
					name = data["name"] as? String ?: "",
					description = data["description"] as? String ?: "",
					duration = (data["duration"] as? Number)?.toInt() ?: 0,
					goalAmount = (data["goalAmount"] as? Number)?.toFloat() ?: 0f,
					units = data["units"] as? String ?: "",
					precision = data["precision"] as? String ?: "",
					goalData = (data["goalData"] as? List<*>
						?: emptyList<Any>()).mapNotNull { item ->
						(item as? Map<*, *>)?.let { map ->
							val x = (map["x"] as? Number)?.toFloat()
							val y = (map["y"] as? Number)?.toFloat()
							if (x != null && y != null) GoalPoint(x, y) else null
						}
					},
					userDataId = data["userDataId"] as? String ?: "",
					notificationTriggered = data["notificationTriggered"] as? Boolean ?: false,
				)

				onComplete(habit)
			}
			.addOnFailureListener { onComplete(null) }
	}

	fun createHabitForUser(
		context: Context,
		user: FirebaseUser,
		habit: NewHabit,
		onSuccess: () -> Unit,
		onFailure: (String) -> Unit
	) {
		val db = getDatabase()

		val habitData = hashMapOf(
			"name" to habit.name,
			"description" to habit.description,
			"duration" to habit.duration,
			"goalAmount" to habit.goalAmount,
			"goalData" to habit.goalData,
			"precision" to habit.precision,
			"remindersEnabled" to habit.remindersEnabled,
			"reminderFrequency" to habit.reminderFrequency,
			"category" to habit.category,
			"frequency" to habit.frequency,
			"customReminderValue" to habit.customReminderValue,
			"customReminderUnit" to habit.customReminderUnit,
			"userDataId" to habit.userDataId,
			"notificaitonTriggered" to habit.notificationTriggered
		)

		db.collection("habits").add(habitData).addOnSuccessListener { docRef ->
			loadHabitsFromFirestore(user,
				onSuccess = { currentHabits ->
					val updatedHabits = currentHabits + docRef.id
					updateUserHabitList(user, updatedHabits, onSuccess, onFailure)
				},
				onFailure = { onFailure(it) }
			)

			if (habit.remindersEnabled) {
				val frequency = habit.reminderFrequency
				val customValue = habit.customReminderValue?.toIntOrNull()
				val tag = "reminder_${docRef.id}"

				if (frequency == "custom" && customValue != null) {
					val timeUnit = if (habit.customReminderUnit == "Hours") TimeUnit.HOURS else TimeUnit.DAYS
					scheduleReminderWorker(context, habit.name, customValue, timeUnit, tag)
				} else {
					val unit = when (frequency) {
						"hourly" -> TimeUnit.HOURS
						"weekly" -> TimeUnit.DAYS
						else -> TimeUnit.DAYS
					}
					scheduleReminderWorker(context, habit.name, 1, unit, tag)
				}
			}
		}.addOnFailureListener { e ->
			onFailure(e.message ?: "Error creating habit")
		}
	}

	fun deleteHabitsForUser(
		context: Context,
		user: FirebaseUser,
		habitIds: Set<String>,
		onComplete: () -> Unit
	) {
		if (habitIds.isEmpty()) {
			onComplete()
			return
		}

		val db = getDatabase()
		val userDoc = db.collection("users").document(user.uid)
		val batch = db.batch()

		batch.update(userDoc, "habits", FieldValue.arrayRemove(*habitIds.toTypedArray()))

		habitIds.forEach { habitId ->
			batch.delete(db.collection("habits").document(habitId))
			WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$habitId")
		}

		batch.commit()
			.addOnSuccessListener { onComplete() }
			.addOnFailureListener { onComplete() }
	}

	private fun scheduleReminderWorker(
		context: Context,
		habitName: String,
		intervalAmount: Int,
		intervalUnit: TimeUnit,
		tag: String
	) {
		val data = workDataOf("habitName" to habitName)

		val request = PeriodicWorkRequestBuilder<ReminderWorker>(
			repeatInterval = intervalAmount.toLong(),
			repeatIntervalTimeUnit = intervalUnit
		)
			.setInputData(data)
			.addTag(tag)
			.build()

		WorkManager.getInstance(context).enqueueUniquePeriodicWork(
			tag,
			ExistingPeriodicWorkPolicy.REPLACE,
			request
		)
	}
}

