package com.example.ganadoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordatorios")
data class Recordatorio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val animalId: Long,
    val nombreVacuna: String,
    val fechaProxima: String,
    val aplicada: Boolean = false // 👈 Nuevo campo: por defecto empieza en falso (pendiente)
)