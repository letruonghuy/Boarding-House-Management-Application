package com.example.qunlphngtr.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.R
import com.example.qunlphngtr.model.Bill
class BillAdapter(private var billList: MutableList<Bill>) :
    RecyclerView.Adapter<BillAdapter.ViewHolder>() {

    var onItemClick: ((Bill) -> Unit)? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvInvoiceTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvInvoiceDesc)
        val tvPrice: TextView = view.findViewById(R.id.tvInvoicePrice)

        init {
            view.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(billList[pos])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bill = billList[position]
        holder.tvTitle.text = "Hóa đơn tháng ${bill.month}"
        holder.tvDesc.text = "Tiền điện: ${bill.electric} | Nước: ${bill.water} | Phòng: ${bill.roomFee} | Internet: ${bill.internet}"
        holder.tvPrice.text = "${bill.total} đ"
    }

    override fun getItemCount() = billList.size

    fun updateList(newList: MutableList<Bill>) {
        billList = newList
        notifyDataSetChanged()
    }
}