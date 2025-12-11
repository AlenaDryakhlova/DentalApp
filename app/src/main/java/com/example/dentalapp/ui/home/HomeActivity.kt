package com.example.dentalapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.view.Menu
import android.view.View
import android.view.MenuItem
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import java.util.concurrent.TimeUnit
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.example.dentalapp.ui.auth.LoginActivity
import com.example.dentalapp.ui.profile.EditProfileActivity
import com.example.dentalapp.ui.appointments.AppointmentActivity
import com.example.dentalapp.ui.appointments.MyAppointmentsActivity
import com.example.dentalapp.ui.info.ClinicInfoActivity
import com.example.dentalapp.ui.chat.ChatBotActivity
import com.example.dentalapp.ui.medical.MedicalRecordActivity
import com.example.dentalapp.ui.promotions.PromotionsActivity
import com.example.dentalapp.ui.notifications.NotificationsActivity
import com.example.dentalapp.workers.AppointmentReminderWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*

// Главный экран приложения
class HomeActivity : AppCompatActivity() {
    // UI элементы
    private lateinit var welcomeTextView: TextView
    private lateinit var appointmentButton: Button
    private lateinit var myAppointmentsButton: Button
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button
    private lateinit var clinicInfoButton: Button
    private lateinit var chatBotButton: Button
    private lateinit var promotionsButton: Button
    private lateinit var medicalRecordButton: Button
    private lateinit var nextAppointmentTextView: TextView

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Инициализация UI элементов
        welcomeTextView = findViewById(R.id.welcomeTextView)
        appointmentButton = findViewById(R.id.appointmentButton)
        myAppointmentsButton = findViewById(R.id.myAppointmentsButton)
        editProfileButton = findViewById(R.id.editProfileButton)
        logoutButton = findViewById(R.id.logoutButton)
        clinicInfoButton = findViewById(R.id.clinicInfoButton)
        chatBotButton = findViewById(R.id.chatBotButton)
        nextAppointmentTextView = findViewById(R.id.nextAppointmentTextView)
        medicalRecordButton = findViewById(R.id.medicalRecordsButton)
        promotionsButton = findViewById<Button>(R.id.promotionsButton)

        loadNextAppointment()   // Загрузка ближайшей записи
        loadUserName()          // Динамическое приветствие
        fetchFcmToken()         // Сохранение FCM токена
        checkWorkManagerTasks() // Проверка запланированных уведомлений

        // Обработчики кнопок
        appointmentButton.setOnClickListener {
            startActivity(Intent(this, AppointmentActivity::class.java))  // Новая запись
        }

