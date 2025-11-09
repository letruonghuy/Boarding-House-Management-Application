package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton

class TenantHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Đặt layout cho Trang chủ (đổi tên file XML nếu cần)
        setContentView(R.layout.activity_main_user) // Gợi ý: Đổi tên file này thành "activity_home.xml"

        // 2. Gán sự kiện cho các nút trên màn hình Home
        setupHomeEvents()

        // 3. Gán sự kiện cho thanh điều hướng
        setupBottomNavigation()
    }

    private fun setupHomeEvents() {
        // Nút trong thẻ xem nhanh
        findViewById<MaterialButton>(R.id.btnPayNow).setOnClickListener {
            Toast.makeText(this, "Mở trang thanh toán", Toast.LENGTH_SHORT).show()
        }

        // 4 nút trong Grid
        findViewById<CardView>(R.id.cardUpcomingBills).setOnClickListener {
            Toast.makeText(this, "Mở Chi tiết hóa đơn", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.cardNotifications).setOnClickListener {
            // Click vào nút "Thông báo" cũng sẽ mở Activity Thông báo
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardPaymentHistory).setOnClickListener {
            Toast.makeText(this, "Mở Lịch sử thanh toán", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.cardServices).setOnClickListener {
            Toast.makeText(this, "Mở Dịch vụ & Tiện ích", Toast.LENGTH_SHORT).show()
        }

        // 2 nút hành động nhanh
        findViewById<MaterialButton>(R.id.btnReportIssue).setOnClickListener {
            Toast.makeText(this, "Mở trang Báo cáo sự cố", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnContactManager).setOnClickListener {
            Toast.makeText(this, "Mở trang Liên hệ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        // 3 nút Bottom Nav
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            // Không làm gì cả, vì đang ở Trang chủ
            Toast.makeText(this, "Đang ở Trang chủ", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener {
            // Mở Activity Thông báo
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            // Ngăn hiệu ứng nhấp nháy
            overridePendingTransition(0, 0)
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            // Mở Activity Cá nhân
            val intent = Intent(this, ProfileUserActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}