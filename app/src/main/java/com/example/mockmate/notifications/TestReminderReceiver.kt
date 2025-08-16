package com.example.mockmate.notifications

import android.app.*
import android.content.*
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class TestReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "test_reminder_channel"
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
            val morningScheduled = scheduleDaily(context, alarmManager, 9, 0, MORNING_CODE)
            val afternoonScheduled = scheduleDaily(context, alarmManager, 14, 0, AFTERNOON_CODE)
            val eveningScheduled = scheduleDaily(context, alarmManager, 19, 0, EVENING_CODE)

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
            minute: Int,
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
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                // If the scheduled time today has already passed, schedule for the next day.
                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.e("TestReminderReceiver", "SCHEDULE_EXACT_ALARM permission not granted. Cannot schedule exact daily reminder for $hour:$minute. Please enable 'Alarms & Reminders' for the app in system settings.")
                return false // Indicate failure
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("TestReminderReceiver", "Alarm scheduled for $hour:$minute with request code $requestCode at ${calendar.timeInMillis}")
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

        showNotification(context, msg)
        // This reschedules all three for their next occurrences.
        // We can choose to handle the return value here if needed, e.g., log if rescheduling fails.
        scheduleTestReminder(context)
    }

    private fun showNotification(context: Context, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Test Reminders", // Channel name
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Daily reminders to take a test"
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("UPSC Reminder 📚")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard Android icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d("TestReminderReceiver", "Notification shown: $message")
    }
}
