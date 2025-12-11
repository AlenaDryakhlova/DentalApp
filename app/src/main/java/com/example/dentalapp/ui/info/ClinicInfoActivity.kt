package com.example.dentalapp.ui.info

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R

// Экран информации о клинике
class ClinicInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_info)

        // Кнопка "Назад"
        val backTextView: TextView = findViewById(R.id.backTextView)
        backTextView.setOnClickListener {
            finish()  // Закрытие активности
        }
    }
}