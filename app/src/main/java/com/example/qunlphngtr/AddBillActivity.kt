package com.example.qunlphngtr

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.model.Room
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.*

class AddBillActivity : AppCompatActivity() {

    // --- Views ---
    private lateinit var tvRoomNumber: TextView
    private lateinit var etDate: TextInputEditText
    private lateinit var etOldElectric: TextInputEditText
    private lateinit var etOldWater: TextInputEditText
    private lateinit var etNewElectric: TextInputEditText
    private lateinit var etNewWater: TextInputEditText
    private lateinit var etRoomPrice: TextInputEditText
    private lateinit var etNote: TextInputEditText

    // --- Image Views ---
    private lateinit var ivNewElectric: ImageView
    private lateinit var btnAddNewElectricImage: ImageButton
    private lateinit var ivNewWater: ImageView
    private lateinit var btnAddNewWaterImage: ImageButton


    // Summary Table Views
    private lateinit var tvElectricQuantity: TextView
    private lateinit var tvElectricPrice: TextView
    private lateinit var tvElectricTotal: TextView
    private lateinit var tvWaterQuantity: TextView
    private lateinit var tvWaterPrice: TextView
    private lateinit var tvWaterTotal: TextView
    private lateinit var tvRoomPriceDisplay: TextView
    private lateinit var tvRoomTotal: TextView
    private lateinit var tvGrandTotal: TextView

    private lateinit var btnApproveInvoice: MaterialButton

    // --- Data ---
    private var selectedRoom: Room? = null
    private lateinit var billDao: BillDao
    private lateinit var roomDao: RoomDao
    private var selectedDateString = ""
    private var electricImageUri: Uri? = null
    private var waterImageUri: Uri? = null

    // --- State Management for Image Picking ---
    private enum class ImageType { ELECTRIC, WATER }
    private var currentImageType: ImageType = ImageType.ELECTRIC

    // --- Calculated Values ---
    private var elecUsage: Int = 0
    private var waterUsage: Int = 0
    private var elecTotal: Double = 0.0
    private var waterTotal: Double = 0.0
    private var roomFee: Double = 0.0
    private var grandTotal: Double = 0.0

    // Hardcoded prices
    private val ELECTRICITY_PRICE = 4000.0
    private val WATER_PRICE = 100000.0

