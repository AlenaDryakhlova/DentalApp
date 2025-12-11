package com.example.dentalapp.ui.appointments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.example.dentalapp.ui.home.HomeActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import com.example.dentalapp.workers.AppointmentReminderWorker
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import java.util.*

// Экран записи на прием
class AppointmentActivity : AppCompatActivity() {
    // UI элементы
    private lateinit var serviceSpinner: Spinner
    private lateinit var doctorSpinner: Spinner
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var submitButton: Button
    private lateinit var backTextView: TextView

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Данные
    private var doctorList = mutableListOf<String>()
    private var serviceList = mutableListOf<String>()
    private var isDateSelected = false
    private var isTimeSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        // Инициализация UI
        serviceSpinner = findViewById(R.id.serviceSpinner)
        doctorSpinner = findViewById(R.id.doctorSpinner)
        dateTextView = findViewById(R.id.dateTextView)
        timeTextView = findViewById(R.id.timeTextView)
        submitButton = findViewById(R.id.submitButton)
        backTextView = findViewById(R.id.backTextView)

        // Обработчики
        backTextView.setOnClickListener { finish() }
        loadServices()
        loadDoctors()
        dateTextView.setOnClickListener { showDatePicker() }
        timeTextView.setOnClickListener { showTimePicker() }
        submitButton.setOnClickListener { attemptSaveAppointment() }
    }

    // Загрузка врачей
    private fun loadDoctors() {
        db.collection("doctors").get()
            .addOnSuccessListener { result ->
                doctorList.clear()
                for (doc in result) {
                    doc.getString("name")?.let { doctorList.add(it) }
                }
                if (doctorList.isEmpty()) doctorList.add("Нет доступных врачей")
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, doctorList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                doctorSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка загрузки врачей: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Загрузка услуг
    private fun loadServices() {
        db.collection("services").get()
            .addOnSuccessListener { result ->
                serviceList.clear()
                for (doc in result) {
                    doc.getString("name")?.let { serviceList.add(it) }
                }
                if (serviceList.isEmpty()) serviceList.add("Нет доступных услуг")
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, serviceList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                serviceSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка загрузки услуг: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Выбор даты
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val picker = DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = String.format("%02d/%02d/%04d", day, month + 1, year)
                dateTextView.text = date
                isDateSelected = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        picker.datePicker.minDate = System.currentTimeMillis() - 1000  // Запрет прошлых дат
        picker.show()
    }

    // Выбор времени
    private fun showTimePicker() {
        if (!isDateSelected) {
            Toast.makeText(this, "Сначала выберите дату", Toast.LENGTH_SHORT).show()
            return
        }
        val calendar = Calendar.getInstance()
        val picker = TimePickerDialog(this, { _, hour, minute ->
            // Проверка времени работы клиники
            if (!isWithinClinicHours(hour, dateTextView.text.toString())) {
                Toast.makeText(this, "Выберите время работы клиники", Toast.LENGTH_SHORT).show()
                return@TimePickerDialog
            }
            val time = String.format("%02d:%02d", hour, minute)
            timeTextView.text = time
            isTimeSelected = true
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        picker.show()
    }

    // Проверка времени работы клиники
    private fun isWithinClinicHours(hour: Int, date: String): Boolean {
        if (date.isBlank()) return false
        try {
            val parts = date.split("/").map { it.toInt() }
            val cal = Calendar.getInstance()
            cal.set(parts[2], parts[1] - 1, parts[0])
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                // Пн-Пт: 9:00-20:00
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY -> hour in 9..19
                // Сб: 10:00-18:00
                Calendar.SATURDAY -> hour in 10..17
                // Вс: выходной
                else -> false
            }
        } catch (_: Exception) { return false }
    }

    // Попытка сохранения записи
    private fun attemptSaveAppointment() {
        val service = serviceSpinner.selectedItem?.toString() ?: ""
        val doctor = doctorSpinner.selectedItem?.toString() ?: ""
        val date = dateTextView.text.toString().trim()
        val time = timeTextView.text.toString().trim()
        val uid = auth.currentUser?.uid

        // Валидация выбора
        if (service.isBlank() || doctor.isBlank() || service == "Нет доступных услуг" || doctor == "Нет доступных врачей") {
            Toast.makeText(this, "Пожалуйста, выберите услугу и врача", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isDateSelected || !isTimeSelected) {
            Toast.makeText(this, "Выберите дату и время", Toast.LENGTH_SHORT).show()
            return
        }

        // Парсинг даты и времени
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateTime = try { sdf.parse("$date $time") } catch (e: Exception) { null }
        if (dateTime == null) {
            Toast.makeText(this, "Некорректная дата или время", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка на дублирование записи
        uid?.let { userId ->
            val start = Timestamp(dateTime)
            // Проверка по timestamp
            db.collection("appointments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("timestamp", start)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        Toast.makeText(this, "У вас уже есть запись на это время", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    } else {
                        // Дополнительная проверка по date+time
                        db.collection("appointments")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("date", date)
                            .whereEqualTo("time", time)
                            .get()
                            .addOnSuccessListener { docs2 ->
                                if (!docs2.isEmpty) {
                                    Toast.makeText(this, "У вас уже есть запись на это время", Toast.LENGTH_SHORT).show()
                                    return@addOnSuccessListener
                                } else {
                                    // Сохранение записи
                                    saveAppointmentToFirestore(userId, service, doctor, date, time, start)
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Ошибка проверки: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка проверки: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
        }
    }

    // Сохранение в Firestore
    private fun saveAppointmentToFirestore(userId: String, service: String, doctor: String, date: String, time: String, timestamp: Timestamp) {
        val appointment = hashMapOf(
            "service" to service,
            "doctor" to doctor,
            "date" to date,
            "time" to time,
            "userId" to userId,
            "timestamp" to timestamp
        )
        db.collection("appointments").add(appointment)
            .addOnSuccessListener {
                // Отправка push-уведомления
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { doc ->
                        val token = doc.getString("fcmToken")
                        if (!token.isNullOrBlank()) {
                            FcmSender.sendPush(
                                token,
                                "Новая запись",
                                "Вы записаны на $service к $doctor в $date $time"
                            )
                        }
                    }

                // Диалог подтверждения
                AlertDialog.Builder(this)
                    .setTitle("Вы записаны")
                    .setMessage("Ваша запись на $service подтверждена.\n$date $time")
                    .setPositiveButton("ОК") { _, _ ->
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        scheduleReminder(date, time, doctor, service)  // Планирование напоминаний
    }

    // Планирование напоминаний
    private fun scheduleReminder(date: String, time: String, doctor: String, service: String) {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val appointmentDate = sdf.parse("$date $time") ?: return
        val now = System.currentTimeMillis()

        // Напоминание за 24 часа
        val delay24h = appointmentDate.time - now - TimeUnit.HOURS.toMillis(24)
        if (delay24h > 0) {
            val data24 = Data.Builder()
                .putString("doctor", doctor)
                .putString("service", service)
                .putString("dateTime", "$date $time")
                .build()

            val work24 = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
                .setInitialDelay(delay24h, TimeUnit.MILLISECONDS)
                .setInputData(data24)
                .build()

            WorkManager.getInstance(this).enqueue(work24)
        }

        // Напоминание за 2 часа
        val delay2h = appointmentDate.time - now - TimeUnit.HOURS.toMillis(2)
        if (delay2h > 0) {
            val data2 = Data.Builder()
                .putString("doctor", doctor)
                .putString("service", service)
                .putString("dateTime", "$date $time")
                .build()

            val work2 = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
                .setInitialDelay(delay2h, TimeUnit.MILLISECONDS)
                .setInputData(data2)
                .build()

            WorkManager.getInstance(this).enqueue(work2)
        }
    }

    // Объект для отправки FCM уведомлений
    object FcmSender {
        private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
        private const val SERVER_KEY = "ВАШ_SERVER_KEY_ИЗ_FIREBASE"

        fun sendPush(token: String, title: String, body: String) {
            val json = """
            {
              "to": "$token",
              "notification": {
                "title": "$title",
                "body": "$body"
              }
            }
        """.trimIndent()

            val client = okhttp3.OkHttpClient()
            val requestBody = json.toRequestBody("application/json".toMediaType())
            val request = okhttp3.Request.Builder()
                .url(FCM_URL)
                .addHeader("Authorization", "key=$SERVER_KEY")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {}
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {}
            })
        }
    }
}