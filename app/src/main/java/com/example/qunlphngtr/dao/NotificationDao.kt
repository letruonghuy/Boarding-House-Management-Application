package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.qunlphngtr.NotificationHandler
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Notification

class NotificationDao(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val notificationHandler = NotificationHandler(context)

    fun insertNotification(title: String, message: String, type: String?, date: String?, tenantId: Int?): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("message", message)
            put("type", type)
            put("date", date)
            if (tenantId != null) put("tenant_id", tenantId) else putNull("tenant_id")
        }
        val id = db.insert("Notification", null, values)
        db.close()

        if (id != -1L) {
            notificationHandler.showNotification(title, message)
        }

        return id
    }

    fun getNotificationsForTenant(tenantId: Int): List<Notification> {
        val notifications = mutableListOf<Notification>()
        val db = dbHelper.readableDatabase
        val cursor = db.query("Notification", null, "tenant_id = ?", arrayOf(tenantId.toString()), null, null, "id DESC")
        if (cursor.moveToFirst()) {
            do {
                notifications.add(cursorToNotification(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return notifications
    }

    fun getNotificationsForLandlord(): List<Notification> {
        val notifications = mutableListOf<Notification>()
        val db = dbHelper.readableDatabase
        val cursor = db.query("Notification", null, "tenant_id IS NULL", null, null, null, "id DESC")
        if (cursor.moveToFirst()) {
            do {
                notifications.add(cursorToNotification(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return notifications
    }

    fun markAsRead(notificationId: Int): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("is_read", 1)
        }
        val result = db.update("Notification", values, "id = ?", arrayOf(notificationId.toString()))
        db.close()
        return result
    }

    fun getUnreadCount(tenantId: Int): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Notification WHERE tenant_id = ? AND is_read = 0", arrayOf(tenantId.toString()))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun getUnreadCountForLandlord(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Notification WHERE tenant_id IS NULL AND is_read = 0", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun broadcastToAll(title: String, message: String): Int {
        val db = dbHelper.writableDatabase
        var inserted = 0
        try {
            db.beginTransaction()
            val cursor = db.rawQuery("SELECT id FROM Tenant", null)
            if (cursor.moveToFirst()) {
                do {
                    val tenantId = cursor.getInt(0)
                    val values = ContentValues().apply {
                        put("title", title)
                        put("message", message)
                        put("type", "broadcast")
                        put("date", java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
                        put("tenant_id", tenantId)
                    }
                    val id = db.insert("Notification", null, values)
                    if (id != -1L) {
                        inserted++
                        notificationHandler.showNotification(title, message)
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
        return inserted
    }

    private fun cursorToNotification(cursor: Cursor): Notification {
        val tenantIdIndex = cursor.getColumnIndex("tenant_id")
        val tenantId = if (tenantIdIndex != -1 && !cursor.isNull(tenantIdIndex)) cursor.getInt(tenantIdIndex) else null
        return Notification(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            message = cursor.getString(cursor.getColumnIndexOrThrow("message")),
            type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
            date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
            isRead = cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1,
            tenantId = tenantId
        )
    }
}
