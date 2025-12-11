package com.example.dentalapp.ui.admin

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.example.dentalapp.databinding.ActivityEditPromotionBinding
import com.google.firebase.firestore.FirebaseFirestore

// Экран создания/редактирования акции
class EditPromotionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditPromotionBinding
    private val db = FirebaseFirestore.getInstance()  // Firestore
    private var promotionId: String? = null  // ID акции (null = создание)
    private lateinit var backTextView: TextView  // Кнопка "Назад"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPromotionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получение ID акции (если редактирование)
        promotionId = intent.getStringExtra("promotionId")

        // Если ID есть - заполняем поля данными
        if (promotionId != null) {
            binding.titleInput.setText(intent.getStringExtra("title"))
            binding.descriptionInput.setText(intent.getStringExtra("description"))
            binding.imageInput.setText(intent.getStringExtra("imageUrl"))
            binding.btnSave.text = "Сохранить изменения"  // Меняем текст кнопки
        }

        // Кнопка сохранения
        binding.btnSave.setOnClickListener {
            save()
        }

        // Кнопка "Назад"
        backTextView = findViewById(R.id.backTextView)
        backTextView.setOnClickListener { finish() }
    }

    // Сохранение акции
    private fun save() {
        val title = binding.titleInput.text.toString()
        val desc = binding.descriptionInput.text.toString()
        val image = binding.imageInput.text.toString()

        // Данные для сохранения
        val map = mapOf(
            "title" to title,
            "description" to desc,
            "imageUrl" to image
        )

        // Создание новой или обновление существующей
        if (promotionId == null) {
            // Добавление новой акции
            db.collection("promotions").add(map).addOnSuccessListener {
                finish()  // Закрытие экрана после сохранения
            }
        } else {
            // Обновление существующей
            db.collection("promotions").document(promotionId!!).update(map).addOnSuccessListener {
                finish()  // Закрытие экрана
            }
        }
    }
}