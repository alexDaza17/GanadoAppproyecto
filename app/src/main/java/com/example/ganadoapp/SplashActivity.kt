package com.example.ganadoapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivLogo = findViewById<ImageView>(R.id.ivLogoSplash)

        // Animación de desvanecimiento (Fade In: aparece poco a poco)
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1500 // Duración de 1.5 segundos
        }
        ivLogo.startAnimation(fadeIn)

        // Temporizador de 2.5 segundos para mostrar la bienvenida y abrir el MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra el Splash para que al presionar "atrás" no regrese a la bienvenida
        }, 2500)
    }
}