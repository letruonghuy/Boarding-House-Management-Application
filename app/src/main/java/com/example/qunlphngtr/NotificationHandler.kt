package com.example.qunlphngtr

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHandler(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "boarding_house_channel"
        private const val CHANNEL_NAME = "Thông báo chung"
        private const val CHANNEL_DESC = "Kênh cho tất cả các thông báo của ứng dụng"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications) // Thay thế bằng icon của bạn
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Tự động xóa khi người dùng nhấn vào

        try {
            with(NotificationManagerCompat.from(context)) {
                // notificationId là một số int duy nhất cho mỗi thông báo. 
                // Chúng ta có thể dùng System.currentTimeMillis() để nó không bao giờ trùng nhau.
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Xảy ra khi người dùng không cấp quyền POST_NOTIFICATIONS trên Android 13+
            e.printStackTrace()
        }
    }
}