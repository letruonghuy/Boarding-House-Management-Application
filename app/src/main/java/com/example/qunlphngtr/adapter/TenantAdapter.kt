package com.example.qunlphngtr.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.qunlphngtr.R
import com.example.qunlphngtr.model.Tenant

class TenantAdapter(private var tenantList: MutableList<Tenant>) : RecyclerView.Adapter<TenantAdapter.TenantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tenant, parent, false)
        return TenantViewHolder(view)
    }

    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        val tenant = tenantList[position]
        holder.bind(tenant)
    }

    override fun getItemCount(): Int = tenantList.size

    inner class TenantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val phoneTextView: TextView = itemView.findViewById(R.id.tvPhone)
        private val roomNameTextView: TextView = itemView.findViewById(R.id.tvGender) // Assuming tvGender shows room info now
        private val imageView: ImageView = itemView.findViewById(R.id.imgTenant) // SỬA LỖI Ở ĐÂY

        fun bind(tenant: Tenant) {
            nameTextView.text = tenant.name
            phoneTextView.text = tenant.phone ?: "Chưa có SĐT"
            roomNameTextView.text = if (tenant.room_id != null) "Phòng: ${tenant.room_id}" else "Chưa có phòng"

            if (tenant.imageUri != null) {
                Glide.with(itemView.context).load(tenant.imageUri).placeholder(R.drawable.ic_person).into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_person)
            }
        }
    }

    fun getItemAt(position: Int): Tenant {
        return tenantList[position]
    }

    fun updateList(newList: List<Tenant>) {
        tenantList.clear()
        tenantList.addAll(newList)
        notifyDataSetChanged()
    }
}