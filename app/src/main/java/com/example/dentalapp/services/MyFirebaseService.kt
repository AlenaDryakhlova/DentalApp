package com.example.dentalapp.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService

// Сервис для работы с FCM (Firebase Cloud Messaging)
class MyFirebaseService : FirebaseMessagingService() {

    // Вызывается при генерации нового токена
    override fun onNewToken(token: String) {
        // Получаем UID текущего пользователя
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Обновляем токен в Firestore
        FirebaseFirestore.getInstance()
            .collection("users")  // Коллекция пользователей
            .document(uid)        // Документ текущего пользователя
            .update("fcmToken", token)  // Обновляем поле fcmToken
    }
}