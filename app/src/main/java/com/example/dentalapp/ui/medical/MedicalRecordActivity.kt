package com.example.dentalapp.ui.medical

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

// Экран медицинской карты
class MedicalRecordActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()  // Firestore
    private val auth = FirebaseAuth.getInstance()     // Auth
    private lateinit var listView: ListView          // Список записей
    private lateinit var backTextView: TextView      // Кнопка "Назад"
    private val listData = mutableListOf<String>()   // Данные для списка
    private lateinit var adapter: ArrayAdapter<String> // Адаптер списка

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_records)

        // Инициализация UI
        listView = findViewById(R.id.recordsListView)
        backTextView = findViewById(R.id.backTextView)

        // Настройка адаптера
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listData)
        listView.adapter = adapter

        // Обработчики
        backTextView.setOnClickListener { finish() }
        loadRecords()  // Загрузка данных
    }

    // Загрузка записей из Firestore
    private fun loadRecords() {
        val uid = auth.currentUser?.uid ?: return  // ID текущего пользователя

        db.collection("medical_records")
            .whereEqualTo("userId", uid)           // Только записи пользователя
            .orderBy("createdAt")                  // Сортировка по дате
            .get()
            .addOnSuccessListener { q ->
                listData.clear()
                // Преобразование документов в строки для отображения
                for (doc in q.documents) {
                    val date = doc.getTimestamp("createdAt")?.toDate() ?: Date()
                    val text = doc.getString("text") ?: ""
                    // Формат: "дата — текст"
                    val display = "${android.text.format.DateFormat.format("dd.MM.yyyy", date)} — $text"
                    listData.add(display)
                }
                adapter.notifyDataSetChanged()  // Обновление списка
            }
    }
}