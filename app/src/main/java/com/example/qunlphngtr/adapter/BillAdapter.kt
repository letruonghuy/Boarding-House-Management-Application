package com.example.qunlphngtr.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.qunlphngtr.R
import com.example.qunlphngtr.model.Bill
import java.text.NumberFormat
import java.util.Locale

class BillAdapter(private var billList: MutableList<Bill>) :
    RecyclerView.Adapter<BillAdapter.ViewHolder>() {

    var onItemClick: ((Bill) -> Unit)? = null
    var onItemLongClick: ((Bill, Int) -> Unit)? = null 

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvInvoiceTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvInvoiceDesc)
        val tvPrice: TextView = view.findViewById(R.id.tvInvoicePrice)
        val tvStatus: TextView = view.findViewById(R.id.tvInvoiceStatus)

        init {
            view.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(billList[pos])
                }
            }
            view.setOnLongClickListener { 
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemLongClick?.invoke(billList[pos], pos)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bill = billList[position]
        holder.tvTitle.text = "Hóa đơn tháng ${bill.month} - Phòng ${bill.roomName}"
        holder.tvDesc.text = "Điện: ${formatCurrency(bill.electric)} | Nước: ${formatCurrency(bill.water)}"
        holder.tvPrice.text = formatCurrency(bill.total)

        when (bill.status) {
            "paid" -> {
                holder.tvStatus.text = "Đã thanh toán"
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            }
            "pending_confirmation" -> {
                holder.tvStatus.text = "Chờ xác nhận"
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.orange))
            }
            else -> { // unpaid
                holder.tvStatus.text = "Chưa thanh toán"
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            }
        }
    }

    override fun getItemCount() = billList.size

    fun updateList(newList: MutableList<Bill>) {
        billList = newList
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        billList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getBillAt(position: Int): Bill {
        return billList[position]
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount)
    }
}