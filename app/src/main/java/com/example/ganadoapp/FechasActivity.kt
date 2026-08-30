package com.example.ganadoapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class FechasActivity : AppCompatActivity() {

    private lateinit var fotoVaca: ImageView
    private lateinit var nombreVacuna: EditText
    private lateinit var nombreAnimal: EditText
    private lateinit var notaVacuna: EditText
    private lateinit var calendario: CalendarView
    private lateinit var relojVacuna: TimePicker
    private var tiempoAlarma: Long = 0

    private val abrirCamara = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoVaca.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show()
        }
    }

    private val abrirGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            fotoVaca.setImageURI(uri)
        } else {
            Toast.makeText(this, "No se seleccionó ninguna foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fechas)

        fotoVaca = findViewById(R.id.foto_vaca)
        nombreVacuna = findViewById(R.id.nombre_vacuna)
        nombreAnimal = findViewById(R.id.nombre_animal)
        notaVacuna = findViewById(R.id.nota_vacuna)
        calendario = findViewById(R.id.calendario_vacunas)
        relojVacuna = findViewById(R.id.reloj_vacuna)
        val botonCamara = findViewById<Button>(R.id.boton_camara)
        val botonGaleria = findViewById<Button>(R.id.boton_galeria)
        val botonGuardar = findViewById<Button>(R.id.boton_guardar_alarma)

        actualizarTiempoAlarma()

        calendario.setOnDateChangeListener { _, _, _, _ ->
            actualizarTiempoAlarma()
        }

        relojVacuna.setOnTimeChangedListener { _, _, _ ->
            actualizarTiempoAlarma()
        }

        botonCamara.setOnClickListener {
            abrirCamara.launch(null)
        }

        botonGaleria.setOnClickListener {
            abrirGaleria.launch("image/*")
        }

        botonGuardar.setOnClickListener {
            val textoVacuna = nombreVacuna.text.toString()
            val textoAnimal = nombreAnimal.text.toString()

            if (textoVacuna.isEmpty()) {
                Toast.makeText(this, "Por favor, escriba el nombre de la vacuna.", Toast.LENGTH_LONG).show()
            } else if (textoAnimal.isEmpty()) {
                Toast.makeText(this, "Por favor, escriba el nombre de la vaca o toro.", Toast.LENGTH_LONG).show()
            } else {
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        tiempoAlarma,
                        pendingIntent
                    )
                    Toast.makeText(this, "¡Alarma para $textoAnimal programada con éxito!", Toast.LENGTH_LONG).show()
                } catch (e: SecurityException) {
                    Toast.makeText(this, "Error de permisos de alarma exactas", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun actualizarTiempoAlarma() {
        val fechaMillis = calendario.date
        val calendarFecha = Calendar.getInstance().apply {
            timeInMillis = fechaMillis
        }

        val anio = calendarFecha.get(Calendar.YEAR)
        val mes = calendarFecha.get(Calendar.MONTH)
        val dia = calendarFecha.get(Calendar.DAY_OF_MONTH)
        val hora = relojVacuna.hour
        val minuto = relojVacuna.minute

        val calendarFinal = Calendar.getInstance().apply {
            set(anio, mes, dia, hora, minuto, 0)
        }

        tiempoAlarma = calendarFinal.timeInMillis
    }
}