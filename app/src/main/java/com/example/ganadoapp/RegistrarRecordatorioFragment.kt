package com.example.ganadoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar

class RegistrarRecordatorioFragment : Fragment(R.layout.fragment_registrar_recordatorio) {

    private lateinit var viewModel: GanadoViewModel
    private var fechaSeleccionada = ""

    // Inicializamos con la fecha y hora actual para evitar valores en cero
    private val calendarActualInit = Calendar.getInstance()
    private var anioSeleccionado = calendarActualInit.get(Calendar.YEAR)
    private var mesSeleccionado = calendarActualInit.get(Calendar.MONTH)
    private var diaSeleccionado = calendarActualInit.get(Calendar.DAY_OF_MONTH)
    private var horaSeleccionada = calendarActualInit.get(Calendar.HOUR_OF_DAY)
    private var minutoSeleccionado = calendarActualInit.get(Calendar.MINUTE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[GanadoViewModel::class.java]

        val etNombre = view.findViewById<EditText>(R.id.etNombreVacuna)
        val etFecha = view.findViewById<EditText>(R.id.etFechaProxima)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarRecordatorio)

        // Al tocar el campo fecha, abrimos el calendario
        etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    anioSeleccionado = year
                    mesSeleccionado = month
                    diaSeleccionado = day
                    fechaSeleccionada = "$day/${month + 1}/$year"

                    // Inmediatamente después de elegir la fecha, abrimos el reloj para la hora exacta
                    abrirSelectorDeHora(etFecha)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Al hacer clic en el botón Guardar
        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()

            if (nombre.isNotEmpty() && fechaSeleccionada.isNotEmpty()) {
                val nuevoRecordatorio = Recordatorio(
                    animalId = 1L,
                    nombreVacuna = nombre,
                    fechaProxima = fechaSeleccionada
                )

                // 🚀 Enviamos el recordatorio junto con la hora y minuto exactos elegidos
                viewModel.agregarRecordatorio(nuevoRecordatorio, horaSeleccionada, minutoSeleccionado)

                Toast.makeText(requireContext(), "¡Vacuna programada con éxito!", Toast.LENGTH_SHORT).show()

                // Limpiamos los campos después de guardar
                etNombre.text.clear()
                etFecha.text.clear()
                fechaSeleccionada = ""
            } else {
                Toast.makeText(requireContext(), "Por favor complete el nombre y la fecha", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun abrirSelectorDeHora(etFecha: EditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                // Guardamos la hora y minuto que seleccionó el usuario
                horaSeleccionada = hourOfDay
                minutoSeleccionado = minute

                val horaFormateada = String.format("%02d:%02d", hourOfDay, minute)
                etFecha.setText("$fechaSeleccionada a las $horaFormateada")
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // Formato de 24 horas activado
        ).show()
    }
}