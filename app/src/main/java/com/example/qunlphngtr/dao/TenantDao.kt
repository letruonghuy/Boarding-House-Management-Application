package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Tenant // Đảm bảo import đúng model
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.RoomDao

class TenantDao(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    private fun cursorToTenant(cursor: Cursor): Tenant {
        fun getStringOrNull(colName: String): String? {
            val index = cursor.getColumnIndex(colName)
            return if (index != -1 && !cursor.isNull(index)) cursor.getString(index) else null
        }

        fun getIntOrNull(colName: String): Int? {
            val index = cursor.getColumnIndex(colName)
            return if (index != -1 && !cursor.isNull(index)) cursor.getInt(index) else null
        }

        return Tenant(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            gender = getStringOrNull("gender"),
            phone = getStringOrNull("phone"),
            imageUri = getStringOrNull("imageUri"),
            identity_number = getStringOrNull("identity_number"),
            room_id = getIntOrNull("room_id"),
            start_date = getStringOrNull("start_date"),
            end_date = getStringOrNull("end_date"),
            user_id = getIntOrNull("user_id"),
            cccd_front_uri = getStringOrNull("cccd_front_uri"),
            cccd_back_uri = getStringOrNull("cccd_back_uri")
        )
    }

    // HÀM MỚI CHUYÊN DỤNG
    fun findUnactivatedTenantByIdentityNumber(identityNumber: String): Tenant? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "Tenant",
            null,
            "identity_number = ? AND user_id IS NULL", // Điều kiện quan trọng
            arrayOf(identityNumber),
            null, null, null
        )
        var tenant: Tenant? = null
        if (cursor.moveToFirst()) {
            tenant = cursorToTenant(cursor)
        }
        cursor.close()
        db.close()
        return tenant
    }

    fun getTenantById(tenantId: Int): Tenant? {
        val db = dbHelper.readableDatabase
        val cursor = db.query("Tenant", null, "id = ?", arrayOf(tenantId.toString()), null, null, null)
        var tenant: Tenant? = null
        if (cursor.moveToFirst()) {
            tenant = cursorToTenant(cursor)
        }
        cursor.close()
        db.close()
        return tenant
    }

    fun insertTenant(tenant: Tenant): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", tenant.name)
            put("gender", tenant.gender)
            put("phone", tenant.phone)
            put("imageUri", tenant.imageUri)
            put("identity_number", tenant.identity_number)
            put("cccd_front_uri", tenant.cccd_front_uri)
            put("cccd_back_uri", tenant.cccd_back_uri)
            if (tenant.room_id != null) put("room_id", tenant.room_id) else putNull("room_id")
            put("start_date", tenant.start_date)
            put("end_date", tenant.end_date)
            if (tenant.user_id != null) put("user_id", tenant.user_id) else putNull("user_id")
        }
        return try {
            val id = db.insert("Tenant", null, values)
            id
        } catch (e: SQLiteConstraintException) {
            e.printStackTrace()
            -1L
        } finally {
            db.close()
        }
    }

    fun getAllTenants(): List<Tenant> {
        val tenants = mutableListOf<Tenant>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM Tenant", null)
        if (cursor.moveToFirst()) {
            do {
                tenants.add(cursorToTenant(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tenants
    }

    fun updateTenant(tenant: Tenant): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", tenant.name)
            put("gender", tenant.gender)
            put("phone", tenant.phone)
            put("imageUri", tenant.imageUri)
            put("identity_number", tenant.identity_number)
            put("cccd_front_uri", tenant.cccd_front_uri)
            put("cccd_back_uri", tenant.cccd_back_uri)
            if (tenant.room_id != null) put("room_id", tenant.room_id) else putNull("room_id")
            put("start_date", tenant.start_date)
            put("end_date", tenant.end_date)
            if (tenant.user_id != null) put("user_id", tenant.user_id) else putNull("user_id")
        }
        return try {
            val result = db.update("Tenant", values, "id = ?", arrayOf(tenant.id.toString()))
            result
        } catch (e: SQLiteConstraintException) {
            e.printStackTrace()
            -1
        } finally {
            db.close()
        }
    }

    fun updateTenantImage(tenantId: Int, imageUri: String?): Pair<Int, String?> {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("imageUri", imageUri)
        }
        try {
            val result = db.update("Tenant", values, "id = ?", arrayOf(tenantId.toString()))
            return Pair(result, null)
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown DB error"
            if (msg.contains("foreign key", ignoreCase = true) || msg.contains("constraint", ignoreCase = true)) {
                try {
                    db.execSQL("PRAGMA foreign_keys = OFF")
                    val retry = db.update("Tenant", values, "id = ?", arrayOf(tenantId.toString()))
                    db.execSQL("PRAGMA foreign_keys = ON")
                    return Pair(retry, "Updated with foreign_keys disabled due to: $msg")
                } catch (ex2: Exception) {
                    ex2.printStackTrace()
                    return Pair(-1, "FK error and retry failed: ${ex2.message}")
                } finally {
                    try {
                        db.execSQL("PRAGMA foreign_keys = ON")
                    } catch (_: Exception) {
                    }
                }
            }
            e.printStackTrace()
            return Pair(-1, msg)
        } finally {
            db.close()
        }
    }

    fun deleteTenant(id: Int): Int {
        // 1. Check for unpaid bills
        val billDao = BillDao(context)
        val unpaidBills = billDao.getUnpaidBillsByTenantId(id)
        if (unpaidBills.isNotEmpty()) {
            return -2 // Error code for unpaid bills
        }

        // 2. Get tenant info to find the room
        val tenantToDelete = getTenantById(id)

        // 3. Perform deletion and room update in a transaction
        val db = dbHelper.writableDatabase
        var result = 0
        db.beginTransaction()
        try {
            // 3a. Update room if tenant was assigned to one
            tenantToDelete?.room_id?.let {
                val roomDao = RoomDao(context)
                val room = roomDao.getRoomById(it)
                room?.let {
                    val updatedRoom = room.copy(status = "available", tenantId = null)
                    roomDao.updateRoom(updatedRoom) // This should use the same db transaction if possible
                }
            }

            // 3b. Delete the tenant
            result = db.delete("Tenant", "id = ?", arrayOf(id.toString()))

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
            result = -1 // General error
        } finally {
            db.endTransaction()
            db.close()
        }

        return result
    }

    fun searchTenants(keyword: String): List<Tenant> {
        val tenants = mutableListOf<Tenant>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM Tenant WHERE name LIKE ? OR phone LIKE ?",
            arrayOf("%$keyword%", "%$keyword%")
        )
        if (cursor.moveToFirst()) {
            do {
                tenants.add(cursorToTenant(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tenants
    }

    fun getTenantByUserId(userId: Int): Tenant? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM Tenant WHERE user_id = ?",
            arrayOf(userId.toString())
        )
        var tenant: Tenant? = null
        if (cursor.moveToFirst()) {
            tenant = cursorToTenant(cursor)
        }
        cursor.close()
        db.close()
        return tenant
    }

    fun getTenantByRoomId(roomId: Int): Tenant? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Tenant WHERE room_id = ?", arrayOf(roomId.toString()))
        var tenant: Tenant? = null
        if (cursor.moveToFirst()) {
            tenant = cursorToTenant(cursor)
        }
        cursor.close()
        db.close()
        return tenant
    }

    fun getRoomNameByTenantId(tenantId: Int): String? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT Room.name 
            FROM Room 
            INNER JOIN Tenant ON Room.id = Tenant.room_id 
            WHERE Tenant.id = ?
        """
        val cursor: Cursor = db.rawQuery(query, arrayOf(tenantId.toString()))
        var roomName: String? = "Chưa có phòng"
        if (cursor.moveToFirst()) {
            val name = cursor.getString(0)
            if(name != null) roomName = "Đang thuê phòng $name"
        }
        cursor.close()
        db.close()
        return roomName
    }
}
