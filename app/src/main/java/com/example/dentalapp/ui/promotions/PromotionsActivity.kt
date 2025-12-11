package com.example.dentalapp.ui.promotions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dentalapp.databinding.ActivityPromotionsBinding
import com.example.dentalapp.models.Promotion
import com.example.dentalapp.ui.adapters.PromotionsAdapter
import com.google.firebase.firestore.FirebaseFirestore

// Экран акций
class PromotionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromotionsBinding
    private val db = FirebaseFirestore.getInstance()  // Firestore
    private val promotions = mutableListOf<Promotion>()  // Список акций
    private lateinit var adapter: PromotionsAdapter      // Адаптер

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromotionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка RecyclerView
        adapter = PromotionsAdapter(promotions)
        binding.recyclerPromotions.layoutManager = LinearLayoutManager(this)
        binding.recyclerPromotions.adapter = adapter

        // Кнопка "Назад"
        binding.backTextView.setOnClickListener { finish() }

        loadPromotionsRealtime()  // Загрузка акций
    }

    // Загрузка акций в реальном времени
    private fun loadPromotionsRealtime() {
        db.collection("promotions")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    promotions.clear()
                    // Преобразование документов в объекты Promotion
                    for (doc in snapshot) {
                        promotions.add(
                            Promotion(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                imageUrl = doc.getString("imageUrl") ?: ""
                            )
                        )
                    }
                    adapter.updateList(promotions)  // Обновление списка
                }
            }
    }
}