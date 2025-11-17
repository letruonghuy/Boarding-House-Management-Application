package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import com.example.qunlphngtr.database.DatabaseHelper

class NotificationDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

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
        return id
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
                    if (id != -1L) inserted++
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
}

