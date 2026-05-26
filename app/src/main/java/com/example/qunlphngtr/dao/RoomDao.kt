package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Room

class RoomDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insertRoom(room: Room): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", room.name)
            put("price", room.price)
            put("area", room.area)
            put("status", room.status)
            put("description", room.description)
            put("imageUri", room.imageUri)
            if (room.tenantId != null) {
                put("tenantId", room.tenantId)
            } else {
                putNull("tenantId")
            }
        }
        val id = db.insert("Room", null, values)
        db.close()
        return id
    }

    fun getRoomById(roomId: Int): Room? {
        val db = dbHelper.readableDatabase
        val cursor = db.query("Room", null, "id = ?", arrayOf(roomId.toString()), null, null, null)
        var room: Room? = null
        if (cursor.moveToFirst()) {
            val tenantIdIndex = cursor.getColumnIndex("tenantId")
            val tenantId = if (tenantIdIndex != -1 && !cursor.isNull(tenantIdIndex)) cursor.getInt(tenantIdIndex) else null
            room = Room(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                area = cursor.getDouble(cursor.getColumnIndexOrThrow("area")),
                status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                tenantId = tenantId
            )
        }
        cursor.close()
        db.close()
        return room
    }

    fun getAllRooms(): List<Room> {
        val rooms = mutableListOf<Room>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM Room", null)
        if (cursor.moveToFirst()) {
            do {
                val tenantIdIndex = cursor.getColumnIndex("tenantId")
                val tenantId = if (tenantIdIndex != -1 && !cursor.isNull(tenantIdIndex)) cursor.getInt(tenantIdIndex) else null

                rooms.add(
                    Room(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        area = cursor.getDouble(cursor.getColumnIndexOrThrow("area")),
                        status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                        tenantId = tenantId
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return rooms
    }

    fun updateRoom(room: Room): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", room.name)
            put("price", room.price)
            put("area", room.area)
            put("status", room.status)
            put("description", room.description)
            put("imageUri", room.imageUri)
            if (room.tenantId != null) {
                put("tenantId", room.tenantId)
            } else {
                putNull("tenantId")
            }
        }
        val result = db.update("Room", values, "id = ?", arrayOf(room.id.toString()))
        db.close()
        return result
    }

    fun deleteRoom(roomId: Int): Int {
        val db = dbHelper.writableDatabase
        val result = db.delete("Room", "id = ?", arrayOf(roomId.toString()))
        db.close()
        return result
    }

    fun searchRooms(keyword: String): List<Room> {
        val rooms = mutableListOf<Room>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM Room WHERE name LIKE ? OR description LIKE ?",
            arrayOf("%$keyword%", "%$keyword%")
        )
        if (cursor.moveToFirst()) {
            do {
                val tenantIdIndex = cursor.getColumnIndex("tenantId")
                val tenantId = if (tenantIdIndex != -1 && !cursor.isNull(tenantIdIndex)) cursor.getInt(tenantIdIndex) else null

                rooms.add(
                    Room(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        area = cursor.getDouble(cursor.getColumnIndexOrThrow("area")),
                        status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                        tenantId = tenantId
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return rooms
    }
}