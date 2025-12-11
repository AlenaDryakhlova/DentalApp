package com.example.dentalapp.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.example.dentalapp.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.DatePickerDialog
import java.util.Calendar

// Экран редактирования профиля
class EditProfileActivity : AppCompatActivity() {
    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI элементы
    private lateinit var lastNameEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var birthDateEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteAccountButton: TextView
    private lateinit var backTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Инициализация Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Инициализация UI
        lastNameEditText = findViewById(R.id.lastNameEditText)
        firstNameEditText = findViewById(R.id.firstNameEditText)
        middleNameEditText = findViewById(R.id.middleNameEditText)
        birthDateEditText = findViewById(R.id.birthDateEditText)

        // Настройка выбора даты рождения
        birthDateEditText.isFocusable = false
        birthDateEditText.isClickable = true
        birthDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear)
                birthDateEditText.setText(formattedDate)
            }, year, month, day).show()
        }

        phoneEditText = findViewById(R.id.phoneEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        saveButton = findViewById(R.id.saveButton)
        deleteAccountButton = findViewById(R.id.deleteAccountButton)
        backTextView = findViewById(R.id.backTextView)

        // Проверка авторизации
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadUserData()  // Загрузка данных профиля

        // Обработчики
        backTextView.setOnClickListener { finish() }
        saveButton.setOnClickListener { saveUserData() }
        deleteAccountButton.setOnClickListener { confirmDelete() }
    }

    // Загрузка данных пользователя из Firestore
    private fun loadUserData() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    // Заполнение полей данными
                    lastNameEditText.setText(doc.getString("lastName") ?: "")
                    firstNameEditText.setText(doc.getString("firstName") ?: "")
                    middleNameEditText.setText(doc.getString("middleName") ?: "")
                    birthDateEditText.setText(doc.getString("birthDate") ?: "")
                    phoneEditText.setText(doc.getString("phone") ?: "")
                } else {
                    Toast.makeText(this, "Данные профиля не найдены", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
            }
    }

    // Сохранение изменений
    private fun saveUserData() {
        val user = auth.currentUser ?: return
        val updates = mapOf(
            "lastName" to lastNameEditText.text.toString().trim(),
            "firstName" to firstNameEditText.text.toString().trim(),
            "middleName" to middleNameEditText.text.toString().trim(),
            "birthDate" to birthDateEditText.text.toString().trim(),
            "phone" to phoneEditText.text.toString().trim()
        )

        // Обновление в Firestore
        db.collection("users").document(user.uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка при сохранении данных: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Подтверждение удаления аккаунта
    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Удалить аккаунт?")
            .setMessage("Это действие нельзя отменить.")
            .setPositiveButton("Да") { _, _ -> deleteAccount() }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // Удаление аккаунта
    private fun deleteAccount() {
        val user = auth.currentUser ?: return
        // Удаление из Firestore
        db.collection("users").document(user.uid).delete()
            .addOnSuccessListener {
                // Удаление из Firebase Auth
                user.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Аккаунт удалён", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Ошибка удаления аккаунта", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка удаления данных профиля", Toast.LENGTH_SHORT).show()
            }
    }
}