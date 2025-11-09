package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Bill

class BillDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insertBill(month: String, electric: Double, water: Double, roomFee: Double, internet: Double, roomId: Int, tenantId: Int): Long {
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
            put("tenant_id", tenantId)
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
