package com.example.qunlphngtr

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.qunlphngtr.adapter.TenantAdapter
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.model.Tenant // <-- Đảm bảo import model 10-trường
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TenantListActivity : AppCompatActivity() {

    private lateinit var tenantAdapter: TenantAdapter
    private lateinit var allTenantList: MutableList<Tenant>
    private lateinit var tenantList: MutableList<Tenant>
    private lateinit var db: TenantDao
    private lateinit var roomDao: RoomDao
    private lateinit var addTenantLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Array<String>>
    private var currentEditImageView: ImageView? = null
    private var currentEditImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_list)

        try {
            pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    currentEditImageUri = it.toString()
                    // Use Glide to render the content URI (handles content:// safely)
                    currentEditImageView?.let { iv ->
                        Glide.with(this).load(it).placeholder(R.drawable.ic_person).into(iv)
                    }
                    try {
                        contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }

            db = TenantDao(this)
            roomDao = RoomDao(this)

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            val searchView = findViewById<SearchView>(R.id.searchView)
            val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTenant)

            addTenantLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    data?.let {
                        val name = it.getStringExtra("name") ?: ""
                        val gender = it.getStringExtra("gender") ?: ""
                        val phone = it.getStringExtra("phone") ?: ""
                        val imageUri = it.getStringExtra("imageUri")

                        // --- SỬA LỖI 1: Dùng model 10-trường ---
                        // Khi "Admin" thêm, ta chưa có các thông tin khác
                        // Tạm thời để là null.
                        val tenantToInsert = Tenant(
                            id = 0, // Sẽ tự tăng
                            name = name,
                            gender = gender,
                            phone = phone,
                            imageUri = imageUri,
                            identity_number = null, // Sẽ sửa sau
                            room_id = null,         // Sẽ sửa sau
                            start_date = null,
                            end_date = null,
                            user_id = null          // Sẽ sửa sau
                        )

                        val newIdLong = db.insertTenant(tenantToInsert)
                        val newId = newIdLong.toInt()

                        // Tạo object mới với ID đúng
                        val newTenant = tenantToInsert.copy(id = newId)

                        allTenantList.add(newTenant)
                        tenantList.add(newTenant)
                        tenantAdapter.notifyItemInserted(tenantList.size - 1)
                    }
                }
            }

            allTenantList = db.getAllTenants().toMutableList()
            tenantList = allTenantList.toMutableList()

            // LƯU Ý: File TenantAdapter của bạn cũng có thể bị lỗi
            // vì nó có thể đang dùng model 5-trường.
            tenantAdapter = TenantAdapter(tenantList)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = tenantAdapter

            tenantAdapter.onItemClick = { tenant, pos ->
                showEditDialog(tenant, pos)
            }

            tenantAdapter.onItemLongClick = { tenant, pos ->
                AlertDialog.Builder(this)
                    .setTitle("Xóa người thuê")
                    .setMessage("Bạn có chắc muốn xóa ${tenant.name}?")
                    .setPositiveButton("Xóa") { _, _ ->
                        // Before deleting, free the room if assigned
                        val previousRoomId = tenant.room_id
                        val deleteResult = db.deleteTenant(tenant.id)
                        if (deleteResult > 0) {
                            if (previousRoomId != null) {
                                val room = roomDao.getAllRooms().find { it.id == previousRoomId }
                                room?.let { r ->
                                    if (r.status != "available") {
                                        roomDao.updateRoom(r.copy(status = "available"))
                                    }
                                }
                            }

                            val fullIndex = allTenantList.indexOfFirst { it.id == tenant.id }
                            if (fullIndex != -1) allTenantList.removeAt(fullIndex)
                            tenantList.removeAt(pos)
                            tenantAdapter.notifyItemRemoved(pos)

                            // Notify room list to refresh
                            try {
                                val intent = Intent("com.example.qunlphngtr.ACTION_ROOMS_UPDATED")
                                sendBroadcast(intent)
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }

            fabAdd.setOnClickListener {
                // LƯU Ý: AddTenantActivity của bạn cũng cần được sửa
                // để gửi về đủ 10 trường
                val intent = Intent(this, AddTenantActivity::class.java)
                addTenantLauncher.launch(intent)
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    filterList(query)
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    filterList(newText)
                    return true
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khởi tạo màn hình: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showEditDialog(tenant: Tenant, pos: Int) {
        val layout = layoutInflater.inflate(R.layout.dialog_edit_tenant, null)
        val edtName = layout.findViewById<EditText>(R.id.edtName)
        val edtGender = layout.findViewById<EditText>(R.id.edtGender)
        val edtPhone = layout.findViewById<EditText>(R.id.edtPhone)
        val edtIdentity = layout.findViewById<EditText?>(R.id.edtIdentity)
        val spinnerRooms = layout.findViewById<Spinner?>(R.id.spinnerRooms)
        val edtStartDate = layout.findViewById<EditText?>(R.id.edtStartDate)
         val imgTenant = layout.findViewById<ImageView>(R.id.imgTenant)
         val btnChooseImage = layout.findViewById<android.widget.Button>(R.id.btnChooseImage)

        // Populate rooms spinner (guard if spinner not found)
        val rooms = roomDao.getAllRooms()
        if (spinnerRooms != null) {
            val roomNames = rooms.map { it.name + if (it.status == "available") " (Available)" else " (Occupied)" }
            val roomAdapterSpinner = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, roomNames)
            roomAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRooms.adapter = roomAdapterSpinner

            // Preselect current room if exists
            if (tenant.room_id != null) {
                val idx = rooms.indexOfFirst { it.id == tenant.room_id }
                if (idx >= 0) spinnerRooms.setSelection(idx)
            }
        }

        // Date picker for start date
        edtStartDate.setOnClickListener {
            val cal = java.util.Calendar.getInstance()
            val dp = android.app.DatePickerDialog(this, { _, y, m, d ->
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                cal.set(y, m, d)
                edtStartDate.setText(sdf.format(cal.time))
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH))
            dp.show()
        }

        edtName.setText(tenant.name)
        edtGender.setText(tenant.gender)
        edtPhone.setText(tenant.phone)
        edtIdentity?.setText(tenant.identity_number ?: "")
        edtStartDate?.setText(tenant.start_date ?: "")

        if (!tenant.imageUri.isNullOrEmpty()) {
            Glide.with(this).load(Uri.parse(tenant.imageUri)).placeholder(R.drawable.ic_person).into(imgTenant)
            currentEditImageUri = tenant.imageUri
        } else {
            imgTenant.setImageResource(R.drawable.ic_person)
            currentEditImageUri = null
        }

        btnChooseImage.setOnClickListener {
            currentEditImageView = imgTenant
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        AlertDialog.Builder(this)
            .setTitle("Sửa thông tin người thuê")
            .setView(layout)
            .setPositiveButton("Lưu") { _, _ ->

                // --- SỬA LỖI 2: Dùng .copy() để cập nhật ---
                // Cách này sẽ giữ lại các trường cũ (room_id, user_id...)
                // mà dialog này không sửa.
                // Build updated tenant with identity and start date and possibly new room assignment
                val selectedRoomIndex = spinnerRooms?.selectedItemPosition ?: -1
                val selectedRoomId = if (selectedRoomIndex >= 0 && selectedRoomIndex < rooms.size) rooms[selectedRoomIndex].id else null

                val previousRoomId = tenant.room_id

                val updatedTenant = tenant.copy(
                    name = edtName.text.toString(),
                    gender = edtGender.text.toString(),
                    phone = edtPhone.text.toString(),
                    imageUri = currentEditImageUri,
                    identity_number = edtIdentity.text.toString().ifEmpty { null },
                    room_id = selectedRoomId,
                    start_date = edtStartDate.text.toString().ifEmpty { null }
                )

                val result = db.updateTenant(updatedTenant)
                if (result > 0) {
                    // If assigned to a room, mark that room as occupied
                    if (selectedRoomId != null && selectedRoomId != previousRoomId) {
                        val room = rooms.find { it.id == selectedRoomId }
                        room?.let { r ->
                            if (r.status != "occupied") {
                                roomDao.updateRoom(r.copy(status = "occupied"))
                            }
                        }
                    }

                    // If previous room existed and is different from newly selected, free it
                    if (previousRoomId != null && previousRoomId != selectedRoomId) {
                        val prevRoom = rooms.find { it.id == previousRoomId }
                        prevRoom?.let { pr ->
                            if (pr.status != "available") {
                                roomDao.updateRoom(pr.copy(status = "available"))
                            }
                        }
                    }

                    // Notify other screens that rooms have been updated
                    try {
                        val intent = Intent("com.example.qunlphngtr.ACTION_ROOMS_UPDATED")
                        sendBroadcast(intent)
                    } catch (e: Exception) { e.printStackTrace() }

                    val fullIndex = allTenantList.indexOfFirst { it.id == tenant.id }
                    if (fullIndex != -1) allTenantList[fullIndex] = updatedTenant
                    tenantList[pos] = updatedTenant
                    tenantAdapter.notifyItemChanged(pos)

                    currentEditImageView = null
                    currentEditImageUri = null
                } else {
                    Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }
             }
             .setNegativeButton("Hủy", null)
             .show()
    }

    private fun filterList(query: String?) {
        val q = query?.trim().orEmpty()
        val filtered = if (q.isEmpty()) allTenantList
        else allTenantList.filter {
            it.name.contains(q, ignoreCase = true) || (it.phone?.contains(q, ignoreCase = true) ?: false)
        }
        tenantList.clear()
        tenantList.addAll(filtered)
        tenantAdapter.notifyDataSetChanged()
    }
}