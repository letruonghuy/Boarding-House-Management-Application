package com.example.qunlphngtr

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class AddTenantActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tenant)

        val imgTenant = findViewById<ImageView>(R.id.imgTenant)
        val edtName = findViewById<EditText>(R.id.edtName)
        val edtGender = findViewById<EditText>(R.id.edtGender)
        val edtPhone = findViewById<EditText>(R.id.edtPhone)
        val btnChooseImage = findViewById<Button>(R.id.btnChooseImage)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // register picker (OpenDocument -> allows persistable uri permission)
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                Glide.with(this).load(it).placeholder(R.drawable.ic_person).into(imgTenant)
                // try to persist permission so app can read the URI later
                try {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (ex: Exception) {
                    // some providers may not support persistable permissions
                    ex.printStackTrace()
                }
            }
        }

        btnChooseImage.setOnClickListener {
            // MIME type image/* (OpenDocument expects array)
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            val gender = edtGender.text.toString().trim()
            val phone = edtPhone.text.toString().trim()

            if (name.isNotEmpty() && gender.isNotEmpty() && phone.isNotEmpty()) {
                val result = Intent().apply {
                    putExtra("name", name)
                    putExtra("gender", gender)
                    putExtra("phone", phone)
                    selectedImageUri?.toString()?.let { putExtra("imageUri", it) }
                }
                setResult(RESULT_OK, result)
                finish()
            }
        }
    }
}
