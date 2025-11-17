package com.example.qunlphngtr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.model.Report

class ReportsAdapter(private val items: List<Report>, private val onClick: (Report) -> Unit) : RecyclerView.Adapter<ReportsAdapter.VH>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvReportTitle)
        val tvSub: TextView = view.findViewById(R.id.tvReportSub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.tvTitle.text = r.title
        holder.tvSub.text = r.content ?: ""
        holder.itemView.setOnClickListener { onClick(r) }
    }

    override fun getItemCount() = items.size
}

