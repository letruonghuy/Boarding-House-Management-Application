package com.example.qunlphngtr

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.qunlphngtr.adapter.TenantAdapter
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Tenant
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*
import android.text.InputFilter
import android.text.method.DigitsKeyListener

class TenantListActivity : AppCompatActivity() {
    private lateinit var db: TenantDao
    private lateinit var roomDao: RoomDao
    private lateinit var tenantAdapter: TenantAdapter
    private lateinit var tenantList: MutableList<Tenant>
    private lateinit var allTenantList: MutableList<Tenant>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var addTenantLauncher: ActivityResultLauncher<Intent>

    // Biến tạm để giữ ImageView và Uri khi chọn ảnh trong dialog
    private var currentEditImageView: ImageView? = null
    private var currentEditImageUri: String? = null
    private var currentEditCccdFrontUri: String? = null
    private var currentEditCccdBackUri: String? = null

    // Which CCCD/image is being picked currently
    private var currentPickingTarget: Int = 0
    private val PICK_TARGET_MAIN = 1
    private val PICK_TARGET_CCCD_FRONT = 2
    private val PICK_TARGET_CCCD_BACK = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_list)

        try {
            pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    // Lấy quyền truy cập vĩnh viễn
                    try {
                        contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (ex: SecurityException) {
                        ex.printStackTrace()
                    }

                    when (currentPickingTarget) {
                        PICK_TARGET_MAIN -> {
                            currentEditImageUri = it.toString()
                            currentEditImageView?.let { iv ->
                                Glide.with(this).load(it).placeholder(R.drawable.ic_person).into(iv)
                            }
                        }
                        PICK_TARGET_CCCD_FRONT -> {
                            currentEditCccdFrontUri = it.toString()
                            currentEditImageView?.let { iv ->
                                Glide.with(this).load(it).placeholder(R.drawable.ic_person).into(iv)
                            }
                        }
                        PICK_TARGET_CCCD_BACK -> {
                            currentEditCccdBackUri = it.toString()
                            currentEditImageView?.let { iv ->
                                Glide.with(this).load(it).placeholder(R.drawable.ic_person).into(iv)
                            }
                        }
                    }
                }
            }

            addTenantLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let { data ->
                        val name = data.getStringExtra("name") ?: ""
                        if (name.isBlank()) return@let // Không thêm nếu tên trống

                        val newTenant = Tenant(
                            id = 0,
                            name = name,
                            gender = data.getStringExtra("gender"),
                            phone = data.getStringExtra("phone"),
                            identity_number = data.getStringExtra("identity"),
                            imageUri = data.getStringExtra("imageUri"),
                            room_id = data.getIntExtra("roomId", -1).let { if (it == -1) null else it },
                            start_date = data.getStringExtra("startDate"),
                            end_date = null,
                            user_id = null,
                            cccd_front_uri = data.getStringExtra("cccdFront"),
                            cccd_back_uri = data.getStringExtra("cccdBack")
                        )

                        val newId = db.insertTenant(newTenant)
                        if (newId > -1) {
                            val insertedTenant = newTenant.copy(id = newId.toInt())
                            allTenantList.add(insertedTenant)
                            filterList(findViewById<SearchView>(R.id.searchView).query.toString())

                            // Nếu có chọn phòng, cập nhật trạng thái phòng
                            insertedTenant.room_id?.let { roomId ->
                                val room = roomDao.getAllRooms().find { it.id == roomId }
                                room?.let {
                                    roomDao.updateRoom(it.copy(status = "occupied", tenantId = insertedTenant.id))
                                    // Gửi broadcast để các màn hình khác (như Room list) cập nhật
                                    sendBroadcast(Intent("com.example.qunlphngtr.ACTION_ROOMS_UPDATED"))
                                }
                            }
                            Toast.makeText(this, "Đã thêm người thuê mới", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Thêm người thuê thất bại", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            db = TenantDao(this)
            roomDao = RoomDao(this)

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            val searchView = findViewById<SearchView>(R.id.searchView)
            val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTenant)

            allTenantList = db.getAllTenants().toMutableList()
            tenantList = allTenantList.toMutableList()

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
                        val previousRoomId = tenant.room_id
                        if (db.deleteTenant(tenant.id) > 0) {
                            previousRoomId?.let { roomId ->
                                roomDao.getAllRooms().find { it.id == roomId }?.let { room ->
                                    if (room.status != "available") {
                                        roomDao.updateRoom(room.copy(status = "available", tenantId = null))
                                    }
                                }
                            }

                            allTenantList.removeAll { it.id == tenant.id }
                            tenantList.removeAt(pos)
                            tenantAdapter.notifyItemRemoved(pos)

                            sendBroadcast(Intent("com.example.qunlphngtr.ACTION_ROOMS_UPDATED"))
                            Toast.makeText(this, "Đã xóa người thuê", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }

            fabAdd.setOnClickListener {
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

    private fun filterList(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allTenantList.toMutableList()
        } else {
            allTenantList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        (it.phone?.contains(query, ignoreCase = true) == true)
            }.toMutableList()
        }
        tenantAdapter.filterList(filteredList) // Sửa lỗi ở đây
        tenantList = filteredList
    }

    private fun showEditDialog(tenant: Tenant, pos: Int) {
        val layout = layoutInflater.inflate(R.layout.dialog_edit_tenant, null)
        val edtName = layout.findViewById<EditText>(R.id.edtName)
        val spinnerGender = layout.findViewById<Spinner>(R.id.spinnerGender)
        val edtPhone = layout.findViewById<EditText>(R.id.edtPhone)
        val edtIdentity = layout.findViewById<EditText>(R.id.edtIdentity)
        val spinnerRooms = layout.findViewById<Spinner>(R.id.spinnerRooms)
        val edtStartDate = layout.findViewById<EditText>(R.id.edtStartDate)
        val imgTenant = layout.findViewById<ImageView>(R.id.imgTenant)
        val btnChooseImage = layout.findViewById<android.widget.Button>(R.id.btnChooseImage)

        val imgCccdFront = layout.findViewById<ImageView>(R.id.imgCccdFront)
        val imgCccdBack = layout.findViewById<ImageView>(R.id.imgCccdBack)
        val btnChooseCccdFront = layout.findViewById<Button>(R.id.btnChooseCccdFront)
        val btnChooseCccdBack = layout.findViewById<Button>(R.id.btnChooseCccdBack)

        val availableRooms = roomDao.getAllRooms().filter { it.status == "available" || it.id == tenant.room_id }
        val roomDisplayList = mutableListOf("Bỏ chọn phòng")
        roomDisplayList.addAll(availableRooms.map { it.name })

        val roomAdapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomDisplayList)
        roomAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRooms.adapter = roomAdapterSpinner

        tenant.room_id?.let { currentRoomId ->
            val idx = availableRooms.indexOfFirst { it.id == currentRoomId }
            if (idx != -1) spinnerRooms.setSelection(idx + 1)
        }

        edtName.setText(tenant.name)

        // Setup gender spinner values and select current
        val genders = listOf("Nam", "Nữ")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter
        tenant.gender?.let { g ->
            val posg = genders.indexOfFirst { it.equals(g, ignoreCase = true) }
            if (posg >= 0) spinnerGender.setSelection(posg)
        }

        // Phone and identity input constraints
        edtPhone.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        edtPhone.keyListener = DigitsKeyListener.getInstance("0123456789")
        edtPhone.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(10))

        edtIdentity.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        edtIdentity.keyListener = DigitsKeyListener.getInstance("0123456789")
        edtIdentity.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(12))

        edtPhone.setText(tenant.phone)
        edtIdentity.setText(tenant.identity_number ?: "")
        edtStartDate.setText(tenant.start_date ?: "")

        currentEditImageUri = tenant.imageUri
        currentEditCccdFrontUri = tenant.cccd_front_uri
        currentEditCccdBackUri = tenant.cccd_back_uri

        if (!tenant.imageUri.isNullOrEmpty()) {
            Glide.with(this).load(Uri.parse(tenant.imageUri)).placeholder(R.drawable.ic_person).into(imgTenant)
        } else {
            imgTenant.setImageResource(R.drawable.ic_person)
        }

        if (!tenant.cccd_front_uri.isNullOrEmpty()) {
            Glide.with(this).load(Uri.parse(tenant.cccd_front_uri)).placeholder(R.drawable.ic_person).into(imgCccdFront)
        }
        if (!tenant.cccd_back_uri.isNullOrEmpty()) {
            Glide.with(this).load(Uri.parse(tenant.cccd_back_uri)).placeholder(R.drawable.ic_person).into(imgCccdBack)
        }

        edtStartDate.setOnClickListener { showDatePickerDialog(edtStartDate) }
        btnChooseImage.setOnClickListener {
            currentEditImageView = imgTenant
            currentPickingTarget = PICK_TARGET_MAIN
            pickImageLauncher.launch(arrayOf("image/*"))
        }
        btnChooseCccdFront.setOnClickListener {
            currentEditImageView = imgCccdFront
            currentPickingTarget = PICK_TARGET_CCCD_FRONT
            pickImageLauncher.launch(arrayOf("image/*"))
        }
        btnChooseCccdBack.setOnClickListener {
            currentEditImageView = imgCccdBack
            currentPickingTarget = PICK_TARGET_CCCD_BACK
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        AlertDialog.Builder(this)
            .setTitle("Sửa thông tin người thuê")
            .setView(layout)
            .setPositiveButton("Lưu") { _, _ ->
                val previousRoomId = tenant.room_id
                val selectedIdx = spinnerRooms.selectedItemPosition
                val newRoomId = if (selectedIdx > 0) availableRooms[selectedIdx - 1].id else null

                val updatedTenant = tenant.copy(
                    name = edtName.text.toString(),
                    gender = spinnerGender.selectedItem?.toString()?.let { if (it.isBlank()) null else it },
                    phone = edtPhone.text.toString().ifEmpty { null },
                    imageUri = currentEditImageUri,
                    identity_number = edtIdentity.text.toString().ifEmpty { null },
                    room_id = newRoomId,
                    start_date = edtStartDate.text.toString().ifEmpty { null },
                    cccd_front_uri = currentEditCccdFrontUri,
                    cccd_back_uri = currentEditCccdBackUri
                )

                if (db.updateTenant(updatedTenant) > 0) {
                    if (previousRoomId != newRoomId) {
                        previousRoomId?.let {
                            roomDao.getAllRooms().find { it.id == previousRoomId }?.let { room ->
                                roomDao.updateRoom(room.copy(status = "available", tenantId = null))
                            }
                        }
                        newRoomId?.let {
                            availableRooms.find { it.id == newRoomId }?.let { room ->
                                roomDao.updateRoom(room.copy(status = "occupied", tenantId = tenant.id))
                            }
                        }
                    }

                    sendBroadcast(Intent("com.example.qunlphngtr.ACTION_ROOMS_UPDATED"))
                    allTenantList[allTenantList.indexOfFirst { it.id == tenant.id }] = updatedTenant
                    tenantList[pos] = updatedTenant
                    tenantAdapter.notifyItemChanged(pos)
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showDatePickerDialog(editText: EditText) {
        val cal = Calendar.getInstance()
        val dp = DatePickerDialog(this, { _, y, m, d ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            cal.set(y, m, d)
            editText.setText(sdf.format(cal.time))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dp.show()
    }
}
