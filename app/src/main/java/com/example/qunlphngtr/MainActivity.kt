package com.example.qunlphngtr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.dao.RoomDao
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private lateinit var roomDao: RoomDao

    // Stats Views
    private lateinit var tvTotalRooms: TextView
    private lateinit var tvOccupiedRooms: TextView
    private lateinit var tvEmptyRooms: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        roomDao = RoomDao(this)
        bindViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun bindViews() {
        tvTotalRooms = findViewById(R.id.tvTotalRooms)
        tvOccupiedRooms = findViewById(R.id.tvOccupiedRooms)
        tvEmptyRooms = findViewById(R.id.tvEmptyRooms)
    }

    private fun loadStats() {
        val allRooms = roomDao.getAllRooms()
        val occupiedCount = allRooms.count { it.status != "available" }
        val emptyCount = allRooms.size - occupiedCount

        tvTotalRooms.text = allRooms.size.toString()
        tvOccupiedRooms.text = occupiedCount.toString()
        tvEmptyRooms.text = emptyCount.toString()
    }

    private fun setupListeners() {
        // Feature clicks - Sửa lỗi: Tìm đúng kiểu MaterialCardView
        findViewById<MaterialCardView>(R.id.featureRoomManagement).setOnClickListener {
            startActivity(Intent(this, RoomManagementActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.featureTenantManagement).setOnClickListener {
            startActivity(Intent(this, TenantListActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.featureBillManagement).setOnClickListener {
            startActivity(Intent(this, BillActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.featureContractManagement).setOnClickListener {
            startActivity(Intent(this, ContractListActivity::class.java))
        }

        // Header icons
        findViewById<ImageView>(R.id.ivNotifications).setOnClickListener {
            /*startActivity(Intent(this, NotificationsActivity::class.java))*/
        }
        findViewById<ImageView>(R.id.ivLogout).setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_bill -> {
                    startActivity(Intent(this, BillActivity::class.java))
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
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
