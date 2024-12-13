package com.protel.myapplication.meds

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.protel.myapplication.R
import com.protel.myapplication.api.ApiClient
import com.protel.myapplication.api.DeleteMedicineResponse
import com.protel.myapplication.api.Medicine
import com.protel.myapplication.api.MedicinesResponse
import com.protel.myapplication.api.Reminder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReminderList : AppCompatActivity() {

    private val reminders = mutableListOf<Reminder>()
    private lateinit var reminderAdapter: ReminderAdapter

    companion object {
        const val ADD_REMINDER_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_reminder_page)

        val btnTambahJadwal: Button = findViewById(R.id.btnTambahJadwal)
        val rvSchedule: RecyclerView = findViewById(R.id.rvSchedule)

        reminderAdapter = ReminderAdapter(reminders) { reminder ->
            // Ketika delete ditekan, panggil fungsi untuk hapus obat
            deleteMedicineFromAPI(reminder)
        }
        rvSchedule.layoutManager = LinearLayoutManager(this)
        rvSchedule.adapter = reminderAdapter

        btnTambahJadwal.setOnClickListener {
            val intent = Intent(this, AddReminder::class.java)
            startActivityForResult(intent, ADD_REMINDER_REQUEST_CODE)
        }

        fetchMedicines()

        val sharedPreferences = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val selectedButton = sharedPreferences.getString("selected_button", "default_value")

        if (selectedButton == "pasien") {
            btnTambahJadwal.visibility = View.INVISIBLE
            btnTambahJadwal.isClickable = false
        } else {
            btnTambahJadwal.visibility = View.VISIBLE
            btnTambahJadwal.isClickable = true
        }
    }

    private fun fetchMedicines() {
        val sharedPreferences = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val selectedButton = sharedPreferences.getString("selected_button", "default_value")

        val patientId = if (selectedButton == "pendamping") {
            // Ambil selectedPatientID dari SharedPreferences
            sharedPreferences.getString("selected_patient_id", null)
        } else if (selectedButton == "pasien") {
            // Ambil pat_username dari SharedPreferences
            sharedPreferences.getString("pat_id", null)
        } else {
            null
        }

        if (patientId != null) {
            ApiClient.apiService.getMedicines(patientId).enqueue(object : Callback<MedicinesResponse> {
                override fun onResponse(call: Call<MedicinesResponse>, response: Response<MedicinesResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val medicines = response.body()?.medicines ?: emptyList()
                        updateReminders(medicines)
                    } else {
                        Toast.makeText(this@ReminderList, "Failed to fetch medicines", Toast.LENGTH_SHORT).show()
                        Log.e("ReminderList", "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<MedicinesResponse>, t: Throwable) {
                    Toast.makeText(this@ReminderList, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ReminderList", "Request failed: ${t.message}")
                }
            })
        } else {
            Toast.makeText(this, "Patient ID is missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateReminders(medicines: List<Medicine>) {
        reminders.clear() // Clear existing reminders
        for (medicine in medicines) {
            reminders.add(
                Reminder(
                    med_id = medicine.med_id,
                    med_username = medicine.med_username,
                    med_dosage = medicine.med_dosage,
                    med_function = medicine.med_function,
                    med_remaining = medicine.med_remaining,
                    consumption_times = medicine.consumption_times,
                    med_slot = medicine.med_slot
                )
            )
        }
        reminderAdapter.notifyDataSetChanged() // Refresh RecyclerView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_REMINDER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val medUsername = data.getStringExtra("medUsername")
            val medFunction = data.getStringExtra("medFunction")
            val medDosage = data.getIntExtra("medDosage", 0)
            val medRemaining = data.getIntExtra("medRemaining", 0)
            val consumptionTimes = data.getStringArrayListExtra("consumptionTimes")
            val medSlot = data.getIntExtra("med_slot",0)

            if (medUsername != null && medFunction != null && medDosage != null && consumptionTimes != null) {
                val dummyMedId = ""
                reminders.add(

                    Reminder(
                        dummyMedId,
                        medUsername,
                        medDosage,
                        medFunction,
                        medRemaining,
                        consumptionTimes,
                        medSlot
                    )
                )
                reminderAdapter.notifyDataSetChanged() // Refresh RecyclerView
            }
        }
    }

    private fun deleteMedicineFromAPI(reminder: Reminder) {
        val sharedPreferences = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        val selectedButton = sharedPreferences.getString("selected_button", "default_value")

        // Tentukan patientId sesuai logic Anda:
        // Jika pendamping, ambil dari "selected_patient_id"
        // Jika pasien, ambil dari "pat_username"
        val patientId = if (selectedButton == "pendamping") {
            sharedPreferences.getString("selected_patient_id", null)
        } else {
            sharedPreferences.getString("pat_id", null)
        }

        val medId = reminder.med_id // Pastikan Reminder memiliki field med_id

        if (patientId != null && medId != null) {
            ApiClient.apiService.deleteMedicine(patientId, medId).enqueue(object : Callback<DeleteMedicineResponse> {
                override fun onResponse(call: Call<DeleteMedicineResponse>, response: Response<DeleteMedicineResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@ReminderList, response.body()?.message ?: "Deleted", Toast.LENGTH_SHORT).show()
                        // Hapus item dari adapter
                        reminderAdapter.removeItem(reminder)
                    } else {
                        Toast.makeText(this@ReminderList, "Failed to delete medicine", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DeleteMedicineResponse>, t: Throwable) {
                    Toast.makeText(this@ReminderList, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Patient ID or Medicine ID is missing", Toast.LENGTH_SHORT).show()
        }
    }
}
