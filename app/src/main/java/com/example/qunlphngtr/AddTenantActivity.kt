package com.example.qunlphngtr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.model.Tenant

class AddTenantActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tenant)

        val edtName = findViewById<EditText>(R.id.edtName)
        val edtGender = findViewById<EditText>(R.id.edtGender)
        val edtPhone = findViewById<EditText>(R.id.edtPhone)
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            val gender = edtGender.text.toString().trim()
            val phone = edtPhone.text.toString().trim()

            if (name.isNotEmpty() && gender.isNotEmpty() && phone.isNotEmpty()) {
                val result = Intent().apply {
                    putExtra("name", name)
                    putExtra("gender", gender)
                    putExtra("phone", phone)
                }
                setResult(Activity.RESULT_OK, result)
                finish()
            }
        }
    }
}
