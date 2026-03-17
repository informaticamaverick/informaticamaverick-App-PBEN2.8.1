package com.example.myapplication.prestador.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.prestador.R

class NotificationHelper(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "chat_messages"
        private const val CHANNEL_NAME = "Mensajes de chat"
        private const val CHANNEL_DESCRIPTION = "Notificaciones de nuevos mensajes de clientes"

        const val NOTIFICATION_ID_BASE = 10000
    }

    init {
        createNotificationChannel()
    }

    /**
     * Crea el canal de notificaciones (necesario para Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            println("📢 Canal de notificaciones creado")
        }
    }

    /**
     * Muestra una notificación de nuevo mensaje (versión simplificada)
     */
    fun showMessageNotification(
        context: Context,
        clientName: String,
        message: String
    ) {
        // Generar un notificationId único basado en el nombre del cliente
        val notificationId = NOTIFICATION_ID_BASE + clientName.hashCode()
        showMessageNotification(
            userId = clientName, // Usar nombre como ID temporal
            userName = clientName,
            messageText = message,
            notificationId = notificationId
        )
    }
    
    /**
     * Muestra una notificación de nuevo mensaje
     */
    fun showMessageNotification(
        userId: String,
        userName: String,
        messageText: String,
        notificationId: Int = NOTIFICATION_ID_BASE
    ) {
        // Verificar permisos en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("⚠️ No hay permiso para mostrar notificaciones")
                return
            }
        }

        // Intent para abrir el chat cuando se toque la notificación
        val intent = Intent(context, Class.forName("com.example.myapplication.prestador.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_chat", true)
            putExtra("user_id", userId)
            putExtra("user_name", userName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email) // Usamos icono del sistema temporalmente
            .setContentTitle(userName)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            println("📬 Notificación mostrada: $userName - $messageText")
        } catch (e: Exception) {
            println("❌ Error al mostrar notificación: ${e.message}")
        }
    }

    /**
     * Muestra una notificación de cita confirmada (versión simplificada)
     */
    fun showAppointmentConfirmedNotification(
        context: Context,
        clientName: String,
        date: String,
        time: String
    ) {
        showAppointmentConfirmedNotification(clientName, date, time)
    }
    
    /**
     * Muestra una notificación de cita confirmada
     */
    fun showAppointmentConfirmedNotification(
        clientName: String,
        date: String,
        time: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("✅ Cita Confirmada")
            .setContentText("$clientName confirmó su cita para el $date a las $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + 1, notification)
        } catch (e: Exception) {
            println("❌ Error al mostrar notificación: ${e.message}")
        }
    }

    /**
     * Muestra una notificación de recordatorio de cita
     */
    fun showReminderNotification(
        clientName: String,
        service: String,
        date: String,
        time: String,
        hoursUntil: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val title = if (hoursUntil == 1) "⏰ Cita en 1 hora" else "📅 Cita mañana"
        val text = "$service con $clientName — $date a las $time"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        try {
            val notifId = NOTIFICATION_ID_BASE + 2000 + clientName.hashCode() + hoursUntil
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: Exception) {
            println("❌ Error recordatorio: ${e.message}")
        }
    }

    /**
     * Cancela una notificación específica
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * Verifica si los permisos de notificaciones están otorgados
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showSolicitudFastNotification(titulo: String, clienteNombre: String, distanciaKm: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }
        val texto = "Cliente: $clienteNombre · %.1f km".format(distanciaKm)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚡ Nueva urgencia Fast: $titulo")
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + 9000 + titulo.hashCode(), notification)
        } catch (e: Exception) {
            println("❌ Error notif fast: ${e.message}")
        }
    }

    fun showPresupuestoAceptadoNotification(clientName: String, total: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("💰 Presupuesto aceptado")
            .setContentText("$clientName aceptó el presupuesto por $${"%.2f".format(total)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + 3, notification)
        } catch (e: Exception) {
            println("❌ Error notificación presupuesto: ${e.message}")
        }
    }
}

