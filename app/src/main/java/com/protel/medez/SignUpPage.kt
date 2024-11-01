package com.protel.medez

import android.content.Intent // Untuk navigasi antar Activity
import android.os.Bundle // Untuk menyimpan status Activity
import android.provider.ContactsContract.CommonDataKinds.Email
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity // Untuk membuat Activity
import com.protel.medez.databinding.SignupPageBinding

class SignUpPage : AppCompatActivity(){
    private lateinit var binding : SignupPageBinding

    lateinit var username : EditText
    lateinit var email:  Email
    lateinit var password : EditText
    lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SignupPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.SignUpButton.setOnClickListener(View.OnClickListener {
            if(binding.username.text.toString() == "user" && binding.email.text.toString() == "medez@gmail.com" && binding.password.text.toString() == "1234"){
                Toast.makeText(this, "Akun Berhasil Dibuat!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, LoginPage::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this, "Gagal membuat Akun", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<TextView>(R.id.punyaakun).setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
            finish()
        }
    }
}