package com.example.qunlphngtr.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "boarding_house.db"
        const val DATABASE_VERSION = 5
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

        db.execSQL("""
            CREATE TABLE Room (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                price REAL,
                area REAL,
                status TEXT CHECK(status IN ('available','occupied')) NOT NULL,
                description TEXT,
                imageUri TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE Tenant (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                gender TEXT,
                phone TEXT,
                identity_number TEXT,
                room_id INTEGER,
                start_date TEXT,
                end_date TEXT,
                user_id INTEGER,
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
                FOREIGN KEY (room_id) REFERENCES Room(id),
                FOREIGN KEY (tenant_id) REFERENCES Tenant(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE Service (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                price REAL
            )
        """)

        db.execSQL("""
            CREATE TABLE Bill_Service (
                bill_id INTEGER,
                service_id INTEGER,
                quantity INTEGER,
                subtotal REAL,
                PRIMARY KEY (bill_id, service_id),
                FOREIGN KEY (bill_id) REFERENCES Bill(id),
                FOREIGN KEY (service_id) REFERENCES Service(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE Notification (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                message TEXT,
                type TEXT,
                date TEXT,
                is_read INTEGER DEFAULT 0,
                tenant_id INTEGER,
                FOREIGN KEY (tenant_id) REFERENCES Tenant(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE Report (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                content TEXT,
                date TEXT,
                status TEXT,
                tenant_id INTEGER,
                response TEXT,
                FOREIGN KEY (tenant_id) REFERENCES Tenant(id)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS User")
        db.execSQL("DROP TABLE IF EXISTS Room")
        db.execSQL("DROP TABLE IF EXISTS Tenant")
        db.execSQL("DROP TABLE IF EXISTS Bill")
        db.execSQL("DROP TABLE IF EXISTS Service")
        db.execSQL("DROP TABLE IF EXISTS Bill_Service")
        db.execSQL("DROP TABLE IF EXISTS Notification")
        db.execSQL("DROP TABLE IF EXISTS Report")
        onCreate(db)
    }
}
