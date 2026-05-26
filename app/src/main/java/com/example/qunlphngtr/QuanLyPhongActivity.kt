package com.example.qunlphngtr/*
package com.example.qunlphngtr

import android.app.Dialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.RoomAdapter
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Room
import com.google.android.material.bottomnavigation.BottomNavigationView

class QuanLyPhongActivity : AppCompatActivity() {

    private lateinit var roomDao: RoomDao
    private lateinit var tenantDao: TenantDao
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var rvRooms: RecyclerView
    private var selectedImageUri: Uri? = null
    private var imgPhong: ImageView? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedImageUri = uri
                imgPhong?.setImageURI(uri)
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(this, "Không thể lấy quyền truy cập ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        roomDao = RoomDao(this)
        tenantDao = TenantDao(this)
        rvRooms = findViewById(R.id.rvRooms)
        rvRooms.layoutManager = LinearLayoutManager(this)

        val roomList = roomDao.getAllRooms().toMutableList()
        roomAdapter = RoomAdapter(roomList) { */
/* bấm vào item nếu muốn *//*
 }
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
        bottomNav?.let {
            it.selectedItemId = R.id.nav_home
            it.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> true
                    R.id.nav_bill -> {
                        startActivity(Intent(this, BillActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                        true
                    }
                    R.id.nav_settings -> {
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                        finish()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val updated = roomDao.getAllRooms()
            roomAdapter.filterList(updated)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAddRoomDialog(roomToEdit: Room? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_them_phong)

        val edtTenPhong = dialog.findViewById<EditText>(R.id.edtTenPhong)
        val edtGiaPhong = dialog.findViewById<EditText>(R.id.edtGiaPhong)
        val edtDienTich = dialog.findViewById<EditText>(R.id.edtDienTich)
        val spinnerTenant = dialog.findViewById<Spinner>(R.id.spinnerTenant)
        imgPhong = dialog.findViewById(R.id.imgPhong)
        val btnChonAnh = dialog.findViewById<Button>(R.id.btnChonAnh)
        val btnLuu = dialog.findViewById<Button>(R.id.btnLuu)
        val btnHuy = dialog.findViewById<Button>(R.id.btnHuy)

        selectedImageUri = null
        var selectedTenantId: Int? = null

        // Lấy danh sách người thuê chưa có phòng HOẶC là người thuê hiện tại của phòng đang sửa
        val availableTenants = tenantDao.getAllTenants().filter {
            it.room_id == null || it.id == roomToEdit?.tenantId
        }

        val tenantDisplayList = mutableListOf("Trống")
        tenantDisplayList.addAll(availableTenants.map { it.name })

        val tenantAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tenantDisplayList)
        tenantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTenant.adapter = tenantAdapter

        spinnerTenant.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTenantId = if (position == 0) null else availableTenants[position - 1].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedTenantId = roomToEdit?.tenantId
            }
        }

        if (roomToEdit != null) {
            edtTenPhong.setText(roomToEdit.name)
            edtGiaPhong.setText(roomToEdit.price.toString())
            edtDienTich.setText(roomToEdit.area.toString())
            selectedImageUri = roomToEdit.imageUri?.let { Uri.parse(it) }
            selectedImageUri?.let { imgPhong?.setImageURI(it) }

            roomToEdit.tenantId?.let { tenantId ->
                val tenantPosition = availableTenants.indexOfFirst { it.id == tenantId }
                if (tenantPosition != -1) {
                    spinnerTenant.setSelection(tenantPosition + 1)
                }
            }
        } else {
            imgPhong?.setImageResource(R.drawable.ic_room)
        }

        btnChonAnh.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        btnLuu.setOnClickListener {
            val name = edtTenPhong.text.toString()
            val price = edtGiaPhong.text.toString().toDoubleOrNull()
            val area = edtDienTich.text.toString().toDoubleOrNull()

            if (name.isBlank() || price == null || area == null) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val finalImageUri: String? = if (selectedImageUri != null) {
                selectedImageUri.toString()
            } else roomToEdit?.imageUri

            val newStatus = if (selectedTenantId == null) "available" else "occupied"
            val newTenantId = selectedTenantId

            if (roomToEdit == null) {
                val newRoom = Room(
                    id = 0, name = name, price = price, area = area,
                    status = newStatus,
                    description = "Chưa có mô tả",
                    imageUri = finalImageUri,
                    tenantId = newTenantId
                )
                val newRoomId = roomDao.insertRoom(newRoom)

                if (newRoomId > 0) {
                    roomAdapter.addRoom(newRoom.copy(id = newRoomId.toInt()))
                    rvRooms.scrollToPosition(roomAdapter.itemCount - 1)

                    newTenantId?.let { tenantId ->
                        val tenant = availableTenants.find { it.id == tenantId }
                        tenant?.let {
                            val updatedTenant = it.copy(room_id = newRoomId.toInt())
                            tenantDao.updateTenant(updatedTenant)
                        }
                    }
                }
            } else {
                val oldTenantId = roomToEdit.tenantId
                val updatedRoom = roomToEdit.copy(
                    name = name, price = price, area = area,
                    imageUri = finalImageUri,
                    status = newStatus,
                    tenantId = newTenantId
                )
                val rowsAffected = roomDao.updateRoom(updatedRoom)

                if (rowsAffected > 0) {
                    if (oldTenantId != newTenantId) {
                        oldTenantId?.let {
                            tenantDao.getAllTenants().find { it.id == oldTenantId }?.let { tenant ->
                                tenantDao.updateTenant(tenant.copy(room_id = null))
                            }
                        }
                        newTenantId?.let {
                            availableTenants.find { it.id == newTenantId }?.let { tenant ->
                                tenantDao.updateTenant(tenant.copy(room_id = roomToEdit.id))
                            }
                        }
                    }
                    roomAdapter.filterList(roomDao.getAllRooms())
                }
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
                if (position == RecyclerView.NO_POSITION) return
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

                if (dX > 0) {
                    paint.color = Color.parseColor("#4CAF50")
                    background.color = paint.color
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    background.draw(c)
                } else if (dX < 0) {
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

    private val roomsUpdatedReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            try {
                val updated = roomDao.getAllRooms()
                roomAdapter.filterList(updated)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            val filter = android.content.IntentFilter("com.example.qunlphngtr.ACTION_ROOMS_UPDATED")
            registerReceiver(roomsUpdatedReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(roomsUpdatedReceiver)
        } catch (e: Exception) { */
/* ignore *//*
 }
    }
}
*/