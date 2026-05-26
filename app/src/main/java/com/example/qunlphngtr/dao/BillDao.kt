package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Bill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillDao(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insertBill(bill: Bill): Long {
        val db = dbHelper.writableDatabase
        val values = billToContentValues(bill)
        val id = db.insert("Bill", null, values)
        db.close()
        return id
    }

    fun updateBill(bill: Bill): Int {
        val db = dbHelper.writableDatabase
        val values = billToContentValues(bill)
        val result = db.update("Bill", values, "id = ?", arrayOf(bill.id.toString()))
        db.close()
        return result
    }
    fun getUnpaidBillsByTenantId(tenantId: Int): List<Bill> {
        val bills = mutableListOf<Bill>()
        val db = dbHelper.readableDatabase
        val query = "SELECT b.*, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id WHERE b.tenant_id = ? AND b.status IN ('unpaid', 'pending_confirmation')"
        val cursor: Cursor = db.rawQuery(query, arrayOf(tenantId.toString()))
        if (cursor.moveToFirst()) {
            do {
                bills.add(cursorToBill(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bills
    }
    fun getUnpaidBillsByRoomId(roomId: Int): List<Bill> {
        val bills = mutableListOf<Bill>()
        val db = dbHelper.readableDatabase
        val query = "SELECT b.*, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id WHERE b.room_id = ? AND b.status IN ('unpaid', 'pending_confirmation')"
        val cursor: Cursor = db.rawQuery(query, arrayOf(roomId.toString()))
        if (cursor.moveToFirst()) {
            do {
                bills.add(cursorToBill(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bills
    }

    fun getBillsByRoomId(roomId: Int): List<Bill> {
        val bills = mutableListOf<Bill>()
        val db = dbHelper.readableDatabase
        val query = "SELECT b.*, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id WHERE b.room_id = ? ORDER BY b.id DESC"
        val cursor: Cursor = db.rawQuery(query, arrayOf(roomId.toString()))
        if (cursor.moveToFirst()) {
            do {
                bills.add(cursorToBill(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bills
    }

    fun getAllBills(): MutableList<Bill> {
        val bills = mutableListOf<Bill>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT b.*, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id ORDER BY b.id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                bills.add(cursorToBill(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bills
    }

    fun getBillsByMonth(monthYear: String): MutableList<Bill> {
        val bills = mutableListOf<Bill>()
        val db = dbHelper.readableDatabase
        val query = "SELECT b.*, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id WHERE b.month = ? ORDER BY b.id DESC"
        val cursor: Cursor = db.rawQuery(query, arrayOf(monthYear))
        if (cursor.moveToFirst()) {
            do {
                bills.add(cursorToBill(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bills
    }

    fun getBillById(id: Int): Bill? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT b.*, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id WHERE b.id = ?", arrayOf(id.toString()))
        var bill: Bill? = null
        if (cursor.moveToFirst()) {
            bill = cursorToBill(cursor)
        }
        cursor.close()
        db.close()
        return bill
    }

    fun getLatestBillForTenant(tenantId: Int): Bill? {
        val db = dbHelper.readableDatabase
        val query = "SELECT b.*, r.name as roomName FROM Bill b JOIN Room r ON b.room_id = r.id WHERE b.tenant_id = ? ORDER BY b.id DESC LIMIT 1"
        val cursor: Cursor = db.rawQuery(query, arrayOf(tenantId.toString()))
        var bill: Bill? = null
        if (cursor.moveToFirst()) {
            bill = cursorToBill(cursor)
        }
        cursor.close()
        db.close()
        return bill
    }

    fun generateMonthlyBills(monthYear: String): Int {
        val db = dbHelper.writableDatabase
        var inserted = 0
        try {
            db.beginTransaction()
            val cursor = db.rawQuery(
                "SELECT t.id as tenantId, r.id as roomId, r.price FROM Tenant t JOIN Room r ON t.room_id = r.id WHERE t.room_id IS NOT NULL",
                null
            )
            if (cursor.moveToFirst()) {
                val notificationDao = NotificationDao(context)
                do {
                    val tenantId = cursor.getInt(cursor.getColumnIndexOrThrow("tenantId"))
                    val roomId = cursor.getInt(cursor.getColumnIndexOrThrow("roomId"))
                    val roomFee = cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
                    val internetFee = 100000.0
                    val total = roomFee + internetFee

                    val values = ContentValues().apply {
                        put("month", monthYear)
                        put("electric", 0.0)
                        put("water", 0.0)
                        put("roomFee", roomFee)
                        put("internet", internetFee)
                        put("total", total)
                        put("status", "unpaid")
                        put("room_id", roomId)
                        put("tenant_id", tenantId)
                    }

                    val id = db.insert("Bill", null, values)
                    if (id != -1L) {
                        inserted++
                        try {
                            val title = "Hóa đơn mới"
                            val message = "Bạn có hóa đơn cho tháng $monthYear. Vui lòng kiểm tra."
                            val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                            notificationDao.insertNotification(title, message, "bill", date, tenantId)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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

    fun updateBillStatusAndProof(id: Int, status: String, proofUri: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("status", status)
            put("payment_proof_uri", proofUri)
        }
        val result = db.update("Bill", values, "id = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun deleteBill(id: Int): Int {
        val bill = getBillById(id)
        if (bill != null && bill.status != "paid") {
            return -2 // Cannot delete unpaid bill
        }
        val db = dbHelper.writableDatabase
        val result = db.delete("Bill", "id = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    private fun billToContentValues(bill: Bill): ContentValues {
        return ContentValues().apply {
            put("month", bill.month)
            put("electric", bill.electric)
            put("water", bill.water)
            put("roomFee", bill.roomFee)
            put("internet", bill.internet)
            put("total", bill.total)
            put("status", bill.status)
            put("room_id", bill.roomId)
            put("tenant_id", bill.tenantId)
            put("old_electric_reading", bill.oldElectricReading)
            put("new_electric_reading", bill.newElectricReading)
            put("old_water_reading", bill.oldWaterReading)
            put("new_water_reading", bill.newWaterReading)
            put("old_electric_image_uri", bill.oldElectricImageUri)
            put("new_electric_image_uri", bill.newElectricImageUri)
            put("old_water_image_uri", bill.oldWaterImageUri)
            put("new_water_image_uri", bill.newWaterImageUri)
            put("payment_proof_uri", bill.paymentProofUri)
        }
    }

    private fun cursorToBill(cursor: Cursor): Bill {
        fun getStringOrNull(cursor: Cursor, columnName: String): String? {
            val index = cursor.getColumnIndex(columnName)
            return if (index != -1 && !cursor.isNull(index)) cursor.getString(index) else null
        }

        return Bill(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            month = cursor.getString(cursor.getColumnIndexOrThrow("month")),
            electric = cursor.getDouble(cursor.getColumnIndexOrThrow("electric")),
            water = cursor.getDouble(cursor.getColumnIndexOrThrow("water")),
            roomFee = cursor.getDouble(cursor.getColumnIndexOrThrow("roomFee")),
            internet = cursor.getDouble(cursor.getColumnIndexOrThrow("internet")),
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total")),
            roomId = cursor.getInt(cursor.getColumnIndexOrThrow("room_id")),
            tenantId = cursor.getInt(cursor.getColumnIndexOrThrow("tenant_id")),
            roomName = cursor.getString(cursor.getColumnIndexOrThrow("roomName")),
            status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
            oldElectricReading = cursor.getInt(cursor.getColumnIndexOrThrow("old_electric_reading")),
            newElectricReading = cursor.getInt(cursor.getColumnIndexOrThrow("new_electric_reading")),
            oldWaterReading = cursor.getInt(cursor.getColumnIndexOrThrow("old_water_reading")),
            newWaterReading = cursor.getInt(cursor.getColumnIndexOrThrow("new_water_reading")),
            oldElectricImageUri = getStringOrNull(cursor, "old_electric_image_uri"),
            newElectricImageUri = getStringOrNull(cursor, "new_electric_image_uri"),
            oldWaterImageUri = getStringOrNull(cursor, "old_water_image_uri"),
            newWaterImageUri = getStringOrNull(cursor, "new_water_image_uri"),
            paymentProofUri = getStringOrNull(cursor, "payment_proof_uri")
        )
    }
}
