package com.example.ganadoapp

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class RecordatoriosFragment : Fragment() {

    private lateinit var containerAlertas: LinearLayout
    private lateinit var viewModel: GanadoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recordatorios, container, false)

        containerAlertas = view.findViewById(R.id.containerAlertas)
        viewModel = ViewModelProvider(requireActivity())[GanadoViewModel::class.java]

        viewModel.listaRecordatorios.observe(viewLifecycleOwner) { lista ->
            actualizarListaAlertas(lista)
        }

        return view
    }

    private fun actualizarListaAlertas(lista: List<Recordatorio>) {
        containerAlertas.removeAllViews()

        // Separamos la lista en pendientes y aplicadas usando el campo booleano 'aplicada'
        val pendientes = lista.filter { !it.aplicada }
        val aplicadas = lista.filter { it.aplicada }

        // Sección 1: Alertas y Vacunas Pendientes
        val tituloPendientes = TextView(requireContext()).apply {
            text = "⏰ Alertas y Vacunas Pendientes"
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
            setPadding(0, 10, 0, 15)
        }
        containerAlertas.addView(tituloPendientes)

        if (pendientes.isEmpty()) {
            val vacioPendientes = TextView(requireContext()).apply {
                text = "No hay alertas pendientes por ahora. ¡Todo al día en la finca!"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                setPadding(0, 0, 0, 20)
            }
            containerAlertas.addView(vacioPendientes)
        } else {
            for (recordatorio in pendientes) {
                containerAlertas.addView(crearTarjetaRecordatorio(recordatorio))
            }
        }

        // Espacio separador entre secciones
        val separador = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 30)
        }
        containerAlertas.addView(separador)

        // Sección 2: Historial de Vacunas Aplicadas
        val tituloAplicadas = TextView(requireContext()).apply {
            text = "✅ Historial de Vacunas Aplicadas"
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            setPadding(0, 10, 0, 15)
        }
        containerAlertas.addView(tituloAplicadas)

        if (aplicadas.isEmpty()) {
            val vacioAplicadas = TextView(requireContext()).apply {
                text = "Aún no hay registros en el historial."
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                setPadding(0, 0, 0, 20)
            }
            containerAlertas.addView(vacioAplicadas)
        } else {
            for (recordatorio in aplicadas) {
                containerAlertas.addView(crearTarjetaRecordatorio(recordatorio))
            }
        }
    }

    private fun crearTarjetaRecordatorio(recordatorio: Recordatorio): LinearLayout {
        val tarjeta = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 30)
            background = resources.getDrawable(android.R.drawable.dialog_holo_light_frame, null)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
        }

        val estadoStr = if (recordatorio.aplicada) "✅ Estado: Aplicada" else "⏳ Estado: Pendiente"

        val textoInfo = TextView(requireContext()).apply {
            text = "🔔 Alerta: ${recordatorio.nombreVacuna}\n📅 Fecha programada: ${recordatorio.fechaProxima}\n$estadoStr"
            textSize = 15f
            setTextColor(resources.getColor(android.R.color.black, null))
        }
        tarjeta.addView(textoInfo)

        return tarjeta
    }
}