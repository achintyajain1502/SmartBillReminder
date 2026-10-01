package com.example.smartbillreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object BillAlarmScheduler {

    fun scheduleReminder(
        context: Context,
        billId: Int,
        billName: String,
        amount: Double,
        dueDate: String
    ) {

        try {

            val dateFormat =
                SimpleDateFormat("d/M/yyyy", Locale.getDefault())

            val parsedDate = dateFormat.parse(dueDate)
                ?: return

            val reminderCalendar = Calendar.getInstance()

            reminderCalendar.time = parsedDate

            // Set reminder to 9:00 AM
            reminderCalendar.set(Calendar.HOUR_OF_DAY, 9)
            reminderCalendar.set(Calendar.MINUTE, 0)
            reminderCalendar.set(Calendar.SECOND, 0)
            reminderCalendar.set(Calendar.MILLISECOND, 0)

            // Reminder one day before due date
            reminderCalendar.add(Calendar.DAY_OF_YEAR, -1)

            // Don't schedule reminders that are already in the past
            if (reminderCalendar.timeInMillis <= System.currentTimeMillis()) {
                return
            }

            val intent = Intent(
                context,
                BillReminderReceiver::class.java
            )

            intent.putExtra("bill_id", billId)
            intent.putExtra("bill_name", billName)
            intent.putExtra("bill_amount", amount)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                billId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderCalendar.timeInMillis,
                pendingIntent
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelReminder(
        context: Context,
        billId: Int
    ) {

        val intent = Intent(
            context,
            BillReminderReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            billId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.cancel(pendingIntent)
    }
}