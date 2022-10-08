package com.gonpas.nasaapis.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gonpas.nasaapis.repository.NasaRepository
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import javax.security.auth.callback.Callback

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
            "nasa")
                .addCallback(object: RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase){
                        super.onCreate(db)
                        // pre-populate (pre-propagar) data
                        Executors.newSingleThreadExecutor().execute{
                            INSTANCE.let {
                                CoroutineScope(Dispatchers.IO).launch{
                                    initialLoad(it)
                                }
                            }
                        }
                    }
                })
                .build()
        }
    }
    return INSTANCE
}

suspend fun initialLoad(db: NasaDatabase){
    withContext(Dispatchers.IO){
        NasaRepository(db).getRandomApods(10)
    }
}

