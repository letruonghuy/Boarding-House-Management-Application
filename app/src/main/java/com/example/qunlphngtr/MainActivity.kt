package com.example.qunlphngtr

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.qunlphngtr.dao.NotificationDao
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    private val REQUEST_POST_NOTIFICATIONS = 1001
    private lateinit var notificationDao: NotificationDao
    private lateinit var notificationBadge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userRole = prefs.getString("role", null)

        if (userRole == "tenant") {
            startActivity(Intent(this, TenantHomeActivity::class.java))
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_POST_NOTIFICATIONS)
            }
        }

        setContentView(R.layout.activity_main)
        notificationDao = NotificationDao(this)
        notificationBadge = findViewById(R.id.notificationBadge)

        setupNavigation()
        setupCardListeners()
    }


    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
    }

    private fun setupNavigation() {
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

    private fun setupCardListeners() {
        findViewById<MaterialCardView>(R.id.card1).setOnClickListener {
            startActivity(Intent(this, QuanLyPhongActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card2).setOnClickListener {
            startActivity(Intent(this, TenantListActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card3).setOnClickListener {
            startActivity(Intent(this, BillActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card4).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.card5).setOnClickListener { // Contract Management
            startActivity(Intent(this, ContractListActivity::class.java))
        }
    }

    private fun updateNotificationBadge() {
        val unreadCount = notificationDao.getUnreadCountForLandlord()
        if (unreadCount > 0) {
            notificationBadge.visibility = View.VISIBLE
            notificationBadge.text = unreadCount.toString()
        } else {
            notificationBadge.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Cho phép gửi thông báo", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Quyền thông báo bị từ chối.", Toast.LENGTH_LONG).show()
            }
        }
    }
}



































