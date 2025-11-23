package com.example.qunlphngtr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.TenantDao
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class TenantHomeActivity : AppCompatActivity() {

    // --- Views ---
    private lateinit var tvBillAmount: TextView
    private lateinit var tvBillDueDate: TextView

    // --- DAO & Data ---
    private lateinit var billDao: BillDao
    private lateinit var tenantDao: TenantDao
    private var currentUserId: Int = -1
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_user)

        // Init DAOs
        billDao = BillDao(this)
        tenantDao = TenantDao(this)

        // Get current user
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getInt("user_id", -1)

        // Bind Views
        bindViews()

        // Setup Events
        setupHomeEvents()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        // Luôn làm mới dữ liệu hóa đơn khi quay lại màn hình này
        updateLatestBillView()
    }

    private fun bindViews() {
        tvBillAmount = findViewById(R.id.tvBillAmount)
        tvBillDueDate = findViewById(R.id.tvBillDueDate)
    }

    /**
     * Lấy hóa đơn mới nhất của người dùng và cập nhật lên giao diện.
     */
    private fun updateLatestBillView() {
        if (currentUserId == -1) return

        try {
            val tenant = tenantDao.getTenantByUserId(currentUserId)
            if (tenant != null) {
                val latestBill = billDao.getLatestBillForTenant(tenant.id)
                if (latestBill != null && latestBill.status != "paid") {
                    val remaining = latestBill.total - latestBill.paidAmount
                    tvBillAmount.text = currencyFormat.format(remaining)
                    tvBillDueDate.text = "Hạn chót: ${latestBill.month}"
                } else {
                    tvBillAmount.text = "Không có hóa đơn"
                    tvBillDueDate.text = "Bạn không có công nợ nào."
                }
            } else {
                tvBillAmount.text = "Không có hóa đơn"
                tvBillDueDate.text = ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tvBillAmount.text = "Lỗi tải dữ liệu"
            tvBillDueDate.text = ""
        }
    }

    private fun setupHomeEvents() {
        val btnPayNow = findViewById<MaterialButton>(R.id.btnPayNow)
        btnPayNow.setOnClickListener {
            if (currentUserId == -1) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tenant = tenantDao.getTenantByUserId(currentUserId)
            if (tenant == null) {
                Toast.makeText(this, "Không tìm thấy thông tin người thuê", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val latestBill = billDao.getLatestBillForTenant(tenant.id)
            if (latestBill == null || latestBill.status == "paid") {
                Toast.makeText(this, "Không có hóa đơn nào cần thanh toán", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mở BillDetailActivity để xem và thanh toán
            val intent = Intent(this, BillDetailActivity::class.java)
            intent.putExtra("invoice_id", latestBill.id)
            startActivity(intent)
        }

        // 4 nút trong Grid
        findViewById<CardView>(R.id.cardUpcomingBills).setOnClickListener {
            Toast.makeText(this, "Mở Chi tiết hóa đơn", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.cardNotifications).setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
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
            val intent = Intent(this, NotificationActivity::class.java)
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