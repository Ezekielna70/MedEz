package com.protel.medez // Ganti dengan package yang sesuai

import android.content.Intent // Untuk navigasi antar Activity
import android.os.Bundle // Untuk menyimpan status Activity
import androidx.appcompat.app.AppCompatActivity // Untuk membuat Activity
import android.widget.RelativeLayout


class LandingPage1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terms_page_1)

        val preferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("isFirstLaunch", false) // Mengubah status menjadi tidak pertama kali
        editor.apply()

        val buttonMulai = findViewById<RelativeLayout>(R.id.buttonmulai)
        buttonMulai.setOnClickListener {
            val intent = Intent(this, LandingPage2::class.java)
            startActivity(intent)
            finish()
        }
    }
}