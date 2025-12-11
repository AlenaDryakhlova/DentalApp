package com.example.dentalapp.ui.appointments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.dentalapp.R
import java.util.*

// Адаптер для списка записей на прием
class AppointmentsAdapter(
    private val context: Context,
    private val appointments: MutableList<Appointment>,  // Список записей
    private val onCancel: (Appointment) -> Unit,          // Колбэк отмены
    private val onReschedule: (Appointment, String, String) -> Unit  // Колбэк переноса
) : BaseAdapter() {

    override fun getCount(): Int = appointments.size
    override fun getItem(position: Int): Appointment = appointments[position]
    override fun getItemId(position: Int): Long = position.toLong()

    // Создание/заполнение элемента списка
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.appointment_item, parent, false)

        // UI элементы
        val infoText = view.findViewById<TextView>(R.id.appointmentInfoText)
        val rescheduleBtn = view.findViewById<Button>(R.id.rescheduleButton)
        val cancelBtn = view.findViewById<Button>(R.id.cancelButton)

        val item = appointments[position]

        // Отображение информации
        infoText.text = "${item.date} ${item.time}\n${item.doctor}\n${item.service}"

        // Кнопка отмены
        cancelBtn.setOnClickListener {
            onCancel(item)
        }

        // Кнопка переноса
        rescheduleBtn.setOnClickListener {
            pickNewDateTime { newDate, newTime ->
                onReschedule(item, newDate, newTime)
            }
        }

        return view
    }

    // Выбор новой даты и времени
    private fun pickNewDateTime(onPicked: (String, String) -> Unit) {
        val c = Calendar.getInstance()

        // Диалог выбора даты
        DatePickerDialog(
            context,
            { _, y, m, d ->
                // Диалог выбора времени
                TimePickerDialog(
                    context,
                    { _, h, min ->
                        val newDate = String.format("%02d/%02d/%04d", d, m + 1, y)
                        val newTime = String.format("%02d:%02d", h, min)
                        onPicked(newDate, newTime)  // Возврат новых значений
                    },
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    true
                ).show()
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}