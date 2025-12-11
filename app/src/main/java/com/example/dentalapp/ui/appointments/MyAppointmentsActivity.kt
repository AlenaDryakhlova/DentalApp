package com.example.dentalapp.ui.appointments

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AlertDialog

// Экран "Мои записи"
class MyAppointmentsActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: AppointmentsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val appointmentList = mutableListOf<Appointment>()  // Список записей

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_appointments)

        listView = findViewById(R.id.appointmentsListView)

        // Инициализация адаптера
        adapter = AppointmentsAdapter(
            this,
            appointmentList,
            onCancel = { appointment ->
                // Диалог подтверждения отмены
                AlertDialog.Builder(this)
                    .setTitle("Отмена записи")
                    .setMessage("Точно ли вы хотите отменить запись?")
                    .setPositiveButton("Да") { dialog, _ ->
                        deleteAppointment(appointment)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Нет") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onReschedule = { appointment, newDate, newTime ->
                updateAppointment(appointment, newDate, newTime)
            }
        )

        listView.adapter = adapter

        loadAppointments()  // Загрузка записей

        // Кнопка "Назад"
        findViewById<TextView>(R.id.backTextView).setOnClickListener { finish() }
    }

    // Загрузка записей пользователя
    private fun loadAppointments() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("appointments")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                appointmentList.clear()

                // Преобразование документов в объекты Appointment
                for (doc in result) {
                    val appointment = doc.toObject(Appointment::class.java)
                    appointment.id = doc.id
                    appointmentList.add(appointment)
                }

                adapter.notifyDataSetChanged()  // Обновление списка
            }
    }

    // Удаление записи
    private fun deleteAppointment(appointment: Appointment) {
        db.collection("appointments")
            .document(appointment.id)
            .delete()
            .addOnSuccessListener {
                appointmentList.remove(appointment)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Запись отменена", Toast.LENGTH_SHORT).show()
            }
    }

    // Перенос записи
    private fun updateAppointment(appointment: Appointment, newDate: String, newTime: String) {
        db.collection("appointments")
            .document(appointment.id)
            .update(
                mapOf(
                    "date" to newDate,
                    "time" to newTime
                )
            )
            .addOnSuccessListener {
                appointment.date = newDate
                appointment.time = newTime
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Запись перенесена", Toast.LENGTH_SHORT).show()
            }
    }
}