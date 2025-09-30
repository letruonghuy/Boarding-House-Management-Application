package com.example.qunlphngtr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.TenantAdapter
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.Tenant
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TenantListActivity : AppCompatActivity() {

    private lateinit var tenantAdapter: TenantAdapter
    private lateinit var allTenantList: MutableList<Tenant>
    private lateinit var tenantList: MutableList<Tenant>
    private lateinit var db: DatabaseHelper
    private val ADD_TENANT_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_list)

        db = DatabaseHelper(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTenant)

        // ✅ Lấy dữ liệu từ SQLite
        allTenantList = db.getAllTenants()
        tenantList = allTenantList.toMutableList()

        tenantAdapter = TenantAdapter(tenantList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = tenantAdapter

        // Sửa thông tin
        tenantAdapter.onItemClick = { tenant, pos -> showEditDialog(tenant, pos) }

        // Xóa
        tenantAdapter.onItemLongClick = { tenant, pos ->
            AlertDialog.Builder(this)
                .setTitle("Xóa người thuê")
                .setMessage("Bạn có chắc muốn xóa ${tenant.name}?")
                .setPositiveButton("Xóa") { _, _ ->
                    db.deleteTenant(tenant.name)
                    allTenantList.remove(tenant)
                    tenantList.removeAt(pos)
                    tenantAdapter.notifyItemRemoved(pos)
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        // Thêm mới
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddTenantActivity::class.java)
            startActivityForResult(intent, ADD_TENANT_REQUEST)
        }

        // Tìm kiếm
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also { filterList(query) }
            override fun onQueryTextChange(newText: String?) = true.also { filterList(newText) }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TENANT_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.let {
                val name = it.getStringExtra("name") ?: ""
                val gender = it.getStringExtra("gender") ?: ""
                val phone = it.getStringExtra("phone") ?: ""
                db.insertTenant(name, gender, phone)   // ✅ Lưu DB
                val newTenant = Tenant(name, gender, phone)
                allTenantList.add(newTenant)
                tenantList.add(newTenant)
                tenantAdapter.notifyItemInserted(tenantList.size - 1)
            }
        }
    }

    private fun showEditDialog(tenant: Tenant, pos: Int) {
        val layout = layoutInflater.inflate(R.layout.dialog_edit_tenant, null)
        val edtName = layout.findViewById<EditText>(R.id.edtName)
        val edtGender = layout.findViewById<EditText>(R.id.edtGender)
        val edtPhone = layout.findViewById<EditText>(R.id.edtPhone)
        edtName.setText(tenant.name)
        edtGender.setText(tenant.gender)
        edtPhone.setText(tenant.phone)

        AlertDialog.Builder(this)
            .setTitle("Sửa thông tin")
            .setView(layout)
            .setPositiveButton("Lưu") { _, _ ->
                val updated = Tenant(
                    edtName.text.toString(),
                    edtGender.text.toString(),
                    edtPhone.text.toString()
                )
                db.updateTenant(tenant.name, updated)
                allTenantList[pos] = updated
                tenantList[pos] = updated
                tenantAdapter.notifyItemChanged(pos)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun filterList(query: String?) {
        val filtered = if (query.isNullOrEmpty()) allTenantList
        else allTenantList.filter {
            it.name.contains(query, true) || it.phone.contains(query)
        }
        tenantList.clear()
        tenantList.addAll(filtered)
        tenantAdapter.notifyDataSetChanged()
    }
}
