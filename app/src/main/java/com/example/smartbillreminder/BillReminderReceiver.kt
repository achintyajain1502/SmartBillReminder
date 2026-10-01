package com.example.smartbillreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class BillReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val billName =
            intent.getStringExtra("bill_name") ?: "Bill"

        val amount =
            intent.getDoubleExtra("bill_amount", 0.0)

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channel = NotificationChannel(
            "bill_reminders",
            "Bill Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(
            context,
            "bill_reminders"
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Bill Due Tomorrow")
            .setContentText(
                "$billName is due tomorrow. Amount: ₹$amount"
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}