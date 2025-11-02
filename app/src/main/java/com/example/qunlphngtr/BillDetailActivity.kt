package com.example.qunlphngtr

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.dao.BillDao

class BillDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_detail)

        val invoiceId = intent.getIntExtra("invoice_id", -1)
        val tvTitle = findViewById<TextView>(R.id.tvInvoiceDetailTitle)
        val tvElectric = findViewById<TextView>(R.id.tvElectric)
        val tvWater = findViewById<TextView>(R.id.tvWater)
        val tvRoom = findViewById<TextView>(R.id.tvRoom)
        val tvInternet = findViewById<TextView>(R.id.tvInternet)
        val tvSum = findViewById<TextView>(R.id.sumBill)
        val btnPay = findViewById<Button>(R.id.btnPay)

        val billDao = BillDao(this)
        val bill = if (invoiceId != -1) billDao.getBillById(invoiceId) else null

        if (bill == null) {
            Toast.makeText(this, "Không tìm thấy hóa đơn", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvTitle.text = "Hóa đơn tháng ${bill.month}"
        tvElectric.text = "${bill.electric} đ"
        tvWater.text = "${bill.water} đ"
        tvRoom.text = "${bill.room} đ"
        tvInternet.text = "${bill.internet} đ"
        tvSum.text = "${bill.total} đ"

        btnPay.setOnClickListener {
            val updated = billDao.updateBillStatus(bill.id, "paid")
            if (updated > 0) {
                Toast.makeText(this, "Đã đánh dấu là đã thanh toán", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