    private val locale = Locale("vi", "VN")
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)


    // --- LAUNCHERS ---

    // Launcher for picking an image from the gallery
    private val pickImageLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                when (currentImageType) {
                    ImageType.ELECTRIC -> {
                        electricImageUri = it
                        ivNewElectric.setImageURI(it)
                        ivNewElectric.visibility = View.VISIBLE
                        btnAddNewElectricImage.visibility = View.GONE
                    }
                    ImageType.WATER -> {
                        waterImageUri = it
                        ivNewWater.setImageURI(it)
                        ivNewWater.visibility = View.VISIBLE
                        btnAddNewWaterImage.visibility = View.GONE
                    }
                }
            }
        }

    // Launcher for requesting a permission from the user
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickImageLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Quyền truy cập thư viện ảnh đã bị từ chối.", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bill)

        billDao = BillDao(this)
        roomDao = RoomDao(this)

        val roomId = intent.getIntExtra("ROOM_ID", -1)
        if (roomId == -1) {
            Toast.makeText(this, "Lỗi: Không có thông tin phòng!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        selectedRoom = roomDao.getAllRooms().find { it.id == roomId }
        if (selectedRoom == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy phòng!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        setupStaticData()
        setupListeners()
        calculateTotals()
    }

    private fun bindViews() {
        tvRoomNumber = findViewById(R.id.tvRoomNumber)
        etDate = findViewById(R.id.etDate)
        etOldElectric = findViewById(R.id.etOldElectric)
        etOldWater = findViewById(R.id.etOldWater)
        etNewElectric = findViewById(R.id.etNewElectric)
        etNewWater = findViewById(R.id.etNewWater)
        etRoomPrice = findViewById(R.id.etRoomPrice)
        etNote = findViewById(R.id.etNote)

        ivNewElectric = findViewById(R.id.ivNewElectric)
        btnAddNewElectricImage = findViewById(R.id.btnAddNewElectricImage)
        ivNewWater = findViewById(R.id.ivNewWater) // Assuming this ID exists in your XML
        btnAddNewWaterImage = findViewById(R.id.btnAddNewWaterImage) // Assuming this ID exists

        tvElectricQuantity = findViewById(R.id.tvElectricQuantity)
        tvElectricPrice = findViewById(R.id.tvElectricPrice)
        tvElectricTotal = findViewById(R.id.tvElectricTotal)
        tvWaterQuantity = findViewById(R.id.tvWaterQuantity)
        tvWaterPrice = findViewById(R.id.tvWaterPrice)
        tvWaterTotal = findViewById(R.id.tvWaterTotal)
        tvRoomPriceDisplay = findViewById(R.id.tvRoomPriceDisplay)
        tvRoomTotal = findViewById(R.id.tvRoomTotal)
        tvGrandTotal = findViewById(R.id.tvGrandTotal)
        btnApproveInvoice = findViewById(R.id.btnApproveInvoice)
    }

    private fun setupStaticData() {
        selectedRoom?.let {
            tvRoomNumber.text = it.name
            etRoomPrice.setText(it.price.toString())
        }
        tvElectricPrice.text = formatCurrency(ELECTRICITY_PRICE)
        tvWaterPrice.text = formatCurrency(WATER_PRICE)
    }

    private fun setupListeners() {
        etDate.setOnClickListener { showDatePickerDialog() }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateTotals()
            }
        }

        etOldElectric.addTextChangedListener(textWatcher)
        etNewElectric.addTextChangedListener(textWatcher)
        etOldWater.addTextChangedListener(textWatcher)
        etNewWater.addTextChangedListener(textWatcher)
        etRoomPrice.addTextChangedListener(textWatcher)

        btnApproveInvoice.setOnClickListener { saveBill() }

        btnAddNewElectricImage.setOnClickListener {
            currentImageType = ImageType.ELECTRIC
            openGallery()
        }

        btnAddNewWaterImage.setOnClickListener {
            currentImageType = ImageType.WATER
            openGallery()
        }
    }

    private fun openGallery() {
        val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, requiredPermission) == PackageManager.PERMISSION_GRANTED) {
            pickImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(requiredPermission)
        }
    }


    private fun calculateTotals() {
        val oldElec = etOldElectric.text.toString().toIntOrNull() ?: 0
        val newElec = etNewElectric.text.toString().toIntOrNull() ?: 0
        val oldWater = etOldWater.text.toString().toIntOrNull() ?: 0
        val newWater = etNewWater.text.toString().toIntOrNull() ?: 0

        elecUsage = if (newElec > oldElec) newElec - oldElec else 0
        waterUsage = if (newWater > oldWater) newWater - oldWater else 0

        elecTotal = elecUsage * ELECTRICITY_PRICE
        waterTotal = waterUsage * WATER_PRICE
        roomFee = etRoomPrice.text.toString().toDoubleOrNull() ?: 0.0

        grandTotal = elecTotal + waterTotal + roomFee

        tvElectricQuantity.text = elecUsage.toString()
        tvWaterQuantity.text = waterUsage.toString()
        tvElectricTotal.text = formatCurrency(elecTotal)
        tvWaterTotal.text = formatCurrency(waterTotal)
        tvRoomPriceDisplay.text = formatCurrency(roomFee)
        tvRoomTotal.text = formatCurrency(roomFee)
        tvGrandTotal.text = formatCurrency(grandTotal)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val formattedDate = "$dayOfMonth/${month + 1}/$year"
                etDate.setText(formattedDate)
                selectedDateString = String.format("%02d/%d", month + 1, year)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveBill() {
        if (selectedDateString.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày cho hóa đơn!", Toast.LENGTH_SHORT).show()
            return
        }

        selectedRoom?.let { room ->
            val tenantId = room.tenantId
            if (tenantId == null || tenantId <= 0) {
                Toast.makeText(this, "Không thể tạo hóa đơn cho phòng trống.", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                val result = billDao.insertBill(
                    month = selectedDateString,
                    electric = elecTotal,
                    water = waterTotal,
                    roomFee = roomFee,
                    internet = 0.0,
                    tenantId = tenantId,
                    roomId = room.id
                )

                if (result > 0) {
                    Toast.makeText(this, "Tạo hóa đơn thành công!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Tạo hóa đơn thất bại! Có thể hóa đơn đã tồn tại cho tháng này.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Tạo hóa đơn thất bại! Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount).replace("₫", "").trim()
    }
}
