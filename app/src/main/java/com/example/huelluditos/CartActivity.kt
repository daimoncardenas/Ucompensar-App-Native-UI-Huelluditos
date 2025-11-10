package com.example.huelluditos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices

class CartActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: CartAdapter

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                obtainLocationAndSave()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        db = DatabaseHelper(this)

        val rv = findViewById<RecyclerView>(R.id.rvCart)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmPurchase)

        adapter = CartAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadCart()

        btnConfirm.setOnClickListener {
            checkLocationPermissionAndSave()
        }
    }

    private fun loadCart() {
        val prefs = getSharedPreferences("huelluditos_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("current_user_id", -1)
        if (userId != -1) {
            val items = db.getCartItemsForCustomer(userId)
            adapter.updateData(items)
        }
    }

    private fun checkLocationPermissionAndSave() {
        val perm = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) {
            obtainLocationAndSave()
        } else {
            locationPermissionLauncher.launch(perm)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun obtainLocationAndSave() {
        val prefs = getSharedPreferences("huelluditos_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("current_user_id", -1)
        if (userId == -1) return

        val client = LocationServices.getFusedLocationProviderClient(this)
        client.lastLocation.addOnSuccessListener { location ->
            val lat: Double
            val lon: Double

            if (location != null) {
                lat = location.latitude
                lon = location.longitude
            } else {
                // Ubicación por defecto (por ejemplo Bogotá)
                lat = 4.60971
                lon = -74.08175
                Toast.makeText(this, "Se usó una ubicación por defecto", Toast.LENGTH_SHORT).show()
            }

            db.saveCartLocation(userId, lat, lon)
            Toast.makeText(this, "Compra confirmada con ubicación registrada", Toast.LENGTH_LONG).show()
        }
    }

}
