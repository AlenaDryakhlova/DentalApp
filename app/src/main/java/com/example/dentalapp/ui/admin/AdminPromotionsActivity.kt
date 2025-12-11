package com.example.dentalapp.ui.admin

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dentalapp.R
import com.example.dentalapp.databinding.ActivityAdminPromotionsBinding
import com.example.dentalapp.models.Promotion
import com.example.dentalapp.ui.adapters.AdminPromotionsAdapter
import com.google.firebase.firestore.FirebaseFirestore

// Экран управления акциями в админ-панели
class AdminPromotionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPromotionsBinding
    private val db = FirebaseFirestore.getInstance()  // Firestore
    private val promotions = mutableListOf<Promotion>()  // Список акций
    private lateinit var adapter: AdminPromotionsAdapter  // Адаптер
    private lateinit var backTextView: TextView  // Кнопка "Назад"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding
        binding = ActivityAdminPromotionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация адаптера
        adapter = AdminPromotionsAdapter(
            promotions,
            onEdit = { promo -> openEdit(promo) },      // Колбэк редактирования
            onDelete = { promo -> confirmDelete(promo) } // Колбэк удаления
        )

        // Настройка RecyclerView
        binding.recyclerPromotions.layoutManager = LinearLayoutManager(this)
        binding.recyclerPromotions.adapter = adapter

        // Кнопка добавления
        binding.btnAddPromotion.setOnClickListener {
            startActivity(Intent(this, EditPromotionActivity::class.java))
        }

        loadPromotions()  // Загрузка данных

        // Кнопка "Назад"
        backTextView = findViewById(R.id.backTextView)
        backTextView.setOnClickListener { finish() }
    }

    // Загрузка акций из Firestore
    private fun loadPromotions() {
        db.collection("promotions")
            .get()
            .addOnSuccessListener { result ->
                promotions.clear()
                // Преобразование документов в объекты Promotion
                for (doc in result) {
                    promotions.add(
                        Promotion(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: ""
                        )
                    )
                }
                adapter.notifyDataSetChanged()  // Обновление списка
            }
    }

    // Открытие редактирования
    private fun openEdit(promotion: Promotion) {
        val intent = Intent(this, EditPromotionActivity::class.java)
        // Передача данных
        intent.putExtra("promotionId", promotion.id)
        intent.putExtra("title", promotion.title)
        intent.putExtra("description", promotion.description)
        intent.putExtra("imageUrl", promotion.imageUrl)
        startActivity(intent)
    }

    // Подтверждение удаления
    private fun confirmDelete(promotion: Promotion) {
        AlertDialog.Builder(this)
            .setTitle("Удалить акцию?")
            .setMessage("Действие необратимо.")
            .setPositiveButton("Удалить") { _: DialogInterface, _: Int ->
                deletePromotion(promotion)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // Удаление акции из Firestore
    private fun deletePromotion(promotion: Promotion) {
        db.collection("promotions")
            .document(promotion.id)
            .delete()
            .addOnSuccessListener {
                promotions.remove(promotion)  // Удаление из списка
                adapter.notifyDataSetChanged() // Обновление адаптера
            }
    }

    // При возвращении на экран
    override fun onResume() {
        super.onResume()
        loadPromotions()  // Обновление данных
    }
}