package com.protel.medez

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.protel.medez.databinding.SignupPageBinding
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Define the API service interface
interface ApiService {
    @POST("/patient/signup")
    fun registerPatient(@Body requestBody: SignUpRequest): Call<SignUpResponse>
}

data class SignUpRequest(
    val pat_username: String,
    val pat_email: String,
    val pat_password: String
)

// Response data class
data class SignUpResponse(
    val status: String,
    val message: String
)

class SignUpPage : AppCompatActivity() {
    private lateinit var binding: SignupPageBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://greatly-closing-monitor.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Handle sign-up button click
        binding.SignUpButton.setOnClickListener(View.OnClickListener {
            val username = binding.username.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString() // Replace with actual device ID if available

            if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                registerPatient(username, email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<TextView>(R.id.punyaakun).setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerPatient(username: String, email: String, password: String) {
        val request = SignUpRequest(username, email, password)

        apiService.registerPatient(request).enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.status == "success") {
                        Toast.makeText(this@SignUpPage, responseBody.message, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignUpPage, LoginPage::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@SignUpPage, responseBody?.message ?: "Error occurred", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SignUpPage, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                Toast.makeText(this@SignUpPage, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
