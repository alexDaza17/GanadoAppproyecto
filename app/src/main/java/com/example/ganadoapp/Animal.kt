package com.example.ganadoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animales")
data class Animal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val categoria: String,
    val sexos: String,
    val descripcion: String,
    val raza: String = "",
    val edad: String = "",
    val fotoUriString: String? = null
)