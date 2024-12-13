package com.protel.myapplication.meds

import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.protel.myapplication.R
import com.protel.myapplication.api.Reminder

class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onDeleteClick: (Reminder) -> Unit // Callback untuk hapus item
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tvReminderName)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvReminderDescription)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteBtnIdle) // Hanya satu ImageView
        val tvMedSlot: TextView = itemView.findViewById(R.id.tvMedSlot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ReminderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.nameTextView.text = reminder.med_username
        holder.descriptionTextView.text = reminder.med_function

        holder.tvMedSlot.text = "Slot: ${reminder.med_slot}"

        // Default gambar idle
        holder.deleteButton.setImageResource(R.drawable.trash_idle)

        // Handle pergantian gambar ketika tombol disentuh
        holder.deleteButton.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    // Ganti ke gambar hover
                    holder.deleteButton.setImageResource(R.drawable.trash_hovered)
                }
                android.view.MotionEvent.ACTION_UP -> {
                    // Kembali ke gambar idle setelah klik
                    holder.deleteButton.setImageResource(R.drawable.trash_idle)
                    // Callback untuk hapus item
                    onDeleteClick(reminder)
                }
                android.view.MotionEvent.ACTION_CANCEL -> {
                    // Kembali ke gambar idle jika touch dibatalkan
                    holder.deleteButton.setImageResource(R.drawable.trash_idle)
                }
            }
            true
        }

        // Handle klik pada item untuk membuka InfoObat
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, InfoObat::class.java)
            intent.putExtra("medUsername", reminder.med_username)
            intent.putExtra("medDosage", reminder.med_dosage)
            intent.putExtra("medFunction", reminder.med_function)
            intent.putExtra("medRemaining", reminder.med_remaining)
            intent.putStringArrayListExtra("consumptionTimes", ArrayList(reminder.consumption_times))
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = reminders.size

    fun removeItem(reminder: Reminder) {
        val position = reminders.indexOf(reminder)
        if (position != -1) {
            reminders.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}