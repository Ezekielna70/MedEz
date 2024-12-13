package com.protel.myapplication

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.protel.myapplication.api.ApiClient
import com.protel.myapplication.api.DeviceStoreRequest
import com.protel.myapplication.api.DeviceStoreResponse
import com.protel.myapplication.authentication.LoginPage
import com.protel.myapplication.authentication.SignUpPage
import com.protel.myapplication.databinding.ScanQrBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ScanQr : AppCompatActivity() {
    private lateinit var binding: ScanQrBinding


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                showCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                val sharedPreferences = getSharedPreferences("APP_PREF", MODE_PRIVATE)
                val selectedButton = sharedPreferences.getString("selected_button", "default_value")
                saveDeviceIdToSharedPreferences(result.contents)

                if(selectedButton == "pasien"){
                    navigateToSignUpPage()

                }else{
                    val deviceId = binding.textResult.text // Replace with your input
                    val deviceUsername = "user1" // Replace with your input
                    val deviceStatus = "active" // Replace with your input
                    val deviceTime = "2024-12-05T12:34:56Z" // Replace with your input
                    val caregiverId = sharedPreferences.getString("care_id",null) // Replace with your input

                    val request = caregiverId?.let {
                        DeviceStoreRequest(
                            dev_id = deviceId.toString(),
                            dev_username = deviceUsername,
                            dev_status = deviceStatus,
                            dev_time = deviceTime,
                            care_id = it
                        )
                    }

                    if (request != null) {
                        storeDevice(request)
                    }
                    navigateToHomePage()

                }

            }
        }

    private fun storeDevice(request: DeviceStoreRequest) {
        ApiClient.apiService.storeDevice(request).enqueue(object : Callback<DeviceStoreResponse> {
            override fun onResponse(call: Call<DeviceStoreResponse>, response: Response<DeviceStoreResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.device != null) {
                        Toast.makeText(
                            this@ScanQr,
                            "Success: ${responseBody.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("DeviceAPI", "Stored device: ${responseBody.device}")
                    } else {
                        Toast.makeText(
                            this@ScanQr,
                            "Failed: ${responseBody?.details}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("DeviceAPI", "Failed to store device: ${responseBody?.details}")
                    }
                } else {
                    Toast.makeText(
                        this@ScanQr,
                        "Failed with status code: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("DeviceAPI", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<DeviceStoreResponse>, t: Throwable) {
                Toast.makeText(this@ScanQr, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("DeviceAPI", "Request failed: ${t.message}")
            }
        })
    }

    private fun saveDeviceIdToSharedPreferences(deviceId: String) {
        binding.textResult.text = deviceId

        val sharedPreferences = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("DEVICE_ID", deviceId)
        editor.apply()



    }

    private fun navigateToHomePage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignUpPage() {
        val intent = Intent(this, SignUpPage::class.java)
        startActivity(intent)
        finish()
    }

    private fun showCamera() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan QR code")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setOrientationLocked(true)
        }
        scanLauncher.launch(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initViews()

        binding.loginAja.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        binding.qrLogo.setOnClickListener {
            checkPermissionCamera(this)
        }
    }

    private fun checkPermissionCamera(context: Context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showCamera()
        } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            Toast.makeText(context, "CAMERA permission required", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun initBinding() {
        binding = ScanQrBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
