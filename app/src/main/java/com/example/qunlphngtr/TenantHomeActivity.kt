package com.example.qunlphngtr

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton

class TenantHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Set layout mà bạn vừa tạo
        setContentView(R.layout.activity_main_user)

        // 2. Gán sự kiện click (ví dụ)
        // Chúng ta dùng tên ID từ file XML của bạn
        findViewById<CardView>(R.id.cardUpcomingBills).setOnClickListener {
            Toast.makeText(this, "Mở Hóa đơn sắp tới", Toast.LENGTH_SHORT).show()
            // Ví dụ: startActivity(Intent(this, UpcomingBillsActivity::class.java))
        }

        findViewById<CardView>(R.id.cardNotifications).setOnClickListener {
            Toast.makeText(this, "Mở Thông báo", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.cardBillDetails).setOnClickListener {
            Toast.makeText(this, "Mở Chi tiết hóa đơn", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.cardRentalReports).setOnClickListener {
            Toast.makeText(this, "Mở Báo cáo", Toast.LENGTH_SHORT).show()
            // Ví dụ: startActivity(Intent(this, CreateReportActivity::class.java))
        }

        findViewById<CardView>(R.id.cardPaymentHistory).setOnClickListener {
            Toast.makeText(this, "Mở Lịch sử thanh toán", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.cardTenant).setOnClickListener {
            Toast.makeText(this, "Mở Thông tin người thuê", Toast.LENGTH_SHORT).show()
            // Ví dụ: startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnViewDashboard).setOnClickListener {
            Toast.makeText(this, "Nút này có thể không cần thiết, vì đây đã là Dashboard", Toast.LENGTH_LONG).show()
        }

        // Xử lý các nút bottom nav (ví dụ)
        findViewById<CardView>(R.id.navHome).setOnClickListener {
            Toast.makeText(this, "Đang ở Home", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.navReports).setOnClickListener {
            Toast.makeText(this, "Mở Báo cáo", Toast.LENGTH_SHORT).show()
        }
    }
}