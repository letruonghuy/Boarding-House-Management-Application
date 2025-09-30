package com.example.qunlphngtr.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.qunlphngtr.model.Room
import com.example.qunlphngtr.model.Tenant

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "QuanLyPhongTro.db"
        private const val DATABASE_VERSION = 3   // tăng version mỗi khi đổi cấu trúc

        // ===== Bảng Room =====
        const val TABLE_ROOM = "rooms"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_AREA = "area"
        const val COLUMN_STATUS = "status"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_IMAGE_URI = "imageUri"

        // ===== Bảng Tenant =====
        const val TABLE_TENANT = "tenants"
        const val TENANT_ID = "id"
        const val TENANT_NAME = "name"
        const val TENANT_GENDER = "gender"
        const val TENANT_PHONE = "phone"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createRoomTable = """
            CREATE TABLE $TABLE_ROOM (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_PRICE REAL,
                $COLUMN_AREA REAL,
                $COLUMN_STATUS TEXT,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_IMAGE_URI TEXT
            )
        """.trimIndent()
        db.execSQL(createRoomTable)

        val createTenantTable = """
            CREATE TABLE $TABLE_TENANT (
                $TENANT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $TENANT_NAME TEXT,
                $TENANT_GENDER TEXT,
                $TENANT_PHONE TEXT
            )
        """.trimIndent()
        db.execSQL(createTenantTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ROOM")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TENANT")
        onCreate(db)
    }

    // ===== CRUD Room =====
    fun insertRoom(
        name: String,
        price: Double,
        area: Double,
        status: String,
        description: String,
        imageUri: String? = null
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PRICE, price)
            put(COLUMN_AREA, area)
            put(COLUMN_STATUS, status)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_IMAGE_URI, imageUri)
        }
        val result = db.insert(TABLE_ROOM, null, values)
        db.close()
        return result
    }

    fun getAllRooms(): List<Room> {
        val roomList = mutableListOf<Room>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ROOM", null)
        if (cursor.moveToFirst()) {
            do {
                val room = Room(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                    area = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AREA)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
                )
                roomList.add(room)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return roomList
    }

    // ===== CRUD Tenant =====
    fun insertTenant(name: String, gender: String, phone: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TENANT_NAME, name)
            put(TENANT_GENDER, gender)
            put(TENANT_PHONE, phone)
        }
        val result = db.insert(TABLE_TENANT, null, values)
        db.close()
        return result
    }

    fun getAllTenants(): MutableList<Tenant> {
        val list = mutableListOf<Tenant>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TENANT", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Tenant(
                        name = cursor.getString(cursor.getColumnIndexOrThrow(TENANT_NAME)),
                        gender = cursor.getString(cursor.getColumnIndexOrThrow(TENANT_GENDER)),
                        phone = cursor.getString(cursor.getColumnIndexOrThrow(TENANT_PHONE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun updateTenant(oldName: String, newTenant: Tenant): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TENANT_NAME, newTenant.name)
            put(TENANT_GENDER, newTenant.gender)
            put(TENANT_PHONE, newTenant.phone)
        }
        val rows = db.update(
            TABLE_TENANT,
            values,
            "$TENANT_NAME = ?",
            arrayOf(oldName)
        )
        db.close()
        return rows
    }

    fun deleteTenant(name: String): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_TENANT, "$TENANT_NAME = ?", arrayOf(name))
        db.close()
        return rows
    }
}
