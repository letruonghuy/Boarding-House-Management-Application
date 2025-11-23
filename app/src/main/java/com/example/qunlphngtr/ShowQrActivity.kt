package com.example.qunlphngtr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.qunlphngtr.dao.BillDao
import com.example.qunlphngtr.dao.NotificationDao
import com.example.qunlphngtr.dao.TenantDao
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ShowQrActivity : AppCompatActivity() {

    private lateinit var ivQrCode: ImageView
    private lateinit var tvAmount: TextView
    private lateinit var tvContent: TextView
    private lateinit var tvBankInfo: TextView
    private lateinit var btnUploadProof: Button

    private var billId: Int = -1
    private lateinit var billDao: BillDao
    private lateinit var tenantDao: TenantDao
    private lateinit var notificationDao: NotificationDao

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                updateBillWithProof(it.toString())
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(this, "Không thể truy cập ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_qr)

        billDao = BillDao(this)
        tenantDao = TenantDao(this)
        notificationDao = NotificationDao(this)
        bindViews()

        val amount = intent.getDoubleExtra("amount", 0.0)
        val content = intent.getStringExtra("content") ?: ""
        billId = intent.getIntExtra("bill_id", -1)

        val prefs = getSharedPreferences("PaymentPrefs", Context.MODE_PRIVATE)
        val bankName = prefs.getString("bankName", "") ?: ""
        val accountNumber = prefs.getString("accountNumber", "") ?: ""
        val accountHolder = prefs.getString("accountHolder", "") ?: ""

        if (bankName.isEmpty() || accountNumber.isEmpty()) {
            Toast.makeText(this, "Chủ trọ chưa cấu hình thông tin thanh toán", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        tvAmount.text = "Số tiền: ${currencyFormat.format(amount)}"
        tvContent.text = "Nội dung: $content"
        tvBankInfo.text = "Ngân hàng: $bankName\nSTK: $accountNumber\nChủ TK: $accountHolder"

        val bankId = getBankId(bankName)
        if (bankId.isNotEmpty()) {
            val qrUrl = "https://img.vietqr.io/image/$bankId-$accountNumber-print.png?amount=${amount.toLong()}&addInfo=$content"
            Glide.with(this).load(qrUrl).error(R.drawable.ic_notifications).into(ivQrCode)
        } else {
            Toast.makeText(this, "Ngân hàng chưa được hỗ trợ tạo QR", Toast.LENGTH_SHORT).show()
        }

        btnUploadProof.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }
    }

    private fun bindViews() {
        ivQrCode = findViewById(R.id.ivQrCode)
        tvAmount = findViewById(R.id.tvAmount)
        tvContent = findViewById(R.id.tvContent)
        tvBankInfo = findViewById(R.id.tvBankInfo)
        btnUploadProof = findViewById(R.id.btnUploadProof)
    }

    private fun updateBillWithProof(proofUri: String) {
        val bill = billDao.getBillById(billId)
        if (bill == null) return

        val rowsAffected = billDao.updateBillStatusAndProof(billId, "pending_confirmation", proofUri)
        if (rowsAffected > 0) {
            val tenant = tenantDao.getTenantById(bill.tenantId)
            val message = "${tenant?.name ?: "Người thuê"} ở phòng ${bill.roomName} đã gửi bằng chứng chuyển khoản."
            sendNotificationToLandlord("Yêu cầu xác nhận", message)
            Toast.makeText(this, "Đã gửi bằng chứng thanh toán", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Lỗi: Không thể gửi bằng chứng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNotificationToLandlord(title: String, message: String) {
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        notificationDao.insertNotification(title, message, "bill_confirmation", date, null)
    }

    private fun getBankId(bankName: String): String {
        return when {
            bankName.contains("vietcombank", ignoreCase = true) -> "970436"
            bankName.contains("acb", ignoreCase = true) -> "970416"
            bankName.contains("mbbank", ignoreCase = true) || bankName.contains("mb bank", ignoreCase = true) -> "970422"
            bankName.contains("techcombank", ignoreCase = true) -> "970407"
            bankName.contains("bidv", ignoreCase = true) -> "970418"
            bankName.contains("vietinbank", ignoreCase = true) -> "970415"
            else -> ""
        }
    }
}