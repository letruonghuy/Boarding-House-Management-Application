package com.example.qunlphngtr.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.R
import com.example.qunlphngtr.model.ContractDetails
import java.text.NumberFormat
import java.util.Locale

class ContractAdapter(private var contractDetails: List<ContractDetails>) : RecyclerView.Adapter<ContractAdapter.ViewHolder>() {

    var onItemClick: ((ContractDetails) -> Unit)? = null
    var onPdfClick: ((ContractDetails) -> Unit)? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTenantAndRoom: TextView = view.findViewById(R.id.tvTenantAndRoom)
        val tvContractDuration: TextView = view.findViewById(R.id.tvContractDuration)
        val tvRentAndDeposit: TextView = view.findViewById(R.id.tvRentAndDeposit)
        val btnViewPdf: Button = view.findViewById(R.id.btnViewPdf)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(contractDetails[adapterPosition]) }
            btnViewPdf.setOnClickListener { onPdfClick?.invoke(contractDetails[adapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contract, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detail = contractDetails[position]
        val contract = detail.contract
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        holder.tvTenantAndRoom.text = "HĐ cho: ${detail.tenantName} - P. ${detail.roomName}"
        holder.tvContractDuration.text = "Thời hạn: ${contract.startDate} - ${contract.endDate}"
        holder.tvRentAndDeposit.text = "Giá thuê: ${currencyFormat.format(contract.rentPrice)} - Cọc: ${currencyFormat.format(contract.depositAmount)}"

        holder.btnViewPdf.visibility = if (contract.contractPdfUri != null) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = contractDetails.size

    fun updateData(newDetails: List<ContractDetails>) {
        contractDetails = newDetails
        notifyDataSetChanged()
    }
}