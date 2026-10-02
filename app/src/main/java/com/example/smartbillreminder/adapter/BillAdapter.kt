package com.example.smartbillreminder.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbillreminder.BillDetailActivity
import com.example.smartbillreminder.R

data class Bill(
    val id: Int,
    val name: String,
    val amount: Double,
    val category: String,
    val date: String,
    val recurrence: String,
    val notes: String
)

class BillAdapter(
    private var bills: List<Bill>
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    private var allBills: List<Bill> = bills

    class BillViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvBillName: TextView =
            itemView.findViewById(R.id.tvBillName)

        val tvBillAmount: TextView =
            itemView.findViewById(R.id.tvBillAmount)

        val tvBillCategory: TextView =
            itemView.findViewById(R.id.tvBillCategory)

        val tvBillDate: TextView =
            itemView.findViewById(R.id.tvBillDate)

        val tvBillRecurrence: TextView =
            itemView.findViewById(R.id.tvBillRecurrence)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BillViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill, parent, false)

        return BillViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BillViewHolder,
        position: Int
    ) {

        val bill = bills[position]

        holder.tvBillName.text = bill.name
        holder.tvBillAmount.text = "₹${bill.amount}"
        holder.tvBillCategory.text = bill.category
        holder.tvBillDate.text = "Due: ${bill.date}"
        holder.tvBillRecurrence.text = bill.recurrence

        holder.itemView.setOnClickListener {

            val intent = Intent(
                holder.itemView.context,
                BillDetailActivity::class.java
            )

            intent.putExtra("bill_id", bill.id)
            intent.putExtra("bill_name", bill.name)
            intent.putExtra("bill_amount", bill.amount)
            intent.putExtra("bill_category", bill.category)
            intent.putExtra("bill_date", bill.date)
            intent.putExtra("bill_recurrence", bill.recurrence)
            intent.putExtra("bill_notes", bill.notes)

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return bills.size
    }

    fun updateBills(newBills: List<Bill>) {

        allBills = newBills
        bills = newBills

        notifyDataSetChanged()
    }

    fun filterBills(query: String) {

        bills = if (query.isEmpty()) {

            allBills

        } else {

            allBills.filter { bill ->

                bill.name.contains(
                    query,
                    ignoreCase = true
                ) ||
                        bill.category.contains(
                            query,
                            ignoreCase = true
                        )
            }
        }

        notifyDataSetChanged()
    }
}