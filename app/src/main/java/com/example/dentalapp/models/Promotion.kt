package com.example.dentalapp.models

// Модель акции/спецпредложения
data class Promotion(
    val id: String = "",           // ID акции
    val title: String = "",        // Название
    val description: String = "",  // Описание
    val imageUrl: String = ""      // Ссылка на изображение
)