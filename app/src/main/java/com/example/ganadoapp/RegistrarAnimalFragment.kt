package com.example.ganadoapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Calendar

class RegistrarAnimaFragment : Fragment() {

    private lateinit var viewModel: GanadoViewModel
    private var fotoUriString: String? = null
    private var fechaSeleccionada = ""

    private var horaElegida = 8
    private var minutoElegido = 0

    private lateinit var etNombre: EditText
    private lateinit var etRaza: EditText
    private lateinit var etEdad: EditText
    private lateinit var etVacuna: EditText
    private lateinit var etFechaVacuna: EditText
    private lateinit var etHoraVacuna: EditText
    private lateinit var ivFoto: ImageView
    private lateinit var btnGuardar: Button

    private val seleccionarImagenLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { sourceUri ->
            val savedUri = copiarUriAAlmacenamientoInterno(sourceUri)
            if (savedUri != null) {
                fotoUriString = savedUri.toString()
                ivFoto.setImageURI(savedUri)
            } else {
                Toast.makeText(requireContext(), "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registrar_animal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[GanadoViewModel::class.java]

        ivFoto = view.findViewById(R.id.ivFotoAnimal)
        etNombre = view.findViewById(R.id.etNombre)
        etRaza = view.findViewById(R.id.etRaza)
        etEdad = view.findViewById(R.id.etEdad)
        etVacuna = view.findViewById(R.id.etVacunaAnimal)
        etFechaVacuna = view.findViewById(R.id.etFechaVacunaAnimal)
        etHoraVacuna = view.findViewById(R.id.etHoraVacunaAnimal)
        btnGuardar = view.findViewById(R.id.btnGuardar)

        ivFoto.setOnClickListener {
            seleccionarImagenLauncher.launch("image/*")
        }

        etFechaVacuna.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    fechaSeleccionada = "$day/${month + 1}/$year"
                    etFechaVacuna.setText(fechaSeleccionada)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etHoraVacuna.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    horaElegida = hourOfDay
                    minutoElegido = minute

                    val amPm = if (hourOfDay < 12) "AM" else "PM"
                    val hourIn12 = if (hourOfDay == 0) 12 else if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                    etHoraVacuna.setText(String.format("%02d:%02d %s", hourIn12, minute, amPm))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val raza = etRaza.text.toString().trim()
            val edad = etEdad.text.toString().trim()
            val vacuna = etVacuna.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(requireContext(), "Escribe el nombre o identificador del animal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fechaSeleccionada.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona la fecha de la vacuna", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🚀 Ventana emergente para seleccionar la categoría real antes de guardar
            mostrarSelectorCategoria { categoriaElegida ->
                val nuevoAnimal = Animal(
                    nombre = nombre,
                    categoria = categoriaElegida, // 🏷️ Aquí se guarda la categoría seleccionada
                    sexos = "Hembra",
                    descripcion = "Registro de finca",
                    raza = raza,
                    edad = edad,
                    fotoUriString = fotoUriString
                )

                // Guardamos en Room y programamos la alarma exacta
                viewModel.agregarAnimalConVacuna(nuevoAnimal, vacuna, fechaSeleccionada, horaElegida, minutoElegido)

                Toast.makeText(requireContext(), "🎉 ¡Animal y categoría guardados con éxito!", Toast.LENGTH_SHORT).show()

                // Retorno automático a la pantalla anterior
                parentFragmentManager.popBackStack()
            }
        }
    }

    // 🛠️ Diálogo elegante para elegir la categoría del ganado
    private fun mostrarSelectorCategoria(onCategoriaSeleccionada: (String) -> Unit) {
        val opcionesCategorias = arrayOf(
            "🐄 Vaca Lechera",
            "🐂 Ganado de Ceba / Engorde",
            "🍼 Cría / Ternero",
            "🐃 Reproductor / Toro",
            "🏷️ General"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccione la Categoría del Animal")
            .setItems(opcionesCategorias) { _, which ->
                val categoriaLimpia = when (which) {
                    0 -> "Vaca Lechera"
                    1 -> "Ceba / Engorde"
                    2 -> "Cría / Ternero"
                    3 -> "Reproductor / Toro"
                    else -> "General"
                }
                onCategoriaSeleccionada(categoriaLimpia)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🛠️ Copia la foto de la galería al almacenamiento interno de la app
    private fun copiarUriAAlmacenamientoInterno(sourceUri: Uri): Uri? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(sourceUri)
            val fileName = "ganado_foto_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}