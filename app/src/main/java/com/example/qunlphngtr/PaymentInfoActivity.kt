package com.example.qunlphngtr

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText

class PaymentInfoActivity : AppCompatActivity() {

    private lateinit var etBankName: TextInputEditText
    private lateinit var etAccountNumber: TextInputEditText
    private lateinit var etAccountHolder: TextInputEditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_info)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etBankName = findViewById(R.id.etBankName)
        etAccountNumber = findViewById(R.id.etAccountNumber)
        etAccountHolder = findViewById(R.id.etAccountHolder)
        btnSave = findViewById(R.id.btnSavePaymentInfo)

        loadPaymentInfo()

        btnSave.setOnClickListener {
            savePaymentInfo()
        }
    }

    private fun loadPaymentInfo() {
        val prefs = getSharedPreferences("PaymentPrefs", Context.MODE_PRIVATE)
        etBankName.setText(prefs.getString("bankName", ""))
        etAccountNumber.setText(prefs.getString("accountNumber", ""))
        etAccountHolder.setText(prefs.getString("accountHolder", ""))
    }

    private fun savePaymentInfo() {
        val bankName = etBankName.text.toString().trim()
        val accountNumber = etAccountNumber.text.toString().trim()
        val accountHolder = etAccountHolder.text.toString().trim()

        if (bankName.isEmpty() || accountNumber.isEmpty() || accountHolder.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("PaymentPrefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString("bankName", bankName)
            putString("accountNumber", accountNumber)
            putString("accountHolder", accountHolder)
            apply()
        }

        Toast.makeText(this, "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show()
        finish() // Đóng activity sau khi lưu
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}