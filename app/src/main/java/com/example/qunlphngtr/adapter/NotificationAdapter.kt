package com.example.qunlphngtr.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.R
import com.example.qunlphngtr.model.Notification

class NotificationAdapter(private var notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    var onItemClick: ((Notification) -> Unit)? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNotificationTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotificationMessage)
        val tvDate: TextView = view.findViewById(R.id.tvNotificationDate)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(notifications[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message
        holder.tvDate.text = notification.date

        if (!notification.isRead) {
            holder.tvTitle.setTypeface(null, Typeface.BOLD)
            holder.itemView.setBackgroundColor(Color.parseColor("#E8F0FE")) // Màu nền xanh nhạt cho thông báo chưa đọc
        } else {
            holder.tvTitle.setTypeface(null, Typeface.NORMAL)
            holder.itemView.setBackgroundColor(Color.WHITE)
        }
    }

    override fun getItemCount() = notifications.size

    fun updateList(newList: List<Notification>) {
        notifications = newList
        notifyDataSetChanged()
    }
}