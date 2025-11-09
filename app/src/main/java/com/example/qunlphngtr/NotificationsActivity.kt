package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Đặt layout cho màn hình Thông báo
        setContentView(R.layout.activity_notifications) // Gợi ý: Đổi tên file này thành "activity_notifications.xml"

        // 2. Gán sự kiện cho thanh điều hướng
        setupBottomNavigation()

        // (Bạn có thể thêm code xử lý cho RecyclerView thông báo ở đây)
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
            // Không làm gì cả, vì đang ở Thông báo
            Toast.makeText(this, "Đang ở Thông báo", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            // Mở Activity Cá nhân
            val intent = Intent(this, ProfileUserActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish() // Đóng Activity hiện tại
        }
    }
}