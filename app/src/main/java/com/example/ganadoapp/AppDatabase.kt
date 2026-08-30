package com.example.ganadoapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Animal::class, Recordatorio::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun recordatorioDao(): RecordatorioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ganado_database"
                )
                    .fallbackToDestructiveMigration() // 👈 Esto borra y actualiza la tabla automáticamente para evitar que la app se cierre
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}