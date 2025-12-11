package com.example.dentalapp.ui.auth

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.example.dentalapp.ui.home.HomeActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

// Экран регистрации
class RegisterActivity : AppCompatActivity() {
    // UI элементы
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var middleNameEditText: TextInputEditText
    private lateinit var birthDateEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var loginPromptTextView: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Инициализация Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Инициализация UI
        lastNameEditText = findViewById(R.id.lastNameEditText)
        firstNameEditText = findViewById(R.id.firstNameEditText)
        middleNameEditText = findViewById(R.id.middleNameEditText)
        birthDateEditText = findViewById(R.id.birthDateEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginPromptTextView = findViewById(R.id.loginPromptTextView)

        // Выбор даты рождения
        birthDateEditText.setOnClickListener {
            showDatePickerDialog(birthDateEditText)
        }

        // Обработка регистрации
        registerButton.setOnClickListener {
            val lastName = lastNameEditText.text.toString().trim()
            val firstName = firstNameEditText.text.toString().trim()
            val middleName = middleNameEditText.text.toString().trim()
            val birthDate = birthDateEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Проверка обязательных полей
            if (lastName.isEmpty() || firstName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailEditText.error = "Введите email"
                return@setOnClickListener
            }

            // Проверка формата email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Введите корректный email"
                return@setOnClickListener
            }

            // Проверка длины пароля
            if (password.length < 6) {
                passwordEditText.error = "Пароль должен быть не меньше 6 символов"
                return@setOnClickListener
            }

            // Проверка совпадения паролей
            if (password != confirmPassword) {
                confirmPasswordEditText.error = "Пароли не совпадают"
                return@setOnClickListener
            }

            // Проверка телефона
            val digitsOnlyPhone = phone.replace(Regex("[^\\d]"), "")
            if (digitsOnlyPhone.length < 10) {
                phoneEditText.error = "Введите корректный номер телефона"
                return@setOnClickListener
            }

            // Регистрация пользователя
            registerUser(
                lastName, firstName, middleName, birthDate, email, phone, password
            )
        }

        // Переход ко входу
        loginPromptTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Регистрация в Firebase
    private fun registerUser(
        lastName: String,
        firstName: String,
        middleName: String,
        birthDate: String,
        email: String,
        phone: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
                // Данные пользователя для Firestore
                val userMap = hashMapOf(
                    "lastName" to lastName,
                    "firstName" to firstName,
                    "middleName" to middleName,
                    "birthDate" to birthDate,
                    "email" to email,
                    "phone" to phone
                )
                // Сохранение в Firestore
                firestore.collection("users").document(userId)
                    .set(userMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Ошибка сохранения данных: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка регистрации: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Диалог выбора даты
    private fun showDatePickerDialog(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val dateString = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
            editText.setText(dateString)
        }, year, month, day)

        datePicker.show()
    }
}