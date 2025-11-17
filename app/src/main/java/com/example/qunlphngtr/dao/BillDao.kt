package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Bill

class BillDao(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val notificationDao = NotificationDao(context)

    // tenantId can be null (room may be vacant) -> handle putNull
    fun insertBill(month: String, electric: Double, water: Double, roomFee: Double, internet: Double, roomId: Int, tenantId: Int?): Long {
        val total = electric + water + roomFee + internet
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("month", month)
            put("electric", electric)
            put("water", water)
            put("roomFee", roomFee)
            put("internet", internet)
            put("total", total)
            put("room_id", roomId)
            if (tenantId != null) put("tenant_id", tenantId) else putNull("tenant_id")
            put("status", "unpaid")
        }
        val id = db.insert("Bill", null, values)
        db.close()
        return id
    }

    fun getAllBills(): MutableList<Bill> {
        val bills = mutableListOf<Bill>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT b.id, b.month, b.electric, b.water, b.roomFee, b.internet, b.total, b.room_id, b.tenant_id, b.status, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id ORDER BY b.id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val month = cursor.getString(cursor.getColumnIndexOrThrow("month"))
                val electric = cursor.getDouble(cursor.getColumnIndexOrThrow("electric"))
                val water = cursor.getDouble(cursor.getColumnIndexOrThrow("water"))
                val roomFee = cursor.getDouble(cursor.getColumnIndexOrThrow("roomFee"))
                val internet = cursor.getDouble(cursor.getColumnIndexOrThrow("internet"))
                val total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
                val roomId = cursor.getInt(cursor.getColumnIndexOrThrow("room_id"))
                val tenantId = cursor.getInt(cursor.getColumnIndexOrThrow("tenant_id"))
                val roomName = cursor.getString(cursor.getColumnIndexOrThrow("roomName"))
                val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

                bills.add(Bill(id, month, electric, water, roomFee, internet, total, roomId, tenantId, roomName, status))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bills
    }
    fun getBillsByMonth(monthYear: String): MutableList<Bill> {
        val bills = mutableListOf<Bill>()
        val db = dbHelper.readableDatabase
        val query = "SELECT b.id, b.month, b.electric, b.water, b.roomFee, b.internet, b.total, b.room_id, b.tenant_id, b.status, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id WHERE b.month = ? ORDER BY b.id DESC"
        val cursor: Cursor = db.rawQuery(query, arrayOf(monthYear))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val month = cursor.getString(cursor.getColumnIndexOrThrow("month"))
                val electric = cursor.getDouble(cursor.getColumnIndexOrThrow("electric"))
                val water = cursor.getDouble(cursor.getColumnIndexOrThrow("water"))
                val roomFee = cursor.getDouble(cursor.getColumnIndexOrThrow("roomFee"))
                val internet = cursor.getDouble(cursor.getColumnIndexOrThrow("internet"))
                val total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
                val roomId = cursor.getInt(cursor.getColumnIndexOrThrow("room_id"))
                val tenantId = cursor.getInt(cursor.getColumnIndexOrThrow("tenant_id"))
                val roomName = cursor.getString(cursor.getColumnIndexOrThrow("roomName"))
                val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

                bills.add(Bill(id, month, electric, water, roomFee, internet, total, roomId, tenantId, roomName, status))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bills
    }


    fun getBillById(id: Int): Bill? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT b.id, b.month, b.electric, b.water, b.roomFee, b.internet, b.total, b.room_id, b.tenant_id, b.status, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id WHERE b.id = ?", arrayOf(id.toString()))
        var bill: Bill? = null
        if (cursor.moveToFirst()) {
            val iid = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val month = cursor.getString(cursor.getColumnIndexOrThrow("month"))
            val electric = cursor.getDouble(cursor.getColumnIndexOrThrow("electric"))
            val water = cursor.getDouble(cursor.getColumnIndexOrThrow("water"))
            val roomFee = cursor.getDouble(cursor.getColumnIndexOrThrow("roomFee"))
            val internet = cursor.getDouble(cursor.getColumnIndexOrThrow("internet"))
            val total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
            val roomId = cursor.getInt(cursor.getColumnIndexOrThrow("room_id"))
            val tenantId = cursor.getInt(cursor.getColumnIndexOrThrow("tenant_id"))
            val roomName = cursor.getString(cursor.getColumnIndexOrThrow("roomName"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            bill = Bill(iid, month, electric, water, roomFee, internet, total, roomId, tenantId, roomName, status)
        }
        cursor.close()
        db.close()
        return bill
    }

    // Lấy hóa đơn mới nhất cho 1 tenant (theo id giảm dần)
    fun getLatestBillForTenant(tenantId: Int): Bill? {
        val db = dbHelper.readableDatabase
        val query = "SELECT b.id, b.month, b.electric, b.water, b.roomFee, b.internet, b.total, b.room_id, b.tenant_id, b.status, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id WHERE b.tenant_id = ? ORDER BY b.id DESC LIMIT 1"
        val cursor: Cursor = db.rawQuery(query, arrayOf(tenantId.toString()))
        var bill: Bill? = null
        if (cursor.moveToFirst()) {
            val iid = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val month = cursor.getString(cursor.getColumnIndexOrThrow("month"))
            val electric = cursor.getDouble(cursor.getColumnIndexOrThrow("electric"))
            val water = cursor.getDouble(cursor.getColumnIndexOrThrow("water"))
            val roomFee = cursor.getDouble(cursor.getColumnIndexOrThrow("roomFee"))
            val internet = cursor.getDouble(cursor.getColumnIndexOrThrow("internet"))
            val total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
            val roomId = cursor.getInt(cursor.getColumnIndexOrThrow("room_id"))
            val tenant = cursor.getInt(cursor.getColumnIndexOrThrow("tenant_id"))
            val roomName = cursor.getString(cursor.getColumnIndexOrThrow("roomName"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            bill = Bill(iid, month, electric, water, roomFee, internet, total, roomId, tenant, roomName, status)
        }
        cursor.close()
        db.close()
        return bill
    }

    // Generate bills for all tenants who have a room assignment for a given month (e.g., "11/2025")
    fun generateMonthlyBills(monthYear: String): Int {
        val db = dbHelper.writableDatabase
        var inserted = 0
        try {
            db.beginTransaction()
            // Select tenants who have room_id not null and join with room to get price
            val cursor = db.rawQuery(
                "SELECT t.id as tenantId, r.id as roomId, r.price FROM Tenant t JOIN Room r ON t.room_id = r.id WHERE t.room_id IS NOT NULL",
                null
            )
            if (cursor.moveToFirst()) {
                do {
                    val tenantId = cursor.getInt(cursor.getColumnIndexOrThrow("tenantId"))
                    val roomId = cursor.getInt(cursor.getColumnIndexOrThrow("roomId"))
                    val roomFee = cursor.getDouble(cursor.getColumnIndexOrThrow("price"))

                    val values = ContentValues().apply {
                        put("month", monthYear)
                        put("electric", 0.0)
                        put("water", 0.0)
                        put("roomFee", roomFee)
                        put("internet", 0.0)
                        put("total", roomFee)
                        put("status", "unpaid")
                        put("room_id", roomId)
                        put("tenant_id", tenantId)
                    }
                    val id = db.insert("Bill", null, values)
                    if (id != -1L) inserted++

                    // Create a notification for this tenant to inform them a bill was created
                    try {
                        val title = "Hóa đơn mới"
                        val message = "Bạn có hóa đơn cho tháng $monthYear. Vui lòng kiểm tra."
                        val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        notificationDao.insertNotification(title, message, "bill", date, tenantId)

                        // Removed system notification call to avoid permission/analysis issues
                        // try {
                        //     val helper = com.example.qunlphngtr.NotificationHelper(context)
                        //     helper.notifyTenant(tenantId, title, message, tenantId)
                        // } catch (n2: Exception) {
                        //     n2.printStackTrace()
                        // }
                    } catch (nEx: Exception) {
                        // Notification failure shouldn't stop bill creation; just log
                        nEx.printStackTrace()
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

    fun updateBillStatus(id: Int, status: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("status", status)
        }
        val result = db.update("Bill", values, "id = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun deleteBill(id: Int): Int {
        val db = dbHelper.writableDatabase
        val result = db.delete("Bill", "id = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

}
