package com.gonpas.nasaapis.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ApodDb::class], version = 1)
abstract class NasaDatabase : RoomDatabase() {
    abstract val nasaDao: NasaDao
}


private lateinit var INSTANCE: NasaDatabase

fun getDatabase(context: Context): NasaDatabase {
    synchronized(NasaDatabase::class.java) {
        if( !::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
            NasaDatabase::class.java,
            "nasa").build()
        }
    }
    return INSTANCE
}

