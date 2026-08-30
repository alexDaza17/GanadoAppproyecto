package com.example.ganadoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlertaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("TITULO") ?: "🔔 Control Sanitario"
        val mensaje = intent.getStringExtra("MENSAJE") ?: "Tienes una vacuna pendiente en la finca."

        val serviceIntent = Intent(context, AlarmaService::class.java).apply {
            putExtra("TITULO", titulo)
            putExtra("MENSAJE", mensaje)
        }

        // Lanzamos el servicio en primer plano para asegurar que suene sin importar el estado de la app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}