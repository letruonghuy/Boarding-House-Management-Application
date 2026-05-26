package com.example.qunlphngtr

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Room
import com.example.qunlphngtr.model.Tenant
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*
import com.example.qunlphngtr.R

class AddTenantActivity : AppCompatActivity() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var selectedImageUri: Uri? = null
    private var cccdFrontUri: Uri? = null
    private var cccdBackUri: Uri? = null

    private lateinit var roomDao: RoomDao
    private lateinit var tenantDao: TenantDao

    private var availableRooms: List<Room> = listOf()
    private var passedInRoomId: Int = -1
    private var tenantIdToEdit: Int = -1
    private var isEditMode = false
    private var tenantToEdit: Tenant? = null

    private lateinit var edtName: EditText
    private lateinit var edtGender: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtIdentity: EditText
    private lateinit var edtDeposit: EditText
    private lateinit var edtStartDate: EditText
    private lateinit var spinnerRooms: Spinner
    private lateinit var imgTenant: ImageView
    private lateinit var imgCccdFront: ImageView
    private lateinit var imgCccdBack: ImageView
    private lateinit var btnSave: Button


    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var currentPickingImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tenant)

        initDaos()
        bindViews()
        initImagePicker()

        tenantIdToEdit = intent.getIntExtra("TENANT_ID", -1)
        passedInRoomId = intent.getIntExtra("ROOM_ID", -1)

        isEditMode = tenantIdToEdit != -1

        if (isEditMode) {
            title = "Chỉnh sửa người thuê"
            btnSave.text = "Cập nhật"
            loadTenantForEditing()
        } else {
            title = "Thêm người thuê mới"
            setupDefaultDate()
        }


        setupRoomSpinner()
        setupInputValidation()
        setupClickListeners()
    }

    private fun loadTenantForEditing() {
        tenantToEdit = tenantDao.getTenantById(tenantIdToEdit)
        tenantToEdit?.let { tenant ->
            edtName.setText(tenant.name)
            edtGender.setText(tenant.gender)
            edtPhone.setText(tenant.phone)
            edtIdentity.setText(tenant.identity_number)
            edtStartDate.setText(tenant.start_date)


            // Load images
            tenant.imageUri?.let {
                selectedImageUri = Uri.parse(it)
                Glide.with(this).load(selectedImageUri).placeholder(R.drawable.ic_launcher_background).into(imgTenant)
            }
            tenant.cccd_front_uri?.let {
                cccdFrontUri = Uri.parse(it)
                Glide.with(this).load(cccdFrontUri).placeholder(R.drawable.ic_launcher_background).into(imgCccdFront)
            }
            tenant.cccd_back_uri?.let {
                cccdBackUri = Uri.parse(it)
                Glide.with(this).load(cccdBackUri).placeholder(R.drawable.ic_launcher_background).into(imgCccdBack)
            }

            // In edit mode, the room is determined by the tenant's current room
            passedInRoomId = tenant.room_id ?: -1
        }
    }


    private fun initDaos() {
        roomDao = RoomDao(this)
        tenantDao = TenantDao(this)
    }

    private fun bindViews() {
        edtName = findViewById(R.id.edtName)
        edtGender = findViewById(R.id.edtGender)
        edtPhone = findViewById(R.id.edtPhone)
        edtIdentity = findViewById(R.id.edtIdentity)
        edtDeposit = findViewById(R.id.edtDeposit)
        edtStartDate = findViewById(R.id.edtStartDate)
        spinnerRooms = findViewById(R.id.spinnerRooms)
        imgTenant = findViewById(R.id.imgTenant)
        imgCccdFront = findViewById(R.id.imgCccdFront)
        imgCccdBack = findViewById(R.id.imgCccdBack)
        btnSave = findViewById(R.id.btnSave)

    }

    private fun initImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                        handleImageSelection(uri)
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Không có quyền truy cập ảnh.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleImageSelection(uri: Uri) {
        val targetView = currentPickingImageView
        targetView?.let { view -> Glide.with(this).load(uri).into(view) }

        when (targetView?.id) {
            R.id.imgTenant -> selectedImageUri = uri
            R.id.imgCccdFront -> {
                cccdFrontUri = uri
                if (!isEditMode) processImageForText(uri) // Only run OCR in add mode
            }
            R.id.imgCccdBack -> cccdBackUri = uri
        }
    }

    private fun setupDefaultDate() {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtStartDate.setText(sdf.format(cal.time))
    }

    private fun setupInputValidation() {
        edtPhone.keyListener = DigitsKeyListener.getInstance("0123456789")
        edtPhone.filters = arrayOf(InputFilter.LengthFilter(10))
        edtIdentity.keyListener = DigitsKeyListener.getInstance("0123456789")
        edtIdentity.filters = arrayOf(InputFilter.LengthFilter(12))
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnChooseImage).setOnClickListener { openImagePicker(imgTenant) }
        findViewById<Button>(R.id.btnChooseCccdFront).setOnClickListener { openImagePicker(imgCccdFront) }
        findViewById<Button>(R.id.btnChooseCccdBack).setOnClickListener { openImagePicker(imgCccdBack) }
        edtStartDate.setOnClickListener { showDatePickerDialog(edtStartDate) }
        btnSave.setOnClickListener { saveTenant() }
    }

    private fun openImagePicker(imageView: ImageView) {
        currentPickingImageView = imageView
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun processImageForText(uri: Uri) {
        try {
            val inputImage = InputImage.fromFilePath(this, uri)
            Toast.makeText(this, "Đang phân tích ảnh...", Toast.LENGTH_SHORT).show()
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    Toast.makeText(this, "Phân tích thành công!", Toast.LENGTH_SHORT).show()
                    extractInfoFromText(visionText)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Phân tích ảnh thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractInfoFromText(visionText: Text) {
        val rawText = visionText.text
        var idFound = false
        var nameFound = false
        var genderFound = false

        val cccdPattern = Regex("(id{12})")
        cccdPattern.find(rawText.replace("\\s".toRegex(), ""))?.let {
            edtIdentity.setText(it.value)
            idFound = true
        }

        for (block in visionText.textBlocks) {
            val blockText = block.text.replace("\n", " ")
            Log.d("OCR_BLOCK", blockText)

            if (!nameFound && blockText.matches(Regex("^[A-ZÀ-Ỹ ]{5,}$")) && blockText.split(" ").size in 2..4) {
                edtName.setText(blockText)
                nameFound = true
            }

            if (!genderFound) {
                val lines = mutableListOf<String>()
                for (b in visionText.textBlocks) { for (line in b.lines) { lines.add(line.text.trim()) } }

                for (i in 0 until lines.size) {
                    val combined = (lines.getOrNull(i)?.lowercase() ?: "") + " " + (lines.getOrNull(i + 1)?.lowercase() ?: "")
                    if (combined.contains("giới tính") || combined.contains("sex")) {
                        if (combined.contains("nam")) {
                            edtGender.setText("Nam")
                            genderFound = true
                            break
                        } else if (combined.contains("nữ") || combined.contains("nu")) {
                            edtGender.setText("Nữ")
                            genderFound = true
                            break
                        }
                    }
                }
            }
        }

        val notFound = mutableListOf<String>()
        if (!idFound) notFound.add("Số CCCD")
        if (!nameFound) notFound.add("Tên")
        if (!genderFound) notFound.add("Giới tính")

        if (notFound.isNotEmpty() && notFound.size < 3) {
            Toast.makeText(this, "Không thể tự động điền: ${notFound.joinToString()}. Vui lòng nhập tay.", Toast.LENGTH_LONG).show()
        } else if (notFound.size == 3) {
            Toast.makeText(this, "Không thể trích xuất thông tin từ ảnh này.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRoomSpinner() {
        // Case 1: A specific room is passed in (from RoomDashboard or similar) OR in edit mode.
        // The spinner should show only this room and be disabled.
        if (passedInRoomId != -1) {
            val room = roomDao.getRoomById(passedInRoomId)
            room?.let {
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(it.name))
                spinnerRooms.adapter = adapter
                spinnerRooms.isEnabled = false // User cannot change the room
            }
        } else {
            // Case 2: Adding a new tenant from the general tenant list.
            // Show a list of available rooms for the user to choose from.
            availableRooms = roomDao.getAllRooms().filter { it.status == "available" }
            val roomNames = listOf("Chọn phòng") + availableRooms.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomNames)
            spinnerRooms.adapter = adapter
            spinnerRooms.isEnabled = true
        }
    }

    private fun saveTenant() {
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val identity = edtIdentity.text.toString().trim()
        val gender = edtGender.text.toString().trim()
        val startDate = edtStartDate.text.toString().trim()

        if (name.isEmpty() || identity.isEmpty() || startDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ Tên, Số CCCD và Ngày bắt đầu", Toast.LENGTH_SHORT).show()
            return
        }

        if (phone.isNotEmpty() && !phone.matches(Regex("^\\d{10}$"))) {
            Toast.makeText(this, "Số điện thoại phải đúng 10 chữ số", Toast.LENGTH_SHORT).show()
            return
        }
        if (!identity.matches(Regex("^\\d{12}$"))) {
            Toast.makeText(this, "CCCD/CMND phải là 12 chữ số", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRoomId: Int?
        if (passedInRoomId != -1) {
            selectedRoomId = passedInRoomId
        } else {
            val selectedPos = spinnerRooms.selectedItemPosition
            if (selectedPos > 0) {
                selectedRoomId = availableRooms[selectedPos - 1].id
            } else {
                if (!isEditMode) { // In add mode, a room must be selected
                    Toast.makeText(this, "Vui lòng chọn phòng cho người thuê", Toast.LENGTH_SHORT).show()
                    return
                } else { // In edit mode, the tenant might not have a room
                    selectedRoomId = null
                }
            }
        }

        if (isEditMode) {
            val updatedTenant = tenantToEdit!!.copy(
                name = name,
                gender = gender,
                phone = phone,
                identity_number = identity,
                start_date = startDate,
                imageUri = selectedImageUri?.toString(),
                cccd_front_uri = cccdFrontUri?.toString(),
                cccd_back_uri = cccdBackUri?.toString(),
                room_id = selectedRoomId // Update room_id as well
            )

            val rowsAffected = tenantDao.updateTenant(updatedTenant)
            if (rowsAffected > 0) {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
            }

        } else {
             if (selectedRoomId == null) {
                Toast.makeText(this, "Lỗi: Không có phòng nào được chọn.", Toast.LENGTH_SHORT).show()
                return
            }

            val newTenant = Tenant(
                id = 0, name = name, gender = gender, phone = phone,
                imageUri = selectedImageUri?.toString(),
                identity_number = identity, room_id = selectedRoomId,
                start_date = startDate,
                end_date = null, user_id = null,
                cccd_front_uri = cccdFrontUri?.toString(),
                cccd_back_uri = cccdBackUri?.toString()
            )

            val newTenantId = tenantDao.insertTenant(newTenant)

            if (newTenantId > 0) {
                val roomToUpdate = roomDao.getRoomById(selectedRoomId)
                roomToUpdate?.let {
                    val updatedRoom = it.copy(status = "occupied", tenantId = newTenantId.toInt())
                    roomDao.updateRoom(updatedRoom)
                }
                Toast.makeText(this, "Thêm khách thuê thành công!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Thêm khách thuê thất bại! CCCD có thể đã tồn tại.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val cal = Calendar.getInstance()
        val currentText = editText.text.toString()
        if (currentText.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                cal.time = sdf.parse(currentText)!!
            } catch (e: Exception) { /* Keep cal at current date if parsing fails */ }
        }

        DatePickerDialog(
            this, { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                editText.setText(sdf.format(cal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
