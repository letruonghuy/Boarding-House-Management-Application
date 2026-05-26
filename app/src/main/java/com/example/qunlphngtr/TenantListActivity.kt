package com.example.qunlphngtr

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.adapter.TenantAdapter
import com.example.qunlphngtr.dao.RoomDao
import com.example.qunlphngtr.dao.TenantDao
import com.example.qunlphngtr.model.Tenant
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TenantListActivity : AppCompatActivity() {
    private lateinit var tenantDao: TenantDao
    private lateinit var roomDao: RoomDao
    private lateinit var tenantAdapter: TenantAdapter
    private var allTenants = listOf<Tenant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_list)

        tenantDao = TenantDao(this)
        roomDao = RoomDao(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTenant)

        tenantAdapter = TenantAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = tenantAdapter

        fabAdd.setOnClickListener {
            val intent = Intent(this, AddTenantActivity::class.java)
            addTenantLauncher.launch(intent)
        }

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
        
        setupSwipeActions(recyclerView)
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayTenants()
    }

    private val addTenantLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadAndDisplayTenants() 
        }
    }

    private fun loadAndDisplayTenants() {
        allTenants = tenantDao.getAllTenants()
        tenantAdapter.updateList(allTenants)
    }

    private fun filterList(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allTenants
        } else {
            allTenants.filter {
                it.name.contains(query, ignoreCase = true) ||
                        (it.phone?.contains(query, ignoreCase = true) == true)
            }
        }
        tenantAdapter.updateList(filteredList)
    }

    private fun setupSwipeActions(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val tenant = tenantAdapter.getItemAt(position)

                if (direction == ItemTouchHelper.RIGHT) { // EDIT
                    val intent = Intent(this@TenantListActivity, AddTenantActivity::class.java)
                    intent.putExtra("TENANT_ID", tenant.id) // Pass ID to edit
                    addTenantLauncher.launch(intent)
                    tenantAdapter.notifyItemChanged(position)
                } else if (direction == ItemTouchHelper.LEFT) { // DELETE
                    AlertDialog.Builder(this@TenantListActivity)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa ${tenant.name}?")
                        .setPositiveButton("Xóa") { _, _ ->
                            val deletedResult = tenantDao.deleteTenant(tenant.id)
                            when {
                                deletedResult > 0 -> {
                                    Toast.makeText(this@TenantListActivity, "Đã xóa ${tenant.name}", Toast.LENGTH_SHORT).show()
                                    loadAndDisplayTenants()
                                }
                                deletedResult == -2 -> {
                                    Toast.makeText(this@TenantListActivity, "Không thể xóa người thuê vì còn hóa đơn chưa thanh toán.", Toast.LENGTH_LONG).show()
                                    tenantAdapter.notifyItemChanged(position) // Revert swipe
                                }
                                else -> {
                                    Toast.makeText(this@TenantListActivity, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                                    tenantAdapter.notifyItemChanged(position) // Revert swipe
                                }
                            }
                        }
                        .setNegativeButton("Hủy") { _, _ -> tenantAdapter.notifyItemChanged(position) }
                        .setCancelable(false)
                        .show()
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable()
                if (dX > 0) {
                    background.color = Color.parseColor("#4CAF50") // Green for Edit
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                } else {
                    background.color = Color.parseColor("#F44336") // Red for Delete
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                }
                background.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }
}
