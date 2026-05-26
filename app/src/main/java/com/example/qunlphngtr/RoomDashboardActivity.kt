package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Room
import com.example.qunlphngtr.model.Tenant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoomDashboardActivity : AppCompatActivity() {

    private var roomId: Int = -1
    private lateinit var roomDao: RoomDao
    private lateinit var tenantDao: TenantDao
    private lateinit var billDao: BillDao
    private var currentRoom: Room? = null
    private var currentTenant: Tenant? = null

    // UI Components
    private lateinit var toolbar: Toolbar
    private lateinit var tvTenantName: TextView
    private lateinit var tvTenantPhone: TextView
    private lateinit var tvLastBillStatus: TextView
    private lateinit var btnCreateBill: Button
    private lateinit var btnViewAllBills: Button
    private lateinit var btnRemoveTenant: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_dashboard)

        roomId = intent.getIntExtra("ROOM_ID", -1)
        if (roomId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy phòng!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        initDaos()
        loadData()
        setupToolbar()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadData() // Tải lại dữ liệu khi quay lại màn hình
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        tvTenantName = findViewById(R.id.tvTenantName)
        tvTenantPhone = findViewById(R.id.tvTenantPhone)
        tvLastBillStatus = findViewById(R.id.tvLastBillStatus)
        btnCreateBill = findViewById(R.id.btnCreateBill)
        btnViewAllBills = findViewById(R.id.btnViewAllBills)
        btnRemoveTenant = findViewById(R.id.btnRemoveTenant)
    }

    private fun initDaos() {
        roomDao = RoomDao(this)
        tenantDao = TenantDao(this)
        billDao = BillDao(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadData() {
        currentRoom = roomDao.getRoomById(roomId)
        currentTenant = currentRoom?.tenantId?.let { tenantDao.getTenantById(it) }

        supportActionBar?.title = "Quản lý ${currentRoom?.name ?: "Phòng"}"

        if (currentTenant != null) {
            tvTenantName.text = "Tên: ${currentTenant?.name ?: "N/A"}"
            tvTenantPhone.text = "SĐT: ${currentTenant?.phone ?: "N/A"}"
        } else {
            tvTenantName.text = "Phòng trống"
            tvTenantPhone.text = ""
        }

        val lastBill = billDao.getBillsByRoomId(roomId).maxByOrNull { it.id }
        if (lastBill != null) {
            tvLastBillStatus.text = "Hóa đơn gần nhất: ${lastBill.status}"
        } else {
            tvLastBillStatus.text = "Chưa có hóa đơn"
        }
    }

    private fun setupListeners() {
        btnCreateBill.setOnClickListener {
            val intent = Intent(this, AddBillActivity::class.java)
            intent.putExtra("ROOM_ID", roomId)
            startActivity(intent)
        }

        btnViewAllBills.setOnClickListener {
            val intent = Intent(this, BillActivity::class.java)
            intent.putExtra("FILTER_ROOM_ID", roomId) // Gửi ID phòng để lọc
            startActivity(intent)
        }

        btnRemoveTenant.setOnClickListener {
            showRemoveTenantConfirmationDialog()
        }
    }

    private fun showRemoveTenantConfirmationDialog() {
        if (currentTenant == null || currentRoom == null) {
            Toast.makeText(this, "Phòng này hiện đang trống.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Xác nhận Xóa Khách")
            .setMessage("Bạn có chắc muốn xóa '${currentTenant?.name}' khỏi phòng này? Phòng sẽ được chuyển về trạng thái trống.")
            .setPositiveButton("Xác nhận") { _, _ -> removeTenantFromRoom() }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun removeTenantFromRoom() {
        // Check for unpaid bills for this room
        val unpaidBills = billDao.getUnpaidBillsByRoomId(roomId)
        if (unpaidBills.isNotEmpty()) {
            Toast.makeText(this, "Không thể xóa khách khỏi phòng vì còn hóa đơn chưa thanh toán.", Toast.LENGTH_LONG).show()
            return // Stop the process
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = sdf.format(Date())
        currentTenant?.let {
            val updatedTenant = it.copy(end_date = today, room_id = null)
            tenantDao.updateTenant(updatedTenant)
        }

        currentRoom?.let {
            val updatedRoom = it.copy(status = "available", tenantId = null)
            roomDao.updateRoom(updatedRoom)
            Toast.makeText(this, "Đã xóa khách và cập nhật phòng về trạng thái trống.", Toast.LENGTH_LONG).show()
            finish() // Quay lại màn hình trước đó
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
