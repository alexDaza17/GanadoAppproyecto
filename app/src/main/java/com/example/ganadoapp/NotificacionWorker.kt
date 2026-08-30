package com.example.ganadoapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificacionWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val titulo = inputData.getString("TITULO") ?: "¡Alerta Sanitaria Ganadera!"
        val mensaje = inputData.getString("MENSAJE") ?: "Tiene un control de vacunación pendiente en la finca."

        mostrarAlarmaReal(titulo, mensaje)

        return Result.success()
    }

    private fun mostrarAlarmaReal(titulo: String, mensaje: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ganado_canal_alarma_real_v3"

        // Obtenemos el tono oficial de alarma del dispositivo móvil
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Configuración para Android 8.0 (Oreo) en adelante
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Alarmas Urgentes de Ganado",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal de alarmas con sonido de alarma y vibración para el ganado"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent que se ejecuta al presionar la notificación o el botón RECORDATORIO
        val intentAccion = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intentAccion,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        // Construcción de la notificación con comportamiento de Alarma
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                android.R.drawable.ic_menu_info_details,
                "RECORDATORIO",
                pendingIntent
            )
            .setContentIntent(pendingIntent)

        // Creación limpia y directa de la notificación
        val notificationId = System.currentTimeMillis().toInt()
        val notificacionFinal = notificationBuilder.build()
        notificationManager.notify(notificationId, notificacionFinal)
    }
}