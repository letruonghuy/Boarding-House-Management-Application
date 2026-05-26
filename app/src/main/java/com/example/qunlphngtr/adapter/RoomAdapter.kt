package com.example.qunlphngtr.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.R
import com.example.qunlphngtr.model.Room
import com.example.qunlphngtr.model.Tenant
import java.text.NumberFormat
import java.util.Locale

data class RoomAdapterItem(val room: Room, val tenant: Tenant?, val unpaidBillCount: Int)

class RoomAdapter(
    private var items: MutableList<RoomAdapterItem>,
    private val onManageClick: (RoomAdapterItem) -> Unit,
    private val onAddTenantClick: (RoomAdapterItem) -> Unit
) : RecyclerView.Adapter<RoomAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Common
        val tvRoomName: TextView = view.findViewById(R.id.tvRoomName)
        val tvRoomStatus: TextView = view.findViewById(R.id.tvRoomStatus)

        // Empty State
        val layoutEmpty: LinearLayout = view.findViewById(R.id.layoutEmpty)
        val tvRoomPriceEmpty: TextView = view.findViewById(R.id.tvRoomPriceEmpty)
        val btnAddTenant: Button = view.findViewById(R.id.btnAddTenant)

        // Occupied State
        val layoutOccupied: LinearLayout = view.findViewById(R.id.layoutOccupied)
        val tvTenantName: TextView = view.findViewById(R.id.tvTenantName)
        val tvTenantPhone: TextView = view.findViewById(R.id.tvTenantPhone)
        val tvRoomPriceOccupied: TextView = view.findViewById(R.id.tvRoomPriceOccupied)
        val tvBillInfo: TextView = view.findViewById(R.id.tvBillInfo)
        val btnManageRoom: Button = view.findViewById(R.id.btnManageRoom)

        fun bind(item: RoomAdapterItem) {
            val room = item.room
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            tvRoomName.text = room.name

            if (room.status == "available") {
                // Show Empty Layout
                layoutEmpty.visibility = View.VISIBLE
                layoutOccupied.visibility = View.GONE
                tvRoomStatus.text = "Trống"
                tvRoomStatus.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
                tvRoomPriceEmpty.text = "Giá: ${currencyFormat.format(room.price)}"

                btnAddTenant.setOnClickListener { onAddTenantClick(item) }

            } else {
                // Show Occupied Layout
                layoutEmpty.visibility = View.GONE
                layoutOccupied.visibility = View.VISIBLE
                tvRoomStatus.text = "Đang thuê"
                tvRoomStatus.setBackgroundColor(Color.parseColor("#F44336")) // Red

                tvTenantName.text = "Người thuê: ${item.tenant?.name ?: "N/A"}"
                tvTenantPhone.text = "SĐT: ${item.tenant?.phone ?: "N/A"}"
                tvRoomPriceOccupied.text = "Giá: ${currencyFormat.format(room.price)}"

                if (item.unpaidBillCount > 0) {
                    tvBillInfo.text = "Có ${item.unpaidBillCount} hóa đơn chưa trả"
                    tvBillInfo.setTextColor(Color.RED)
                } else {
                    tvBillInfo.text = "Không có công nợ"
                    tvBillInfo.setTextColor(Color.DKGRAY)
                }

                btnManageRoom.setOnClickListener { onManageClick(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<RoomAdapterItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): RoomAdapterItem {
        return items[position]
    }
}