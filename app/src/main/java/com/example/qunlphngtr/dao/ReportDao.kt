package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Report

class ReportDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun getAllReports(): MutableList<Report> {
        val list = mutableListOf<Report>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM Notification ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val message = cursor.getString(cursor.getColumnIndexOrThrow("message"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val status = cursor.getInt(cursor.getColumnIndexOrThrow("is_read"))
                val tenantId = cursor.getInt(cursor.getColumnIndexOrThrow("tenant_id"))
                val st = if (status == 0) "New" else "Read"
                list.add(Report(id, title, message, date, st, tenantId))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun updateReportStatus(id: Int, status: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("is_read", if (status == "Read" || status == "Resolved") 1 else 0)
        }
        val res = db.update("Notification", values, "id = ?", arrayOf(id.toString()))
        db.close()
        return res
    }

    fun addResponse(id: Int, response: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("response", response)
        }
        val res = db.update("Report", values, "id = ?", arrayOf(id.toString()))
        db.close()
        return res
    }
}

