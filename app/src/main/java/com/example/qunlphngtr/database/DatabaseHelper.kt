package com.example.qunlphngtr.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "boarding_house.db"
        // Version 14: Add Contract table
        const val DATABASE_VERSION = 14
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

        db.execSQL("""
            CREATE TABLE Contract (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tenant_id INTEGER NOT NULL,
                room_id INTEGER NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL,
                rent_price REAL NOT NULL,
                deposit_amount REAL NOT NULL,
                contract_pdf_uri TEXT,
                FOREIGN KEY (tenant_id) REFERENCES Tenant(id),
                FOREIGN KEY (room_id) REFERENCES Room(id)
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
        if (oldVersion < 11) {
            db.execSQL("ALTER TABLE Bill ADD COLUMN old_electric_image_uri TEXT")
            db.execSQL("ALTER TABLE Bill ADD COLUMN new_electric_image_uri TEXT")
            db.execSQL("ALTER TABLE Bill ADD COLUMN old_water_image_uri TEXT")
            db.execSQL("ALTER TABLE Bill ADD COLUMN new_water_image_uri TEXT")
        }
        if (oldVersion < 12) {
            db.execSQL("ALTER TABLE Bill ADD COLUMN old_electric_reading INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE Bill ADD COLUMN new_electric_reading INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE Bill ADD COLUMN old_water_reading INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE Bill ADD COLUMN new_water_reading INTEGER DEFAULT 0")
        }
        if (oldVersion < 13) {
            db.execSQL("ALTER TABLE Bill ADD COLUMN payment_proof_uri TEXT")
        }
        if (oldVersion < 14) {
            db.execSQL("""
                CREATE TABLE Contract (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tenant_id INTEGER NOT NULL,
                    room_id INTEGER NOT NULL,
                    start_date TEXT NOT NULL,
                    end_date TEXT NOT NULL,
                    rent_price REAL NOT NULL,
                    deposit_amount REAL NOT NULL,
                    contract_pdf_uri TEXT,
                    FOREIGN KEY (tenant_id) REFERENCES Tenant(id),
                    FOREIGN KEY (room_id) REFERENCES Room(id)
                )
            """)
        }
    }
}