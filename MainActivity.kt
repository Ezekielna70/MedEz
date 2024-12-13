package com.protel.myapplication


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.protel.myapplication.api.Patient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.protel.myapplication.api.ApiClient
import com.protel.myapplication.databinding.MainBinding
import com.protel.myapplication.meds.ReminderList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.protel.myapplication.api.PatientsResponse


class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainBinding
    private var patients: List<Patient> = emptyList() // Store patients from API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        val sharedPreferences = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val selectedButton = sharedPreferences.getString("selected_button", "default_value")

        if (selectedButton == "pendamping") {
            binding.scanqrButton.visibility = View.VISIBLE
            binding.helpButtonContainer.visibility = View.GONE
            binding.patInfoContainer.visibility = View.VISIBLE

            fetchPatients { patientUsernames ->
                val dropdown = binding.patientDropdown
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, patientUsernames).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                dropdown.adapter = adapter

                dropdown.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedPatient = patients[position]
                        binding.namaPasien.text = selectedPatient.PatUsername
                        binding.agePasien.text = "${selectedPatient.PatAge} Tahun"

                        val editor = sharedPreferences.edit()
                        editor.putString("selected_patient_id", selectedPatient.PatID) // Simpan PatID
                        editor.putString("selected_patient_username", selectedPatient.PatUsername) // Simpan Username
                        editor.putInt("selected_patient_age", selectedPatient.PatAge) // Simpan Umur
                        editor.apply()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Optional: Handle no selection
                    }
                })
            }
        } else if (selectedButton == "patient") {
            binding.scanqrButton.visibility = View.GONE
            binding.helpButtonContainer.visibility = View.VISIBLE
            binding.patInfoContainer.visibility = View.GONE
        }

        if (isLoggedIn) {
            setContentView(binding.root)
        } else {
            val intent = Intent(this, LandingPage1::class.java)
            startActivity(intent)
            finish()
        }

        binding.obatButton.setOnClickListener {
            val intent = Intent(this, ReminderList::class.java)
            startActivity(intent)
        }

        binding.cekProfilButton.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        binding.scanqrButton.setOnClickListener {
            val intent = Intent(this, ScanQr::class.java)
            startActivity(intent)
        }
    }

    private fun initBinding() {
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun fetchPatients(onResult: (List<String>) -> Unit) {
        val sharedPreferences = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val caregiverId = sharedPreferences.getString("care_id", "Unknown")

        if (caregiverId != null) {
            ApiClient.apiService.getPatients(caregiverId).enqueue(object : Callback<PatientsResponse> {
                override fun onResponse(call: Call<PatientsResponse>, response: Response<PatientsResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        patients = response.body()?.patients ?: emptyList()
                        val patientUsernames = patients.map { it.PatUsername }
                        onResult(patientUsernames)
                    } else {
                        onResult(emptyList())
                        Toast.makeText(this@MainActivity, "Failed to fetch patients", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PatientsResponse>, t: Throwable) {
                    onResult(emptyList())
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
