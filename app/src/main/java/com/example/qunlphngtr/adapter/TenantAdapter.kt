package com.example.qunlphngtr.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.R
import com.example.qunlphngtr.model.Tenant

class TenantAdapter(
    private var tenantList: MutableList<Tenant>
) : RecyclerView.Adapter<TenantAdapter.TenantViewHolder>() {

    /** Callback cho Activity xử lý sự kiện */
    var onItemClick: ((Tenant, Int) -> Unit)? = null       // bấm 1 lần (sửa)
    var onItemLongClick: ((Tenant, Int) -> Unit)? = null   // nhấn giữ (xóa)

    inner class TenantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvGender: TextView = itemView.findViewById(R.id.tvGender)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)

        init {
            // Bấm 1 lần
            itemView.setOnClickListener {
                val pos = adapterPosition      // ✅ sửa ở đây
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(tenantList[pos], pos)
                }
            }
            // Nhấn giữ
            itemView.setOnLongClickListener {
                val pos = adapterPosition      // ✅ sửa ở đây
                if (pos != RecyclerView.NO_POSITION) {
                    onItemLongClick?.invoke(tenantList[pos], pos)
                }
                true
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tenant, parent, false)
        return TenantViewHolder(view)
    }

    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        val tenant = tenantList[position]
        holder.tvName.text = tenant.name
        holder.tvGender.text = "Giới tính: ${tenant.gender}"
        holder.tvPhone.text = tenant.phone
    }

    override fun getItemCount(): Int = tenantList.size

    /** Lọc danh sách */
    fun filterList(filteredList: List<Tenant>) {
        tenantList.clear()
        tenantList.addAll(filteredList)
        notifyDataSetChanged()
    }

    /** Xóa 1 item */
    fun removeAt(position: Int) {
        tenantList.removeAt(position)
        notifyItemRemoved(position)
    }

    /** Cập nhật 1 item */
    fun updateItem(position: Int, tenant: Tenant) {
        tenantList[position] = tenant
        notifyItemChanged(position)
    }
}
