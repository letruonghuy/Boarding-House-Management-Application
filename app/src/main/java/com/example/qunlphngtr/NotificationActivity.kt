package com.example.qunlphngtr

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.NotificationAdapter
import com.example.qunlphngtr.dao.NotificationDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Notification

class NotificationActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationAdapter
    private lateinit var notificationDao: NotificationDao
    private lateinit var tenantDao: TenantDao
    private var notificationList = listOf<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        notificationDao = NotificationDao(this)
        tenantDao = TenantDao(this)

        val recyclerView: RecyclerView = findViewById(R.id.rvNotifications)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userRole = prefs.getString("role", "")
        val userId = prefs.getInt("user_id", -1)

        if (userRole == "landlord") {
            loadNotificationsForLandlord()
        } else {
            val tenant = if (userId != -1) tenantDao.getTenantByUserId(userId) else null
            if (tenant != null) {
                loadNotificationsForTenant(tenant.id)
            } else {
                showEmptyState()
            }
        }
    }

    private fun loadNotificationsForLandlord() {
        notificationList = notificationDao.getNotificationsForLandlord()
        adapter = NotificationAdapter(notificationList)
        findViewById<RecyclerView>(R.id.rvNotifications).adapter = adapter
        adapter.onItemClick = { notification ->
            if (!notification.isRead) {
                notificationDao.markAsRead(notification.id)
                loadNotificationsForLandlord()
            }
            // TODO: Navigate to the specific bill
        }
    }

    private fun loadNotificationsForTenant(tenantId: Int) {
        notificationList = notificationDao.getNotificationsForTenant(tenantId)
        adapter = NotificationAdapter(notificationList)
        findViewById<RecyclerView>(R.id.rvNotifications).adapter = adapter
        adapter.onItemClick = { notification ->
            if (!notification.isRead) {
                notificationDao.markAsRead(notification.id)
                loadNotificationsForTenant(tenantId)
            }
            // TODO: Navigate to the specific bill
        }
    }

    private fun showEmptyState() {
        adapter = NotificationAdapter(emptyList())
        findViewById<RecyclerView>(R.id.rvNotifications).adapter = adapter
        Toast.makeText(this, "Chưa có thông báo", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
