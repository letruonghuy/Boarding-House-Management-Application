package com.example.qunlphngtr

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.model.Room
import java.text.SimpleDateFormat
import java.util.*
import android.text.InputFilter
import android.text.method.DigitsKeyListener

class AddTenantActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private var cccdFrontUri: Uri? = null
    private var cccdBackUri: Uri? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var roomDao: RoomDao
    private var availableRooms: List<Room> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tenant)

        roomDao = RoomDao(this)

        val imgTenant = findViewById<ImageView>(R.id.imgTenant)
        val edtName = findViewById<EditText>(R.id.edtName)
        val spinnerGender = findViewById<Spinner>(R.id.spinnerGender)
        val edtPhone = findViewById<EditText>(R.id.edtPhone)
        val edtIdentity = findViewById<EditText>(R.id.edtIdentity)
        val edtStartDate = findViewById<EditText>(R.id.edtStartDate)
        val spinnerRooms = findViewById<Spinner>(R.id.spinnerRooms)
        val btnChooseImage = findViewById<Button>(R.id.btnChooseImage)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val imgCccdFront = findViewById<ImageView>(R.id.imgCccdFront)
        val imgCccdBack = findViewById<ImageView>(R.id.imgCccdBack)
        val btnChooseCccdFront = findViewById<Button>(R.id.btnChooseCccdFront)
        val btnChooseCccdBack = findViewById<Button>(R.id.btnChooseCccdBack)

        // Lấy danh sách các phòng còn trống
        availableRooms = roomDao.getAllRooms().filter { it.status == "available" }
        val roomDisplayList = mutableListOf("Chưa chọn phòng") // Mục đầu tiên
        roomDisplayList.addAll(availableRooms.map { it.name })

        val roomAdapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomDisplayList)
        roomAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRooms.adapter = roomAdapterSpinner

        // Gender spinner
        val genders = listOf("Nam", "Nữ")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        // Input constraints: phone digits only and max length 10, identity digits only max length 12
        edtPhone.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        edtPhone.keyListener = DigitsKeyListener.getInstance("0123456789")
        edtPhone.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(10))

        edtIdentity.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        edtIdentity.keyListener = DigitsKeyListener.getInstance("0123456789")
        edtIdentity.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(12))

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                try {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (currentPicking == PICK_MAIN_IMAGE) {
                        selectedImageUri = it
                        Glide.with(this).load(it).placeholder(R.drawable.ic_person).into(imgTenant)
                    } else if (currentPicking == PICK_CCCD_FRONT) {
                        cccdFrontUri = it
                        Glide.with(this).load(it).placeholder(R.drawable.ic_person).into(imgCccdFront)
                    } else if (currentPicking == PICK_CCCD_BACK) {
                        cccdBackUri = it
                        Glide.with(this).load(it).placeholder(R.drawable.ic_person).into(imgCccdBack)
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Không thể lấy quyền truy cập lâu dài cho ảnh.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        var tempSelectedRoomId = -1

        btnChooseImage.setOnClickListener {
            currentPicking = PICK_MAIN_IMAGE
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        btnChooseCccdFront.setOnClickListener {
            currentPicking = PICK_CCCD_FRONT
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        btnChooseCccdBack.setOnClickListener {
            currentPicking = PICK_CCCD_BACK
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        edtStartDate.setOnClickListener {
            showDatePickerDialog(edtStartDate)
        }

        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            val gender = spinnerGender.selectedItem?.toString()
            val phone = edtPhone.text.toString().trim()
            val identity = edtIdentity.text.toString().trim()
            val startDate = edtStartDate.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate phone
            if (phone.isNotEmpty() && !phone.matches(Regex("^\\d{10}"))) {
                Toast.makeText(this, "Số điện thoại phải đúng 10 chữ số", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate identity (allow empty or up to 12 digits)
            if (identity.isNotEmpty() && !identity.matches(Regex("^\\d{1,12}"))) {
                Toast.makeText(this, "CCCD/CMND chỉ chữ số và tối đa 12 chữ số", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRoomPosition = spinnerRooms.selectedItemPosition
            val selectedRoomId = if (selectedRoomPosition > 0) {
                availableRooms[selectedRoomPosition - 1].id
            } else {
                -1 // -1 để báo hiệu không chọn phòng
            }

            val result = Intent().apply {
                putExtra("name", name)
                putExtra("gender", gender)
                putExtra("phone", phone.ifEmpty { null })
                putExtra("identity", identity.ifEmpty { null })
                putExtra("startDate", startDate.ifEmpty { null })
                putExtra("roomId", selectedRoomId)
                selectedImageUri?.toString()?.let { putExtra("imageUri", it) }
                cccdFrontUri?.toString()?.let { putExtra("cccdFront", it) }
                cccdBackUri?.toString()?.let { putExtra("cccdBack", it) }
            }
            setResult(RESULT_OK, result)
            finish()
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            editText.setText(sdf.format(cal.time))
        }
        DatePickerDialog(
            this,
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    companion object {
        private var currentPicking = 0
        private const val PICK_MAIN_IMAGE = 1
        private const val PICK_CCCD_FRONT = 2
        private const val PICK_CCCD_BACK = 3
    }
}