        myAppointmentsButton.setOnClickListener {
            startActivity(Intent(this, MyAppointmentsActivity::class.java))  // Мои записи
        }

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))  // Редактировать профиль
        }

        clinicInfoButton.setOnClickListener {
            startActivity(Intent(this, ClinicInfoActivity::class.java))  // Информация о клинике
        }

        chatBotButton.setOnClickListener {
            startActivity(Intent(this, ChatBotActivity::class.java))  // Чат-бот
        }

        promotionsButton.setOnClickListener {
            startActivity(Intent(this, PromotionsActivity::class.java))  // Акции
        }

        medicalRecordButton.setOnClickListener {
            try {
                startActivity(Intent(this, MedicalRecordActivity::class.java))  // Медкарта
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка открытия медицинской карты", Toast.LENGTH_SHORT).show()
            }
        }

        // Выход из аккаунта
        logoutButton.setOnClickListener {
            auth.signOut()  // Выход из Firebase
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Планирование уведомления о записи
    private fun scheduleAppointmentNotification(
        doctor: String,
        service: String,
        dateTime: String,
        appointmentDate: Date
    ) {
        val delay = appointmentDate.time - System.currentTimeMillis()
        if (delay <= 0) return  // Если время уже прошло

        // Данные для передачи в Worker
        val inputData = Data.Builder()
            .putString("doctor", doctor)
            .putString("service", service)
            .putString("dateTime", dateTime)
            .build()

        // Создание WorkRequest
        val request = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("appointment_reminder")
            .build()

        // Планирование задачи
        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "appointment_${appointmentDate.time}",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    // Проверка задач WorkManager (для отладки)
    private fun checkWorkManagerTasks() {
        val wm = WorkManager.getInstance(this)

        Thread {
            val works = wm.getWorkInfosByTag("appointment_reminder").get()

            runOnUiThread {
                if (works.isNullOrEmpty()) {
                    Toast.makeText(this, "Нет запланированных уведомлений", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                val sb = StringBuilder()
                works.forEach { work ->
                    sb.append("ID: ${work.id}\n")
                    sb.append("Статус: ${work.state}\n")

                    val triggerAtMillis = work.outputData.getLong("triggerAt", -1)
                    if (triggerAtMillis > 0) {
                        val date = Date(triggerAtMillis)
                        sb.append("Следующее срабатывание: $date\n")
                    } else {
                        sb.append("Нет данных о времени запуска\n")
                    }
                    sb.append("\n---\n\n")
                }

                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Запланированные уведомления")
                    .setMessage(sb.toString())
                    .setPositiveButton("OK", null)
                    .show()
            }
        }.start()
    }

    // Проверка авторизации при старте
    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Динамическое приветствие с именем пользователя
    private fun loadUserName() {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val firstName = doc.getString("firstName") ?: ""
                    welcomeTextView.text = "Здравствуйте, $firstName!"
                } else {
                    welcomeTextView.text = "Здравствуйте!"
                }
            }
            .addOnFailureListener {
                welcomeTextView.text = "Здравствуйте!"
            }
    }

    // Загрузка ближайшей записи
    private fun loadNextAppointment() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("appointments")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    nextAppointmentTextView.text = "Ошибка загрузки данных"
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    nextAppointmentTextView.text = "Ближайший визит: нет записей"
                    return@addSnapshotListener
                }

                // Форматы дат для парсинга
                val sdfInList = listOf(
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
                    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()),
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                )
                val sdfOut = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

                var nearestDate: Date? = null
                var nearestDoctor = ""
                var nearestService = ""

                // Поиск ближайшей записи
                for (doc in snapshot.documents) {
                    val dateStr = doc.getString("date") ?: continue
                    val timeStr = doc.getString("time") ?: continue
                    val doctor = doc.getString("doctor") ?: "Не указан врач"
                    val service = doc.getString("service") ?: "приём"
                    val combined = "$dateStr $timeStr"

                    var parsedDate: Date? = null
                    for (fmt in sdfInList) {
                        try {
                            parsedDate = fmt.parse(combined)
                            if (parsedDate != null) break
                        } catch (_: Exception) {}
                    }

                    if (parsedDate != null && parsedDate.after(Date())) {
                        if (nearestDate == null || parsedDate.before(nearestDate)) {
                            nearestDate = parsedDate
                            nearestDoctor = doctor
                            nearestService = service
                        }
                    }
                }

                nearestDate?.let {
                    val formattedDate = sdfOut.format(it)
                    nextAppointmentTextView.text = "Ближайший визит:\n$formattedDate\n$nearestDoctor"

                    // Планирование уведомления
                    scheduleAppointmentNotification(nearestDoctor, nearestService, formattedDate, it)
                } ?: run {
                    nextAppointmentTextView.text = "Ближайший визит: нет записей"
                }
            }
    }

    private var badgeTextView: TextView? = null

    // Создание меню с уведомлениями
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)

        val menuItem = menu?.findItem(R.id.action_notifications)
        val actionView = menuItem?.actionView
        badgeTextView = actionView?.findViewById(R.id.notification_badge)

        actionView?.setOnClickListener {
            onOptionsItemSelected(menuItem!!)
        }

        updateNotificationBadge()  // Обновление бейджа
        return true
    }

    // Обработка выбора пункта меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                startActivity(Intent(this, NotificationsActivity::class.java))  // Открытие уведомлений
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Обновление бейджа уведомлений
    private fun updateNotificationBadge(count: Int = 0) {
        badgeTextView?.visibility = if (count > 0) View.VISIBLE else View.GONE
        badgeTextView?.text = count.toString()
    }

    // Получение и сохранение FCM токена
    private fun fetchFcmToken() {
        val uid = auth.currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener

            val token = task.result
            db.collection("users").document(uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    println("FCM token saved for user $uid")
                }
                .addOnFailureListener {
                    println("Failed to save FCM token: ${it.message}")
                }
        }
    }

    // Обновление данных при возвращении на экран
    override fun onResume() {
        super.onResume()
        loadNextAppointment()
    }
}