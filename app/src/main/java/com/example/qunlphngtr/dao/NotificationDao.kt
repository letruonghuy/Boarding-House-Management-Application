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

    // HÀM MỚI: Hợp nhất logic lấy thông báo
    fun getNotificationsForUser(tenantId: Int?): List<Notification> {
        val db = dbHelper.readableDatabase
        val selection = if (tenantId == null) "tenant_id IS NULL" else "tenant_id = ?"
        val selectionArgs = if (tenantId == null) null else arrayOf(tenantId.toString())
        val cursor = db.query("Notification", null, selection, selectionArgs, null, null, "id DESC")
        
        val notifications = mutableListOf<Notification>()
        if (cursor.moveToFirst()) {
            do {
                notifications.add(cursorToNotification(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return notifications
    }
    
    // HÀM MỚI: Hợp nhất logic đánh dấu đã đọc
    fun markAllAsRead(tenantId: Int?) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("is_read", 1)
        }
        val whereClause = if (tenantId == null) "tenant_id IS NULL" else "tenant_id = ?"
        val whereArgs = if (tenantId == null) null else arrayOf(tenantId.toString())
        db.update("Notification", values, whereClause, whereArgs)
        db.close()
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

    // ... (Các hàm khác giữ nguyên)

    private fun cursorToNotification(cursor: Cursor): Notification {
        val tenantIdIndex = cursor.getColumnIndex("tenant_id")
        val tenantIdValue = if (tenantIdIndex != -1 && !cursor.isNull(tenantIdIndex)) cursor.getInt(tenantIdIndex) else null
        return Notification(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            message = cursor.getString(cursor.getColumnIndexOrThrow("message")),
            type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
            date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
            isRead = cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1,
            tenantId = tenantIdValue
        )
    }
}