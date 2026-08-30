package com.example.ganadoapp

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class GanadoViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao: AnimalDao
    private val recordatorioDao: RecordatorioDao
    val listaAnimales: LiveData<List<Animal>>
    val listaRecordatorios: LiveData<List<Recordatorio>>

    init {
        val database = AppDatabase.getDatabase(application)
        animalDao = database.animalDao()
        recordatorioDao = database.recordatorioDao()
        listaAnimales = animalDao.obtenerTodos()
        listaRecordatorios = recordatorioDao.obtenerTodosLosRecordatorios()
    }

    fun agregarAnimal(animal: Animal) {
        viewModelScope.launch(Dispatchers.IO) {
            animalDao.insertar(animal)
        }
    }

    fun actualizarAnimal(animal: Animal) {
        viewModelScope.launch(Dispatchers.IO) {
            animalDao.actualizar(animal)
        }
    }

    fun eliminarAnimal(animal: Animal) {
        viewModelScope.launch(Dispatchers.IO) {
            animalDao.eliminar(animal)
        }
    }

    fun agregarAnimalConVacuna(animal: Animal, nombreVacuna: String, fechaVacuna: String, hora: Int, minuto: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val animalId = animalDao.insertar(animal)

            if (nombreVacuna.isNotBlank() && fechaVacuna.isNotBlank()) {
                val recordatorio = Recordatorio(
                    animalId = animalId,
                    nombreVacuna = nombreVacuna,
                    fechaProxima = fechaVacuna
                )
                recordatorioDao.insertar(recordatorio)
                programarAlarmasDespertador(animal.nombre, nombreVacuna, fechaVacuna, hora, minuto)
            }
        }
    }

    fun agregarRecordatorio(recordatorio: Recordatorio, hora: Int, minuto: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            recordatorioDao.insertar(recordatorio)
            programarAlarmasDespertador("Control Sanitario", recordatorio.nombreVacuna, recordatorio.fechaProxima, hora, minuto)
        }
    }

    fun actualizarRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch(Dispatchers.IO) {
            recordatorioDao.actualizar(recordatorio)
        }
    }

    fun eliminarRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch(Dispatchers.IO) {
            recordatorioDao.eliminar(recordatorio)
        }
    }

    fun obtenerRecordatoriosDeAnimal(animalId: Long): LiveData<List<Recordatorio>> {
        return recordatorioDao.obtenerRecordatoriosPorAnimal(animalId)
    }

    // 🚀 SISTEMA DE ALARMAS EXACTAS TIPO RELOJ DESPERTADOR
    private fun programarAlarmasDespertador(nombreAnimal: String, nombreVacuna: String, fechaProxima: String, hora: Int, minuto: Int) {
        try {
            var day = 0
            var month = 0
            var year = 0

            if (fechaProxima.contains("/")) {
                val parts = fechaProxima.split("/")
                if (parts.size == 3) {
                    if (parts[0].length == 4) {
                        year = parts[0].toInt()
                        month = parts[1].toInt() - 1
                        day = parts[2].toInt()
                    } else {
                        day = parts[0].toInt()
                        month = parts[1].toInt() - 1
                        year = parts[2].toInt()
                    }
                }
            } else if (fechaProxima.contains("-")) {
                val parts = fechaProxima.split("-")
                if (parts.size == 3) {
                    if (parts[0].length == 4) {
                        year = parts[0].toInt()
                        month = parts[1].toInt() - 1
                        day = parts[2].toInt()
                    } else {
                        day = parts[0].toInt()
                        month = parts[1].toInt() - 1
                        year = parts[2].toInt()
                    }
                }
            }

            if (year > 0 && day > 0) {
                val targetCalendar = Calendar.getInstance().apply {
                    set(year, month, day, hora, minuto, 0)
                }
                val targetTimeMillis = targetCalendar.timeInMillis
                val currentTime = System.currentTimeMillis()

                val milisegundosDia = 24L * 60 * 60 * 1000L
                val hitosAlertas = listOf(
                    Pair(30L * milisegundosDia, "Falta 1 mes"),
                    Pair(15L * milisegundosDia, "Faltan 15 días"),
                    Pair(7L * milisegundosDia, "Falta 1 semana"),
                    Pair(1L * milisegundosDia, "Falta 1 día (Mañana es la cita)"),
                    Pair(0L, "¡Es HOY! Día de vacunación obligatoria")
                )

                val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager

                for ((offset, mensajeAnticipo) in hitosAlertas) {
                    val alertTime = targetTimeMillis - offset

                    // Solo programamos si la alarma es para el futuro
                    if (alertTime > currentTime) {
                        val intent = Intent(getApplication(), AlertaReceiver::class.java).apply {
                            putExtra("TITULO", "🔔 Control Sanitario: $nombreAnimal")
                            putExtra("MENSAJE", "$mensajeAnticipo para la vacuna: $nombreVacuna ($fechaProxima)")
                        }

                        val pendingIntent = PendingIntent.getBroadcast(
                            getApplication(),
                            (System.currentTimeMillis() + offset).toInt(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        // 🎯 USAMOS ALARMCLOCK: El despertador oficial que suena exacto sin retrasos de batería
                        val alarmClockInfo = AlarmManager.AlarmClockInfo(alertTime, pendingIntent)
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

                        Log.d("DEBUG_ALARMAS", "¡Alarma tipo reloj programada con éxito para el tiempo: $alertTime!")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR_ALARMAS", "Fallo al programar la alarma de reloj", e)
        }
    }
}