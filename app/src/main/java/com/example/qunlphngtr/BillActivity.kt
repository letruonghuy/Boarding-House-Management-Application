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
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.BillAdapter
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Bill
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class BillActivity : AppCompatActivity() {
    private lateinit var billDao: BillDao
    private lateinit var roomDao: RoomDao
    private lateinit var tenantDao: TenantDao
    private lateinit var adapter: BillAdapter
    private lateinit var billList: MutableList<Bill>
    private lateinit var etSearch: EditText
    private lateinit var btnSelectMonth: LinearLayout
    private lateinit var tvSelectedMonth: TextView

    private val billLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            reloadList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bills)

        billDao = BillDao(this)
        roomDao = RoomDao(this)
        tenantDao = TenantDao(this)

        val recyclerView = findViewById<RecyclerView>(R.id.rvRooms)
        setupRecyclerView(recyclerView)
        setupSwipeActions(recyclerView)

        val btnAddBill = findViewById<Button>(R.id.btnAddRoom)
        btnAddBill.setOnClickListener { showRoomSelectionDialog() }

        val btnGenerateAll = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGenerateAll)
        btnGenerateAll.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dp = DatePickerDialog(this, { _, y, m, _ ->
                val monthYear = String.format(Locale.getDefault(), "%02d/%d", m + 1, y)
                billDao.generateMonthlyBills(monthYear)
                reloadList()
                Toast.makeText(this, "Đã tạo hóa đơn cho tháng $monthYear", Toast.LENGTH_LONG).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dp.show()
        }

        etSearch = findViewById(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filter(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSelectMonth = findViewById(R.id.btnSelectMonth)
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)

        btnSelectMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, selectedYear, selectedMonth, _ ->
                val monthYear = String.format(Locale.getDefault(), "%02d/%d", selectedMonth + 1, selectedYear)
                tvSelectedMonth.text = "Tháng: $monthYear"
                billList = billDao.getBillsByMonth(monthYear)
                filter(etSearch.text.toString())
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        setupBottomNavigation()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        billList = billDao.getAllBills()
        adapter = BillAdapter(billList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // SỰ KIỆN 1: Nhấn để XEM CHI TIẾT
        adapter.onItemClick = { bill ->
            val intent = Intent(this, BillDetailActivity::class.java)
            intent.putExtra("invoice_id", bill.id)
            intent.putExtra("isAdmin", true) // <-- THÊM DÒNG NÀY
            billLauncher.launch(intent) // Dùng launcher để nhận kết quả
        }

        // SỰ KIỆN 2: Nhấn giữ để XÓA
        adapter.onItemLongClick = { bill, position ->
            AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa hóa đơn tháng ${bill.month} của phòng ${bill.roomName}?")
                .setPositiveButton("Xóa") { _, _ ->
                    if (billDao.deleteBill(bill.id) > 0) {
                        reloadList() // Tải lại để cập nhật danh sách
                        Toast.makeText(this, "Xóa hóa đơn thành công", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Xóa hóa đơn thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun setupSwipeActions(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) { // Vuốt sang phải
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val bill = adapter.getBillAt(position)

                // SỰ KIỆN 3: Vuốt sang phải để SỬA
                val intent = Intent(this@BillActivity, AddBillActivity::class.java)
                intent.putExtra("BILL_ID", bill.id)
                billLauncher.launch(intent)

                adapter.notifyItemChanged(position) // Trả item về vị trí cũ
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable()
                if (dX > 0) { // Chỉ vẽ khi vuốt sang phải
                    background.color = Color.parseColor("#4CAF50") // Màu xanh lá
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    background.draw(c)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }

    private fun filter(text: String) {
        val allBills = billDao.getAllBills()
        val filteredList = if (text.isEmpty()) {
            allBills
        } else {
            allBills.filter {
                it.roomName.contains(text, ignoreCase = true)
            }.toMutableList()
        }
        adapter.updateList(filteredList)
    }

    private fun showRoomSelectionDialog() {
        val rooms = roomDao.getAllRooms().filter { tenantDao.getTenantByRoomId(it.id) != null }
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

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_bill
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_bill -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        reloadList()
    }

    private fun reloadList() {
        val currentSearch = etSearch.text.toString()
        val allBills = billDao.getAllBills()
        billList.clear()
        billList.addAll(allBills)
        filter(currentSearch)
    }
}