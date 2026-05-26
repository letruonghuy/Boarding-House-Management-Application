/*
package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.NotificationAdapter
import com.example.qunlphngtr.dao.NotificationDao
import com.example.qunlphngtr.model.Notification
import com.google.android.material.tabs.TabLayout

class NotificationsActivity : AppCompatActivity() {

    private lateinit var notificationDao: NotificationDao
    private lateinit var adapter: NotificationAdapter
    private lateinit var allNotifications: List<Notification>
    private lateinit var rvNotifications: RecyclerView
    private lateinit var emptyView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        notificationDao = NotificationDao(this)

        rvNotifications = findViewById(R.id.rv_notifications)
        emptyView = findViewById(R.id.empty_view)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout_notifications)
        val markAllRead = findViewById<TextView>(R.id.tv_mark_all_read)

        setupRecyclerView()
        loadAndFilterNotifications(0) // Load all notifications initially

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                loadAndFilterNotifications(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        markAllRead.setOnClickListener {
            val userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("user_id", -1)
            val tenantId = if (userId == -1) null else userId
            notificationDao.markAllAsRead(tenantId)
            loadAndFilterNotifications(tabLayout.selectedTabPosition)
            Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList())
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter

        adapter.onItemClick = { notification ->
            // Mark as read when clicked
            if (notification.is_read == 0) {
                notificationDao.markAsRead(notification.id)
                loadAndFilterNotifications(findViewById<TabLayout>(R.id.tab_layout_notifications).selectedTabPosition)
            }

            // Navigate if it's a bill notification
            if (notification.type == "bill") {
                // Assuming the message contains the bill ID in some way, or we add it to the model
                // For now, let's just go to the bill list
                val intent = Intent(this, BillActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun loadAndFilterNotifications(tabPosition: Int) {
        val userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("user_id", -1)
        val tenantId = if (userId == -1) null else userId
        allNotifications = notificationDao.getNotificationsForUser(tenantId)


        val filteredList = when (tabPosition) {
            1 -> allNotifications.filter { it.type == "bill" }
            2 -> allNotifications.filter { it.type == "system" }
            3 -> allNotifications.filter { it.is_read == 0 }
            else -> allNotifications // Case 0: Tất cả
        }

        adapter.updateList(filteredList)

        if (filteredList.isEmpty()) {
            rvNotifications.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            rvNotifications.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
}
*/
