package com.protel.medez

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class PilihJenisAkun : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pilih_jenis_akun)

        findViewById<ImageView>(R.id.iconpasien).setOnClickListener {
            val intent = Intent(this, JenisAkunPasien::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.iconpendamping).setOnClickListener {
            val intent = Intent(this, JenisAkunPendamping::class.java)
            startActivity(intent)
            finish()
        }

    }
}