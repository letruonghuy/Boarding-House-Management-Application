package com.example.qunlphngtr

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunlphngtr.dao.ReportDao

class ReportDetailActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var edtResponse: EditText
    private lateinit var btnUpdate: Button
    private lateinit var reportDao: ReportDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        reportDao = ReportDao(this)
        tvTitle = findViewById(R.id.tvReportDetailTitle)
        tvContent = findViewById(R.id.tvReportDetailContent)
        edtResponse = findViewById(R.id.edtAdminResponse)
        btnUpdate = findViewById(R.id.btnUpdateReport)

        val id = intent.getIntExtra("report_id", -1)
        if (id == -1) finish()

        // For simplicity, reload list and find by id
        val report = reportDao.getAllReports().find { it.id == id }
        report?.let {
            tvTitle.text = it.title
            tvContent.text = it.content
        }

        btnUpdate.setOnClickListener {
            val resp = edtResponse.text.toString().trim()
            if (resp.isEmpty()) {
                Toast.makeText(this, "Nhập phản hồi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val res = reportDao.addResponse(id, resp)
            if (res > 0) {
                reportDao.updateReportStatus(id, "Resolved")
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

