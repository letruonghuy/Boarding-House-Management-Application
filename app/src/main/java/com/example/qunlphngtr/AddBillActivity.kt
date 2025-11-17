package com.example.qunlphngtr

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.dao.NotificationDao
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
    private lateinit var tenantDao: TenantDao
    private var selectedDateString = ""

    // Hardcoded prices (can be moved to a settings screen later)
    private val ELECTRICITY_PRICE = 4000.0
    private val WATER_PRICE = 100000.0 // Assuming this is price per m³, adjust if needed

    private val locale = Locale("vi", "VN")
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bill)

        billDao = BillDao(this)
        roomDao = RoomDao(this)
        tenantDao = TenantDao(this)

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
        calculateTotals() // Initial calculation
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

        // Summary Table
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
    }

    private fun calculateTotals() {
        val oldElec = etOldElectric.text.toString().toIntOrNull() ?: 0
        val newElec = etNewElectric.text.toString().toIntOrNull() ?: 0
        val oldWater = etOldWater.text.toString().toIntOrNull() ?: 0
        val newWater = etNewWater.text.toString().toIntOrNull() ?: 0

        val elecUsage = if (newElec > oldElec) newElec - oldElec else 0
        val waterUsage = if (newWater > oldWater) newWater - oldWater else 0

        val elecTotal = elecUsage * ELECTRICITY_PRICE
        val waterTotal = waterUsage * WATER_PRICE
        val roomFee = etRoomPrice.text.toString().toDoubleOrNull() ?: 0.0

        val grandTotal = elecTotal + waterTotal + roomFee

        // Update summary table
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
                // use explicit locale to avoid lint warning
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

        selectedRoom?.let { room ->
            val oldElec = etOldElectric.text.toString().toIntOrNull() ?: 0
            val newElec = etNewElectric.text.toString().toIntOrNull() ?: 0
            val elecUsage = if (newElec > oldElec) newElec - oldElec else 0
            val elecTotal = elecUsage * ELECTRICITY_PRICE

            val oldWater = etOldWater.text.toString().toIntOrNull() ?: 0
            val newWater = etNewWater.text.toString().toIntOrNull() ?: 0
            val waterUsage = if (newWater > oldWater) newWater - oldWater else 0
            val waterTotal = waterUsage * WATER_PRICE

            val roomFee = etRoomPrice.text.toString().toDoubleOrNull() ?: 0.0

            // Require a tenant to exist for this room before creating a bill
            val tenant = tenantDao.getTenantByRoomId(room.id)
            if (tenant == null) {
                Toast.makeText(this, "Phòng chưa có người thuê, không thể tạo hóa đơn.", Toast.LENGTH_LONG).show()
                return
            }

            val result = billDao.insertBill(
                month = selectedDateString,
                electric = elecTotal,
                water = waterTotal,
                roomFee = roomFee,
                internet = 0.0, // No internet field in the new layout
                roomId = room.id,
                tenantId = tenant.id
            )

            if (result > 0) {
                // Create a notification to tenant about this new bill
                try {
                    val notificationDao = NotificationDao(this)
                    val title = "Hóa đơn mới"
                    val message = "Bạn có hóa đơn cho tháng $selectedDateString. Vui lòng kiểm tra."
                    val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                    notificationDao.insertNotification(title, message, "bill", date, tenant.id)

                    // Removed system notification call to avoid permission analysis errors; app shows in-app notifications from DB
                    // try {
                    //     val helper = NotificationHelper(this)
                    //     helper.notifyTenant(tenant.id, title, message)
                    // } catch (e: Exception) { e.printStackTrace() }
                } catch (nEx: Exception) {
                    nEx.printStackTrace()
                }

                Toast.makeText(this, "Tạo hóa đơn thành công!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Tạo hóa đơn thất bại!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount).replace("₫", "").trim()
    }
}
