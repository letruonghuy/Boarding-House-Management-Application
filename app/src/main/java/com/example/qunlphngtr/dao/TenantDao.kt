package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Tenant

class TenantDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    // Thêm tenant mới
    fun insertTenant(tenant: Tenant): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", tenant.name)
            put("gender", tenant.gender)
            put("phone", tenant.phone)
            put("imageUri", tenant.imageUri)
        }
        val id = db.insert("Tenant", null, values)
        db.close()
        return id
    }

    // Lấy tất cả tenant
    fun getAllTenants(): List<Tenant> {
        val tenants = mutableListOf<Tenant>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT id, name, gender, phone, imageUri FROM Tenant", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
                val imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri"))
                tenants.add(Tenant(id, name, gender, phone, imageUri))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tenants
    }

    // Cập nhật tenant
    fun updateTenant(tenant: Tenant): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", tenant.name)
            put("gender", tenant.gender)
            put("phone", tenant.phone)
            put("imageUri", tenant.imageUri)
        }
        val result = db.update("Tenant", values, "id = ?", arrayOf(tenant.id.toString()))
        db.close()
        return result
    }

    // Xóa tenant theo id
    fun deleteTenant(id: Int): Int {
        val db = dbHelper.writableDatabase
        val result = db.delete("Tenant", "id = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    // Tìm kiếm tenant
    fun searchTenants(keyword: String): List<Tenant> {
        val tenants = mutableListOf<Tenant>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name, gender, phone, imageUri FROM Tenant WHERE name LIKE ? OR phone LIKE ?",
            arrayOf("%$keyword%", "%$keyword%")
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
                val imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri"))
                tenants.add(Tenant(id, name, gender, phone, imageUri))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tenants
    }
}
