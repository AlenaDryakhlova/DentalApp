package com.example.dentalapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dentalapp.R

// Worker для напоминаний о записях
class AppointmentReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "appointment_reminders"  // ID канала уведомлений
    }

    override suspend fun doWork(): Result {
        // Получение данных из inputData
        val doctor = inputData.getString("doctor") ?: "Ваш врач"
        val service = inputData.getString("service") ?: "услуга"
        val dateTime = inputData.getString("dateTime") ?: ""

        // Показ уведомления
        showNotification(
            title = "Ближайший визит",
            message = "$service с $doctor в $dateTime"
        )

        return Result.success()
    }

    // Создание и показ уведомления
    private fun showNotification(title: String, message: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создание канала (для Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о записях",  // Название канала
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // Построение уведомления
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)  // Иконка
            .setContentTitle(title)               // Заголовок
            .setContentText(message)              // Текст
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // Высокий приоритет
            .setAutoCancel(true)                  // Автоудаление при клике
            .build()

        // Показ уведомления (уникальный ID)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}