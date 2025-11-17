package com.example.qunlphngtr

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID_BILLS = "channel_bills"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Thông báo hóa đơn"
                val descriptionText = "Thông báo khi có hóa đơn mới"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID_BILLS, name, importance)
                channel.description = descriptionText
                channel.enableLights(true)
                channel.lightColor = Color.RED
                nm.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    @Suppress("MissingPermission")
    fun notifyTenant(tenantId: Int, title: String, message: String, notifIdSeed: Int = 0) {
        try {
            // On Android 13+ we must have POST_NOTIFICATIONS permission granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    // Don't attempt to post system notification if permission is missing
                    return
                }
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_BILLS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notifId = (tenantId.hashCode() * 31) xor notifIdSeed
            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(notifId, builder.build())
                }
            } catch (e: SecurityException) {
                // If permission missing at runtime, ignore
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
