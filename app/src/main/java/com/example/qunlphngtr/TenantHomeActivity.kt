package com.example.qunlphngtr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.TenantDao
import java.util.Locale

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

    override fun onResume() {
        super.onResume()
        // Refresh quick view when returning to home (for example after payment)
        try {
            val tvBillAmount = findViewById<TextView>(R.id.tvBillAmount)
            val tvBillDueDate = findViewById<TextView>(R.id.tvBillDueDate)
            val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val currentUserId = prefs.getInt("user_id", -1)
            if (currentUserId != -1) {
                val tenantDao = TenantDao(this)
                val tenant = tenantDao.getTenantByUserId(currentUserId)
                if (tenant != null) {
                    val billDao = BillDao(this)
                    val latest = billDao.getLatestBillForTenant(tenant.id)
                    if (latest != null) {
                        tvBillAmount.text = String.format(Locale.getDefault(), "%,.0f VNĐ", latest.total)
                        tvBillDueDate.text = "Hạn chót: ${latest.month}"
                    } else {
                        tvBillAmount.text = "Không có hóa đơn"
                        tvBillDueDate.text = ""
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun setupHomeEvents() {
        // Nút trong thẻ xem nhanh: lấy hóa đơn mới nhất của tenant và mở chi tiết
        val btnPayNow = findViewById<MaterialButton>(R.id.btnPayNow)
        val tvBillAmount = findViewById<TextView>(R.id.tvBillAmount)
        val tvBillDueDate = findViewById<TextView>(R.id.tvBillDueDate)

        // Cập nhật quick view ngay khi mở
        try {
            val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val currentUserId = prefs.getInt("user_id", -1)
            if (currentUserId != -1) {
                val tenantDao = TenantDao(this)
                val tenant = tenantDao.getTenantByUserId(currentUserId)
                if (tenant != null) {
                    val billDao = BillDao(this)
                    val latest = billDao.getLatestBillForTenant(tenant.id)
                    if (latest != null) {
                        tvBillAmount.text = String.format("%,.0f VNĐ", latest.total)
                        tvBillDueDate.text = "Hạn chót: ${latest.month}" // month currently stored as MM/yyyy
                    } else {
                        tvBillAmount.text = "Không có hóa đơn"
                        tvBillDueDate.text = ""
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        btnPayNow.setOnClickListener {
            val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val currentUserId = prefs.getInt("user_id", -1)
            if (currentUserId == -1) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tenantDao = TenantDao(this)
            val tenant = tenantDao.getTenantByUserId(currentUserId)
            if (tenant == null) {
                Toast.makeText(this, "Không tìm thấy thông tin người thuê", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val billDao = BillDao(this)
            val latest = billDao.getLatestBillForTenant(tenant.id)
            if (latest == null) {
                Toast.makeText(this, "Không có hóa đơn để thanh toán", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mở BillDetailActivity để xem và thanh toán
            val intent = Intent(this, BillDetailActivity::class.java)
            intent.putExtra("invoice_id", latest.id)
            startActivity(intent)
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