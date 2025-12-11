package com.example.dentalapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dentalapp.R
import com.example.dentalapp.models.Promotion

// Адаптер для отображения списка акций
class PromotionsAdapter(
    private var promotions: List<Promotion>  // Список акций
) : RecyclerView.Adapter<PromotionsAdapter.PromoViewHolder>() {

    // ViewHolder для элемента акции
    class PromoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val description: TextView = view.findViewById(R.id.description)
        val image: ImageView = view.findViewById(R.id.imagePromo)
    }

    // Создание ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotion, parent, false)
        return PromoViewHolder(view)
    }

    // Привязка данных
    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        val promo = promotions[position]
        holder.title.text = promo.title
        holder.description.text = promo.description

        // Загрузка изображения
        Glide.with(holder.itemView.context)
            .load(promo.imageUrl)
            .into(holder.image)
    }

    // Количество элементов
    override fun getItemCount(): Int = promotions.size

    // Обновление списка
    fun updateList(newList: List<Promotion>) {
        promotions = newList
        notifyDataSetChanged()  // Уведомление об изменении
    }
}