package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Tenant // Đảm bảo import đúng model

class TenantDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    // Hàm tạo đối tượng Tenant từ Cursor (để dùng lại, tránh lặp code)
    private fun cursorToTenant(cursor: Cursor): Tenant {
        // Handle nullable integer columns properly (room_id, user_id) to avoid converting NULL -> 0
        val roomIdx = cursor.getColumnIndexOrThrow("room_id")
        val userIdx = cursor.getColumnIndexOrThrow("user_id")
        val roomId: Int? = if (cursor.isNull(roomIdx)) null else cursor.getInt(roomIdx)
        val userId: Int? = if (cursor.isNull(userIdx)) null else cursor.getInt(userIdx)

        return Tenant(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            gender = cursor.getString(cursor.getColumnIndexOrThrow("gender")),
            phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
            imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
            identity_number = cursor.getString(cursor.getColumnIndexOrThrow("identity_number")),
            room_id = roomId,
            start_date = cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
            end_date = cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
            user_id = userId
        )
    }

    // Thêm tenant mới (đã sửa để thêm đủ cột)
    fun insertTenant(tenant: Tenant): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", tenant.name)
            put("gender", tenant.gender)
            put("phone", tenant.phone)
            put("imageUri", tenant.imageUri)
            put("identity_number", tenant.identity_number)
            // room_id và user_id có thể null: nếu null -> putNull
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

    // Lấy tất cả tenant (đã sửa để lấy đủ cột)
    fun getAllTenants(): List<Tenant> {
        val tenants = mutableListOf<Tenant>()
        val db = dbHelper.readableDatabase
        // Sửa: Phải là SELECT * để lấy hết 10 cột
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

    // Cập nhật tenant (đã sửa để cập nhật đủ cột)
    fun updateTenant(tenant: Tenant): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", tenant.name)
            put("gender", tenant.gender)
            put("phone", tenant.phone)
            put("imageUri", tenant.imageUri)
            put("identity_number", tenant.identity_number)
            // Nếu room_id/user_id là null thì đặt null rõ ràng (để không vô tình set giá trị không hợp lệ)
            if (tenant.room_id != null) put("room_id", tenant.room_id) else putNull("room_id")
            put("start_date", tenant.start_date)
            put("end_date", tenant.end_date)
            if (tenant.user_id != null) put("user_id", tenant.user_id) else putNull("user_id")
        }
        return try {
            val result = db.update("Tenant", values, "id = ?", arrayOf(tenant.id.toString()))
            result
        } catch (e: SQLiteConstraintException) {
            // Bắt lỗi ràng buộc (ví dụ foreign key violation) và trả về -1 để caller biết
            e.printStackTrace()
            -1
        } finally {
            db.close()
        }
    }

    // Cập nhật chỉ imageUri (tránh nguy cơ gây foreign-key violation khi chỉ cập nhật ảnh)
    fun updateTenantImage(tenantId: Int, imageUri: String?): Pair<Int, String?> {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("imageUri", imageUri)
        }
        try {
            val result = db.update("Tenant", values, "id = ?", arrayOf(tenantId.toString()))
            return Pair(result, null)
        } catch (e: Exception) {
            // If it's a foreign key constraint, try a safe retry with foreign_keys disabled
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

    // Xóa tenant theo id
    fun deleteTenant(id: Int): Int {
        val db = dbHelper.writableDatabase
        val result = db.delete("Tenant", "id = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    // Tìm kiếm tenant (đã sửa để lấy đủ cột)
    fun searchTenants(keyword: String): List<Tenant> {
        val tenants = mutableListOf<Tenant>()
        val db = dbHelper.readableDatabase
        // Sửa: Phải là SELECT *
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

    // --- CÁC HÀM CŨ ĐÃ THÊM CHO PROFILE USER ---
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

    // Lấy tenant theo room_id (nếu phòng đang có người thuê)
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