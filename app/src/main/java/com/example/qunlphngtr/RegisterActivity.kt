package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.dao.TenantDao // <--- THÊM IMPORT NÀY
import com.example.qunlphngtr.dao.UserDao
import com.example.qunlphngtr.model.Tenant // <--- THÊM IMPORT NÀY
import com.example.qunlphngtr.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var tenantDao: TenantDao // <--- THÊM DAO MỚI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userDao = UserDao(this)
        tenantDao = TenantDao(this) // <--- KHỞI TẠO DAO MỚI

        val inputFullName = findViewById<TextInputEditText>(R.id.inputFullName)
        val inputPhone = findViewById<TextInputEditText>(R.id.inputPhone)
        val inputUsername = findViewById<TextInputEditText>(R.id.inputUsername)
        val inputPassword = findViewById<TextInputEditText>(R.id.inputPassword)
        val inputConfirmPassword = findViewById<TextInputEditText>(R.id.inputConfirmPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val fullName = inputFullName.text.toString().trim()
            val phone = inputPhone.text.toString().trim()
            val username = inputUsername.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val confirmPassword = inputConfirmPassword.text.toString().trim()

            if (fullName.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            } else {
                val role = "tenant"
                val newUser = User(0, username = username, password = password, role = role)

                // 1. Thêm vào bảng User
                val newUserIdLong = userDao.insertUser(newUser)

                if (newUserIdLong != -1L) {
                    // --- BẮT ĐẦU SỬA Ở ĐÂY ---
                    // 2. Thêm vào bảng Tenant (để liên kết)
                    val newUserId = newUserIdLong.toInt() // Lấy ID của User vừa tạo

                    // Tạo một Tenant mới với thông tin cơ bản
                    // Dùng đúng model 10 trường
                    val newTenant = Tenant(
                        id = 0, // id tự tăng
                        name = fullName,
                        gender = null, // Có thể thêm ô nhập Giới tính
                        phone = phone,
                        imageUri = null, // Ảnh đại diện mặc định
                        identity_number = null, // Cập nhật sau
                        room_id = null, // Admin sẽ gán phòng sau
                        start_date = null,
                        end_date = null,
                        user_id = newUserId // <-- LIÊN KẾT QUAN TRỌNG NHẤT
                    )

                    // 3. Gọi TenantDao
                    tenantDao.insertTenant(newTenant)
                    // --- KẾT THÚC SỬA ---

                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
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