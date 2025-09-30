package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)   // tên layout XML: ProfileActivity.xml

        // Ánh xạ nút "Chỉnh sửa hồ sơ"
        val btnEditProfile: Button = findViewById(R.id.btnEditProfile)

        // 👉 Khi bấm nút thì chuyển sang EditProfileActivity
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
