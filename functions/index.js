const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

exports.sendAppointmentReminders = functions.pubsub
    .schedule('every 5 minutes') // функция проверяет каждые 5 минут
    .onRun(async (context) => {
        const now = new Date();
        const appointmentsSnapshot = await db.collection('appointments').get();

        appointmentsSnapshot.forEach(async (doc) => {
            const data = doc.data();
            const userId = data.userId;
            const doctor = data.doctor || "Ваш врач";
            const dateStr = data.date; // формат "dd/MM/yyyy"
            const timeStr = data.time; // формат "HH:mm"

            // Парсим дату
            const parts = dateStr.split('/');
            const timeParts = timeStr.split(':');
            const appointmentDate = new Date(
                parseInt(parts[2], 10),
                parseInt(parts[1], 10) - 1,
                parseInt(parts[0], 10),
                parseInt(timeParts[0], 10),
                parseInt(timeParts[1], 10)
            );

            const diffMs = appointmentDate - now;
            const diffHours = diffMs / (1000 * 60 * 60);

            if ((diffHours <= 24 && diffHours > 23.9) || (diffHours <= 2 && diffHours > 1.9)) {
                // Получаем fcmToken пользователя
                const userDoc = await db.collection('users').doc(userId).get();
                if (!userDoc.exists) return;
                const token = userDoc.data().fcmToken;
                if (!token) return;

                const message = {
                    token: token,
                    notification: {
                        title: "Напоминание о визите",
                        body: `У вас запланирован визит к ${doctor} в ${timeStr} ${dateStr}`
                    }
                };

                admin.messaging().send(message)
                    .then(() => console.log(`Notification sent to ${userId}`))
                    .catch(err => console.error("Error sending message:", err));
            }
        });

        return null;
    });