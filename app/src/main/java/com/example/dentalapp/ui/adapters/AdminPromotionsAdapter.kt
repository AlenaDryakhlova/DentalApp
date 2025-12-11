package com.example.dentalapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dentalapp.databinding.ItemAdminPromotionBinding
import com.example.dentalapp.models.Promotion

// Адаптер для отображения списка акций в админ-панели
class AdminPromotionsAdapter(
    private val promotions: List<Promotion>,  // Список акций
    private val onEdit: (Promotion) -> Unit,  // Колбэк для редактирования
    private val onDelete: (Promotion) -> Unit // Колбэк для удаления
) : RecyclerView.Adapter<AdminPromotionsAdapter.ViewHolder>() {

    // ViewHolder для элемента списка
    class ViewHolder(val binding: ItemAdminPromotionBinding) : RecyclerView.ViewHolder(binding.root)

    // Создание ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminPromotionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // Привязка данных к ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val promo = promotions[position]

        // Заполнение данными
        holder.binding.title.text = promo.title
        holder.binding.description.text = promo.description

        // Загрузка изображения
        Glide.with(holder.itemView.context)
            .load(promo.imageUrl)
            .into(holder.binding.imagePromo)

        // Обработчики кнопок
        holder.binding.btnEdit.setOnClickListener { onEdit(promo) }
        holder.binding.btnDelete.setOnClickListener { onDelete(promo) }
    }

    // Количество элементов
    override fun getItemCount(): Int = promotions.size
}