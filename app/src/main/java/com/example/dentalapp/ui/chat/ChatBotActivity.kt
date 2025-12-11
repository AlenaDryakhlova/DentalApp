package com.example.dentalapp.ui.chat

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalapp.R
import com.google.firebase.auth.FirebaseAuth

// Экран чат-бота
class ChatBotActivity : AppCompatActivity() {
    // UI элементы
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatContainer: LinearLayout  // Контейнер сообщений
    private lateinit var chatScrollView: ScrollView   // Скролл чата
    private lateinit var backButton: ImageButton      // Кнопка назад
    private lateinit var quickQuestionsContainer: LinearLayout // Быстрые вопросы
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)

        auth = FirebaseAuth.getInstance()

        // Инициализация UI
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        chatContainer = findViewById(R.id.chatContainer)
        chatScrollView = findViewById(R.id.chatScrollView)
        backButton = findViewById(R.id.backButton)
        quickQuestionsContainer = findViewById(R.id.quickQuestionsContainer)

        // Кнопка назад
        backButton.setOnClickListener { finish() }

        // Быстрые вопросы
        findViewById<Button>(R.id.q1).setOnClickListener { sendQuickQuestion("Адрес") }
        findViewById<Button>(R.id.q2).setOnClickListener { sendQuickQuestion("Время работы") }
        findViewById<Button>(R.id.q3).setOnClickListener { sendQuickQuestion("Врач") }
        findViewById<Button>(R.id.q4).setOnClickListener { sendQuickQuestion("Запись") }

        // Отправка сообщения
        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessageToChat("Вы: $message", true)  // Сообщение пользователя
                messageEditText.text.clear()

                val response = generateResponse(message)  // Ответ бота
                addMessageToChat("Бот: $response", false)
            }
        }
    }

    // Отправка быстрого вопроса
    private fun sendQuickQuestion(text: String) {
        addMessageToChat("Вы: $text", true)
        val response = generateResponse(text)
        addMessageToChat("Бот: $response", false)
    }

    // Добавление сообщения в чат
    private fun addMessageToChat(text: String, isUser: Boolean) {
        val textView = TextView(this).apply {
            this.text = text
            textSize = 16f
            setPadding(24, 16, 24, 16)
            // Цвет текста для пользователя и бота
            setTextColor(
                if (isUser) getColor(R.color.white) else getColor(R.color.black)
            )
            // Разные стили для пользователя и бота
            setBackgroundResource(
                if (isUser) R.drawable.chat_bubble_user else R.drawable.chat_bubble_bot
            )

            // Параметры размещения
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(12, 8, 12, 8)
            // Выравнивание: пользователь - справа, бот - слева
            params.gravity = if (isUser) android.view.Gravity.END else android.view.Gravity.START
            layoutParams = params
        }

        chatContainer.addView(textView)
        // Автоскролл к последнему сообщению
        chatScrollView.post { chatScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    // Генерация ответа бота
    private fun generateResponse(message: String): String {
        return when {
            message.contains("Адрес", ignoreCase = true) ->
                "Наш адрес: г. Москва, ул. Улыбок, д. 15"

            message.contains("Время", ignoreCase = true) ||
                    message.contains("работ", ignoreCase = true) ->
                "Мы работаем Пн–Пт: 9:00–20:00, Сб: 10:00–18:00"

            message.contains("Врач", ignoreCase = true) ->
                "Вы можете выбрать врача при записи через раздел 'Записаться на приём'."

            message.contains("Запись", ignoreCase = true) ->
                "Чтобы записаться на приём, откройте раздел 'Запись' на главном экране."

            else ->
                "Спасибо за сообщение! Мы свяжемся с вами в ближайшее время."
        }
    }
}