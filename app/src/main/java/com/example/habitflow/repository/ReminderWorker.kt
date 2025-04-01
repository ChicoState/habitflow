package com.example.habitflow.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.habitflow.repository.ReminderNotificationHelper

class ReminderWorker(
	context: Context,
	params: WorkerParameters
) : Worker(context, params) {

	override fun doWork(): Result {
		val habitName = inputData.getString("habitName") ?: return Result.failure()

		ReminderNotificationHelper.showReminderNotification(applicationContext, habitName)

		return Result.success()
	}
}
