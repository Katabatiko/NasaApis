package com.gonpas.nasaapis.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.gonpas.nasaapis.database.ApodDb
import com.gonpas.nasaapis.database.NasaDatabase
import com.gonpas.nasaapis.database.asDomainModel
import com.gonpas.nasaapis.database.asListDomainModel
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.domain.DomainEpic
import com.gonpas.nasaapis.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat


private const val TAG = "xxNr"

class NasaRepository(private val database: NasaDatabase) {
    /******************************************************************************************************/
    /******************************************************************************************************/
    /** APOD */

    /**
     * Refresh the videos stored in the offline cache.
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     *
     */
    fun getApodsFromDb(): LiveData<List<ApodDb>> {
      //  Log.d(TAG, "Solicitando todos los apods")
        val apods = database.nasaDao.getAllApods()
//        Log.d(TAG,"Total apods recibidos: ${apods.value?.size ?: 0}")
        return apods
    }

    /**
     * Recoje el apod de hoy y lo guarda en la base de datos,
     * comprobando si no ha sido ya recogido
     */
    suspend fun getTodayApod() {
        Log.d(TAG, "Solicitando el apod del servicio")
        withContext(Dispatchers.IO) {
            val lastApod = database.nasaDao.getLastApod()
//            Log.d(TAG, "fecha ultimo apod: ${lastApod?.date ?: "nulo"}")
            val timeMillis = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val todayDate = sdf.format(timeMillis)
            val todayApod: ApodDto
//            Log.d(TAG, "fecha actual: $todayDate")
            if ( lastApod != null) {
                if (lastApod.date != todayDate){
                    todayApod = NasaApi.retrofitApodService.getApod()
//                    Log.d(TAG, "fecha todayApod: ${todayApod.date}")
                    database.nasaDao.insertApod(todayApod.asDatabaseModel())
                } else {
                    Log.d(TAG, "Imagen ya contenida")
                }
            }else{
                // si es nulo es porque la base de datos está vacía
                todayApod = NasaApi.retrofitApodService.getApod()
                database.nasaDao.insertApod(todayApod.asDatabaseModel())
            }
        }
    }

    suspend fun removeApod(key: Long){
        withContext(Dispatchers.IO){
            val afectedRows = database.nasaDao.removeApod(key)
//            Log.d(TAG,"elementos eliminados: $afectedRows")
        }
    }

    suspend fun getApodByDate(date: String): DomainApod?{
        var apodFromDb = database.nasaDao.getApodByDate(date)
        if(apodFromDb == null) {
            withContext(Dispatchers.IO) {
                val apodDate = NasaApi.retrofitApodService.getApodByDate(date = date)
                if (apodDate != null) {
                    database.nasaDao.insertApod(apodDate.asDatabaseModel())
                    apodFromDb = database.nasaDao.getApodByDate(date)
                }
            }
        }
            return apodFromDb!!.asDomainModel()
    }

    suspend fun getRandomApods(count: Int = 5){
       // var apods: List<DomainApod>
        withContext(Dispatchers.IO) {
            val randomApods = NasaApi.retrofitApodService.getRandomApods()
            for (apod in randomApods){
                database.nasaDao.insertApod(apod.asDatabaseModel())
            }
          // apods = database.nasaDao.getLastsInserterApods(count).asListDomainModel()
        }
    }
/******************************************************************************************************/
/******************************************************************************************************/
    /** EPIC */
    suspend fun getLastEpic(): List<EpicDTO>{
        var lista: List<EpicDTO>
        withContext(Dispatchers.IO) {
            lista =  NasaApi.retrofitEpicService.getLastNaturalEpic()
//        Log.d("xxNr","dentro rutina Epics recibidos: $lista")
        }
//        Log.d("xxNr","fuera rutina Epics recibidos: $lista")
        return lista
    }

    suspend fun getNaturalEpicByDate(date: String): List<EpicDTO>{
        var lista: List<EpicDTO>
        withContext(Dispatchers.IO){
            lista = NasaApi.retrofitEpicService.getNaturalEpicByDate(date)
//            Log.d("xxNr","IN rutina Epics by date recibidos: $lista")
        }
//        Log.d("xxNr","OUT rutina Epics recibidos: $lista")
        return lista    }
    /******************************************************************************************************/
    /******************************************************************************************************/
    /** MARS FOTOS */
    suspend fun getMarsPhotos(rover: String, date: String): Photos{
        val fotos: Photos
        withContext(Dispatchers.IO){
            fotos =  NasaApi.retrofitMarsRoversService.getRoverPhotos(rover, date)
        }
        return fotos
    }

    suspend fun getLatestMarsRoversPhotos(rover: String): LatestPhotos{
        val fotos: LatestPhotos
        withContext(Dispatchers.IO){
            fotos = NasaApi.retrofitMarsRoversService.getRoverLatestPhotos(rover)
        }
        return fotos
    }

    suspend fun getRoverManifest(rover: String): PhotoManifest{
        val roverManifest: PhotoManifest
        withContext(Dispatchers.IO){
            roverManifest = NasaApi.retrofitMarsRoversService.getRoverManifest(rover)
        }
        return roverManifest
    }
}

