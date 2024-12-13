package com.protel.myapplication.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.protel.myapplication.MainActivity
import com.protel.myapplication.api.ApiClient
import com.protel.myapplication.api.careLoginResponse
import com.protel.myapplication.api.patLoginResponse
 // Add import for CareLoginRequest
import com.protel.myapplication.api.careLoginRequest
import com.protel.myapplication.api.patLoginRequest
import com.protel.myapplication.databinding.LoginPageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginPage : AppCompatActivity() {
    private lateinit var binding: LoginPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val selectedButton = sharedPreferences.getString("selected_button", "default_value")

        binding.loginButton.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedButton == "pendamping") {
                careLogin(email, password)
            } else {
                patLogin(email, password)
            }
        }

        binding.buatAkun.setOnClickListener{
            val intent = Intent(this@LoginPage, SignUpPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Function to login as "pendamping"
    private fun careLogin(email: String, password: String) {
        val request = careLoginRequest(
            care_email = email,
            care_password = password
        )

        ApiClient.apiService.careLogin(request).enqueue(object : Callback<careLoginResponse> {
            override fun onResponse(call: Call<careLoginResponse>, response: Response<careLoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Toast.makeText(this@LoginPage, "Login berhasil: ${loginResponse.message}", Toast.LENGTH_SHORT).show()

                        val sharedPreferences = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("pat_username", loginResponse.data?.care_username)
                        editor.putString("pat_email", loginResponse.data?.care_email)
                        editor.putString("care_id", loginResponse.data?.care_id)
                        editor.putBoolean("isLoggedIn", true)  // Menyimpan status login
                        editor.apply()

                        val intent = Intent(this@LoginPage, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this@LoginPage, "Response kosong dari server", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Error tidak diketahui"
                    Toast.makeText(this@LoginPage, "Login gagal: $errorMessage", Toast.LENGTH_SHORT).show()
                    Log.e("LoginFailure", "Request failed: $errorMessage")
                }
            }

            override fun onFailure(call: Call<careLoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginPage, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to login as "patient"
    private fun patLogin(email: String, password: String) {
        val request = patLoginRequest(
            pat_email = email,
            pat_password = password
        )

        ApiClient.apiService.patLogin(request).enqueue(object : Callback<patLoginResponse> {
            override fun onResponse(call: Call<patLoginResponse>, response: Response<patLoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Toast.makeText(this@LoginPage, "Login berhasil: ${loginResponse.message}", Toast.LENGTH_SHORT).show()

                        val sharedPreferences = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("pat_username", loginResponse.data?.pat_username)
                        editor.putString("pat_email", loginResponse.data?.pat_email)
                        editor.putString("pat_id", loginResponse.data?.pat_id)
                        editor.putBoolean("isLoggedIn", true)  // Menyimpan status login
                        editor.apply()

                        // Setelah login berhasil, arahkan ke MainActivity
                        val intent = Intent(this@LoginPage, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this@LoginPage, "Response kosong dari server", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Error tidak diketahui"
                    Toast.makeText(this@LoginPage, "Login gagal: $errorMessage", Toast.LENGTH_SHORT).show()
                    Log.e("LoginFailure", "Request failed: $errorMessage")
                }
            }

            override fun onFailure(call: Call<patLoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginPage, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
