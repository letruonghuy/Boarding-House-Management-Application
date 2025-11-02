package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.database.DatabaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dbHelper = DatabaseHelper(this);
        val db = dbHelper.writableDatabase
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Chọn mặc định là Home
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_bill -> {
//                    startActivity(Intent(this, MainActivity::class.java))
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
        cardQuanLyPhong.setOnClickListener {
            val intent = Intent(this, QuanLyPhongActivity::class.java)
            startActivity(intent)
        }
        val cardTenantList = findViewById<MaterialCardView>(R.id.card2);
        cardTenantList.setOnClickListener {
            // Toast để xác nhận sự kiện click đã được gọi
            Toast.makeText(this, "Mở Quản lý Người Thuê...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, TenantListActivity::class.java)
            startActivity(intent)
        }
        val cardBill = findViewById<MaterialCardView>(R.id.card3);
        cardBill.setOnClickListener {
            // Toast để xác nhận sự kiện click đã được gọi
            Toast.makeText(this, "Mở Hóa đơn...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, BillActivity::class.java)
            startActivity(intent)
        }
    }
}
