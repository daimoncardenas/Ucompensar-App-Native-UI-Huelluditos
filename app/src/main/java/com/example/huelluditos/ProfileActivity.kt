package com.example.huelluditos

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = DatabaseHelper(this)

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        val tvLogout = findViewById<TextView>(R.id.tvLogout)
        val tvEditProfile = findViewById<TextView>(R.id.tvEditProfile)
        val tvSettings = findViewById<TextView>(R.id.tvSettings)

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

        // Usar "Editar perfil" para ir al listado de productos (puedes cambiarlo a tu gusto)
        tvEditProfile.setOnClickListener {
            startActivity(Intent(this, ProductListActivity::class.java))
        }

        // O usar "Configuración" para lo mismo (elige uno de los dos)
        tvSettings.setOnClickListener {
            startActivity(Intent(this, ProductListActivity::class.java))
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

    data class UserProfile(
        val id: Int,
        val name: String,
        val email: String
    )
}
