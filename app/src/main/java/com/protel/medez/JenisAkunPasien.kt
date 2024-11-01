package com.protel.medez

import android.content.Intent // Untuk navigasi antar Activity
import android.os.Bundle // Untuk menyimpan status Activity
import androidx.appcompat.app.AppCompatActivity // Untuk membuat Activity
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback



class JenisAkunPasien : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jenis_akun_pasien)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@JenisAkunPasien, PilihJenisAkun::class.java)
                startActivity(intent)
                finish()
            }
        })

        findViewById<TextView>(R.id.buttonscanQRpasien).setOnClickListener {
            val intent = Intent(this, ScanQRPasien::class.java)
            startActivity(intent)
            finish()
        }
    }



}