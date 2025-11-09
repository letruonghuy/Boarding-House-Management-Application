package com.example.qunlphngtr

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.BillAdapter
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.model.Bill
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class BillActivity : AppCompatActivity() {
    private lateinit var billDao: BillDao
    private lateinit var roomDao: RoomDao
    private lateinit var adapter: BillAdapter
    private lateinit var billList: MutableList<Bill>
    private lateinit var etSearch: EditText
    private lateinit var btnSelectMonth: LinearLayout
    private lateinit var tvSelectedMonth: TextView

    // Activity Result Launcher để nhận kết quả từ AddBillActivity
    private val addBillLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            reloadList() // Tải lại danh sách nếu hóa đơn được thêm thành công
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bills)

        billDao = BillDao(this)
        roomDao = RoomDao(this)
        billList = billDao.getAllBills()

        val recyclerView = findViewById<RecyclerView>(R.id.rvRooms)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BillAdapter(billList)
        recyclerView.adapter = adapter

        adapter.onItemClick = { bill ->
            val intent = Intent(this, BillDetailActivity::class.java)
            intent.putExtra("invoice_id", bill.id)
            startActivity(intent)
        }

        val btnAddBill = findViewById<Button>(R.id.btnAddRoom)
        btnAddBill.setOnClickListener {
            showRoomSelectionDialog()
        }

        etSearch = findViewById(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnSelectMonth = findViewById(R.id.btnSelectMonth)
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)

        btnSelectMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, _ ->
                val selectedDate = String.format("Tháng: %02d/%d", selectedMonth + 1, selectedYear)
                tvSelectedMonth.text = selectedDate
                val monthYear = String.format("%02d/%d", selectedMonth + 1, selectedYear)
                val billsForMonth = billDao.getBillsByMonth(monthYear)
                billList = billsForMonth
                filter(etSearch.text.toString())
            }, year, month, day)

            datePickerDialog.show()
        }

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

    private fun filter(text: String) {
        val filteredList = mutableListOf<Bill>()
        for (item in billList) {
            if (item.roomName.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
            }
        }
        adapter.updateList(filteredList)
    }

    private fun showRoomSelectionDialog() {
        val rooms = roomDao.getAllRooms()
        val roomNames = rooms.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Chọn phòng để tạo hóa đơn")
            .setItems(roomNames) { dialog, which ->
                val selectedRoomId = rooms[which].id
                val intent = Intent(this, AddBillActivity::class.java)
                intent.putExtra("ROOM_ID", selectedRoomId)
                addBillLauncher.launch(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Không cần reloadList() ở đây nữa vì đã có ActivityResultLauncher
    }

    private fun reloadList() {
        billList = billDao.getAllBills()
        tvSelectedMonth.text = "Chọn thời gian"
        filter(etSearch.text.toString())
    }
}
