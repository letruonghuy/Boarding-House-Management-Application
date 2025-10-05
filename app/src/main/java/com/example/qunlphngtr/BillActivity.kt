package com.example.qunlphngtr

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.BillAdapter
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Bill
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.view.LayoutInflater

class BillActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: BillAdapter
    private lateinit var billList: MutableList<Bill>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bills)

        db = DatabaseHelper(this)
        billList = db.getAllBills()

        val recyclerView = findViewById<RecyclerView>(R.id.rvRooms)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BillAdapter(billList)
        recyclerView.adapter = adapter

        val btnAddBill = findViewById<Button>(R.id.btnAddRoom)
        btnAddBill.setOnClickListener {
            showAddBillDialog()
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

    private fun showAddBillDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_bill, null)
        val etMonth = dialogView.findViewById<EditText>(R.id.etMonth)
        val etElectric = dialogView.findViewById<EditText>(R.id.etElectric)
        val etWater = dialogView.findViewById<EditText>(R.id.etWater)
        val etRoom = dialogView.findViewById<EditText>(R.id.etRoom)
        val etInternet = dialogView.findViewById<EditText>(R.id.etInternet)

        // 🔹 Thêm Spinner để chọn phòng và người thuê
        val spRoom = dialogView.findViewById<Spinner>(R.id.spRoom)
        val spTenant = dialogView.findViewById<Spinner>(R.id.spTenant)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBill)

        // 🔹 Lấy danh sách phòng và người thuê từ DB
        val roomList = db.getAllRooms()
        val tenantList = db.getAllTenants()

        // 🔹 Gắn dữ liệu vào spinner (hiển thị tên)
        val roomAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomList.map { it.name })
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spRoom.adapter = roomAdapter

        val tenantAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tenantList.map { it.name })
        tenantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTenant.adapter = tenantAdapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val month = etMonth.text.toString().trim()
            val electric = etElectric.text.toString().toDoubleOrNull() ?: 0.0
            val water = etWater.text.toString().toDoubleOrNull() ?: 0.0
            val room = etRoom.text.toString().toDoubleOrNull() ?: 0.0
            val internet = etInternet.text.toString().toDoubleOrNull() ?: 0.0

            if (month.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tháng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 Lấy roomId và tenantId dựa vào vị trí spinner
            val roomId = roomList[spRoom.selectedItemPosition].id
            val tenantId = tenantList[spTenant.selectedItemPosition].id

            val result = db.insertBill(month, electric, water, room, internet, roomId, tenantId)

            if (result > 0) {
                Toast.makeText(this, "Thêm hóa đơn thành công!", Toast.LENGTH_SHORT).show()
                reloadList()
            } else {
                Toast.makeText(this, "Thêm thất bại!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun reloadList() {
        billList = db.getAllBills()
        adapter.updateList(billList)
    }
}
