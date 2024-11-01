package com.protel.medez

import android.content.Intent // Untuk navigasi antar Activity
import android.os.Bundle // Untuk menyimpan status Activity
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity // Untuk membuat Activity
import android.widget.RelativeLayout

class PilihJenisAkun : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pilih_jenis_akun)

        findViewById<ImageView>(R.id.iconpasien).setOnClickListener {
            val intent = Intent(this, JenisAkunPasien::class.java)
            startActivity(intent)
            finish()
            //mak kau hijau
        }

        findViewById<ImageView>(R.id.iconpendamping).setOnClickListener {
            val intent = Intent(this, JenisAkunPendamping::class.java)
            startActivity(intent)
            finish()
        }

    }
}