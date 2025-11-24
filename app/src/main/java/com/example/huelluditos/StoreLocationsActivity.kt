package com.example.huelluditos.ui


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.huelluditos.R

class StoreLocationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_locations)

        // Suba
        val btnOpenSubaInMaps: Button = findViewById(R.id.btnOpenSubaInMaps)
        btnOpenSubaInMaps.setOnClickListener {
            openStoreInMaps(
                latitude = 4.7485,
                longitude = -74.0820,
                label = getString(R.string.store_suba_name)
            )
        }

        // Teusaquillo
        val btnOpenTeusaquilloInMaps: Button = findViewById(R.id.btnOpenTeusaquilloInMaps)
        btnOpenTeusaquilloInMaps.setOnClickListener {
            openStoreInMaps(
                latitude = 4.6390,
                longitude = -74.0740,
                label = getString(R.string.store_teusaquillo_name)
            )
        }

        // Villavicencio
        val btnOpenVillavicencioInMaps: Button = findViewById(R.id.btnOpenVillavicencioInMaps)
        btnOpenVillavicencioInMaps.setOnClickListener {
            openStoreInMaps(
                latitude = 4.1420,
                longitude = -73.6266,
                label = getString(R.string.store_villavicencio_name)
            )
        }

    }

    private fun openStoreInMaps(latitude: Double, longitude: Double, label: String) {
        // URI estilo geo:lat,lon?q=lat,lon(label)
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")

        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            // Opcional: forzar la apertura con Google Maps si está instalado
            setPackage("com.google.android.apps.maps")
        }

        try {
            startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            // Por si no tiene Google Maps, intent genérico
            val unrestrictedIntent = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(unrestrictedIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "No se ha encontrado una aplicación de mapas en el dispositivo.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
