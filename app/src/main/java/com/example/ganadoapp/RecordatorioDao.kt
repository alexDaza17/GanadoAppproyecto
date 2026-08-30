package com.example.ganadoapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordatorioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recordatorio: Recordatorio): Long

    @Query("SELECT * FROM recordatorios")
    fun obtenerTodosLosRecordatorios(): LiveData<List<Recordatorio>>

    @Query("SELECT * FROM recordatorios WHERE animalId = :idAnimal")
    fun obtenerRecordatoriosPorAnimal(idAnimal: Long): LiveData<List<Recordatorio>>

    @Update
    suspend fun actualizar(recordatorio: Recordatorio)

    @Delete
    suspend fun eliminar(recordatorio: Recordatorio)
}