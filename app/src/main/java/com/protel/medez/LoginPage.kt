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
import com.protel.medez.databinding.LoginPageBinding

class LoginPage : AppCompatActivity() {
    private lateinit var binding : LoginPageBinding

    lateinit var email: Email
    lateinit var password : EditText
    lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener(View.OnClickListener {
            if(binding.email.text.toString() == "medez@gmail.com" && binding.password.text.toString() == "1234"){
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<TextView>(R.id.buatAkun).setOnClickListener {
            val intent = Intent(this, SignUpPage::class.java)
            startActivity(intent)
            finish()
        }
    }
}