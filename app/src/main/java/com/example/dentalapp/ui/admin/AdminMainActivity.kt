package com.example.dentalapp.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.example.dentalapp.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

// Главный экран админ-панели
class AdminMainActivity : AppCompatActivity() {
    private lateinit var madePromotion: Button  // Кнопка управления акциями
    private lateinit var logoutButton: Button   // Кнопка выхода

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        // Инициализация кнопок
        madePromotion = findViewById(R.id.madePromotion)
        logoutButton = findViewById(R.id.logoutButton)

        // Переход к управлению акциями
        madePromotion.setOnClickListener {
            startActivity(Intent(this, AdminPromotionsActivity::class.java))
        }

        // Выход из системы
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()  // Выход из Firebase
            startActivity(Intent(this, LoginActivity::class.java))  // Переход на экран логина
            finish()  // Закрытие текущей активности
        }
    }
}