package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ProfileUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Đặt layout cho màn hình Cá nhân
        setContentView(R.layout.activity_user_profile) // Gợi ý: Đổi tên file này thành "activity_profile.xml"

        // 2. Gán sự kiện cho các nút trên màn hình Cá nhân
        setupProfileEvents()

        // 3. Gán sự kiện cho thanh điều hướng
        setupBottomNavigation()
    }

    private fun setupProfileEvents() {
        // Ví dụ: xử lý nút Đăng xuất
        findViewById<MaterialButton>(R.id.btn_logout).setOnClickListener {
            Toast.makeText(this, "Đăng xuất...", Toast.LENGTH_SHORT).show()
            // (Thêm code đăng xuất và quay về màn hình Login ở đây)
        }

        // (Thêm code cho các nút "Đổi mật khẩu", "Xem hợp đồng" ở đây)
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            // Mở Activity Trang chủ
            val intent = Intent(this, TenantHomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish() // Đóng Activity hiện tại
        }

        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener {
            // Mở Activity Thông báo
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish() // Đóng Activity hiện tại
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            // Không làm gì cả
            Toast.makeText(this, "Đang ở Trang cá nhân", Toast.LENGTH_SHORT).show()
        }
    }
}