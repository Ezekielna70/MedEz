package com.protel.myapplication.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.protel.myapplication.api.ApiClient
import com.protel.myapplication.api.SignupResponse
import com.protel.myapplication.api.careSignupRequest
import com.protel.myapplication.api.patSignupRequest
import com.protel.myapplication.databinding.SignupPageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpPage : AppCompatActivity() {
    private lateinit var binding: SignupPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SignupPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        val deviceId = sharedPreferences.getString("DEVICE_ID", null)
        val selectedButton = sharedPreferences.getString("selected_button", "default_value")

        if (selectedButton == "pendamping") {
            binding.deviceIDContainer.visibility = View.GONE
        } else {
            binding.deviceIDContainer.visibility = View.VISIBLE
        }

        if (deviceId != null && selectedButton == "pasien") {

            binding.deviceIDContainer.visibility = View.VISIBLE
            binding.deviceIDContainer.text = "Device ID: $deviceId"
        } else if (selectedButton == "pendamping"){

            binding.deviceIDContainer.visibility = View.GONE
        }

        Log.e("device ID", "Device ID: $deviceId")


        binding.SignUpButton.setOnClickListener {
            val username = binding.username.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val ageText = binding.usia.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || ageText.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageText.toIntOrNull()
            if (age == null || age <= 0) {
                Toast.makeText(this, "Usia invalid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (deviceId != null && selectedButton == "pasien") {
                binding.deviceIDContainer.visibility = View.VISIBLE
                signUpAsPatient(username, email, password, age, deviceId)

            } else if (selectedButton == "pendamping") {
                binding.deviceIDContainer.visibility = View.GONE
                signUpAsPendamping(username, email, password, age)
            } 

        }

        binding.punyaakun.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signUpAsPendamping(username: String, email: String, password: String, age: Int) {
        val request = careSignupRequest(
            care_username = username,
            care_email = email,
            care_password = password,
            care_age = age
        )

        ApiClient.apiService.careSignUp(request).enqueue(object : Callback<SignupResponse> {
            override fun onResponse(call: Call<SignupResponse>, response: Response<SignupResponse>) {
                if (response.isSuccessful) {
                    val signupResponse = response.body()
                    if (signupResponse?.status == "success") {
                        Toast.makeText(this@SignUpPage, signupResponse.message, Toast.LENGTH_SHORT).show()

                    } else {
                        val errorResponse = response.body()
                        Toast.makeText(this@SignUpPage, "Error: ${errorResponse?.message}", Toast.LENGTH_SHORT).show()
                        Log.e("SignUpFailure", "Request failed: ${errorResponse?.message}")
                    }
                } else {
                    val errorResponse = response.body()
                    Toast.makeText(this@SignUpPage, "Error: ${errorResponse?.message}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignUpPage, SignUpPage::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                Log.e("SignUpFailure", "Request failed: ${t.localizedMessage}")
            }
        })
    }


    private fun signUpAsPatient(username: String, email: String, password: String, age: Int, deviceId: String) {
        val request = patSignupRequest(
            pat_username = username,
            pat_email = email,
            pat_password = password,
            pat_age = age,
            dev_id = deviceId
        )

        ApiClient.apiService.patSignUp(request).enqueue(object : Callback<SignupResponse> {
            override fun onResponse(call: Call<SignupResponse>, response: Response<SignupResponse>) {
                if (response.isSuccessful) {
                    val signupResponse = response.body()
                    if (signupResponse?.status == "success") {
                        Toast.makeText(this@SignUpPage, signupResponse.message, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignUpPage, LoginPage::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorResponse = response.body()
                        Toast.makeText(this@SignUpPage, "Error: ${errorResponse?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorResponse = response.body()
                    Toast.makeText(this@SignUpPage, "Error: ${errorResponse?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                Log.e("SignUpFailure", "Request failed: ${t.localizedMessage}")
            }
        })
    }
}

