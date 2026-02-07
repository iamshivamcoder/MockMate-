package com.shivams.mockmate.notifications

import android.app.*
import android.content.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.shivams.mockmate.MainActivity
import com.shivams.mockmate.R
import java.util.*

/**
 * Broadcast receiver for scheduling and displaying study reminder notifications.
 * Schedules 10 notifications distributed over 7 days.
 */
class TestReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "test_reminder_channel"
        private const val TAG = "TestReminderReceiver"
        
        // Base request code for notifications (will be offset by notification index)
        private const val BASE_REQUEST_CODE = 2000
        
        // Notification schedule: 10 notifications over 7 days
        // Each entry: Pair(dayOffset, hourOfDay)
        private val NOTIFICATION_SCHEDULE = listOf(
            Pair(0, 9),   // Day 1, 9:00 AM - Morning motivation
            Pair(0, 19),  // Day 1, 7:00 PM - Evening reminder
            Pair(1, 14),  // Day 2, 2:00 PM - Midday practice
            Pair(2, 9),   // Day 3, 9:00 AM - Study tip
            Pair(2, 20),  // Day 3, 8:00 PM - Progress check
            Pair(3, 18),  // Day 4, 6:00 PM - Evening challenge
            Pair(4, 10),  // Day 5, 10:00 AM - Mid-week push
            Pair(5, 11),  // Day 6, 11:00 AM - Weekend study
            Pair(5, 17),  // Day 6, 5:00 PM - Practice reminder
            Pair(6, 16)   // Day 7, 4:00 PM - Week wrap-up
        )
        
        // Varied notification messages for each scheduled time
        private val NOTIFICATION_MESSAGES = listOf(
            // Day 1 Morning
            listOf(
                "Naya hafte ki nayi shuruaat! Chal ek test maar 🚀",
                "Monday motivation: Padhai shuru kar, sapne poore kar 💪",
                "Uth ja champion, UPSC khud nahi crackega 😜"
            ),
            // Day 1 Evening
            listOf(
                "Din khatam hone se pehle ek revision karle 📖",
                "Shaam ki chai ke saath thoda GK bhi 🍵📚",
                "Raat ko neend achi aayegi agar guilt free soye 😉"
            ),
            // Day 2 Midday
            listOf(
                "Lunch break = Quick quiz time! 🍛📝",
                "Afternoon slump? Ek test se dimaag refresh kar 🧠",
                "Dopahar ki padhai yaad rehti hai, believe me! ⭐"
            ),
            // Day 3 Morning
            listOf(
                "Study Tip: Daily revision > Weekly cramming 🎯",
                "Pro tip: Short sessions, high focus = better retention 💡",
                "Winning strategy: Consistency beats intensity 🏆"
            ),
            // Day 3 Evening
            listOf(
                "Progress check: Tune aaj kitna padha? 📊",
                "3 din ho gaye - ab momentum maintain kar! 🔥",
                "Har din thoda better = exam mein winner 🥇"
            ),
            // Day 4 Evening
            listOf(
                "Challenge time: Aaj ek nayi topic try kar 🎲",
                "Comfort zone se bahar nikal, kuch naya seekh 🌟",
                "4th day strong! Keep pushing 💪"
            ),
            // Day 5 Morning
            listOf(
                "Half week done! Celebrate with a mock test 🎉",
                "Mid-week check: Goals track pe hain? 📈",
                "5 days of consistency = future IAS 😎"
            ),
            // Day 6 Morning
            listOf(
                "Weekend = Extra study time, waste mat kar! 📚",
                "Saturday special: Double revision session? 🤔",
                "Weekend warriors crack UPSC! 🏅"
            ),
            // Day 6 Evening
            listOf(
                "Shaam ko ek quick test - Monday ki taiyaari 📝",
                "Weekend mode ON but padhai bhi ON 😄",
                "Practice karo, perfect bano! ✨"
            ),
            // Day 7 Afternoon
            listOf(
                "Week wrap-up: Kya seekha is hafte? 🤓",
                "Sunday reflection + Monday prep = smart move 🧠",
                "Last push of the week - finish strong! 💥"
            )
        )

        /**
         * Schedule all 10 notifications distributed over 7 days.
         * After 7 days, the cycle repeats.
         */
        fun scheduleTestReminder(context: Context): Boolean {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            Log.d(TAG, "Scheduling 10 notifications over 7 days...")
            
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "SCHEDULE_EXACT_ALARM permission not granted")
                return false
            }
            
            var allScheduled = true
            val startCalendar = getStartOfCycle(context)
            
            NOTIFICATION_SCHEDULE.forEachIndexed { index, (dayOffset, hour) ->
                val scheduled = scheduleNotification(
                    context, 
                    alarmManager, 
                    startCalendar,
                    dayOffset, 
                    hour, 
                    BASE_REQUEST_CODE + index
                )
                if (!scheduled) allScheduled = false
            }
            
            return allScheduled
        }

        /**
         * Get the start of the current 7-day cycle.
         * If a cycle has already started, we use that starting point.
         */
        private fun getStartOfCycle(context: Context): Calendar {
            val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            val cycleStart = prefs.getLong("cycle_start_time", 0L)
            
            val calendar = Calendar.getInstance()
            
            if (cycleStart == 0L || cycleStart < System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)) {
                // Start a new cycle from today
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                prefs.edit().putLong("cycle_start_time", calendar.timeInMillis).apply()
            } else {
                calendar.timeInMillis = cycleStart
            }
            
            return calendar
        }

        /**
         * Schedule a single notification at a specific day offset and hour.
         */
        private fun scheduleNotification(
            context: Context,
            alarmManager: AlarmManager,
            cycleStart: Calendar,
            dayOffset: Int,
            hour: Int,
            requestCode: Int
        ): Boolean {
            val intent = Intent(context, TestReminderReceiver::class.java).apply {
                putExtra("requestCode", requestCode)
                putExtra("notificationIndex", requestCode - BASE_REQUEST_CODE)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val targetCalendar = (cycleStart.clone() as Calendar).apply {
                add(Calendar.DATE, dayOffset)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            
            // If time has passed, schedule for next week
            if (targetCalendar.before(Calendar.getInstance())) {
                targetCalendar.add(Calendar.DATE, 7)
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetCalendar.timeInMillis,
                pendingIntent
            )
            
            Log.d(TAG, "Scheduled notification $requestCode for ${targetCalendar.time}")
            return true
        }

        /**
         * Cancel all scheduled notifications.
         */
        fun cancelTestReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            Log.d(TAG, "Cancelling all scheduled notifications")
            
            NOTIFICATION_SCHEDULE.indices.forEach { index ->
                val intent = Intent(context, TestReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    BASE_REQUEST_CODE + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
            
            // Reset cycle start
            context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
                .edit().remove("cycle_start_time").apply()
        }

        /**
         * Show a notification with the given message.
         */
        fun showNotification(context: Context, message: String) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Study Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Motivational reminders to study for UPSC"
            }
            notificationManager.createNotificationChannel(channel)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("MockMate Reminder 📚")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            // Use unique ID based on current time to allow multiple notifications
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
            Log.d(TAG, "Notification shown: $message")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val requestCode = intent.getIntExtra("requestCode", -1)
        val notificationIndex = intent.getIntExtra("notificationIndex", 0)
            .coerceIn(0, NOTIFICATION_MESSAGES.size - 1)
        
        Log.d(TAG, "Received alarm for request code $requestCode, index $notificationIndex")
        
        // Get a random message from the appropriate message list
        val messages = NOTIFICATION_MESSAGES.getOrElse(notificationIndex) { 
            listOf("Time to study! Open MockMate and practice 📖") 
        }
        val message = messages.random()
        
        showNotification(context, message)
        
        // Reschedule for next week (7 days later)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (alarmManager.canScheduleExactAlarms()) {
            val (dayOffset, hour) = NOTIFICATION_SCHEDULE.getOrElse(notificationIndex) { Pair(0, 9) }
            
            val nextTrigger = Calendar.getInstance().apply {
                add(Calendar.DATE, 7) // Schedule for next week
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            
            val newIntent = Intent(context, TestReminderReceiver::class.java).apply {
                putExtra("requestCode", requestCode)
                putExtra("notificationIndex", notificationIndex)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTrigger.timeInMillis,
                pendingIntent
            )
            Log.d(TAG, "Rescheduled notification $requestCode for ${nextTrigger.time}")
        }
    }
}
