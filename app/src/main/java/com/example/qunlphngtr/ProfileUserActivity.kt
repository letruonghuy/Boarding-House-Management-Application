package com.example.qunlphngtr

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
// --- THÊM IMPORT CỦA GLIDE ---
import com.bumptech.glide.Glide
// ---
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Tenant
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream

class ProfileUserActivity : AppCompatActivity() {

    // Khai báo SharedPreferences và DAO
    private lateinit var tenantDao: TenantDao
    private var currentTenant: Tenant? = null // Lưu lại tenant hiện tại

    // Các View (khai báo ở đây để dùng chung)
    private lateinit var tvName: TextView
    private lateinit var tvRoom: TextView
    private lateinit var ivAvatar: ImageView

    /**
     * Trình chọn ảnh (Đã đúng)
     */
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            handleSelectedImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        tenantDao = TenantDao(this)

        tvName = findViewById(R.id.tv_profile_name)
        tvRoom = findViewById(R.id.tv_profile_room)
        ivAvatar = findViewById(R.id.iv_profile_avatar)

        loadUserProfile()
        setupProfileEvents()
        setupBottomNavigation()
    }

    /**
     * Tải thông tin User (Đã đúng)
     */
    private fun loadUserProfile() {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getInt("user_id", -1)

        if (currentUserId == -1) {
            logout()
            return
        }

        currentTenant = tenantDao.getTenantByUserId(currentUserId)
        val roomName = currentTenant?.let { tenantDao.getRoomNameByTenantId(it.id) }

        if (currentTenant != null) {
            tvName.text = currentTenant!!.name
            tvRoom.text = roomName ?: "Chưa có phòng"
            loadAvatar(currentTenant!!.imageUri)
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người thuê.", Toast.LENGTH_LONG).show()
            tvName.text = "Không tìm thấy"
            tvRoom.text = "Vui lòng liên hệ chủ trọ"
        }
    }

    /**
     * SỬA LỖI CRASH: Dùng GLIDE thay vì setImageURI
     */
    private fun loadAvatar(imageUriString: String?) {
        if (!imageUriString.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(imageUriString)
                // Dùng Glide để tải ảnh
                Glide.with(this) // Context
                    .load(uri) // Tải URI (Glide biết cách xử lý content:// và file://)
                    .placeholder(R.drawable.ic_users) // Ảnh trong lúc chờ tải
                    .error(R.drawable.ic_users) // Ảnh nếu tải lỗi
                    .into(ivAvatar) // Đặt ảnh vào ImageView
            } catch (e: Exception) {
                e.printStackTrace()
                ivAvatar.setImageResource(R.drawable.ic_users)
            }
        } else {
            // Nếu không có URI, đặt ảnh mặc định
            Glide.with(this)
                .load(R.drawable.ic_users)
                .into(ivAvatar)
        }
    }

    /**
     * Gán sự kiện cho các nút (Đã đúng)
     */
    private fun setupProfileEvents() {
        findViewById<MaterialButton>(R.id.btn_logout).setOnClickListener {
            logout()
        }

        findViewById<ImageView>(R.id.btn_edit_profile).setOnClickListener {
            Toast.makeText(this, "Chọn ảnh đại diện mới...", Toast.LENGTH_SHORT).show()
            try {
                pickImageLauncher.launch(arrayOf("image/*"))
            } catch (e: Exception) {
                Toast.makeText(this, "Không tìm thấy ứng dụng chọn ảnh", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<RelativeLayout>(R.id.btn_change_password).setOnClickListener {
            Toast.makeText(this, "Mở đổi mật khẩu", Toast.LENGTH_SHORT).show()
        }

        findViewById<RelativeLayout>(R.id.btn_view_contract).setOnClickListener {
            Toast.makeText(this, "Mở xem hợp đồng", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Xử lý ảnh được chọn - Sửa: copy ảnh về bộ nhớ app để tránh sự cố permission/uri
     */
    private fun handleSelectedImage(uri: Uri) {
        if (currentTenant == null) return

        try {
            // Thử cấp quyền persistable (nếu được)
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Không quan trọng nếu không cấp được, tiếp tục với sao chép
                e.printStackTrace()
            }

            // Sao chép ảnh được chọn vào thư mục nội bộ của app (files/avatars)
            val avatarsDir = File(filesDir, "avatars")
            if (!avatarsDir.exists()) avatarsDir.mkdirs()

            // Đặt tên file theo tenant id để dễ quản lý
            val tenantId = currentTenant!!.id
            val destFile = File(avatarsDir, "tenant_${tenantId}_avatar.jpg")

            // Mở luồng từ URI và ghi vào file đích
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IllegalStateException("Không thể mở InputStream từ URI")

            // Lưu Uri của file nội bộ dưới dạng file:// ... để Glide đọc an toàn
            val savedUriString = Uri.fromFile(destFile).toString()
            // Chỉ cập nhật cột imageUri để tránh vô tình thay đổi các cột khóa ngoại
            val (rowsAffected, errorMsg) = tenantDao.updateTenantImage(tenantId, savedUriString)

            if (rowsAffected > 0) {
                Toast.makeText(this, "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show()
                loadAvatar(savedUriString) // Tải lại ảnh bằng Glide
                // cập nhật in-memory object
                currentTenant = currentTenant!!.copy(imageUri = savedUriString)
            } else {
                val msg = errorMsg ?: "Không thể cập nhật ảnh (lỗi DB)"
                Toast.makeText(this, "Cập nhật ảnh thất bại: $msg", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khi lưu ảnh: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    /**
     * Dọn dẹp SharedPreferences và quay về Login (Đã đúng)
     */
    private fun logout() {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Xử lý thanh điều hướng dưới cùng (Đã đúng)
     */
    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, TenantHomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener {
            Toast.makeText(this, "Mở Thông báo", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            Toast.makeText(this, "Đang ở Trang cá nhân", Toast.LENGTH_SHORT).show()
        }
    }
}