package com.example.qunlphngtr

import android.app.Dialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.RoomAdapter
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.model.Room
import com.google.android.material.bottomnavigation.BottomNavigationView

class QuanLyPhongActivity : AppCompatActivity() {

    private lateinit var roomDao: RoomDao
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var rvRooms: RecyclerView
    private var selectedImageUri: Uri? = null
    private var imgPhong: ImageView? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imgPhong?.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        roomDao = RoomDao(this)
        rvRooms = findViewById(R.id.rvRooms)
        rvRooms.layoutManager = LinearLayoutManager(this)

        val roomList = roomDao.getAllRooms().toMutableList()
        roomAdapter = RoomAdapter(roomList) { /* bấm vào item nếu muốn */ }
        rvRooms.adapter = roomAdapter

        val btnThemPhong = findViewById<Button>(R.id.btnAddRoom)
        val edtSearch = findViewById<EditText>(R.id.etSearch)

        btnThemPhong.setOnClickListener { showAddRoomDialog() }

        edtSearch.addTextChangedListener {
            val keyword = it.toString()
            val result = roomDao.searchRooms(keyword)
            roomAdapter.filterList(result)
        }

        setupSwipeActions()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home
    }

    private fun showAddRoomDialog(roomToEdit: Room? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_them_phong)

        val edtTenPhong = dialog.findViewById<EditText>(R.id.edtTenPhong)
        val edtGiaPhong = dialog.findViewById<EditText>(R.id.edtGiaPhong)
        val edtDienTich = dialog.findViewById<EditText>(R.id.edtDienTich)
        imgPhong = dialog.findViewById(R.id.imgPhong)
        val btnChonAnh = dialog.findViewById<Button>(R.id.btnChonAnh)
        val btnLuu = dialog.findViewById<Button>(R.id.btnLuu)
        val btnHuy = dialog.findViewById<Button>(R.id.btnHuy)

        if (roomToEdit != null) {
            edtTenPhong.setText(roomToEdit.name)
            edtGiaPhong.setText(roomToEdit.price.toString())
            edtDienTich.setText(roomToEdit.area.toString())
            selectedImageUri = roomToEdit.imageUri?.let { Uri.parse(it) }
            selectedImageUri?.let { imgPhong?.setImageURI(it) }
        }

        btnChonAnh.setOnClickListener { pickImageLauncher.launch("image/*") }

        btnLuu.setOnClickListener {
            val name = edtTenPhong.text.toString()
            val price = edtGiaPhong.text.toString().toDoubleOrNull()
            val area = edtDienTich.text.toString().toDoubleOrNull()

            if (name.isBlank() || price == null || area == null) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (roomToEdit == null) {
                val newRoom = Room(0, name, price, area, "available", "Chưa có mô tả", selectedImageUri?.toString())
                val id = roomDao.insertRoom(newRoom)
                if (id > 0) roomAdapter.addRoom(newRoom.copy(id = id.toInt()))
            } else {
                val updated = roomToEdit.copy(
                    name = name, price = price, area = area,
                    imageUri = selectedImageUri?.toString()
                )
                roomDao.updateRoom(updated)
                roomAdapter.updateRoom(updated)
            }

            dialog.dismiss()
        }

        btnHuy.setOnClickListener { dialog.dismiss() }

        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun setupSwipeActions() {
        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val room = roomAdapter.getRoomAt(position)

                if (direction == ItemTouchHelper.LEFT) {
                    val result = roomDao.deleteRoom(room.id)
                    if (result > 0) roomAdapter.removeRoom(room)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    roomAdapter.notifyItemChanged(position)
                    showAddRoomDialog(room)
                }
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint()
                val background = ColorDrawable()

                if (dX > 0) { // vuốt phải -> sửa
                    paint.color = Color.parseColor("#4CAF50")
                    background.color = paint.color
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    background.draw(c)
                } else if (dX < 0) { // vuốt trái -> xóa
                    paint.color = Color.parseColor("#F44336")
                    background.color = paint.color
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })

        itemTouchHelper.attachToRecyclerView(rvRooms)
    }
}
