package com.example.qunlphngtr

import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.EditText
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.graphics.drawable.ColorDrawable
import android.graphics.Color
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.RoomAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Room

class QuanLyPhongActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var rvRooms: RecyclerView

    // Biến lưu URI ảnh được chọn
    private var selectedImageUri: Uri? = null
    private var imgPhong: ImageView? = null

    // Launcher chọn ảnh từ thư viện
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imgPhong?.setImageURI(uri) // Hiển thị ảnh vào ImageView
        }
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        // Khởi tạo DB
        db = DatabaseHelper(this)

        // RecyclerView setup
        rvRooms = findViewById(R.id.rvRooms)
        rvRooms.layoutManager = LinearLayoutManager(this)




        // Load dữ liệu ban đầu từ DB
        val roomList = db.getAllRooms().toMutableList()

        // ✅ Truyền onItemClick khi tạo Adapter
        roomAdapter = RoomAdapter(roomList) { room ->
            val intent = Intent(this, RoomDetailActivity::class.java)
            intent.putExtra("room_id", room.id)
            intent.putExtra("room_name", room.name)
            intent.putExtra("room_price", room.price)
            intent.putExtra("room_area", room.area)
            intent.putExtra("room_status", room.status)
            intent.putExtra("room_description", room.description)
            intent.putExtra("room_image", room.imageUri)
            startActivity(intent)
        }
        rvRooms.adapter = roomAdapter

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

        // Nút mở popup Thêm phòng
        val btnThemPhong = findViewById<Button>(R.id.btnAddRoom)
        btnThemPhong.setOnClickListener {
            showAddRoomDialog()


        }
    }

    private fun showAddRoomDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_them_phong)

        val edtTenPhong = dialog.findViewById<EditText>(R.id.edtTenPhong)
        val edtGiaPhong = dialog.findViewById<EditText>(R.id.edtGiaPhong)
        val edtDienTich = dialog.findViewById<EditText>(R.id.edtDienTich)
        imgPhong = dialog.findViewById(R.id.imgPhong)
        val btnChonAnh = dialog.findViewById<Button>(R.id.btnChonAnh)
        val btnLuu = dialog.findViewById<Button>(R.id.btnLuu)
        val btnHuy = dialog.findViewById<Button>(R.id.btnHuy)

        // Chọn ảnh
        btnChonAnh.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnLuu.setOnClickListener {
            val tenPhong = edtTenPhong.text.toString()
            val giaPhong = edtGiaPhong.text.toString()
            val dienTich = edtDienTich.text.toString()

            if (tenPhong.isBlank() || giaPhong.isBlank() || dienTich.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            } else {
                // Lưu vào DB
                val id = db.insertRoom(
                    name = tenPhong,
                    price = giaPhong.toDouble(),
                    area = dienTich.toDouble(),
                    status = "Còn trống",
                    description = "Chưa có mô tả",
                    imageUri = selectedImageUri?.toString() // Lưu URI ảnh
                )

                if (id > 0) {
                    val newRoom = Room(
                        id = id.toInt(),
                        name = tenPhong,
                        price = giaPhong.toDouble(),
                        area = dienTich.toDouble(),
                        status = "Còn trống",
                        description = "Chưa có mô tả",
                        imageUri = selectedImageUri?.toString()
                    )
                    roomAdapter.addRoom(newRoom) // Cập nhật RecyclerView
                    Toast.makeText(this, "Đã thêm phòng: $tenPhong", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Lỗi khi thêm phòng!", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
        }

        btnHuy.setOnClickListener { dialog.dismiss() }

        // Style dialog
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()
    }
}