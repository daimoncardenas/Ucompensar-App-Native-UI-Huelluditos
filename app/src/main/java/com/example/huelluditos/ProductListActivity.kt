package com.example.huelluditos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProductListActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        db = DatabaseHelper(this)

        val rv = findViewById<RecyclerView>(R.id.rvProducts)
        val btnAdd = findViewById<Button>(R.id.btnAddProduct)
        val btnCart = findViewById<Button>(R.id.btnCart)

        adapter = ProductAdapter(
            emptyList(),
            onAddToCart = { product ->
                val prefs = getSharedPreferences("huelluditos_prefs", MODE_PRIVATE)
                val userId = prefs.getInt("current_user_id", -1)
                if (userId != -1) {
                    db.addProductToCart(userId, product)
                    Toast.makeText(this, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
                }
            },
            onEdit = { product ->
                showEditDialog(product)
            },
            onDelete = { product ->
                db.deleteProduct(product.id)
                loadProducts()
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
        )

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadProducts()

        btnAdd.setOnClickListener {
            showCreateDialog()
        }

        btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun loadProducts() {
        val products = db.getAllProducts()
        adapter.updateData(products)
    }

    private fun showCreateDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etProductName)
        val etPrice = dialogView.findViewById<android.widget.EditText>(R.id.etProductPrice)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Nuevo producto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString()
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                db.insertProduct(name, price, "Producto añadido manualmente")
                loadProducts()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(product: Product) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etProductName)
        val etPrice = dialogView.findViewById<android.widget.EditText>(R.id.etProductPrice)

        etName.setText(product.name)
        etPrice.setText(product.price.toString())

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Editar producto")
            .setView(dialogView)
            .setPositiveButton("Guardar cambios") { _, _ ->
                val name = etName.text.toString()
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                db.updateProduct(product.id, name, price)
                loadProducts()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
