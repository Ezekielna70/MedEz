package com.protel.myapplication.meds

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.protel.myapplication.R

class InfoObat : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_obat_page)

        // References to TextViews
        val namaObatTextView: TextView = findViewById(R.id.nama_obat_nya)
        val dosisTextView: TextView = findViewById(R.id.aturan_minum)
        val stokTextView: TextView = findViewById(R.id.sisa_obat)
        val deskripsiTextView: TextView = findViewById(R.id.deskripsi_container)

        // Retrieve data from intent
        val medUsername = intent.getStringExtra("medUsername")
        val medDosage = intent.getIntExtra("medDosage",0)
        val medFunction = intent.getStringExtra("medFunction")
        val medRemaining = intent.getIntExtra("medRemaining", 0)
        val consumptionTimes = intent.getStringArrayListExtra("consumptionTimes")

        // Update UI
        namaObatTextView.text = medUsername
        dosisTextView.text = "$medDosage"
        stokTextView.text = "$medRemaining"
        deskripsiTextView.text = medFunction

        // Optionally display consumption times
        consumptionTimes?.let {
            deskripsiTextView.append("\nWaktu Konsumsi: ${it.joinToString(", ")}")
        }
    }
}
