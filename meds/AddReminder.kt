package com.protel.myapplication.meds

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.protel.myapplication.R
import com.protel.myapplication.api.*
import com.protel.myapplication.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AddReminder : AppCompatActivity() {
    private lateinit var spinnerMedSlot: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_reminder_page)

        val etReminderName: EditText = findViewById(R.id.etReminderName)
        val etReminderDescription: EditText = findViewById(R.id.etReminderDescription)
        val etContainerA: EditText = findViewById(R.id.etContainerA)
        val etContainerB: EditText = findViewById(R.id.etContainerB)

        spinnerMedSlot = findViewById(R.id.spinnerSlot)


        val btnSetTime1: Button = findViewById(R.id.btnSetTime1)
        val btnSetTime2: Button = findViewById(R.id.btnSetTime2)
        val btnSetTime3: Button = findViewById(R.id.btnSetTime3)

        val timeSet1: TextView = findViewById(R.id.TimeSet1)
        val timeSet2: TextView = findViewById(R.id.TimeSet2)
        val timeSet3: TextView = findViewById(R.id.TimeSet3)

        val btnSaveReminder: Button = findViewById(R.id.btnSaveReminder)
        val btnCancel: Button = findViewById(R.id.btnCancel)

        // Variables to store selected times
        val selectedTimes = mutableListOf<String>()

        // TimePickerDialog for Alarm 1
        btnSetTime1.setOnClickListener {
            showTimePicker { time ->
                timeSet1.text = time
                timeSet1.visibility = View.VISIBLE
                selectedTimes.add(time)
            }
        }

        // TimePickerDialog for Alarm 2
        btnSetTime2.setOnClickListener {
            showTimePicker { time ->
                timeSet2.text = time
                timeSet2.visibility = View.VISIBLE
                selectedTimes.add(time)
            }
        }

        // TimePickerDialog for Alarm 3
        btnSetTime3.setOnClickListener {
            showTimePicker { time ->
                timeSet3.text = time
                timeSet3.visibility = View.VISIBLE
                selectedTimes.add(time)
            }
        }

        btnSaveReminder.setOnClickListener {
            val reminderName = etReminderName.text.toString()
            val reminderDescription = etReminderDescription.text.toString()
            val medDosageString = etContainerA.text.toString()
            val medRemainingString = etContainerB.text.toString()

            if (reminderName.isEmpty() || reminderDescription.isEmpty() || medDosageString.isEmpty() || medRemainingString.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val medRemaining = medRemainingString.toIntOrNull() ?: 0
            val medDosage = medDosageString.toIntOrNull() ?: 0

            val selectedMedSlotString = spinnerMedSlot.selectedItem.toString()
            val medSlot = selectedMedSlotString.toInt()


            // Ambil patientId dari SharedPreferences
            val sharedPreferences = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            val selectedButton = sharedPreferences.getString("selected_button", "default_value")

            val patientId = if (selectedButton == "pendamping") {
                sharedPreferences.getString("selected_patient_id", null)
            } else {
                sharedPreferences.getString("pat_id", null)
            }

            if (patientId == null) {
                Toast.makeText(this, "Patient ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Buat request untuk addMedicine
            val medicineRequest = MedicineRequest(
                med_username = reminderName,
                med_dosage = medDosage,
                med_function = reminderDescription,
                med_remaining = medRemaining,
                consumption_times = selectedTimes,
                med_slot = medSlot
            )

            val addMedicineRequest = AddMedicineRequest(
                pat_id = patientId,
                medicine = medicineRequest
            )

            // Panggil API addMedicine
            ApiClient.apiService.addMedicine(addMedicineRequest).enqueue(object : Callback<AddMedicineResponse> {
                override fun onResponse(call: Call<AddMedicineResponse>, response: Response<AddMedicineResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@AddReminder, response.body()?.message ?: "Medicine added", Toast.LENGTH_SHORT).show()

                        val medId = response.body()?.med_id ?: ""

                        // Kembalikan data ke ReminderList
                        val resultIntent = Intent().apply {
                            putExtra("med_id", medId)
                            putExtra("medUsername", reminderName)
                            putExtra("medFunction", reminderDescription)
                            putExtra("medDosage", medDosage)
                            putExtra("medRemaining", medRemaining)
                            putStringArrayListExtra("consumptionTimes", ArrayList(selectedTimes))
                            putExtra("med_slot", medSlot)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(this@AddReminder, "Failed to add medicine", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AddMedicineResponse>, t: Throwable) {
                    Toast.makeText(this@AddReminder, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(time)
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }
}
