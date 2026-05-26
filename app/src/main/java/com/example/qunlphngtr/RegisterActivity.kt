package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.dao.UserDao
import com.example.qunlphngtr.model.User
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var tenantDao: TenantDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userDao = UserDao(this)
        tenantDao = TenantDao(this)

        val inputIdentity = findViewById<TextInputEditText>(R.id.inputIdentity)
        val inputUsername = findViewById<TextInputEditText>(R.id.inputUsername)
        val inputPassword = findViewById<TextInputEditText>(R.id.inputPassword)
        val inputConfirmPassword = findViewById<TextInputEditText>(R.id.inputConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val identity = inputIdentity.text.toString().trim()
            val username = inputUsername.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val confirmPassword = inputConfirmPassword.text.toString().trim()

            if (identity.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Logic kích hoạt tài khoản bằng CCCD
            val targetTenant = tenantDao.findUnactivatedTenantByIdentityNumber(identity)

            if (targetTenant == null) {
                Toast.makeText(this, "Số CCCD không tồn tại hoặc tài khoản đã được kích hoạt.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Bắt đầu quá trình tạo User và liên kết
            val newUser = User(id = 0, username = username, password = password, role = "tenant")
            val newUserId = userDao.insertUser(newUser)

            if (newUserId > 0) {
                // Tạo User thành công, bây giờ cập nhật Tenant
                val updatedTenant = targetTenant.copy(user_id = newUserId.toInt())
                val rowsAffected = tenantDao.updateTenant(updatedTenant)

                if (rowsAffected > 0) {
                    Toast.makeText(this, "Kích hoạt tài khoản thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
                    finish() // Quay về màn hình đăng nhập
                } else {
                    // Trường hợp hiếm gặp: không thể cập nhật tenant
                    Toast.makeText(this, "Lỗi: Không thể liên kết tài khoản. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                    // Cần có cơ chế xóa User vừa tạo để tránh rác DB
                }
            } else {
                // Lỗi có thể do username đã tồn tại
                Toast.makeText(this, "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.", Toast.LENGTH_SHORT).show()
            }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }
}