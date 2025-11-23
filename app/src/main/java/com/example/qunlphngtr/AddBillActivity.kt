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

    private lateinit var tvElectricQuantity: TextView
    private lateinit var tvElectricTotal: TextView
    private lateinit var tvWaterQuantity: TextView
    private lateinit var tvWaterTotal: TextView
    private lateinit var tvRoomTotal: TextView
    private lateinit var tvInternetTotal: TextView
    private lateinit var tvGrandTotal: TextView

    private lateinit var btnApproveInvoice: MaterialButton

    // --- Data ---
    private var selectedRoom: Room? = null
    private var existingBill: Bill? = null
    private lateinit var billDao: BillDao
    private lateinit var roomDao: RoomDao
    private lateinit var tenantDao: TenantDao
    private lateinit var notificationDao: NotificationDao
    private var selectedDateString = ""

    private var oldElectricImageUri: Uri? = null
    private var newElectricImageUri: Uri? = null
    private var oldWaterImageUri: Uri? = null
    private var newWaterImageUri: Uri? = null

    private lateinit var pickImageLauncher: ActivityResultLauncher<Array<String>>
    private var currentImageView: ImageView? = null

    private val ELECTRICITY_PRICE_PER_UNIT = 4000.0
    private val WATER_PRICE_PER_UNIT = 100000.0

    private val locale = Locale("vi", "VN")
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bill)

        billDao = BillDao(this)
        roomDao = RoomDao(this)
        tenantDao = TenantDao(this)
        notificationDao = NotificationDao(this)

        if (intent.hasExtra("BILL_ID")) {
            val billId = intent.getIntExtra("BILL_ID", -1)
            existingBill = billDao.getBillById(billId)
            selectedRoom = roomDao.getAllRooms().find { it.id == existingBill?.roomId }
        } else if (intent.hasExtra("ROOM_ID")) {
            val roomId = intent.getIntExtra("ROOM_ID", -1)
            selectedRoom = roomDao.getAllRooms().find { it.id == roomId }
        }

        if (selectedRoom == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy phòng!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupImagePicker()
        bindViews()
        setupStaticData()
        setupListeners()
        calculateTotals()
    }

    private fun setupImagePicker() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                try {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    when (currentImageView) {
                        ivOldElectric -> oldElectricImageUri = it
                        ivNewElectric -> newElectricImageUri = it
                        ivOldWater -> oldWaterImageUri = it
                        ivNewWater -> newWaterImageUri = it
                    }
                    currentImageView?.let { view -> Glide.with(this).load(it).into(view) }
                } catch (e: SecurityException) {
                    e.printStackTrace()
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

        tvElectricQuantity = findViewById(R.id.tvElectricQuantity)
        tvElectricTotal = findViewById(R.id.tvElectricTotal)
        tvWaterQuantity = findViewById(R.id.tvWaterQuantity)
        tvWaterTotal = findViewById(R.id.tvWaterTotal)
        tvRoomTotal = findViewById(R.id.tvRoomTotal)
        tvInternetTotal = findViewById(R.id.tvInternetTotal)
        tvGrandTotal = findViewById(R.id.tvGrandTotal)

        btnApproveInvoice = findViewById(R.id.btnApproveInvoice)
    }

    private fun setupStaticData() {
        selectedRoom?.let { tvRoomNumber.text = it.name }

        if (existingBill != null) {
            title = "Sửa hóa đơn"
            btnApproveInvoice.text = "Cập nhật hóa đơn"
            etDate.setText(existingBill!!.month)
            selectedDateString = existingBill!!.month
            etRoomPrice.setText(existingBill!!.roomFee.toLong().toString())
            etInternetPrice.setText(existingBill!!.internet.toLong().toString())

            etOldElectric.setText(existingBill!!.oldElectricReading.toString())
            etNewElectric.setText(existingBill!!.newElectricReading.toString())
            etOldWater.setText(existingBill!!.oldWaterReading.toString())
            etNewWater.setText(existingBill!!.newWaterReading.toString())

            existingBill!!.oldElectricImageUri?.let { oldElectricImageUri = Uri.parse(it); Glide.with(this).load(it).into(ivOldElectric) }
            existingBill!!.newElectricImageUri?.let { newElectricImageUri = Uri.parse(it); Glide.with(this).load(it).into(ivNewElectric) }
            existingBill!!.oldWaterImageUri?.let { oldWaterImageUri = Uri.parse(it); Glide.with(this).load(it).into(ivOldWater) }
            existingBill!!.newWaterImageUri?.let { newWaterImageUri = Uri.parse(it); Glide.with(this).load(it).into(ivNewWater) }
        } else {
            title = "Tạo hóa đơn mới"
            btnApproveInvoice.text = "Tạo hóa đơn"
            selectedRoom?.let { etRoomPrice.setText(it.price.toLong().toString()) }
            etInternetPrice.setText("100000") // Mặc định là 100000
        }
    }

    private fun setupListeners() {
        etDate.setOnClickListener { showDatePickerDialog() }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calculateTotals() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etOldElectric.addTextChangedListener(textWatcher)
        etNewElectric.addTextChangedListener(textWatcher)
        etOldWater.addTextChangedListener(textWatcher)
        etNewWater.addTextChangedListener(textWatcher)
        etRoomPrice.addTextChangedListener(textWatcher)
        etInternetPrice.addTextChangedListener(textWatcher)

        btnAddOldElectricImage.setOnClickListener { currentImageView = ivOldElectric; pickImageLauncher.launch(arrayOf("image/*")) }
        btnAddNewElectricImage.setOnClickListener { currentImageView = ivNewElectric; pickImageLauncher.launch(arrayOf("image/*")) }
        btnAddOldWaterImage.setOnClickListener { currentImageView = ivOldWater; pickImageLauncher.launch(arrayOf("image/*")) }
        btnAddNewWaterImage.setOnClickListener { currentImageView = ivNewWater; pickImageLauncher.launch(arrayOf("image/*")) }

        btnApproveInvoice.setOnClickListener { saveBill() }
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

        val elecTotal = elecUsage * ELECTRICITY_PRICE_PER_UNIT
        val waterTotal = waterUsage * WATER_PRICE_PER_UNIT
        val roomFee = etRoomPrice.text.toString().toDoubleOrNull() ?: 0.0
        val internetFee = etInternetPrice.text.toString().toDoubleOrNull() ?: 0.0
        val grandTotal = elecTotal + waterTotal + roomFee + internetFee

        tvElectricQuantity.text = "$elecUsage kWh"
        tvWaterQuantity.text = "$waterUsage m³"
        tvElectricTotal.text = formatCurrency(elecTotal)
        tvWaterTotal.text = formatCurrency(waterTotal)
        tvRoomTotal.text = formatCurrency(roomFee)
        tvInternetTotal.text = formatCurrency(internetFee)
        tvGrandTotal.text = formatCurrency(grandTotal)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val formattedDate = "$dayOfMonth/${month + 1}/$year"
                etDate.setText(formattedDate)
                selectedDateString = String.format(Locale.getDefault(), "%02d/%d", month + 1, year)
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

        val oldElec = etOldElectric.text.toString().toIntOrNull() ?: 0
        val newElec = etNewElectric.text.toString().toIntOrNull() ?: 0
        if (newElec < oldElec) {
            etNewElectric.requestFocus()
            Toast.makeText(this, "Số điện mới không thể nhỏ hơn số điện cũ.", Toast.LENGTH_SHORT).show()
            return
        }

        val oldWater = etOldWater.text.toString().toIntOrNull() ?: 0
        val newWater = etNewWater.text.toString().toIntOrNull() ?: 0
        if (newWater < oldWater) {
            etNewWater.requestFocus()
            Toast.makeText(this, "Số nước mới không thể nhỏ hơn số nước cũ.", Toast.LENGTH_SHORT).show()
            return
        }

        selectedRoom?.let { room ->
            val electricTotal = tvElectricTotal.text.toString().replace(".", "").toDoubleOrNull() ?: 0.0
            val waterTotal = tvWaterTotal.text.toString().replace(".", "").toDoubleOrNull() ?: 0.0
            val roomFee = etRoomPrice.text.toString().toDoubleOrNull() ?: 0.0
            val internetFee = etInternetPrice.text.toString().toDoubleOrNull() ?: 0.0

            val tenantId = existingBill?.tenantId ?: tenantDao.getTenantByRoomId(room.id)?.id
            if (tenantId == null) {
                Toast.makeText(this, "Phòng chưa có người thuê, không thể tạo hóa đơn.", Toast.LENGTH_LONG).show()
                return
            }

            if (existingBill != null) {
                // Chế độ Sửa
                val updatedBill = existingBill!!.copy(
                    month = selectedDateString,
                    oldElectricReading = oldElec,
                    newElectricReading = newElec,
                    oldWaterReading = oldWater,
                    newWaterReading = newWater,
                    electric = electricTotal,
                    water = waterTotal,
                    roomFee = roomFee,
                    internet = internetFee,
                    total = electricTotal + waterTotal + roomFee + internetFee,
                    oldElectricImageUri = oldElectricImageUri?.toString() ?: existingBill!!.oldElectricImageUri,
                    newElectricImageUri = newElectricImageUri?.toString() ?: existingBill!!.newElectricImageUri,
                    oldWaterImageUri = oldWaterImageUri?.toString() ?: existingBill!!.oldWaterImageUri,
                    newWaterImageUri = newWaterImageUri?.toString() ?: existingBill!!.newWaterImageUri
                )
                val rowsAffected = billDao.updateBill(updatedBill)
                if (rowsAffected > 0) {
                    Toast.makeText(this, "Cập nhật hóa đơn thành công!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Cập nhật hóa đơn thất bại!", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Chế độ Tạo mới
                val newBill = Bill(
                    id = 0,
                    month = selectedDateString,
                    oldElectricReading = oldElec,
                    newElectricReading = newElec,
                    oldWaterReading = oldWater,
                    newWaterReading = newWater,
                    electric = electricTotal,
                    water = waterTotal,
                    roomFee = roomFee,
                    internet = internetFee,
                    total = electricTotal + waterTotal + roomFee + internetFee,
                    roomId = room.id,
                    tenantId = tenantId,
                    roomName = room.name,
                    status = "unpaid",
                    oldElectricImageUri = oldElectricImageUri?.toString(),
                    newElectricImageUri = newElectricImageUri?.toString(),
                    oldWaterImageUri = oldWaterImageUri?.toString(),
                    newWaterImageUri = newWaterImageUri?.toString()
                )
                val newId = billDao.insertBill(newBill)
                if (newId > 0) {
                    val title = "Hóa đơn mới"
                    val message = "Bạn có hóa đơn cho tháng $selectedDateString. Vui lòng kiểm tra."
                    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    notificationDao.insertNotification(title, message, "bill", date, tenantId)

                    Toast.makeText(this, "Tạo hóa đơn thành công!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Tạo hóa đơn thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount).replace("₫", "").trim()
    }
}
