package com.example.qunlphngtr

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.qunlphngtr.dao.ContractDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Contract
import com.example.qunlphngtr.model.Tenant
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class AddEditContractActivity : AppCompatActivity() {

    private lateinit var spinnerTenant: Spinner
    private lateinit var etStartDate: TextInputEditText
    private lateinit var etEndDate: TextInputEditText
    private lateinit var etRentPrice: TextInputEditText
    private lateinit var etDepositAmount: TextInputEditText
    private lateinit var btnAttachPdf: Button
    private lateinit var tvPdfName: TextView
    private lateinit var btnSaveContract: Button

    private lateinit var contractDao: ContractDao
    private lateinit var tenantDao: TenantDao
    private lateinit var roomDao: RoomDao

    private var pdfUri: Uri? = null
    private var selectedTenant: Tenant? = null
    private val availableTenants = mutableListOf<Tenant>()

    private val pickPdfLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            pdfUri = it
            tvPdfName.text = "Đã chọn: ${it.path}"
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_contract)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initDaos()
        bindViews()
        setupSpinner()
        setupListeners()
    }

    private fun initDaos() {
        contractDao = ContractDao(this)
        tenantDao = TenantDao(this)
        roomDao = RoomDao(this)
    }

    private fun bindViews() {
        spinnerTenant = findViewById(R.id.spinnerTenant)
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        etRentPrice = findViewById(R.id.etRentPrice)
        etDepositAmount = findViewById(R.id.etDepositAmount)
        btnAttachPdf = findViewById(R.id.btnAttachPdf)
        tvPdfName = findViewById(R.id.tvPdfName)
        btnSaveContract = findViewById(R.id.btnSaveContract)
    }

    private fun setupSpinner() {
        // Get tenants who have a room but not yet a contract
        val allTenantsWithRoom = tenantDao.getAllTenants().filter { it.room_id != null }
        val allContracts = contractDao.getAllContracts()
        availableTenants.addAll(allTenantsWithRoom.filter { tenant ->
            allContracts.none { contract -> contract.tenantId == tenant.id }
        })

        val tenantNames = availableTenants.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tenantNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTenant.adapter = adapter
    }

    private fun setupListeners() {
        etStartDate.setOnClickListener { showDatePicker(etStartDate) }
        etEndDate.setOnClickListener { showDatePicker(etEndDate) }
        btnAttachPdf.setOnClickListener { pickPdfLauncher.launch(arrayOf("application/pdf")) }
        btnSaveContract.setOnClickListener { saveContract() }
    }
	private fun showDatePicker(editText: TextInputEditText) {
	    val calendar = Calendar.getInstance()
	    DatePickerDialog(
	        this,
	        { _, year, month, dayOfMonth ->
	            val selectedDate = "$dayOfMonth/${month + 1}/$year"
	            editText.setText(selectedDate)
	        },
	        calendar.get(Calendar.YEAR),
	        calendar.get(Calendar.MONTH),
	        calendar.get(Calendar.DAY_OF_MONTH)
	    ).show()
	}

    private fun saveContract() {
        if (spinnerTenant.selectedItemPosition < 0) {
            Toast.makeText(this, "Vui lòng chọn người thuê", Toast.LENGTH_SHORT).show()
            return
        }

        selectedTenant = availableTenants[spinnerTenant.selectedItemPosition]

        val startDate = etStartDate.text.toString()
        val endDate = etEndDate.text.toString()
        val rentPrice = etRentPrice.text.toString().toDoubleOrNull()
        val depositAmount = etDepositAmount.text.toString().toDoubleOrNull()

        if (selectedTenant == null || startDate.isBlank() || endDate.isBlank() || rentPrice == null || depositAmount == null) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val newContract = Contract(
            id = 0,
            tenantId = selectedTenant!!.id,
            roomId = selectedTenant!!.room_id!!, // Assuming tenant has a room
            startDate = startDate,
            endDate = endDate,
            rentPrice = rentPrice,
            depositAmount = depositAmount,
            contractPdfUri = pdfUri?.toString()
        )

        val newId = contractDao.insertContract(newContract)
        if (newId > 0) {
            Toast.makeText(this, "Tạo hợp đồng thành công!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Tạo hợp đồng thất bại!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}