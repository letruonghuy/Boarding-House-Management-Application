package com.example.qunlphngtr

import android.app.Dialog
import android.content.Intent
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
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.RoomAdapter
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao // Import TenantDao
import com.example.qunlphngtr.model.Room
import com.example.qunlphngtr.model.Tenant // Import Tenant
import com.google.android.material.bottomnavigation.BottomNavigationView

class QuanLyPhongActivity : AppCompatActivity() {

    private lateinit var roomDao: RoomDao
    private lateinit var tenantDao: TenantDao // Add TenantDao
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
        tenantDao = TenantDao(this) // Initialize TenantDao
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
        bottomNav?.let {
            it.selectedItemId = R.id.nav_home
            it.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> true
                    R.id.nav_bill -> {
                        val intent = Intent(this, AddBillActivity::class.java)
                        startActivity(intent)
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

    private fun showAddRoomDialog(roomToEdit: Room? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_them_phong)

        val edtTenPhong = dialog.findViewById<EditText>(R.id.edtTenPhong)
        val edtGiaPhong = dialog.findViewById<EditText>(R.id.edtGiaPhong)
        val edtDienTich = dialog.findViewById<EditText>(R.id.edtDienTich)
        val spinnerTenant = dialog.findViewById<Spinner>(R.id.spinnerTenant) // Get Spinner
        imgPhong = dialog.findViewById(R.id.imgPhong)
        val btnChonAnh = dialog.findViewById<Button>(R.id.btnChonAnh)
        val btnLuu = dialog.findViewById<Button>(R.id.btnLuu)
        val btnHuy = dialog.findViewById<Button>(R.id.btnHuy)

        // --- SETUP SPINNER ---
        val tenants = tenantDao.getAllTenants().toMutableList()
        // Create a fake tenant for the "Empty" option
        val emptyTenant = Tenant(id = -1, name = "Để trống", phone = "", gender = "")
        tenants.add(0, emptyTenant)

        // Use a simple string array adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tenants.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTenant.adapter = adapter
        // ---------------------

        selectedImageUri = null

        if (roomToEdit != null) {
            edtTenPhong.setText(roomToEdit.name)
            edtGiaPhong.setText(roomToEdit.price.toString())
            edtDienTich.setText(roomToEdit.area.toString())

            // Set spinner selection
            val tenantPosition = tenants.indexOfFirst { it.id == roomToEdit.tenantId }
            if (tenantPosition >= 0) {
                spinnerTenant.setSelection(tenantPosition)
            } else {
                spinnerTenant.setSelection(0) // Select "Để trống" if tenant not found or null
            }

            selectedImageUri = roomToEdit.imageUri?.let { Uri.parse(it) }
            selectedImageUri?.let { imgPhong?.setImageURI(it) }
        } else {
            spinnerTenant.setSelection(0) // Default to "Để trống" for new room
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

            // Get selected tenant from spinner
            val selectedTenantPosition = spinnerTenant.selectedItemPosition
            val selectedTenant = tenants[selectedTenantPosition]

            val newTenantId: Int?
            val newStatus: String

            if (selectedTenant.id == -1) { // "Để trống" is selected
                newTenantId = null
                newStatus = "Còn phòng"
            } else {
                newTenantId = selectedTenant.id
                newTenantId != null
                newStatus = "Hết phòng"
            }


            val finalImageUri: String? = if (selectedImageUri != null) {
                selectedImageUri.toString()
            } else if (roomToEdit != null) {
                roomToEdit.imageUri
            } else {
                null
            }

            if (roomToEdit == null) {
                val newRoom = Room(
                    id = 0,
                    name = name,
                    price = price,
                    area = area,
                    status = newStatus, // Set status
                    description = "Chưa có mô tả",
                    imageUri = finalImageUri,
                    tenantId = newTenantId // Set tenantId
                )
                val id = roomDao.insertRoom(newRoom)
                if (id > 0) {
                    roomAdapter.addRoom(newRoom.copy(id = id.toInt()))
                    rvRooms.scrollToPosition(roomAdapter.itemCount - 1)
                }
            } else {
                val updated = roomToEdit.copy(
                    name = name,
                    price = price,
                    area = area,
                    imageUri = finalImageUri,
                    status = newStatus, // Update status
                    tenantId = newTenantId // Update tenantId
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
}
