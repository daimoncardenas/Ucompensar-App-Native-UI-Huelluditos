package com.example.huelluditos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CustomerListActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: CustomerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        db = DatabaseHelper(this)

        val rv = findViewById<RecyclerView>(R.id.rvCustomers)
        val btnAdd = findViewById<Button>(R.id.btnAddCustomer)

        adapter = CustomerAdapter(
            emptyList(),
            onEdit = { customer ->
                showEditDialog(customer)
            },
            onDelete = { customer ->
                // Prevent deleting current logged in user
                val prefs = getSharedPreferences("huelluditos_prefs", MODE_PRIVATE)
                val currentUserId = prefs.getInt("current_user_id", -1)

                if (customer.id == currentUserId) {
                    Toast.makeText(this, "No puedes eliminar tu propio usuario", Toast.LENGTH_SHORT).show()
                } else {
                    deleteCustomer(customer)
                }
            }
        )

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadCustomers()

        btnAdd.setOnClickListener {
            showCreateDialog()
        }
    }

    private fun loadCustomers() {
        val customers = db.getAllCustomers()
        adapter.updateData(customers)
    }

    private fun showCreateDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_customer, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etCustomerName)
        val etEmail = dialogView.findViewById<android.widget.EditText>(R.id.etCustomerEmail)
        val etPassword = dialogView.findViewById<android.widget.EditText>(R.id.etCustomerPassword)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Nuevo Cliente")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString()
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()

                if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    val success = db.registerUser(name, email, password)
                    if (success) {
                        Toast.makeText(this, "Cliente creado exitosamente", Toast.LENGTH_SHORT).show()
                        loadCustomers()
                    } else {
                        Toast.makeText(this, "Error: El email ya existe", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(customer: Customer) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_customer, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etCustomerName)
        val etEmail = dialogView.findViewById<android.widget.EditText>(R.id.etCustomerEmail)
        val etPassword = dialogView.findViewById<android.widget.EditText>(R.id.etCustomerPassword)

        etName.setText(customer.name)
        etEmail.setText(customer.email)
        // Password field left empty for security

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Editar Cliente")
            .setView(dialogView)
            .setPositiveButton("Guardar cambios") { _, _ ->
                val name = etName.text.toString()
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()

                if (name.isNotEmpty() && email.isNotEmpty()) {
                    val success = db.updateCustomer(customer.id, name, email, password.ifEmpty { null })
                    if (success) {
                        Toast.makeText(this, "Cliente actualizado exitosamente", Toast.LENGTH_SHORT).show()
                        loadCustomers()
                    } else {
                        Toast.makeText(this, "Error al actualizar el cliente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteCustomer(customer: Customer) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar al cliente ${customer.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                val success = db.deleteCustomer(customer.id)
                if (success) {
                    Toast.makeText(this, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                    loadCustomers()
                } else {
                    Toast.makeText(this, "Error al eliminar el cliente", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}