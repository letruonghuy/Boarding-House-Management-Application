package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Contract

class ContractDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    private fun cursorToContract(cursor: Cursor): Contract {
        return Contract(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            tenantId = cursor.getInt(cursor.getColumnIndexOrThrow("tenant_id")),
            roomId = cursor.getInt(cursor.getColumnIndexOrThrow("room_id")),
            startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
            endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
            rentPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("rent_price")),
            depositAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("deposit_amount")),
            contractPdfUri = cursor.getString(cursor.getColumnIndexOrThrow("contract_pdf_uri"))
        )
    }

    fun insertContract(contract: Contract): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("tenant_id", contract.tenantId)
            put("room_id", contract.roomId)
            put("start_date", contract.startDate)
            put("end_date", contract.endDate)
            put("rent_price", contract.rentPrice)
            put("deposit_amount", contract.depositAmount)
            put("contract_pdf_uri", contract.contractPdfUri)
        }
        val id = db.insert("Contract", null, values)
        db.close()
        return id
    }

    fun getAllContracts(): List<Contract> {
        val contracts = mutableListOf<Contract>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Contract ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                contracts.add(cursorToContract(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return contracts
    }
}