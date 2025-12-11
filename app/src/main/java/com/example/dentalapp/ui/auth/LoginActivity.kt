package com.example.dentalapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.example.dentalapp.ui.home.HomeActivity
import com.example.dentalapp.ui.admin.AdminMainActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

// Экран входа в систему
class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerPromptTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var auth: FirebaseAuth

    private val adminEmail = "admin@dental.com" // Email администратора

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Инициализация UI
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerPromptTextView = findViewById(R.id.registerPromptTextView)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)

        // Переход к восстановлению пароля
        forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Переход к регистрации
        registerPromptTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Обработка входа
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Валидация email
            if (email.isEmpty()) {
                emailEditText.error = "Введите email"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Введите корректный email"
                return@setOnClickListener
            }

            // Валидация пароля
            if (password.isEmpty()) {
                passwordEditText.error = "Введите пароль"
                return@setOnClickListener
            }

            // Аутентификация в Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Вход успешен!", Toast.LENGTH_SHORT).show()

                    val user = auth.currentUser
                    if (user != null) {
                        // Сохранение FCM токена
                        FirebaseMessaging.getInstance().token
                            .addOnSuccessListener { token ->
                                val uid = user.uid
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .update("fcmToken", token)
                            }

                        // Проверка роли (админ/пользователь)
                        if (user.email == adminEmail) {
                            startActivity(Intent(this, AdminMainActivity::class.java))
                        } else {
                            startActivity(Intent(this, HomeActivity::class.java))
                        }
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка входа: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}