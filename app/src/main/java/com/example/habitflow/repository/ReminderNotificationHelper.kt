package com.example.habitflow.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.habitflow.R
import com.example.habitflow.activity.MainActivity

object ReminderNotificationHelper {
	private const val CHANNEL_ID = "habitflow_reminders_channel"
	private const val CHANNEL_NAME = "Habit Reminders"
	private const val CHANNEL_DESC = "Notifications to remind you about your habits"

	fun showReminderNotification(context: Context, habitName: String, habitId: String) {
		// Check if permission is granted
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			val permission = android.Manifest.permission.POST_NOTIFICATIONS
			val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, permission) ==
					android.content.pm.PackageManager.PERMISSION_GRANTED

			if (!isGranted) return // Exit early if permission not granted
		}

		createNotificationChannel(context)

		// Intent to open HabitDetailActivity when notification is clicked
		val intent = Intent(context, MainActivity::class.java).apply {
			putExtra("habitId", habitId)  // Pass the habitId to the new screen
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		}

		// Wrap the intent in a PendingIntent
		val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

		val notification = NotificationCompat.Builder(context, CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_launcher_foreground)
			.setContentTitle("Habit Reminder")
			.setContentText("Don't forget to work on: $habitName")
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setContentIntent(pendingIntent) // Set the PendingIntent here
			.setAutoCancel(true) // Notification disappears once clicked
			.build()

		NotificationManagerCompat.from(context).notify(habitName.hashCode(), notification)
	}

	private fun createNotificationChannel(context: Context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(
				CHANNEL_ID,
				CHANNEL_NAME,
				NotificationManager.IMPORTANCE_HIGH
			).apply {
				description = CHANNEL_DESC
			}
			val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			manager.createNotificationChannel(channel)
		}
	}
}
