package com.example.qunlphngtr.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.R
import com.example.qunlphngtr.model.Room

class RoomAdapter(
    private val rooms: MutableList<Room>,
    private val onItemClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvRoomName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvRoomPrice)
        val imgRoom: ImageView = itemView.findViewById(R.id.imgRoom)

        fun bind(room: Room) {
            tvName.text = room.name
            tvPrice.text = "${room.price} VNĐ"
            if (!room.imageUri.isNullOrEmpty()) {
                imgRoom.setImageURI(Uri.parse(room.imageUri))
            } else {
                imgRoom.setImageResource(R.drawable.ic_room)
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
