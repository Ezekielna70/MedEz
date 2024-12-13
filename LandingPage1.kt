package com.protel.myapplication // Ganti dengan package yang sesuai

import android.content.Intent // Untuk navigasi antar Activity
import android.os.Bundle // Untuk menyimpan status Activity
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity // Untuk membuat Activity
import android.widget.RelativeLayout


class LandingPage1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terms_page_1)

        val preferences = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        val isLoggedIn = preferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            setContentView(R.layout.terms_page_1)
        }

        val buttonMulai = findViewById<Button>(R.id.button_mulai)
        buttonMulai.setOnClickListener {
            val intent = Intent(this, PilihAkun::class.java)
            startActivity(intent)
            finish()
        }
    }
}