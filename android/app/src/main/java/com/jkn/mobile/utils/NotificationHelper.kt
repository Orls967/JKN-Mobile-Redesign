package com.jkn.mobile.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics

object NotificationHelper {
    private const val CHANNEL_ID = "queue_alerts"
    private const val CHANNEL_NAME = "JKN Antrean"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pemanggilan antrean"
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showQueueNotification(context: Context, ticketNumber: Int) {
        try {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Nomor Antrean Anda Dipanggil")
                .setContentText("Nomor antrean Anda ($ticketNumber) sedang dipanggil. Silakan menuju loket sekarang.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(ticketNumber, builder.build())

            // Story 1.5 — Log successful queue called notification
            FirebaseCrashlytics.getInstance().log("Queue called notification triggered: ticket=$ticketNumber")
        } catch (e: Exception) {
            // Story 1.5 — Record notification failure to Crashlytics
            FirebaseCrashlytics.getInstance().recordException(
                Exception("Notification Failure [Queue Called]: ${e.message}", e)
            )
        }
    }

    fun showProximityNotification(context: Context, remainingQueue: Int) {
        try {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Antrean Anda Segera Tiba")
                .setContentText("Tersisa $remainingQueue nomor lagi")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1001, builder.build()) // Fixed ID prevents stacking multiple proximity notifications

            // Story 1.5 — Log successful proximity notification
            FirebaseCrashlytics.getInstance().log("Proximity notification triggered: remaining=$remainingQueue")
        } catch (e: Exception) {
            // Story 1.5 — Record notification failure to Crashlytics
            FirebaseCrashlytics.getInstance().recordException(
                Exception("Notification Failure [Proximity]: ${e.message}", e)
            )
        }
    }
}

