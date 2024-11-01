package com.protel.medez // Ganti dengan package yang sesuai

import android.content.Intent // Untuk navigasi antar Activity
import android.os.Bundle // Untuk menyimpan status Activity
import androidx.appcompat.app.AppCompatActivity // Untuk membuat Activity
import android.widget.RelativeLayout // Jika Anda menggunakan RelativeLayout di layout

class LandingPage2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terms_page_2)

        findViewById<RelativeLayout>(R.id.buttonayo).setOnClickListener {
            val intent = Intent(this, PilihJenisAkun::class.java)
            startActivity(intent)
            finish()
        }
    }
}