package com.example.huelluditos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.net.Uri
import android.widget.ImageView
import java.io.File

class ProductAdapter(
    private var items: List<Product>,
    private val onAddToCart: (Product) -> Unit,
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvName: TextView = v.findViewById(R.id.tvProductName)
        private val tvPrice: TextView = v.findViewById(R.id.tvProductPrice)
        private val btnAdd: Button = v.findViewById(R.id.btnAddToCart)
        private val btnEdit: Button = v.findViewById(R.id.btnEdit)
        private val btnDelete: Button = v.findViewById(R.id.btnDelete)

        private val ivImage: ImageView = v.findViewById(R.id.ivProductImage)

        fun bind(p: Product) {
            tvName.text = p.name
            tvPrice.text = "$${p.price}"

            if (!p.imageUri.isNullOrEmpty()) {
                val file = File(p.imageUri)
                if (file.exists()) {
                    ivImage.setImageURI(Uri.fromFile(file))
                } else {
                    ivImage.setImageResource(R.drawable.logo_huelluditos)
                }
            } else {
                ivImage.setImageResource(R.drawable.logo_huelluditos)
            }

            btnAdd.setOnClickListener { onAddToCart(p) }
            btnEdit.setOnClickListener { onEdit(p) }
            btnDelete.setOnClickListener { onDelete(p) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}

