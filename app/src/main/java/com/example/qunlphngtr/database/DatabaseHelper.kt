package com.example.qunlphngtr.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.qunlphngtr.model.Room
import com.example.qunlphngtr.model.Tenant
import com.example.qunlphngtr.model.Bill

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "QuanLyPhongTro.db"
        private const val DATABASE_VERSION = 7   // tăng version mỗi khi đổi cấu trúc

        // ===== Bảng Room =====
        const val TABLE_ROOM = "rooms"
        const val ROOM_ID = "room_id"
        const val ROOM_NAME = "name"
        const val ROOM_PRICE = "price"
        const val ROOM_AREA = "area"
        const val ROOM_STATUS = "status"
        const val ROOM_DESCRIPTION = "description"
        const val ROOM_IMAGE_URI = "image_uri"

        // ===== Bảng Tenant =====
        const val TABLE_TENANT = "tenants"
        const val TENANT_ID = "id"
        const val TENANT_NAME = "name"
        const val TENANT_GENDER = "gender"
        const val TENANT_PHONE = "phone"
        // ===== Bảng Bill =====
        const val TABLE_BILL = "bills"
        const val BILL_ID = "bill_id"
        const val BILL_MONTH = "month"
        const val BILL_ELECTRIC = "electric"
        const val BILL_WATER = "water"
        const val BILL_ROOM = "room_price"
        const val BILL_INTERNET = "internet"
        const val BILL_TOTAL = "total"
        const val BILL_ROOM_ID = "room_id"
        const val BILL_TENANT_ID = "tenant_id"
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
                $ROOM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $ROOM_NAME TEXT,
                $ROOM_PRICE REAL,
                $ROOM_AREA REAL,
                $ROOM_STATUS TEXT,
                $ROOM_DESCRIPTION TEXT,
                $ROOM_IMAGE_URI TEXT
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
        // ===== Bảng Bill =====
        val createBillTable = """
            CREATE TABLE $TABLE_BILL (
                $BILL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $BILL_MONTH TEXT,
                $BILL_ELECTRIC REAL,
                $BILL_WATER REAL,
                $BILL_ROOM REAL,
                $BILL_INTERNET REAL,
                $BILL_TOTAL REAL,
                $BILL_ROOM_ID INTEGER,
                $BILL_TENANT_ID INTEGER,
                FOREIGN KEY ($BILL_ROOM_ID) REFERENCES $TABLE_ROOM($ROOM_ID),
                FOREIGN KEY ($BILL_TENANT_ID) REFERENCES $TABLE_TENANT($TENANT_ID)
            )
        """.trimIndent()
        db.execSQL(createBillTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ROOM")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TENANT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BILL")
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
            put(ROOM_NAME, name)
            put(ROOM_PRICE, price)
            put(ROOM_AREA, area)
            put(ROOM_STATUS, status)
            put(ROOM_DESCRIPTION, description)
            put(ROOM_IMAGE_URI, imageUri)
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
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(ROOM_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(ROOM_NAME)),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow(ROOM_PRICE)),
                    area = cursor.getDouble(cursor.getColumnIndexOrThrow(ROOM_AREA)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(ROOM_STATUS)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(ROOM_DESCRIPTION)),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow(ROOM_IMAGE_URI))
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
    fun deleteBill(id: Int): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_BILL, "$BILL_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }
}