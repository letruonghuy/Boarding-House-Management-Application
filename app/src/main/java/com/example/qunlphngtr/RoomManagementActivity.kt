package com.example.qunlphngtr

import android.app.Dialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.RoomAdapter
import com.example.qunlphngtr.adapter.RoomAdapterItem
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import java.text.NumberFormat
import java.util.Locale

class RoomManagementActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var rvRooms: RecyclerView
    private lateinit var fabAddRoom: FloatingActionButton
    private lateinit var roomDao: RoomDao
    private lateinit var tenantDao: TenantDao
    private lateinit var billDao: BillDao
    private lateinit var roomAdapter: RoomAdapter
    private var allRoomItems = listOf<RoomAdapterItem>()

    private var selectedImageUri: Uri? = null
    private var imgPhongDialog: ImageView? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                selectedImageUri = it
                imgPhongDialog?.setImageURI(it)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_management)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Quản lý phòng"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initDaos()
        bindViews()
        setupRecyclerView()
        setupListeners()
        setupSwipeActions()
    }

    override fun onResume() {
        super.onResume()
        loadAllDataAndFilter()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initDaos() {
        roomDao = RoomDao(this)
        tenantDao = TenantDao(this)
        billDao = BillDao(this)
    }

    private fun bindViews() {
        tabLayout = findViewById(R.id.tabLayout)
        rvRooms = findViewById(R.id.rvRooms)
        fabAddRoom = findViewById(R.id.fabAddRoom)
    }

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter(mutableListOf(),
            onManageClick = { item ->
                val intent = Intent(this, RoomDashboardActivity::class.java)
                intent.putExtra("ROOM_ID", item.room.id)
                startActivity(intent)
            },
            onAddTenantClick = { item ->
                showAddTenantOptionsDialog(item.room)
            }
        )
        rvRooms.layoutManager = LinearLayoutManager(this)
        rvRooms.adapter = roomAdapter
    }

    private fun setupListeners() {
        fabAddRoom.setOnClickListener { showAddRoomDialog() }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterRoomsByStatus(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadAllDataAndFilter() {
        val allRooms = roomDao.getAllRooms()
        val allTenants = tenantDao.getAllTenants().associateBy { it.id }
        val allBills = billDao.getAllBills()

        allRoomItems = allRooms.map { room ->
            val tenant = if (room.tenantId != null) allTenants[room.tenantId] else null
            val unpaidBillCount = allBills.count { it.roomId == room.id && it.status != "paid" }
            RoomAdapterItem(room, tenant, unpaidBillCount)
        }

        filterRoomsByStatus(tabLayout.selectedTabPosition)
    }

    private fun filterRoomsByStatus(tabPosition: Int) {
        val filteredList = when (tabPosition) {
            0 -> allRoomItems.filter { it.room.status == "available" }
            1 -> allRoomItems.filter { it.room.status != "available" }
            else -> emptyList()
        }
        roomAdapter.updateList(filteredList)
    }

    private fun showAddRoomDialog(roomToEdit: Room? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_them_phong)

        val edtTenPhong = dialog.findViewById<EditText>(R.id.edtTenPhong)
        val edtGiaPhong = dialog.findViewById<EditText>(R.id.edtGiaPhong)
        val edtDienTich = dialog.findViewById<EditText>(R.id.edtDienTich)
        imgPhongDialog = dialog.findViewById(R.id.imgPhong)
        val btnChonAnh = dialog.findViewById<Button>(R.id.btnChonAnh)
        val btnLuu = dialog.findViewById<Button>(R.id.btnLuu)

        addThousandSeparatorTextWatcher(edtGiaPhong)
        addThousandSeparatorTextWatcher(edtDienTich)
        selectedImageUri = null

        if (roomToEdit != null) {
            edtTenPhong.setText(roomToEdit.name)
            edtGiaPhong.setText(roomToEdit.price.toLong().toString())
            edtDienTich.setText(roomToEdit.area.toLong().toString())
            roomToEdit.imageUri?.let {
                selectedImageUri = Uri.parse(it)
                imgPhongDialog?.setImageURI(selectedImageUri)
            }
        } else {
            imgPhongDialog?.setImageResource(R.drawable.ic_room)
        }

        btnChonAnh.setOnClickListener { pickImageLauncher.launch(arrayOf("image/*")) }
        dialog.findViewById<Button>(R.id.btnHuy).setOnClickListener { dialog.dismiss() }

        btnLuu.setOnClickListener {
            val name = edtTenPhong.text.toString().trim()
            val priceString = edtGiaPhong.text.toString().replace(".", "")
            val price = priceString.toDoubleOrNull()
            val areaString = edtDienTich.text.toString().replace(".", "")
            val area = areaString.toDoubleOrNull()

            if (name.isBlank() || price == null || area == null) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (roomToEdit == null) {
                val newRoom = Room(0, name, price, area, "available", "", selectedImageUri?.toString(), null)
                val newId = roomDao.insertRoom(newRoom)
                if (newId > 0) Toast.makeText(this, "Thêm phòng thành công!", Toast.LENGTH_SHORT).show()
            } else {
                val updatedRoom = roomToEdit.copy(
                    name = name,
                    price = price,
                    area = area,
                    imageUri = selectedImageUri?.toString() ?: roomToEdit.imageUri
                )
                val rowsAffected = roomDao.updateRoom(updatedRoom)
                if (rowsAffected > 0) Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
            }
            loadAllDataAndFilter()
            dialog.dismiss()
        }

        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    // Hàm mới để hiển thị lựa chọn
    private fun showAddTenantOptionsDialog(room: Room) {
        val options = arrayOf("Thêm người thuê mới", "Chọn từ danh sách có sẵn")

        AlertDialog.Builder(this)
            .setTitle("Thêm khách cho ${room.name}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, AddTenantActivity::class.java)
                        intent.putExtra("ROOM_ID", room.id)
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(this, AssignTenantActivity::class.java)
                        intent.putExtra("ROOM_ID", room.id)
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun addThousandSeparatorTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    editText.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[.,]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        try {
                            val parsed = cleanString.toLong()
                            val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                            val formatted = formatter.format(parsed)
                            current = formatted
                            editText.setText(formatted)
                            editText.setSelection(formatted.length)
                        } catch (e: NumberFormatException) {
                            // ignore
                        }
                    } else {
                        current = ""
                    }

                    editText.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupSwipeActions() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val roomItem = roomAdapter.getItemAt(position)

                if (direction == ItemTouchHelper.RIGHT) { // EDIT
                    showAddRoomDialog(roomItem.room)
                    roomAdapter.notifyItemChanged(position)
                } else if (direction == ItemTouchHelper.LEFT) { // DELETE
                    AlertDialog.Builder(this@RoomManagementActivity)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa ${roomItem.room.name}?")
                        .setPositiveButton("Xóa") { _, _ ->
                            if (roomItem.tenant != null) {
                                Toast.makeText(this@RoomManagementActivity, "Không thể xóa phòng đang có người thuê.", Toast.LENGTH_LONG).show()
                                roomAdapter.notifyItemChanged(position)
                            } else if (roomItem.unpaidBillCount > 0) {
                                Toast.makeText(this@RoomManagementActivity, "Không thể xóa phòng còn hóa đơn chưa thanh toán.", Toast.LENGTH_LONG).show()
                                roomAdapter.notifyItemChanged(position)
                            } else {
                                val deleted = roomDao.deleteRoom(roomItem.room.id)
                                if (deleted > 0) {
                                    Toast.makeText(this@RoomManagementActivity, "Đã xóa ${roomItem.room.name}", Toast.LENGTH_SHORT).show()
                                    loadAllDataAndFilter()
                                }
                            }
                        }
                        .setNegativeButton("Hủy") { _, _ -> roomAdapter.notifyItemChanged(position) }
                        .setCancelable(false)
                        .show()
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable()
                if (dX > 0) {
                    background.color = Color.parseColor("#4CAF50")
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                } else {
                    background.color = Color.parseColor("#F44336")
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                }
                background.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvRooms)
    }
}
