package com.example.qunlphngtr

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Tenant
import java.text.SimpleDateFormat
import java.util.*

class AssignTenantActivity : AppCompatActivity() {

    private lateinit var spinnerTenants: Spinner
    private lateinit var spinnerRooms: Spinner
    private lateinit var edtIdentity: EditText
    private lateinit var edtStartDate: EditText
    private lateinit var btnSave: Button

    private lateinit var tenantDao: TenantDao
    private lateinit var roomDao: RoomDao

    private var tenantList: List<Tenant> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign_tenant)

        tenantDao = TenantDao(this)
        roomDao = RoomDao(this)

        spinnerTenants = findViewById(R.id.spinnerTenants)
        spinnerRooms = findViewById(R.id.spinnerRooms)
        edtIdentity = findViewById(R.id.edtIdentity)
        edtStartDate = findViewById(R.id.edtStartDate)
        btnSave = findViewById(R.id.btnSaveAssign)

        loadData()

        edtStartDate.setOnClickListener {
            showDatePicker()
        }

        btnSave.setOnClickListener {
            saveAssignment()
        }
    }

    private fun loadData() {
        // Load tenants (those without room assignment)
        tenantList = tenantDao.getAllTenants().filter { it.room_id == null }
        val tenantNames = tenantList.map { it.name }.toMutableList()
        if (tenantNames.isEmpty()) {
            tenantNames.add("(Không có người thuê chưa gán)")
        }
        val tenantAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tenantNames)
        tenantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTenants.adapter = tenantAdapter

        // Load rooms
        val rooms = roomDao.getAllRooms()
        val roomNames = rooms.map { it.name }.toMutableList()
        if (roomNames.isEmpty()) roomNames.add("(Chưa có phòng)")
        val roomAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomNames)
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRooms.adapter = roomAdapter
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            cal.set(y, m, d)
            edtStartDate.setText(sdf.format(cal.time))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveAssignment() {
        val tenantPos = spinnerTenants.selectedItemPosition
        if (tenantList.isEmpty() || tenantPos < 0 || tenantPos >= tenantList.size) {
            Toast.makeText(this, "Vui lòng chọn người thuê hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        val tenant = tenantList[tenantPos]

        val roomPos = spinnerRooms.selectedItemPosition
        val rooms = roomDao.getAllRooms()
        if (rooms.isEmpty() || roomPos < 0 || roomPos >= rooms.size) {
            Toast.makeText(this, "Vui lòng chọn phòng hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        val room = rooms[roomPos]

        val identity = edtIdentity.text.toString().trim()
        val startDate = edtStartDate.text.toString().trim()
        if (identity.isEmpty() || startDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        // Update tenant's room_id and identity_number and start_date
        val updatedTenant = tenant.copy(room_id = room.id, identity_number = identity, start_date = startDate)
        val tenantDao = TenantDao(this)
        val rows = tenantDao.updateTenant(updatedTenant)
        if (rows > 0) {
            // Update room status to occupied
            roomDao.updateRoom(room.copy(status = "occupied", tenantId = tenant.id))
            Toast.makeText(this, "Gán người thuê thành công", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}