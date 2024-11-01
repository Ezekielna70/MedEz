package com.protel.medez

import android.content.Intent // Untuk navigasi antar Activity
import android.os.Bundle // Untuk menyimpan status Activity
import androidx.appcompat.app.AppCompatActivity // Untuk membuat Activity
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback

class JenisAkunPendamping : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jenis_akun_pendamping)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@JenisAkunPendamping, PilihJenisAkun::class.java)
                startActivity(intent)
                finish()
            }
        })

        findViewById<RelativeLayout>(R.id.buttonscanQRpendamping).setOnClickListener {
            val intent = Intent(this, ScanQRPendamping::class.java)
            startActivity(intent)
            finish()
        }
    }

}