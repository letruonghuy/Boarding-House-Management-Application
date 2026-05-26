package com.example.qunlphngtr

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.NotificationDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Bill
import com.example.qunlphngtr.model.Room
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.text.SimpleDateFormat
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
    private lateinit var etInternetPrice: TextInputEditText
    private lateinit var etNote: TextInputEditText

    private lateinit var ivOldElectric: ImageView
    private lateinit var ivNewElectric: ImageView
    private lateinit var ivOldWater: ImageView
    private lateinit var ivNewWater: ImageView
    private lateinit var btnAddOldElectricImage: ImageButton
    private lateinit var btnAddNewElectricImage: ImageButton
    private lateinit var btnAddOldWaterImage: ImageButton
    private lateinit var btnAddNewWaterImage: ImageButton

    // Calculation Table Views
    private lateinit var tvElectricQuantity: TextView
    private lateinit var tvElectricUnitPrice: TextView
    private lateinit var tvElectricTotal: TextView
    private lateinit var tvWaterQuantity: TextView
    private lateinit var tvWaterUnitPrice: TextView
    private lateinit var tvWaterTotal: TextView
    private lateinit var tvRoomUnitPrice: TextView
    private lateinit var tvRoomTotal: TextView
    private lateinit var tvInternetUnitPrice: TextView
    private lateinit var tvInternetTotal: TextView
    private lateinit var tvGrandTotal: TextView

    private lateinit var btnApproveInvoice: MaterialButton

    // --- Data ---
    private var selectedRoom: Room? = null
    private lateinit var billDao: BillDao
    private lateinit var roomDao: RoomDao
    private lateinit var tenantDao: TenantDao
    private lateinit var notificationDao: NotificationDao
    private var selectedDateString = ""

    private var oldElectricImageUri: Uri? = null
    private var newElectricImageUri: Uri? = null
    private var oldWaterImageUri: Uri? = null
    private var newWaterImageUri: Uri? = null
    private var currentImagePicker: ImageView? = null

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private val ELECTRICITY_PRICE_PER_UNIT = 4000.0
    private val WATER_PRICE_PER_UNIT = 20000.0
    private val INTERNET_PRICE = 100000.0
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bill)

        initDaos()
        initImagePicker()

        val roomId = intent.getIntExtra("ROOM_ID", -1)
        selectedRoom = roomDao.getRoomById(roomId)

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

    private fun initDaos() {
        billDao = BillDao(this)
        roomDao = RoomDao(this)
        tenantDao = TenantDao(this)
        notificationDao = NotificationDao(this)
    }

    private fun initImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    try {
                        contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        when (currentImagePicker?.id) {
                            R.id.ivOldElectric -> oldElectricImageUri = it
                            R.id.ivNewElectric -> newElectricImageUri = it
                            R.id.ivOldWater -> oldWaterImageUri = it
                            R.id.ivNewWater -> newWaterImageUri = it
                        }
                        currentImagePicker?.let { imageView -> Glide.with(this).load(it).into(imageView) }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Không thể lấy quyền truy cập ảnh", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun bindViews() {
        tvRoomNumber = findViewById(R.id.tvRoomNumber)
        etDate = findViewById(R.id.etDate)
        etOldElectric = findViewById(R.id.etOldElectric)
        etOldWater = findViewById(R.id.etOldWater)
        etNewElectric = findViewById(R.id.etNewElectric)
        etNewWater = findViewById(R.id.etNewWater)
        etRoomPrice = findViewById(R.id.etRoomPrice)
        etInternetPrice = findViewById(R.id.etInternetPrice)
        etNote = findViewById(R.id.etNote)

        ivOldElectric = findViewById(R.id.ivOldElectric)
        ivNewElectric = findViewById(R.id.ivNewElectric)
        ivOldWater = findViewById(R.id.ivOldWater)
        ivNewWater = findViewById(R.id.ivNewWater)
        btnAddOldElectricImage = findViewById(R.id.btnAddOldElectricImage)
        btnAddNewElectricImage = findViewById(R.id.btnAddNewElectricImage)
        btnAddOldWaterImage = findViewById(R.id.btnAddOldWaterImage)
        btnAddNewWaterImage = findViewById(R.id.btnAddNewWaterImage)

        // Calculation Table
        tvElectricQuantity = findViewById(R.id.tvElectricQuantity)
        tvElectricUnitPrice = findViewById(R.id.tvElectricUnitPrice)
        tvElectricTotal = findViewById(R.id.tvElectricTotal)
        tvWaterQuantity = findViewById(R.id.tvWaterQuantity)
        tvWaterUnitPrice = findViewById(R.id.tvWaterUnitPrice)
        tvWaterTotal = findViewById(R.id.tvWaterTotal)
        tvRoomUnitPrice = findViewById(R.id.tvRoomUnitPrice)
        tvRoomTotal = findViewById(R.id.tvRoomTotal)
        tvInternetUnitPrice = findViewById(R.id.tvInternetUnitPrice)
        tvInternetTotal = findViewById(R.id.tvInternetTotal)
        tvGrandTotal = findViewById(R.id.tvGrandTotal)

        btnApproveInvoice = findViewById(R.id.btnApproveInvoice)
    }

    private fun setupStaticData() {
        tvRoomNumber.text = "Tạo hóa đơn cho ${selectedRoom?.name}"

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etDate.setText(dateFormat.format(calendar.time))
        selectedDateString = String.format(Locale.getDefault(), "%02d/%d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))

        val lastBill = selectedRoom?.let { billDao.getBillsByRoomId(it.id).maxByOrNull { bill -> bill.id } }
        if (lastBill != null) {
            etOldElectric.setText(lastBill.newElectricReading.toString())
            etOldWater.setText(lastBill.newWaterReading.toString())
            etOldElectric.isEnabled = false
            etOldWater.isEnabled = false
        } else {
            etOldElectric.isEnabled = true
            etOldWater.isEnabled = true
        }
    }

    private fun setupListeners() {
        etDate.setOnClickListener { showDatePickerDialog() }
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calculateTotals() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etNewElectric.addTextChangedListener(textWatcher)
        etNewWater.addTextChangedListener(textWatcher)
        etRoomPrice.addTextChangedListener(textWatcher)
        etInternetPrice.addTextChangedListener(textWatcher)

        btnAddOldElectricImage.setOnClickListener { openImagePicker(ivOldElectric) }
        btnAddNewElectricImage.setOnClickListener { openImagePicker(ivNewElectric) }
        btnAddOldWaterImage.setOnClickListener { openImagePicker(ivOldWater) }
        btnAddNewWaterImage.setOnClickListener { openImagePicker(ivNewWater) }

        btnApproveInvoice.setOnClickListener { saveBill() }
    }

    private fun openImagePicker(imageView: ImageView) {
        currentImagePicker = imageView
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun calculateTotals() {
        val oldElec = etOldElectric.text.toString().toIntOrNull() ?: 0
        val newElec = etNewElectric.text.toString().toIntOrNull() ?: 0
        val oldWater = etOldWater.text.toString().toIntOrNull() ?: 0
        val newWater = etNewWater.text.toString().toIntOrNull() ?: 0

        etNewElectric.error = if (newElec > 0 && newElec < oldElec) "Số mới phải lớn hơn hoặc bằng số cũ" else null
        etNewWater.error = if (newWater > 0 && newWater < oldWater) "Số mới phải lớn hơn hoặc bằng số cũ" else null

        val elecUsage = if (newElec >= oldElec) newElec - oldElec else 0
        val waterUsage = if (newWater >= oldWater) newWater - oldWater else 0
        val roomFee = etRoomPrice.text.toString().toDoubleOrNull() ?: (selectedRoom?.price ?: 0.0)
        val internetFee = etInternetPrice.text.toString().toDoubleOrNull() ?: INTERNET_PRICE

        val elecTotal = elecUsage * ELECTRICITY_PRICE_PER_UNIT
        val waterTotal = waterUsage * WATER_PRICE_PER_UNIT
        val grandTotal = elecTotal + waterTotal + roomFee + internetFee

        // Update UI
        tvElectricQuantity.text = elecUsage.toString()
        tvWaterQuantity.text = waterUsage.toString()

        tvElectricUnitPrice.text = currencyFormat.format(ELECTRICITY_PRICE_PER_UNIT)
        tvWaterUnitPrice.text = currencyFormat.format(WATER_PRICE_PER_UNIT)
        tvRoomUnitPrice.text = currencyFormat.format(roomFee)
        tvInternetUnitPrice.text = currencyFormat.format(internetFee)

        tvElectricTotal.text = currencyFormat.format(elecTotal)
        tvWaterTotal.text = currencyFormat.format(waterTotal)
        tvRoomTotal.text = currencyFormat.format(roomFee)
        tvInternetTotal.text = currencyFormat.format(internetFee)
        tvGrandTotal.text = currencyFormat.format(grandTotal)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            etDate.setText(selectedDate)
            selectedDateString = String.format(Locale.getDefault(), "%02d/%d", month + 1, year)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveBill() {
        val oldElec = etOldElectric.text.toString().toIntOrNull() ?: 0
        val newElec = etNewElectric.text.toString().toIntOrNull() ?: 0
        if (newElec < oldElec) {
            etNewElectric.error = "Số mới phải lớn hơn hoặc bằng số cũ"
            etNewElectric.requestFocus()
            return
        }

        val oldWater = etOldWater.text.toString().toIntOrNull() ?: 0
        val newWater = etNewWater.text.toString().toIntOrNull() ?: 0
        if (newWater < oldWater) {
            etNewWater.error = "Số mới phải lớn hơn hoặc bằng số cũ"
            etNewWater.requestFocus()
            return
        }

        val tenantId = selectedRoom?.let { tenantDao.getTenantByRoomId(it.id)?.id }
        if (tenantId == null) {
            Toast.makeText(this, "Phòng chưa có người thuê!", Toast.LENGTH_SHORT).show()
            return
        }

        val roomFee = etRoomPrice.text.toString().toDoubleOrNull() ?: (selectedRoom?.price ?: 0.0)
        val internetFee = etInternetPrice.text.toString().toDoubleOrNull() ?: INTERNET_PRICE
        val elecTotal = (newElec - oldElec) * ELECTRICITY_PRICE_PER_UNIT
        val waterTotal = (newWater - oldWater) * WATER_PRICE_PER_UNIT
        val grandTotal = elecTotal + waterTotal + roomFee + internetFee

        val newBill = Bill(
            id = 0, month = selectedDateString,
            oldElectricReading = oldElec, newElectricReading = newElec,
            oldWaterReading = oldWater, newWaterReading = newWater,
            electric = elecTotal, water = waterTotal, roomFee = roomFee, internet = internetFee, total = grandTotal,
            roomId = selectedRoom!!.id, tenantId = tenantId, roomName = selectedRoom!!.name, status = "unpaid",
            oldElectricImageUri = oldElectricImageUri?.toString(), newElectricImageUri = newElectricImageUri?.toString(),
            oldWaterImageUri = oldWaterImageUri?.toString(), newWaterImageUri = newWaterImageUri?.toString()
        )

        val newId = billDao.insertBill(newBill)
        if (newId > 0) {
            val title = "Hóa đơn mới"
            val message = "Bạn có hóa đơn cho tháng $selectedDateString. Vui lòng kiểm tra."
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            notificationDao.insertNotification(title, message, "bill", date, tenantId)
            Toast.makeText(this, "Tạo hóa đơn thành công!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Tạo hóa đơn thất bại!", Toast.LENGTH_SHORT).show()
        }
    }
}
