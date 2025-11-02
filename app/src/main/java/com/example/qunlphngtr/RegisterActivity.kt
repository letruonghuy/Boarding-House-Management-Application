package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.dao.UserDao // Đã import
import com.example.qunlphngtr.model.User // Đã import
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userDao = UserDao(this)

        val inputFullName = findViewById<TextInputEditText>(R.id.inputFullName)
        val inputPhone = findViewById<TextInputEditText>(R.id.inputPhone)

        // --- BƯỚC 1: THÊM DÒNG NÀY ---
        // Bạn phải thêm ô này vào file activity_register.xml với ID là inputUsername
        val inputUsername = findViewById<TextInputEditText>(R.id.inputUsername)

        val inputPassword = findViewById<TextInputEditText>(R.id.inputPassword)
        val inputConfirmPassword = findViewById<TextInputEditText>(R.id.inputConfirmPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val fullName = inputFullName.text.toString().trim()
            val phone = inputPhone.text.toString().trim()

            // --- BƯỚC 2: LẤY TEXT TỪ Ô MỚI ---
            val username = inputUsername.text.toString().trim()

            val password = inputPassword.text.toString().trim()
            val confirmPassword = inputConfirmPassword.text.toString().trim()

            // --- BƯỚC 3: SỬA LẠI ĐIỀU KIỆN KIỂM TRA ---
            if (fullName.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            } else {
                val role = "tenant"

                // --- BƯỚC 4: DÙNG `username` THAY VÌ `phone` ---
                val newUser = User(0, username = username, password = password, role = role)

                val result = userDao.insertUser(newUser)

                if (result != -1L) {
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // --- BƯỚC 5: SỬA LẠI CÂU THÔNG BÁO ---
                    Toast.makeText(this, "Tên đăng nhập này đã tồn tại", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}