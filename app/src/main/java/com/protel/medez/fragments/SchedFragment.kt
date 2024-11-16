package com.protel.medez.fragments

import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.DatePickerDialog
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.protel.medez.R
import com.protel.medez.databinding.ListObatPageBinding
import com.protel.medez.databinding.TambahObatDialogBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.min


class SchedFragment : Fragment() {

    private var _binding: ListObatPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ListObatPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addMedicine.setOnClickListener {
            addMedicineDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showAddMedicineDialog() {
        addMedicineDialog()
    }

    private fun addMedicineDialog() {
        val v = TambahObatDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(v.root)
            .show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        v.sebelumType.adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.sebelumTypes)
        )
        v.frequencyType1.adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.frequencyType1)
        )



        v.frequencyType2.adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.frequencyType2)
        )
        v.cancelTambahJadwalButton.setOnClickListener {
            dialog.dismiss()
        }


        val pilihJadwal = Calendar.getInstance()
        v.pilihJadwalButton.setOnClickListener {
            val year = pilihJadwal.get(Calendar.YEAR)
            val month = pilihJadwal.get(Calendar.MONTH)
            val day = pilihJadwal.get(Calendar.DAY_OF_MONTH)
            val hour = pilihJadwal.get(Calendar.HOUR_OF_DAY)
            val minute = pilihJadwal.get(Calendar.MINUTE)

            android.app.DatePickerDialog(
                requireContext(),
                android.app.DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    TimePickerDialog(
                        requireContext(),
                        TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                            pilihJadwal.set(year, month, dayOfMonth, hourOfDay, minute)
                            v.pickedTime.text = getCurrentTime(pilihJadwal.timeInMillis)
                        },
                        hour,
                        minute,
                        true
                    ).show()
                }, year, month, day
            ).show()


            v.confirmTambahJadwalButton.setOnClickListener {
                if (v.namaObat.text.isNullOrEmpty()) {
                    v.namaObat.requestFocus()
                    v.namaObat.error = "Tolong isi Nama Obat"
                } else if (v.pickedTime.text == resources.getString(R.string.time)) {
                    v.pickedTime.error = "Tolong Pilih Waktu"
                } else {
                    val timeDelayInSeconds =
                        (pilihJadwal.timeInMillis / 1000L) - (Calendar.getInstance().timeInMillis / 1000L)
                    if (timeDelayInSeconds < 0) {
                        Toast.makeText(requireContext(), "Waktu Salah", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

//                    createWorkRequest(
//                        v.namaObat.text.toString(),
//                        resources.getStringArray(R.array.sebelumTypes)[v.sebelumType.selectedItemPosition],
//                        timeDelayInSeconds
//                    )
                    Toast.makeText(requireContext(), "Jadwal Ditambahkan", Toast.LENGTH_LONG).show()
                    dialog.dismiss()

                }
            }
        }

    }

//    private fun createWorkRequest(title:String, sebelumType : String, delay : Long) {
//        val reminderWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
//            .setInitialDelay(delay, TimeUnit.SECONDS)
//            .setInputData(
//                workDataOf(
//                    "Nama Obat" to "Nama Obat: $title",
//                    "Message" to sebelumType
//                )
//            )
//            .build()
//        WorkManager.getInstance(requireContext()).enqueue(reminderWorkRequest)
//    }

    private fun getCurrentTime(millis : Long): String{
        return SimpleDateFormat("dd-MM-yyy hh:mm a", Locale.getDefault()).format(Date(millis))
    }
}



