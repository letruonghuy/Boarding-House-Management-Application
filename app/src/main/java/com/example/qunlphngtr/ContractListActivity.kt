package com.example.qunlphngtr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.ContractAdapter
import com.example.qunlphngtr.dao.ContractDao
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.ContractDetails
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContractListActivity : AppCompatActivity() {

    private lateinit var rvContracts: RecyclerView
    private lateinit var fabAddContract: FloatingActionButton
    private lateinit var contractDao: ContractDao
    private lateinit var tenantDao: TenantDao
    private lateinit var roomDao: RoomDao
    private lateinit var adapter: ContractAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contract_list)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initDaos()
        bindViews()

        rvContracts.layoutManager = LinearLayoutManager(this)

        fabAddContract.setOnClickListener {
            val intent = Intent(this, AddEditContractActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initDaos() {
        contractDao = ContractDao(this)
        tenantDao = TenantDao(this)
        roomDao = RoomDao(this)
    }

    private fun bindViews() {
        rvContracts = findViewById(R.id.rvContracts)
        fabAddContract = findViewById(R.id.fabAddContract)
    }

    override fun onResume() {
        super.onResume()
        loadContractDetails()
    }

    private fun loadContractDetails() {
        val contracts = contractDao.getAllContracts()
        val tenants = tenantDao.getAllTenants().associateBy { it.id }
        val rooms = roomDao.getAllRooms().associateBy { it.id }

        val contractDetails = contracts.mapNotNull { contract ->
            val tenantName = tenants[contract.tenantId]?.name ?: "N/A"
            val roomName = rooms[contract.roomId]?.name ?: "N/A"
            ContractDetails(contract, tenantName, roomName)
        }

        adapter = ContractAdapter(contractDetails)
        rvContracts.adapter = adapter
        // TODO: Handle PDF click
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
