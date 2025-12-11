package com.example.dentalapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

// Экран восстановления пароля
class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var emailEditText: TextInputEditText
    private lateinit var sendButton: Button
    private lateinit var backTextView: TextView
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Инициализация UI
        emailEditText = findViewById(R.id.emailEditText)
        sendButton = findViewById(R.id.sendButton)
        backTextView = findViewById(R.id.backTextView)

        // Кнопка "Назад"
        backTextView.setOnClickListener { finish() }

        // Кнопка отправки
        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            // Валидация
            if (email.isEmpty()) {
                emailEditText.error = "Введите email"
                return@setOnClickListener
            }

            // Проверка формата email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Введите корректный email"
                return@setOnClickListener
            }

            // Отправка письма восстановления
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Письмо для восстановления отправлено", Toast.LENGTH_SHORT)
                        .show()
                    // Переход на экран входа
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}