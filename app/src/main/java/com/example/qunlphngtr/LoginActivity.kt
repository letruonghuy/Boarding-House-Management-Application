package com.example.qunlphngtr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.dao.UserDao
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userDao = UserDao(this)

        val inputUsername = findViewById<TextInputEditText>(R.id.inputUsername)
        val inputPassword = findViewById<TextInputEditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val username = inputUsername.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = userDao.checkLogin(username, password)

            if (user != null) {
                // Đăng nhập thành công
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                // Lưu thông tin vào SharedPreferences
                val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putInt("user_id", user.id)
                editor.putString("username", user.username)
                editor.putString("role", user.role) // Rất quan trọng
                editor.putBoolean("isLoggedIn", true)
                editor.apply()

                // --- PHẦN SỬA ĐỂ CHUYỂN HƯỚNG ---
                if (user.role == "landlord") {
                    // Nếu là Chủ trọ, vào màn hình quản lý (MainActivity)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Nếu là Người thuê (tenant), vào màn hình mới
                    val intent = Intent(this, TenantHomeActivity::class.java)
                    startActivity(intent)
                }
                finish() // Đóng LoginActivity để không back lại được
                // --- KẾT THÚC PHẦN SỬA ---

            } else {
                Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}