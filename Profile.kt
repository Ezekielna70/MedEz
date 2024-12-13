package com.protel.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Profile : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page_pasien)

        val usernameTextView: TextView = findViewById(R.id.username_profile)
        val emailTextView: TextView = findViewById(R.id.email_profile)
        val jenisAkunTextView: TextView = findViewById(R.id.jenisAkun)

        // Retrieve data from SharedPreferences
        val sharedPreferences = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("pat_username", "Unknown")
        val email = sharedPreferences.getString("pat_email", "Unknown")
        val selectedButton = sharedPreferences.getString("selected_button", "default_value")

        if (selectedButton == "pendamping") {
            jenisAkunTextView.text = "Pendamping"
        } else {
            jenisAkunTextView.text = "Pasien"
        }


        usernameTextView.text = username
        emailTextView.text = email

        val logoutButton: Button = findViewById(R.id.btnLogout)

        logoutButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Navigate back to LoginActivity
            val intent = Intent(this, LandingPage1::class.java)
            startActivity(intent)
            finish()
        }
    }
}