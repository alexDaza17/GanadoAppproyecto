package com.example.ganadoapp

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class GanadoFragment : Fragment(R.layout.fragment_ganado) {

    private lateinit var viewModel: GanadoViewModel

    private var editFotoUriString: String? = null
    private var dialogImageView: ImageView? = null

    private val editarImagenLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { sourceUri ->
            val savedUri = copiarUriAAlmacenamientoInterno(sourceUri)
            if (savedUri != null) {
                editFotoUriString = savedUri.toString()
                dialogImageView?.setImageURI(savedUri)
            } else {
                Toast.makeText(requireContext(), "No estudiante se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[GanadoViewModel::class.java]

        val fabAgregar = view.findViewById<FloatingActionButton>(R.id.fab_agregar_animal)
        fabAgregar?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as View).id, RegistrarAnimaFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🐄 Observamos tanto los animales como los recordatorios para cruzar la información de vacunación
        viewModel.listaRecordatorios.observe(viewLifecycleOwner) { listaRecordatorios ->
            viewModel.listaAnimales.observe(viewLifecycleOwner) { listaAnimales ->
                actualizarInventarioAnimales(listaAnimales, listaRecordatorios)
            }
        }
    }

    private fun actualizarInventarioAnimales(listaAnimales: List<Animal>, listaRecordatorios: List<Recordatorio>) {
        val contenedor = view?.findViewById<LinearLayout>(R.id.container_animales) ?: return
        contenedor.removeAllViews()

        for (animal in listaAnimales) {
            // Buscamos si este animal tiene un recordatorio de vacuna asociado
            val recordatorioAsociado = listaRecordatorios.find { it.animalId == animal.id }

            val tarjeta = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(25, 25, 25, 25)
                background = resources.getDrawable(android.R.drawable.dialog_holo_light_frame, null)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 20)
                layoutParams = params
            }

            // 📸 FOTO CUADRADA, GRANDE Y NÍDIDA DEL ANIMAL
            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(300, 300).apply {
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setMargins(0, 0, 25, 0)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = resources.getDrawable(android.R.drawable.dialog_holo_light_frame, null)

                if (!animal.fotoUriString.isNullOrBlank()) {
                    try {
                        setImageURI(Uri.parse(animal.fotoUriString))
                    } catch (e: Exception) {
                        setImageResource(android.R.drawable.ic_menu_camera)
                    }
                } else {
                    setImageResource(android.R.drawable.ic_menu_camera)
                }
            }
            tarjeta.addView(imageView)

            val nombreVaca = animal.nombre.ifBlank { "Animal sin nombre" }
            val razaVaca = if (animal.raza.isNotBlank()) "\n🧬 Raza: ${animal.raza}" else "\n🧬 Raza: No especificada"
            val edadVaca = if (animal.edad.isNotBlank()) "\n⏳ Edad: ${animal.edad}" else "\n⏳ Edad: No especificada"

            // 💉 Estado de vacunación visible en la tarjeta
            val estadoVacunaTexto = if (recordatorioAsociado != null) {
                if (recordatorioAsociado.aplicada) {
                    "\n✅ Estado: Vacunado (${recordatorioAsociado.nombreVacuna})"
                } else {
                    "\n⏳ Estado: Vacuna Pendiente (${recordatorioAsociado.nombreVacuna})"
                }
            } else {
                "\nℹ️ Estado: Sin vacuna asignada"
            }

            val textoInfo = TextView(requireContext()).apply {
                text = "🐄 Identificador: $nombreVaca$razaVaca$edadVaca\n🏷️ Categoría: ${animal.categoria}$estadoVacunaTexto"
                textSize = 15f
                setTextColor(resources.getColor(android.R.color.black, null))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            tarjeta.addView(textoInfo)

            // Mantener presionado para gestionar el animal y confirmar su vacuna
            tarjeta.setOnLongClickListener {
                mostrarOpcionesAnimal(animal, recordatorioAsociado)
                true
            }

            contenedor.addView(tarjeta)
        }
    }

    private fun mostrarOpcionesAnimal(animal: Animal, recordatorio: Recordatorio?) {
        val opcionesList = mutableListOf("✏️ Editar Animal")

        if (recordatorio != null && !recordatorio.aplicada) {
            opcionesList.add("✅ Confirmar Vacuna Aplicada")
        }
        opcionesList.add("🗑️ Eliminar del Inventario")

        val opciones = opcionesList.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Gestión de Res: ${animal.nombre}")
            .setItems(opciones) { _, cual ->
                val opcionElegida = opciones[cual]
                when {
                    opcionElegida.contains("Editar") -> abrirDialogoEditarAnimal(animal)
                    opcionElegida.contains("Confirmar Vacuna") -> {
                        if (recordatorio != null) {
                            val recordatorioActualizado = recordatorio.copy(aplicada = true)
                            viewModel.actualizarRecordatorio(recordatorioActualizado)
                            Toast.makeText(requireContext(), "🎉 ¡Vacuna confirmada como aplicada!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    opcionElegida.contains("Eliminar") -> {
                        viewModel.eliminarAnimal(animal)
                        if (recordatorio != null) {
                            viewModel.eliminarRecordatorio(recordatorio)
                        }
                        Toast.makeText(requireContext(), "Animal eliminado del inventario", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun abrirDialogoEditarAnimal(animal: Animal) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Editar Datos del Animal")

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        editFotoUriString = animal.fotoUriString

        dialogImageView = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(320, 320).apply {
                gravity = android.view.Gravity.CENTER
                setMargins(0, 0, 0, 20)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = resources.getDrawable(android.R.drawable.dialog_holo_light_frame, null)

            if (!editFotoUriString.isNullOrBlank()) {
                setImageURI(Uri.parse(editFotoUriString))
            } else {
                setImageResource(android.R.drawable.ic_menu_camera)
            }

            setOnClickListener {
                editarImagenLauncher.launch("image/*")
            }
        }
        layout.addView(dialogImageView)

        val hintTocarFoto = TextView(requireContext()).apply {
            text = "Toca la imagen para cambiar la foto"
            textSize = 12f
            gravity = android.view.Gravity.CENTER
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 20)
            }
        }
        layout.addView(hintTocarFoto)

        val inputNombre = EditText(requireContext()).apply {
            setText(animal.nombre)
            hint = "Nombre o Identificador"
        }
        layout.addView(inputNombre)

        val inputRaza = EditText(requireContext()).apply {
            setText(animal.raza)
            hint = "Raza"
        }
        layout.addView(inputRaza)

        val inputEdad = EditText(requireContext()).apply {
            setText(animal.edad)
            hint = "Edad"
        }
        layout.addView(inputEdad)

        builder.setView(layout)

        builder.setPositiveButton("Guardar Cambios") { _, _ ->
            val nuevoNombre = inputNombre.text.toString().trim()
            val nuevaRaza = inputRaza.text.toString().trim()
            val nuevaEdad = inputEdad.text.toString().trim()

            if (nuevoNombre.isNotBlank()) {
                val animalActualizado = animal.copy(
                    nombre = nuevoNombre,
                    raza = nuevaRaza,
                    edad = nuevaEdad,
                    fotoUriString = editFotoUriString
                )
                viewModel.actualizarAnimal(animalActualizado)
                Toast.makeText(requireContext(), "¡Animal actualizado con éxito!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

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