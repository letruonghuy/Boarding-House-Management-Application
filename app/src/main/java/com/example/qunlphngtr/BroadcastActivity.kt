package com.example.qunlphngtr

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.dao.NotificationDao

class BroadcastActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnSendAll: Button
    private lateinit var notificationDao: NotificationDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast)

        notificationDao = NotificationDao(this)
        etTitle = findViewById(R.id.etTitle)
        etMessage = findViewById(R.id.etMessage)
        btnSendAll = findViewById(R.id.btnSendAll)

        btnSendAll.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val msg = etMessage.text.toString().trim()
            if (title.isEmpty() || msg.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val count = notificationDao.broadcastToAll(title, msg)
            Toast.makeText(this, "Đã gửi $count thông báo", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

