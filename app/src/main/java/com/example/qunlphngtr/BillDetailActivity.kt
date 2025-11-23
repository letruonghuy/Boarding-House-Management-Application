package com.example.qunlphngtr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.NotificationDao
import com.example.qunlphngtr.dao.TenantDao
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BillDetailActivity : AppCompatActivity() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Views
    private lateinit var tvBillStatus: TextView
    private lateinit var btnAdminConfirm: MaterialButton
    private lateinit var tenantPaymentOptions: LinearLayout
    private lateinit var layoutProof: LinearLayout
    private lateinit var ivProof: ImageView

    private lateinit var billDao: BillDao
    private lateinit var tenantDao: TenantDao
    private lateinit var notificationDao: NotificationDao
    private var billId: Int = -1
    private var isAdmin: Boolean = false

    private val qrLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bill = billDao.getBillById(billId)
            bill?.let { updateUIForStatus(it.status) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_detail)

        billId = intent.getIntExtra("invoice_id", -1)
        isAdmin = intent.getBooleanExtra("isAdmin", false)
        billDao = BillDao(this)
        tenantDao = TenantDao(this)
        notificationDao = NotificationDao(this)

        bindViews()

        val bill = billDao.getBillById(billId)
        if (bill == null) {
            Toast.makeText(this, "Không tìm thấy hóa đơn", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Populate bill details
        findViewById<TextView>(R.id.tvBillTitle).text = "Hóa đơn tháng ${bill.month}"
        findViewById<TextView>(R.id.tvElectricCost).text = currencyFormat.format(bill.electric)
        findViewById<TextView>(R.id.tvWaterCost).text = currencyFormat.format(bill.water)
        findViewById<TextView>(R.id.tvRoomFee).text = currencyFormat.format(bill.roomFee)
        findViewById<TextView>(R.id.tvInternetFee).text = currencyFormat.format(bill.internet)
        findViewById<TextView>(R.id.tvTotalAmount).text = currencyFormat.format(bill.total)

        updateUIForStatus(bill.status)

        // Set button listeners
        btnAdminConfirm.setOnClickListener { confirmPayment() }
        findViewById<MaterialButton>(R.id.btnPayByCash).setOnClickListener { requestCashPayment() }
        findViewById<MaterialButton>(R.id.btnPayByTransfer).setOnClickListener { startTransferPayment() }
    }

    private fun bindViews() {
        tvBillStatus = findViewById(R.id.tvBillStatus)
        btnAdminConfirm = findViewById(R.id.btnAdminConfirm)
        tenantPaymentOptions = findViewById(R.id.tenantPaymentOptions)
        layoutProof = findViewById(R.id.layoutProof)
        ivProof = findViewById(R.id.ivProof)
    }

    private fun updateUIForStatus(status: String) {
        layoutProof.visibility = View.GONE
        btnAdminConfirm.visibility = View.GONE
        tenantPaymentOptions.visibility = View.GONE

        when (status) {
            "paid" -> {
                tvBillStatus.text = "Đã thanh toán"
                tvBillStatus.setTextColor(ContextCompat.getColor(this, R.color.green))
            }
            "pending_confirmation" -> {
                tvBillStatus.text = "Chờ xác nhận"
                tvBillStatus.setTextColor(ContextCompat.getColor(this, R.color.orange))
                if (isAdmin) {
                    btnAdminConfirm.visibility = View.VISIBLE
                    val bill = billDao.getBillById(billId)
                    bill?.paymentProofUri?.let {
                        if (it.isNotEmpty()) {
                            layoutProof.visibility = View.VISIBLE
                            Glide.with(this).load(it).into(ivProof)
                        }
                    }
                }
            }
            else -> { // unpaid
                tvBillStatus.text = "Chưa thanh toán"
                tvBillStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
                if (!isAdmin) {
                    tenantPaymentOptions.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun confirmPayment() {
        updateBillStatus("paid", "Xác nhận thanh toán thành công")
    }

    private fun requestCashPayment() {
        val bill = billDao.getBillById(billId)
        if (bill == null) return

        val rowsAffected = billDao.updateBillStatus(billId, "pending_confirmation")
        if (rowsAffected > 0) {
            val tenant = tenantDao.getTenantById(bill.tenantId)
            val message = "${tenant?.name ?: "Người thuê"} ở phòng ${bill.roomName} đã báo thanh toán tiền mặt."
            sendNotificationToLandlord("Yêu cầu xác nhận", message)
            Toast.makeText(this, "Đã gửi yêu cầu thanh toán tiền mặt", Toast.LENGTH_SHORT).show()
            updateUIForStatus("pending_confirmation")
            setResult(Activity.RESULT_OK)
        } else {
            Toast.makeText(this, "Lỗi: Không thể gửi yêu cầu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTransferPayment() {
        val bill = billDao.getBillById(billId)
        if (bill == null) {
            Toast.makeText(this, "Không thể lấy thông tin hóa đơn", Toast.LENGTH_SHORT).show()
            return
        }

        val tenant = tenantDao.getTenantById(bill.tenantId)
        val tenantName = tenant?.name?.replace(" ", "") ?: ""

        val today = SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(Date())
        val roomName = bill.roomName.filter { it.isDigit() }
        val content = "$tenantName TT phong $roomName $today"

        val intent = Intent(this, ShowQrActivity::class.java).apply {
            putExtra("amount", bill.total)
            putExtra("content", content)
            putExtra("bill_id", billId)
        }
        qrLauncher.launch(intent)
    }

    private fun updateBillStatus(newStatus: String, toastMessage: String) {
        val rowsAffected = billDao.updateBillStatus(billId, newStatus)
        if (rowsAffected > 0) {
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            updateUIForStatus(newStatus)
            setResult(Activity.RESULT_OK)
        } else {
            Toast.makeText(this, "Lỗi cập nhật trạng thái", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNotificationToLandlord(title: String, message: String) {
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        notificationDao.insertNotification(title, message, "bill_confirmation", date, null) // tenantId is NULL for landlord
    }
}
