package com.protel.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.protel.myapplication.authentication.SignUpPage

class PilihAkun : AppCompatActivity() {

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pilih_jenis_akun)


        findViewById<Button>(R.id.pilih_akun_pasien).setOnClickListener {
            saveButtonSelection("pasien")
            val intent = Intent(this, ScanQr::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.pilih_akun_pendamping).setOnClickListener {
            saveButtonSelection("pendamping")
            val intent = Intent(this, SignUpPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveButtonSelection(selection: String) {
        val sharedPreferences = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selected_button", selection)
        editor.apply()
    }
}
