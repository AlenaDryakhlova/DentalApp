package com.example.dentalapp.ui.appointments

// Модель записи на прием
data class Appointment(
    var id: String = "",       // ID записи
    var service: String = "",  // Услуга
    var doctor: String = "",   // Врач
    var date: String = "",     // Дата
    var time: String = "",     // Время
    var userId: String = ""    // ID пользователя
)