package com.example.qunlphngtr

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    private val REQUEST_POST_NOTIFICATIONS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- PHẦN KIỂM TRA QUYỀN NÂNG CAO ---
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // Nếu CHƯA đăng nhập, đá về LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return // Dừng hàm onCreate ở đây
        }

        // Nếu ĐÃ đăng nhập, kiểm tra vai trò
        val userRole = prefs.getString("role", null)

        if (userRole == "tenant") {
            // Nếu là TENANT, chuyển ngay sang màn hình TenantHomeActivity
            startActivity(Intent(this, TenantHomeActivity::class.java))
            finish()
            return // Dừng hàm onCreate ở đây
        }

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_POST_NOTIFICATIONS)
            }
        }

        // --- NẾU BẠN THẤY MÀN HÌNH NÀY, BẠN LÀ LANDLORD ---
        // Chỉ set layout này nếu người dùng là "landlord"
        setContentView(R.layout.activity_main)

        // --- KẾT THÚC PHẦN KIỂM TRA ---

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_bill -> {
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        val cardQuanLyPhong = findViewById<MaterialCardView>(R.id.card1);
        val cardTenantList = findViewById<MaterialCardView>(R.id.card2);
        val cardBill = findViewById<MaterialCardView>(R.id.card3);

        // Không cần ẩn/hiện nữa, vì màn hình này giờ CHỈ dành cho landlord

        cardQuanLyPhong.setOnClickListener {
            val intent = Intent(this, QuanLyPhongActivity::class.java)
            startActivity(intent)
        }

        cardTenantList.setOnClickListener {
            // Toast để xác nhận sự kiện click đã được gọi
            Toast.makeText(this, "Mở Quản lý Người Thuê...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, TenantListActivity::class.java)
            startActivity(intent)
        }
        cardBill.setOnClickListener {
            // Toast để xác nhận sự kiện click đã ược gọi
            Toast.makeText(this, "Mở Hóa đơn...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, BillActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Cho phép gửi thông báo", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Quyền thông báo bị từ chối. Ứng dụng sẽ chỉ lưu thông báo trong app.", Toast.LENGTH_LONG).show()
            }
        }
    }
}