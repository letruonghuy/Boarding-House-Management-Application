package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.dao.ReportDao
import com.example.qunlphngtr.model.Report

class ReportsAdminActivity : AppCompatActivity() {
    private lateinit var rvReports: RecyclerView
    private lateinit var adapter: ReportsAdapter
    private lateinit var reportDao: ReportDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports_admin)

        reportDao = ReportDao(this)
        rvReports = findViewById(R.id.rvReports)
        rvReports.layoutManager = LinearLayoutManager(this)

        val data = reportDao.getAllReports()
        adapter = ReportsAdapter(data) { report ->
            val i = Intent(this, ReportDetailActivity::class.java)
            i.putExtra("report_id", report.id)
            startActivity(i)
        }
        rvReports.adapter = adapter
    }
}

