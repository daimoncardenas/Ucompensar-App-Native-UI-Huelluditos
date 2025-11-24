package com.example.huelluditos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.huelluditos.ui.StoreLocationsActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = DatabaseHelper(this)

        // Inicializar vistas del perfil
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        val tvLogout: TextView = findViewById(R.id.tvLogout)
        val tvEditProfile: TextView = findViewById(R.id.tvEditProfile)
        val tvProductManagement: TextView = findViewById(R.id.tvProductManagement)
        val tvCustomerManagement: TextView = findViewById(R.id.tvCustomerManagement)

        // Botón para abrir las tiendas Huelluditos (nueva Activity)
        val btnOpenStoreLocations: Button = findViewById(R.id.btnOpenStoreLocations)
        btnOpenStoreLocations.setOnClickListener {
            val intent = Intent(this, StoreLocationsActivity::class.java)
            startActivity(intent)
        }

        // Obtener id del usuario logueado
        val prefs = getSharedPreferences("huelluditos_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("current_user_id", -1)

        if (userId != -1) {
            val user = getUserInfo(userId)
            user?.let {
                tvUserName.text = it.name
                tvUserEmail.text = it.email
            }
        }

        // "Editar perfil"
        tvEditProfile.setOnClickListener {
            showEditProfileDialog(userId)
        }

        // "Gestión de Productos"
        tvProductManagement.setOnClickListener {
            startActivity(Intent(this, ProductListActivity::class.java))
        }

        // "Gestión de Clientes"
        tvCustomerManagement.setOnClickListener {
            startActivity(Intent(this, CustomerListActivity::class.java))
        }

        // Cerrar sesión
        tvLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun getUserInfo(id: Int): UserProfile? {
        val dbReadable = db.readableDatabase
        val cursor = dbReadable.query(
            "customers",
            arrayOf("id", "name", "email"),
            "id = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return UserProfile(
                    id = it.getInt(0),
                    name = it.getString(1),
                    email = it.getString(2)
                )
            }
        }
        return null
    }

    private fun showEditProfileDialog(userId: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_customer, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etCustomerName)
        val etEmail = dialogView.findViewById<android.widget.EditText>(R.id.etCustomerEmail)
        val etPassword = dialogView.findViewById<android.widget.EditText>(R.id.etCustomerPassword)

        // Pre-fill with current user data
        val currentUser = getUserInfo(userId)
        currentUser?.let {
            etName.setText(it.name)
            etEmail.setText(it.email)
        }

        // Hint para la contraseña nueva
        etPassword.hint = "Nueva contraseña (opcional)"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Editar Mi Perfil")
            .setView(dialogView)
            .setPositiveButton("Guardar cambios") { _, _ ->
                val name = etName.text.toString()
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()

                if (name.isNotEmpty() && email.isNotEmpty()) {
                    val success = db.updateCustomer(
                        userId,
                        name,
                        email,
                        password.ifEmpty { null }
                    )
                    if (success) {
                        // Actualizar datos visibles
                        tvUserName.text = name
                        tvUserEmail.text = email
                        Toast.makeText(this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    data class UserProfile(
        val id: Int,
        val name: String,
        val email: String
    )
}
