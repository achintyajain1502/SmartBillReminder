package com.example.smartbillreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object BillAlarmScheduler {

    private fun createPendingIntent(
        context: Context,
        accountNumber: String,
        billName: String,
        amount: Double
    ): PendingIntent {

        val intent = Intent(
            context,
            BillReminderReceiver::class.java
        ).apply {
            data = Uri.parse(
                "smartbill://reminder/${Uri.encode(accountNumber)}"
            )

            putExtra("bill_account_number", accountNumber)
            putExtra("bill_name", billName)
            putExtra("bill_amount", amount)
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleReminder(
        context: Context,
        accountNumber: String,
        billName: String,
        amount: Double,
        dueDate: String
    ) {
        try {
            val dateFormat = SimpleDateFormat(
                "d/M/yyyy",
                Locale.getDefault()
            ).apply {
                isLenient = false
            }

            val parsedDate = dateFormat.parse(dueDate) ?: return

            val reminderCalendar = Calendar.getInstance().apply {
                time = parsedDate
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -1)
            }

            val pendingIntent = createPendingIntent(
                context,
                accountNumber,
                billName,
                amount
            )

            val alarmManager = context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

            if (reminderCalendar.timeInMillis <= System.currentTimeMillis()) {
                alarmManager.cancel(pendingIntent)
                return
            }

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
        accountNumber: String
    ) {
        val pendingIntent = createPendingIntent(
            context,
            accountNumber,
            "",
            0.0
        )

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}