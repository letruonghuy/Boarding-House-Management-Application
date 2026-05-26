package com.example.qunlphngtr.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "boarding_house.db"
        const val DATABASE_VERSION = 15 // Tăng version để trigger onUpgrade
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE User (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT CHECK(role IN ('landlord', 'tenant')) NOT NULL
            )
        """)

        db.execSQL("INSERT INTO User (username, password, role) VALUES ('chutro', '123456', 'landlord')")

        db.execSQL("""
            CREATE TABLE Room (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                price REAL,
                area REAL,
                status TEXT,
                description TEXT,
                imageUri TEXT,
                tenantId INTEGER,
                FOREIGN KEY (tenantId) REFERENCES Tenant(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE Tenant (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                gender TEXT,
                phone TEXT,
                imageUri TEXT,
                identity_number TEXT,
                room_id INTEGER,
                start_date TEXT,
                end_date TEXT,
                user_id INTEGER,
                deposit REAL DEFAULT 0.0, -- Thêm cột deposit
                cccd_front_uri TEXT,
                cccd_back_uri TEXT,
                FOREIGN KEY (room_id) REFERENCES Room(id),
                FOREIGN KEY (user_id) REFERENCES User(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE Bill (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                month TEXT,
                electric REAL,
                water REAL,
                roomFee REAL,
                internet REAL,
                total REAL,
                status TEXT,
                room_id INTEGER,
                tenant_id INTEGER,
                old_electric_image_uri TEXT,
                new_electric_image_uri TEXT,
                old_water_image_uri TEXT,
                new_water_image_uri TEXT,
                old_electric_reading INTEGER DEFAULT 0,
                new_electric_reading INTEGER DEFAULT 0,
                old_water_reading INTEGER DEFAULT 0,
                new_water_reading INTEGER DEFAULT 0,
                payment_proof_uri TEXT,
                FOREIGN KEY (room_id) REFERENCES Room(id),
                FOREIGN KEY (tenant_id) REFERENCES Tenant(id)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 15) {
            db.execSQL("ALTER TABLE Tenant ADD COLUMN deposit REAL DEFAULT 0.0")
        }
    }
}