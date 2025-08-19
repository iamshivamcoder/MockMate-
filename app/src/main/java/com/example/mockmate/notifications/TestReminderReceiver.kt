package com.example.mockmate.notifications

import android.app.*
import android.content.*
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mockmate.MainActivity
import com.example.mockmate.R
import java.util.*

class TestReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "test_reminder_channel"
        private const val NOTIFICATION_ID = 123 // Added a fixed notification ID
        private val MORNING_CODE = 1001
        private val AFTERNOON_CODE = 1002
        private val EVENING_CODE = 1003

        private val morningMsgs = listOf(
            "Uth ja bhai… IAS neend se nahi, notes se banta hai 😜",
            "Subah ki padhai = pura din guilt free 😉",
            "Book khol… warna dreams mein hi topper banega 😂"
        )

        private val afternoonMsgs = listOf(
            "Sleep mode off kar, study mode on kar 😴➡️📖",
            "Ek chhota test maar, dimaag turant refresh ho jayega ☕🧠",
            "Lunch ho gaya? Ab thoda knowledge bhi kha le 🍛📚"
        )

        private val eveningMsgs = listOf(
            "Arre yaar, Insta scroll baad mein… pehle syllabus scroll kar 🤭",
            "Shaam ki revision = kal ka confidence 💪",
            "UPSC bol raha hai… thoda padh le warna guilt ke sath sona padega 😂"
        )

        fun scheduleTestReminder(context: Context): Boolean {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            Log.d("TestReminderReceiver", "scheduleTestReminder called")

            // 3 times: 9 AM, 2 PM, 7 PM
            val morningScheduled = scheduleDaily(context, alarmManager, 9, MORNING_CODE)
            val afternoonScheduled = scheduleDaily(context, alarmManager, 14, AFTERNOON_CODE)
            val eveningScheduled = scheduleDaily(context, alarmManager, 19, EVENING_CODE)

            return morningScheduled && afternoonScheduled && eveningScheduled
        }

        fun cancelTestReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            Log.d("TestReminderReceiver", "cancelTestReminder called")
            cancelDaily(context, alarmManager, MORNING_CODE)
            cancelDaily(context, alarmManager, AFTERNOON_CODE)
            cancelDaily(context, alarmManager, EVENING_CODE)
        }

        private fun scheduleDaily(
            context: Context,
            alarmManager: AlarmManager,
            hour: Int,
            requestCode: Int
        ): Boolean { // Return Boolean
            val intent = Intent(context, TestReminderReceiver::class.java).apply {
                putExtra("requestCode", requestCode)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0) // Set minute to 0 directly
                set(Calendar.SECOND, 0)
                // If the scheduled time today has already passed, schedule for the next day.
                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 1)
                }
            }

            // SDK_INT is always >= 26 (minSdk is 26), so no need for this check
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("TestReminderReceiver", "SCHEDULE_EXACT_ALARM permission not granted. Cannot schedule exact daily reminder for $hour:00. Please enable 'Alarms & Reminders' for the app in system settings.")
                return false // Indicate failure
            }

            // SDK_INT is always >= 26 (minSdk is 26), so setExactAndAllowWhileIdle is always available
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("TestReminderReceiver", "Alarm scheduled for $hour:00 with request code $requestCode at ${calendar.timeInMillis}")
            return true // Indicate success
        }

        private fun cancelDaily(
            context: Context,
            alarmManager: AlarmManager,
            requestCode: Int
        ) {
            val intent = Intent(context, TestReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("TestReminderReceiver", "Alarm canceled for request code $requestCode")
        }

        fun showNotification(context: Context, message: String) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // SDK_INT is always >= 26 (minSdk is 26), so NotificationChannel is always available
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Test Reminders", // Channel name
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Daily reminders to take a test"
            notificationManager.createNotificationChannel(channel)

            // Create an Intent to open MainActivity
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("UPSC Reminder 📚")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Set your app's icon
                .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Use BigTextStyle
                .setContentIntent(pendingIntent) // Set the PendingIntent
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification) // Use a fixed ID to update the notification
            Log.d("TestReminderReceiver", "Notification shown: $message")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val requestCode = intent.getIntExtra("requestCode", -1)
        Log.d("TestReminderReceiver", "Alarm received for request code $requestCode")
        val msg = when (requestCode) {
            MORNING_CODE -> morningMsgs.random()
            AFTERNOON_CODE -> afternoonMsgs.random()
            EVENING_CODE -> eveningMsgs.random()
            else -> "Time to study and crack UPSC 💪" // Default fallback
        }

        showNotification(context, msg) // Removed Companion qualifier
        // This reschedules all three for their next occurrences.
        scheduleTestReminder(context)
    }
}
