package com.example.qunlphngtr

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.BillAdapter
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Bill
import com.google.android.material.bottomnavigation.BottomNavigationView

class BillActivity : AppCompatActivity() {
    private lateinit var billDao: BillDao
    private lateinit var adapter: BillAdapter
    private lateinit var billList: MutableList<Bill>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bills)

        billDao = BillDao(this)
        billList = billDao.getAllBills()

        val recyclerView = findViewById<RecyclerView>(R.id.rvRooms)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BillAdapter(billList)
        recyclerView.adapter = adapter

        // Tap item to open detail
        adapter.onItemClick = { bill ->
            val intent = Intent(this, BillDetailActivity::class.java)
            intent.putExtra("invoice_id", bill.id)
            startActivity(intent)
        }

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

        val spRoom = dialogView.findViewById<Spinner>(R.id.spRoom)
        val spTenant = dialogView.findViewById<Spinner>(R.id.spTenant)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBill)

        // lấy dữ liệu phòng + tenant
        val roomDao = com.example.qunlphngtr.dao.RoomDao(this)
        val tenantDao = com.example.qunlphngtr.dao.TenantDao(this)
        val roomList = roomDao.getAllRooms()
        val tenantList = tenantDao.getAllTenants()

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

            val roomId = roomList[spRoom.selectedItemPosition].id
            val tenantId = tenantList[spTenant.selectedItemPosition].id

            val result = billDao.insertBill(month, electric, water, room, internet, roomId, tenantId)

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

    override fun onResume() {
        super.onResume()
        reloadList()
    }

    private fun reloadList() {
        billList = billDao.getAllBills()
        adapter.updateList(billList)
    }
}
