package com.example.qunlphngtr

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.BillAdapter
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.model.Bill
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class BillActivity : AppCompatActivity() {
    private lateinit var billDao: BillDao
    private lateinit var roomDao: RoomDao
    private lateinit var adapter: BillAdapter
    private var filteredRoomId: Int = -1

    // State for filters
    private var selectedMonth: String = ""
    private var searchQuery: String = ""

    private val billLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadAndFilterBills()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bills)

        billDao = BillDao(this)
        roomDao = RoomDao(this)
        filteredRoomId = intent.getIntExtra("FILTER_ROOM_ID", -1)

        // Bind views
        val recyclerView = findViewById<RecyclerView>(R.id.rvRooms)
        val btnAddBill = findViewById<Button>(R.id.btnAddRoom)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnSelectMonth = findViewById<LinearLayout>(R.id.btnSelectMonth)
        val btnGenerateAll = findViewById<Button>(R.id.btnGenerateAll)

        // Setup UI
        setupRecyclerView(recyclerView)
        setupSwipeActions(recyclerView)
        setupBottomNavigation()

        // Hide redundant button and setup listeners
        btnAddBill.visibility = View.GONE
        btnAdd.setOnClickListener { showRoomSelectionDialog() }
        btnGenerateAll.setOnClickListener { showGenerateAllConfirmationDialog() }
        btnSelectMonth.setOnClickListener { showMonthYearPickerDialog() }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString()
                loadAndFilterBills()
            }
        })

        // Handle single room mode
        if (filteredRoomId != -1) {
            val room = roomDao.getRoomById(filteredRoomId)
            findViewById<TextView>(R.id.tvTitle).text = "Hóa đơn của ${room?.name}"
            // Hide buttons that are not relevant in single room view
            btnAdd.visibility = View.GONE
            btnGenerateAll.visibility = View.GONE
            btnSelectMonth.visibility = View.GONE
        } }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_bill // CORRECTED ID

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_bill -> { // CORRECTED ID
                    // Already here
                    true
                }
                R.id.nav_tenants -> {
                    val intent = Intent(this, TenantListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndFilterBills()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter = BillAdapter(mutableListOf()) // Start with an empty list
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.onItemClick = { bill ->
            val intent = Intent(this, BillDetailActivity::class.java)
            intent.putExtra("invoice_id", bill.id)
            intent.putExtra("isAdmin", true)
            billLauncher.launch(intent)
        }
    }

    private fun loadAndFilterBills() {
        var filteredList = billDao.getAllBills()

        // 1. Filter by Room ID if provided (highest priority)
        if (filteredRoomId != -1) {
            filteredList = filteredList.filter { it.roomId == filteredRoomId }.toMutableList()
        } else {
            // 2. Filter by selected month if not in single-room mode
            if (selectedMonth.isNotEmpty()) {
                filteredList = filteredList.filter { it.month == selectedMonth }.toMutableList()
            }
        }

        // 3. Filter by search query
        if (searchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.roomName.contains(searchQuery, ignoreCase = true) || it.month.contains(searchQuery, true)
            }.toMutableList()
        }

        adapter.updateList(filteredList)
    }

    private fun setupSwipeActions(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val bill = adapter.getBillAt(position)

                if (direction == ItemTouchHelper.RIGHT) { // EDIT
                    val intent = Intent(this@BillActivity, AddBillActivity::class.java)
                    intent.putExtra("BILL_ID", bill.id)
                    billLauncher.launch(intent)
                    adapter.notifyItemChanged(position)
                } else { // DELETE
                    AlertDialog.Builder(this@BillActivity)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa hóa đơn tháng ${bill.month} của phòng ${bill.roomName}?")
                        .setPositiveButton("Xóa") { _, _ ->
                            val deleteResult = billDao.deleteBill(bill.id)
                            when {
                                deleteResult > 0 -> {
                                    loadAndFilterBills()
                                    Toast.makeText(this@BillActivity, "Xóa hóa đơn thành công", Toast.LENGTH_SHORT).show()
                                }
                                deleteResult == -2 -> {
                                    Toast.makeText(this@BillActivity, "Không thể xóa hóa đơn chưa thanh toán.", Toast.LENGTH_LONG).show()
                                    adapter.notifyItemChanged(position) // Revert swipe
                                }
                                else -> {
                                    Toast.makeText(this@BillActivity, "Xóa hóa đơn thất bại", Toast.LENGTH_SHORT).show()
                                    adapter.notifyItemChanged(position) // Revert swipe
                                }
                            }
                        }
                        .setNegativeButton("Hủy") { _, _ -> adapter.notifyItemChanged(position) }
                        .setCancelable(false)
                        .show()
                }
            }

        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }

    private fun showRoomSelectionDialog() {
        val rooms = roomDao.getAllRooms().filter { it.tenantId != null }
        if (rooms.isEmpty()) {
            Toast.makeText(this, "Không có phòng nào đang thuê để tạo hóa đơn.", Toast.LENGTH_LONG).show()
            return
        }
        val roomNames = rooms.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Chọn phòng để tạo hóa đơn")
            .setItems(roomNames) { dialog, which ->
                val intent = Intent(this, AddBillActivity::class.java)
                intent.putExtra("ROOM_ID", rooms[which].id)
                billLauncher.launch(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun showMonthYearPickerDialog() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, _ ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(Calendar.YEAR, year)
                selectedCal.set(Calendar.MONTH, month)
                val sdf = SimpleDateFormat("MM/yyyy", Locale.getDefault())
                selectedMonth = sdf.format(selectedCal.time)
                findViewById<TextView>(R.id.tvSelectedMonth).text = "Tháng: $selectedMonth"
                loadAndFilterBills()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showGenerateAllConfirmationDialog() {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val currentMonth = sdf.format(cal.time)

        AlertDialog.Builder(this)
            .setTitle("Xác nhận Tạo Hóa Đơn")
            .setMessage("Bạn có muốn tạo hóa đơn hàng loạt cho tháng $currentMonth không? Hành động này sẽ tạo hóa đơn cho tất cả các phòng đang có người thuê.")
            .setPositiveButton("Tạo") { _, _ ->
                val billsGenerated = billDao.generateMonthlyBills(currentMonth)
                if (billsGenerated > 0) {
                    Toast.makeText(this, "Đã tạo thành công $billsGenerated hóa đơn.", Toast.LENGTH_LONG).show()
                    loadAndFilterBills() // Refresh the list
                } else {
                    Toast.makeText(this, "Không có hóa đơn nào được tạo. Có thể tất cả phòng đã có hóa đơn cho tháng này.", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
