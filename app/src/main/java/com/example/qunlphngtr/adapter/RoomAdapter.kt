package com.example.qunlphngtr.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.example.qunlphngtr.R
import com.example.qunlphngtr.model.Room

class RoomAdapter(
    private val rooms: MutableList<Room>,
    private val onItemClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvRoomName)
        val imgRoom: ImageView = itemView.findViewById(R.id.imgRoom)

        fun bind(room: Room) {
            tvName.text = room.name
            val tvPrice = itemView.findViewById<TextView>(R.id.tvPrice)
            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)

            // Format price
            try {
                val formatted = java.text.NumberFormat.getInstance(java.util.Locale.getDefault()).format(room.price)
                tvPrice.text = String.format(java.util.Locale.getDefault(), "%s VNĐ", formatted)
            } catch (e: Exception) {
                tvPrice.text = String.format(java.util.Locale.getDefault(), "%s VNĐ", room.price)
            }

            // --- SỬA: Thêm try-catch để chống crash ---
            if (!room.imageUri.isNullOrEmpty()) {
                try {
                    // Cố gắng hiển thị ảnh từ URI
                    imgRoom.setImageURI(Uri.parse(room.imageUri))
                } catch (e: SecurityException) {
                    // Nếu thất bại (ví dụ: mất quyền, file bị xóa),
                    // hiển thị ảnh mặc định và log lỗi ra.
                    Log.e("RoomAdapter", "Không thể tải ảnh: ${room.imageUri}", e)
                    imgRoom.setImageResource(R.drawable.ic_room)
                }
            } else {
                // Không có URI, hiển thị ảnh mặc định
                imgRoom.setImageResource(R.drawable.ic_room)
            }
            // --- Kết thúc sửa ---
            // Set status color
            if (room.status == "available") {
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_available))
                tvStatus.text = itemView.context.getString(R.string.status_available_text)
            } else {
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_rented))
                tvStatus.text = itemView.context.getString(R.string.status_occupied_text)
            }

            itemView.setOnClickListener { onItemClick(room) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun getItemCount() = rooms.size

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    fun addRoom(room: Room) {
        rooms.add(room)
        notifyItemInserted(rooms.size - 1)
    }

    fun updateRoom(updated: Room) {
        val index = rooms.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            rooms[index] = updated
            notifyItemChanged(index)
        }
    }

    fun getRoomAt(position: Int): Room = rooms[position]

    fun removeRoom(room: Room) {
        val index = rooms.indexOf(room)
        if (index != -1) {
            rooms.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun filterList(newList: List<Room>) {
        rooms.clear()
        rooms.addAll(newList)
        notifyDataSetChanged()
    }
}