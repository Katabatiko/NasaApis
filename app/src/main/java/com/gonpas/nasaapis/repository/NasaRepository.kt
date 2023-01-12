package com.gonpas.nasaapis.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.gonpas.nasaapis.database.*
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.domain.DomainFechaVista
import com.gonpas.nasaapis.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat


private const val TAG = "xxNr"

class NasaRepository(private val database: NasaDatabase) {
    /******************************************************************************************************/
    /******************************************************************************************************/
    /** APOD */

    fun getApodsFromDb(): LiveData<List<ApodDb>> {
      //  Log.d(TAG, "Solicitando todos los apods")
        val apods = database.nasaDao.getAllApods()
//        Log.d(TAG,"Total apods recibidos: ${apods.value?.size ?: 0}")
        return apods
    }

    /**
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     *
     */

    /**
     * Recoje el apod de hoy y lo guarda en la base de datos,
     * comprobando antes si no ha sido ya recogido
     */
    suspend fun getTodayApod() :Boolean {
        Log.d(TAG, "Solicitando el apod del servicio")
        var descargado = false
        withContext(Dispatchers.IO) {
            val lastApod = database.nasaDao.getLastApod()
//            Log.d(TAG, "fecha ultimo apod: ${lastApod?.date ?: "nulo"}")
            val timeMillis = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val todayDate = sdf.format(timeMillis)
//            Log.d(TAG, "fecha actual: $todayDate")
            val todayApod: ApodDto
//            if ( lastApod != null) {
                if (lastApod.date != todayDate){
                    todayApod = NasaApi.retrofitApodService.getApod()
//                    fecha = extraerFecha(todayApod.date)
//                    Log.d(TAG, "fecha todayApod: ${todayApod.date}")
                    database.nasaDao.insertApod(todayApod.asDatabaseModel())
                    descargado = true
                } else {
                    Log.d(TAG, "Imagen ya contenida")
                }
//            }else{
//                // si es nulo es porque la base de datos está vacía
//                todayApod = NasaApi.retrofitApodService.getApod()
//                database.nasaDao.insertApod(todayApod.asDatabaseModel())
//            }
        }
        return descargado
    }

    suspend fun insertApod(apodDb: ApodDb): Long{
        val newId: Long
        withContext(Dispatchers.IO) {
            newId = database.nasaDao.insertApod(apodDb)
        }
        return newId
    }

    suspend fun removeApod(key: Long){
        withContext(Dispatchers.IO){
            database.nasaDao.removeApod(key)
        }
    }

    suspend fun getLastApod(): DomainApod{
        // se usa para recoger el todayApod descargado por el worker,
        // por lo que no es nulo nunca
        val lastApod: DomainApod
        withContext(Dispatchers.IO){
            lastApod = database.nasaDao.getLastApod().asDomainModel()
        }
        return lastApod
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
            val randomApods = NasaApi.retrofitApodService.getRandomApods(count)
            for (apod in randomApods){
                database.nasaDao.insertApod(apod.asDatabaseModel())
            }
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

    suspend fun saveMarsPhoto(foto: MarsPhotoDb){
        Log.d(TAG,"guardando foto en db")
        withContext(Dispatchers.IO) {
            database.nasaDao.insertMarsPhoto(foto)
        }
    }

    fun getMarsPhotosFromDb(): LiveData<List<MarsPhotoDb>>{
        return database.nasaDao.getAllMarsPhotos()
    }

    suspend fun removeMarsFoto(id: Int){
        withContext(Dispatchers.IO){
            database.nasaDao.removeMarsPhoto(id)
        }
    }
    /******************************************************************************************************/
    /******************************************************************************************************/
    /** MARS FECHAS VISTAS */

    suspend fun insertFechaVista(rover: String, fecha: String, sol: Int?, disponible: Boolean){
        val fechaVista = FechaVista(rover, fecha, sol, disponible)
        withContext(Dispatchers.IO){
            database.nasaDao.insertFechaVista(fechaVista)
        }
    }

    fun getAllMarsFechas() : LiveData<List<FechaVista>>{
        val fechas: LiveData<List<FechaVista>> = database.nasaDao.getAllFechas()
        return fechas
    }

    fun getFechasByRover(rover: String): LiveData<List<DomainFechaVista>>{
        val fechas: LiveData<List<DomainFechaVista>> = database.nasaDao.getFechasByRover(rover).asListDomainFechaVista()
//        withContext(Dispatchers.IO){
       //     fechas = database.nasaDao.getFechasByRover(rover)
//        Log.d(TAG, "fechasVistas de $rover: ${fechas.value}")
//        }
//        val allFechas: LiveData<List<String>> = fechas.map { it.map { fechaVista -> fechaVista.fecha } }
//            .distinctUntilChanged()
//        Log.d(TAG, "fechas de $rover: ${allFechas.value}")

        return fechas
    }
}

