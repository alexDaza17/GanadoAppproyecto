package com.example.ganadoapp

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView

class InicioFragment : Fragment() {

    private lateinit var viewModel: GanadoViewModel
    private lateinit var tvTotalAnimales: TextView
    private lateinit var tvTotalVacunas: TextView
    private lateinit var tvTotalRecordatorios: TextView
    private lateinit var tvAplicadas: TextView // 👈 Agregado para el contador de aplicadas
    private lateinit var containerInicioVacunas: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inicio, container, false)

        tvTotalAnimales = view.findViewById(R.id.tvTotalAnimales)
        tvTotalVacunas = view.findViewById(R.id.tvTotalVacunas)
        tvTotalRecordatorios = view.findViewById(R.id.tvTotalRecordatorios)
        tvAplicadas = view.findViewById(R.id.tvAplicadas) // 👈 Vinculado con el ID de su tarjeta 4
        containerInicioVacunas = view.findViewById(R.id.containerInicioVacunas)

        viewModel = ViewModelProvider(requireActivity())[GanadoViewModel::class.java]

        // Observamos los animales en tiempo real
        viewModel.listaAnimales.observe(viewLifecycleOwner) { listaAnimales ->
            tvTotalAnimales.text = listaAnimales.size.toString()
        }

        // Observamos las vacunas y recordatorios en tiempo real calculando pendientes y aplicadas
        viewModel.listaRecordatorios.observe(viewLifecycleOwner) { listaRecordatorios ->
            val pendientes = listaRecordatorios.count { !it.aplicada }
            val aplicadas = listaRecordatorios.count { it.aplicada }

            tvTotalVacunas.text = pendientes.toString()
            tvTotalRecordatorios.text = listaRecordatorios.size.toString()
            tvAplicadas.text = aplicadas.toString() // 👈 Actualiza el número de aplicadas en vivo

            actualizarResumenVacunas(listaRecordatorios)
        }

        return view
    }

    private fun actualizarResumenVacunas(lista: List<Recordatorio>) {
        containerInicioVacunas.removeAllViews()

        // Filtramos únicamente las que están pendientes para mostrarlas abajo
        val pendientes = lista.filter { !it.aplicada }

        if (pendientes.isEmpty()) {
            val tvVacio = TextView(requireContext()).apply {
                text = "No hay vacunas pendientes."
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                setPadding(0, 8, 0, 0)
            }
            containerInicioVacunas.addView(tvVacio)
            return
        }

        // Mostramos las vacunas pendientes con su diseño original intacto
        for (recordatorio in pendientes) {
            val cardView = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }
                radius = 12f
                cardElevation = 2f
                setCardBackgroundColor(resources.getColor(android.R.color.white, null))
            }

            val innerLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val tvVacuna = TextView(requireContext()).apply {
                text = "💉 Vacuna: ${recordatorio.nombreVacuna}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(resources.getColor(android.R.color.black, null))
            }

            val tvFecha = TextView(requireContext()).apply {
                text = "Fecha: ${recordatorio.fechaProxima} - Pendiente"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                setPadding(0, 4, 0, 0)
            }

            innerLayout.addView(tvVacuna)
            innerLayout.addView(tvFecha)
            cardView.addView(innerLayout)
            containerInicioVacunas.addView(cardView)
        }
    }
}