package com.example.huelluditos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(private var items: List<CartItemView>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvName: TextView = v.findViewById(R.id.tvCartProductName)
        private val tvQty: TextView = v.findViewById(R.id.tvCartQuantity)
        private val tvPrice: TextView = v.findViewById(R.id.tvCartPrice)

        fun bind(item: CartItemView) {
            tvName.text = item.productName
            tvQty.text = "Cantidad: ${item.quantity}"
            val total = item.quantity * item.unitPrice
            tvPrice.text = "Total: $${"%.2f".format(total)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(v)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<CartItemView>) {
        items = newItems
        notifyDataSetChanged()
    }
}
