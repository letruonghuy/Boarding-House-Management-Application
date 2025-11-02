package com.example.qunlphngtr

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.qunlphngtr.adapter.TenantAdapter
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Tenant
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TenantListActivity : AppCompatActivity() {

    private lateinit var tenantAdapter: TenantAdapter
    private lateinit var allTenantList: MutableList<Tenant>
    private lateinit var tenantList: MutableList<Tenant>
    private lateinit var db: TenantDao
    private lateinit var addTenantLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Array<String>>
    // tham chiếu tới ImageView của dialog đang mở (để cập nhật sau khi pick image)
    private var currentEditImageView: ImageView? = null
    // URI tạm của ảnh đang chỉnh sửa
    private var currentEditImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_list)

        try {
            // khởi tạo launcher chọn ảnh (chung cho Add/Edit)
            pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    currentEditImageUri = it.toString()
                    currentEditImageView?.setImageURI(it)
                    try {
                        contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }

            db = TenantDao(this)

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            val searchView = findViewById<SearchView>(R.id.searchView)
            val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTenant)

            // register activity result launcher
            addTenantLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    data?.let {
                        val name = it.getStringExtra("name") ?: ""
                        val gender = it.getStringExtra("gender") ?: ""
                        val phone = it.getStringExtra("phone") ?: ""
                        val imageUri = it.getStringExtra("imageUri")
                        // create Tenant and insert via TenantDao
                        val tenantToInsert = Tenant(name = name, gender = gender, phone = phone, imageUri = imageUri)
                        val newIdLong = db.insertTenant(tenantToInsert) // returns Long
                        val newId = newIdLong.toInt()
                        val newTenant = Tenant(newId, name, gender, phone, imageUri = imageUri)
                        allTenantList.add(newTenant)
                        tenantList.add(newTenant)
                        tenantAdapter.notifyItemInserted(tenantList.size - 1)
                    }
                }
            }

            // ✅ Lấy danh sách tenant từ SQLite
            allTenantList = db.getAllTenants().toMutableList()
            tenantList = allTenantList.toMutableList()

            tenantAdapter = TenantAdapter(tenantList)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = tenantAdapter

            // ✅ Click để sửa
            tenantAdapter.onItemClick = { tenant, pos ->
                showEditDialog(tenant, pos)
            }

            // ✅ Giữ lâu để xóa
            tenantAdapter.onItemLongClick = { tenant, pos ->
                AlertDialog.Builder(this)
                    .setTitle("Xóa người thuê")
                    .setMessage("Bạn có chắc muốn xóa ${tenant.name}?")
                    .setPositiveButton("Xóa") { _, _ ->
                        db.deleteTenant(tenant.id) // use TenantDao.deleteTenant
                        // remove from full list by id (pos refers to current filtered list)
                        val fullIndex = allTenantList.indexOfFirst { it.id == tenant.id }
                        if (fullIndex != -1) allTenantList.removeAt(fullIndex)
                        tenantList.removeAt(pos)
                        tenantAdapter.notifyItemRemoved(pos)
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }

            // ✅ Thêm tenant mới
            fabAdd.setOnClickListener {
                val intent = Intent(this, AddTenantActivity::class.java)
                addTenantLauncher.launch(intent)
            }

            // ✅ Tìm kiếm tenant
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    filterList(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterList(newText)
                    return true
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khởi tạo màn hình: ${e.message}", Toast.LENGTH_LONG).show()
            // Optionally finish to avoid blank screen
            // finish()
        }
    }

    private fun showEditDialog(tenant: Tenant, pos: Int) {
        val layout = layoutInflater.inflate(R.layout.dialog_edit_tenant, null)
        val edtName = layout.findViewById<EditText>(R.id.edtName)
        val edtGender = layout.findViewById<EditText>(R.id.edtGender)
        val edtPhone = layout.findViewById<EditText>(R.id.edtPhone)
        val imgTenant = layout.findViewById<ImageView>(R.id.imgTenant)
        val btnChooseImage = layout.findViewById<android.widget.Button>(R.id.btnChooseImage)

        edtName.setText(tenant.name)
        edtGender.setText(tenant.gender)
        edtPhone.setText(tenant.phone)
        // hiển thị ảnh hiện có
        if (!tenant.imageUri.isNullOrEmpty()) {
            Glide.with(this).load(tenant.imageUri).placeholder(R.drawable.ic_person).into(imgTenant)
            currentEditImageUri = tenant.imageUri
        } else {
            imgTenant.setImageResource(R.drawable.ic_person)
            currentEditImageUri = null
        }

        // khi bấm chọn ảnh trong dialog -> lưu tham chiếu ImageView hiện tại và mở picker
        btnChooseImage.setOnClickListener {
            currentEditImageView = imgTenant
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        AlertDialog.Builder(this)
            .setTitle("Sửa thông tin người thuê")
            .setView(layout)
            .setPositiveButton("Lưu") { _, _ ->
                val updatedTenant = Tenant(
                    tenant.id,
                    edtName.text.toString(),
                    edtGender.text.toString(),
                    edtPhone.text.toString(),
                    // nếu user đã chọn ảnh mới thì dùng currentEditImageUri, nếu không dùng imageUri cũ
                    currentEditImageUri ?: tenant.imageUri
                )
                db.updateTenant(updatedTenant) // update via TenantDao
                // update full list by id
                val fullIndex = allTenantList.indexOfFirst { it.id == tenant.id }
                if (fullIndex != -1) allTenantList[fullIndex] = updatedTenant
                tenantList[pos] = updatedTenant
                tenantAdapter.notifyItemChanged(pos)
                // reset temporary references
                currentEditImageView = null
                currentEditImageUri = null
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun filterList(query: String?) {
        val q = query?.trim().orEmpty()
        val filtered = if (q.isEmpty()) allTenantList
        else allTenantList.filter {
            it.name.contains(q, ignoreCase = true) || (it.phone?.contains(q, ignoreCase = true) ?: false)
        }
        tenantList.clear()
        tenantList.addAll(filtered)
        tenantAdapter.notifyDataSetChanged()
    }
}
