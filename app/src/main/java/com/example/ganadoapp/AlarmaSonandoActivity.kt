package com.example.ganadoapp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmaSonandoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔓 Despertar la pantalla y mostrarse por encima del bloqueo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_alarma_sonando)

        val titulo = intent.getStringExtra("TITULO") ?: "🔔 Control Sanitario"
        val mensaje = intent.getStringExtra("MENSAJE") ?: "Tienes una vacuna pendiente en la finca."

        val tvTitulo = findViewById<TextView>(R.id.tvAlarmaTitulo)
        val tvMensaje = findViewById<TextView>(R.id.tvAlarmaMensaje)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarAlarma)

        tvTitulo.text = titulo
        tvMensaje.text = mensaje

        // Al presionar el botón de confirmar, detenemos el servicio de sonido y cerramos la pantalla
        btnConfirmar.setOnClickListener {
            val serviceIntent = Intent(this, AlarmaService::class.java)
            stopService(serviceIntent)
            finish()
        }
    }
}