package com.example.qunlphngtr

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtBirthday: EditText
    private lateinit var btnPickDate: ImageButton
    private lateinit var radioGender: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var btnChangeImage: Button

    private val calendar = Calendar.getInstance()

    // Activity Result API: chọn ảnh
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imgProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Ánh xạ view
        imgProfile = findViewById(R.id.imgProfile)
        edtName = findViewById(R.id.edtName)
        edtEmail = findViewById(R.id.edtEmail)
        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)
        edtBirthday = findViewById(R.id.edtBirthday)
        btnPickDate = findViewById(R.id.btnPickDate)
        radioGender = findViewById(R.id.radioGender)
        btnSave = findViewById(R.id.btnSave)
        btnChangeImage = findViewById(R.id.btnChangeImage)



        // Chọn ảnh đại diện
        btnChangeImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Hiện DatePicker khi click
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateBirthday()
        }
        btnPickDate.setOnClickListener {
            showDatePicker(dateSetListener)
        }

        // Lưu thông tin
        btnSave.setOnClickListener {
            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val phone = edtPhone.text.toString()
            val address = edtAddress.text.toString()
            val birthday = edtBirthday.text.toString()
            val gender = when (radioGender.checkedRadioButtonId) {
                R.id.radioMale -> "Nam"
                R.id.radioFemale -> "Nữ"
                else -> ""
            }

            Toast.makeText(
                this,
                "Đã lưu:\n$name\n$email\n$phone\n$address\n$birthday\n$gender",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateBirthday() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtBirthday.setText(format.format(calendar.time))
    }

    private fun showDatePicker(listener: DatePickerDialog.OnDateSetListener) {
        DatePickerDialog(
            this, listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
