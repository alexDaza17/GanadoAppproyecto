package com.example.ganadoapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AnimalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(animal: Animal): Long

    @Query("SELECT * FROM animales")
    fun obtenerTodos(): LiveData<List<Animal>>

    @Update
    suspend fun actualizar(animal: Animal)

    @Delete
    suspend fun eliminar(animal: Animal)
}