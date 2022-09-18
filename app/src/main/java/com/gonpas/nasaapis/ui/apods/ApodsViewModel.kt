package com.gonpas.nasaapis.ui.apods

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.gonpas.nasaapis.database.ApodDb
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.repository.NasaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

enum class NasaApiStatus { LOADING, ERROR, DONE }

class ApodsViewModel(application: Application) : AndroidViewModel(application) {

    private val nasaRepository = NasaRepository(getDatabase(application))

    val allApods: LiveData<List<ApodDb>> = nasaRepository.getApodsFromDb()

    private val _navigateToTodayApod = MutableLiveData<DomainApod?>()
    val navigateToTodayApod: LiveData<DomainApod?>
        get() = _navigateToTodayApod

    private val _status = MutableLiveData<NasaApiStatus>()
    val status: LiveData<NasaApiStatus>
        get() = _status

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String>
        get() = _errorMsg

    init {
        viewModelScope.launch {
            try {
                nasaRepository.getTodayApod()
            }catch (ce: CancellationException) {
                throw ce    // NECESARIO PARA CANCELAR EL SCOPE DE LA RUTINA
            }catch (e: Exception){
                    Log.e("xxAvm", "Error de red: ${e.message}")
                    Toast.makeText(application, "No hay acceso a Internet", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun displayTodayApod(apod: DomainApod){
        _navigateToTodayApod.value = apod
    }

    fun doneNavigation(){
        _navigateToTodayApod.value = null
    }

    fun getApodAleatorios(count: Int = 5){
        Log.d("xxAvm","Entrando en coroutine de apods aleatorios")
        viewModelScope.launch {
            _status.value = NasaApiStatus.LOADING
            try {
                nasaRepository.getRandomApods()
            }catch (e: Exception){
                Log.d("xxAvm","Error: ${e.message}")
                _status.value = NasaApiStatus.ERROR
                _errorMsg.value = e.message
            }
        }
    }


}