package com.gonpas.nasaapis.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.gonpas.nasaapis.database.ApodDb
import com.gonpas.nasaapis.database.NasaDatabase
import com.gonpas.nasaapis.database.asDomainModel
import com.gonpas.nasaapis.database.asListDomainModel
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.domain.DomainEpic
import com.gonpas.nasaapis.network.EpicDTO
import com.gonpas.nasaapis.network.NasaApi
import com.gonpas.nasaapis.network.asDatabaseModel
import com.gonpas.nasaapis.network.asListDomainEpic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private const val EPIC_FOTO_URL = "https://epic.gsfc.nasa.gov/archive/"

class NasaRepository(private val database: NasaDatabase) {

   // var id: Long = 0L

    //private var _allApods = MutableLiveData<List<DomainApod>>()
    /*val allApods: LiveData<List<DomainApod>> = Transformations.map(database.nasaDao.getAllApods()) {
        it.asListDomainModel()
    }*/
      //  get() = _allApods

   /* private val _apod = MutableLiveData<DomainApod?>()
    val apod: MutableLiveData<DomainApod?>
        get() = _apod*/


    /*suspend fun inicializa(){
            _allApods = database.nasaDao.getAllApods()
            if (allApods.value.isNotEmpty()) {
                _apod.value = allApods.value[0].asDomainModel()
            }
    }*/

    /**
     * Refresh the videos stored in the offline cache.
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     *
     */
    fun getApodsFromDb(): LiveData<List<ApodDb>> {
        Log.d("xxNr", "Solicitando todos los apods")
        val apods = database.nasaDao.getAllApods()
        Log.d("xxNr","Total apods recibidos: ${apods.value?.size ?: 0}")
        return apods
    }

    /**
     * Recoje el apod de hoy y lo guarda en la base de datos,
     * comprobando si no ha sido ya recogido
     */
    suspend fun getTodayApod() {
        Log.d("xxNr", "Solicitando el apod del servicio")
        withContext(Dispatchers.IO) {
            val lastApod = database.nasaDao.getLastApod()
            Log.d("xxNr", "fecha ultimo apod: ${lastApod?.date ?: "nulo"}")
            val todayApod = NasaApi.retrofitApodService.getApod()
            Log.d("xxNr", "fecha todayApod: ${todayApod.date}")
            Log.d("xxNr","todayApod: $todayApod")
            if (lastApod == null) {
                database.nasaDao.insertApod(todayApod.asDatabaseModel())
            }else {
                if (lastApod.date != todayApod.date) {
                    database.nasaDao.insertApod(todayApod.asDatabaseModel())
                } else {
                    Log.d("Nasa Repository","Imagen ya contenida")
                }
            }
        }
    }

    suspend fun removeApod(key: Long){
        withContext(Dispatchers.IO){
            val afectedRows = database.nasaDao.removeApod(key)
            Log.d("xxNr","elementos eliminados: $afectedRows")
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
    fun getLastEpic(): LiveData<List<EpicDTO>>{
        var lista: LiveData<List<EpicDTO>>
        //withContext(Dispatchers.IO) {
            lista =  NasaApi.retrofitEpicService.getLastNaturalEpic()
        //}
        Log.d("xxNr","Epics recibidos: $lista")
        return lista
    }
}

