package com.example.ganadoapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class VacunasFragment : Fragment() {

    private lateinit var containerVacunas: LinearLayout
    private lateinit var viewModel: GanadoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vacunas, container, false)

        // Conectamos con el contenedor del XML
        containerVacunas = view.findViewById(R.id.containerVacunas)

        viewModel = ViewModelProvider(requireActivity())[GanadoViewModel::class.java]

        // Observamos en tiempo real los registros de la base de datos
        viewModel.listaRecordatorios.observe(viewLifecycleOwner) { lista ->
            actualizarListaVacunas(lista)
        }

        return view
    }

    private fun actualizarListaVacunas(lista: List<Recordatorio>) {
        containerVacunas.removeAllViews()

        for (recordatorio in lista) {
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

            val textoInfo = TextView(requireContext()).apply {
                text = "💉 Vacuna: ${recordatorio.nombreVacuna}\n📅 Fecha programada: ${recordatorio.fechaProxima}"
                textSize = 15f
                setTextColor(resources.getColor(android.R.color.black, null))
            }
            tarjeta.addView(textoInfo)

            containerVacunas.addView(tarjeta)
        }
    }
}