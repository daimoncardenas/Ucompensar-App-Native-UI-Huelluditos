package com.example.huelluditos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomerAdapter(
    private var items: List<Customer>,
    private val onEdit: (Customer) -> Unit,
    private val onDelete: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    inner class CustomerViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvName: TextView = v.findViewById(R.id.tvCustomerName)
        private val tvEmail: TextView = v.findViewById(R.id.tvCustomerEmail)
        private val tvId: TextView = v.findViewById(R.id.tvCustomerId)
        private val btnEdit: Button = v.findViewById(R.id.btnEditCustomer)
        private val btnDelete: Button = v.findViewById(R.id.btnDeleteCustomer)

        fun bind(customer: Customer) {
            tvName.text = customer.name
            tvEmail.text = customer.email
            tvId.text = "ID: ${customer.id}"

            btnEdit.setOnClickListener { onEdit(customer) }
            btnDelete.setOnClickListener { onDelete(customer) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(v)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Customer>) {
        items = newItems
        notifyDataSetChanged()
    }
}