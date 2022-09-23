package com.gonpas.nasaapis.ui.marsroverphotos

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.lifecycle.*
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.DomainRover
import com.gonpas.nasaapis.network.Photos
import com.gonpas.nasaapis.network.RoversPhotosDTO
import com.gonpas.nasaapis.network.asDomainModel
import com.gonpas.nasaapis.repository.NasaRepository
import com.gonpas.nasaapis.ui.apods.NasaApiStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MarsRoverPhotosViewModel(application: Application) : AndroidViewModel(application){

    private val repository = NasaRepository(getDatabase(application))

    private val _anno = MutableLiveData<String>()
    val anno: LiveData<String>
        get() = _anno
    private val _mes = MutableLiveData<String>()
    val mes: LiveData<String>
        get() = _mes
    private val _dia = MutableLiveData<String>()
    val dia: LiveData<String>
        get() = _dia

    private val _rover = MutableLiveData<String>()
    val rover: LiveData<String>
        get() = _rover

    private val _photos = MutableLiveData<Array<RoversPhotosDTO>>()
    val photos: LiveData<Array<RoversPhotosDTO>>
        get() = _photos

    private val _navigateToRoverManifest = MutableLiveData<DomainRover?>()
    val navigateToRoverManifest: LiveData<DomainRover?>
        get() = _navigateToRoverManifest

    private val _status = MutableLiveData<NasaApiStatus>()
    val status: LiveData<NasaApiStatus>
        get() = _status

    init {
        _navigateToRoverManifest.value = null
        _rover.value = "perseverance"
        getLatestFotos()
    }

    fun setDia(dia: String) {
        _dia.value = dia
        Log.d("xxMrpvm","setDia: ${_dia.value}")
    }
    fun setMes(mes: String) {
        _mes.value = mes
        Log.d("xxMrpvm","setMes: ${_mes.value}")
    }
    fun setAnno(anno: String){
        _anno.value = anno
        Log.d("xxMrpvm","setAño: ${_anno.value}")
    }

    fun radioBtnChecked(view: View){
       // Log.d("xxMrpvm","seleccionado: ${view.id}")
        _rover.value = when(view.id){
            R.id.perseverance -> "perseverance"
            R.id.curiosity -> "curiosity"
            R.id.opportunity -> "opportunity"
            R.id.spirit -> "spirit"
            else -> "Rover desconocido"
        }
        _anno.value = ""
        _mes.value =  ""
        _dia.value =  ""
    }
    fun getLatestFotos(){
        _status.value = NasaApiStatus.LOADING
        viewModelScope.launch {
            try {
                getLatestMarsFotos()
                _status.value = NasaApiStatus.DONE
            }catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                _status.value = NasaApiStatus.ERROR
                Log.d("xxMrpvm", "error de descarga: ${e.message}")
                Toast.makeText(getApplication(), "Fecha fuera de rango\n${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun getLatestMarsFotos(){
        _photos.value = repository.getLatestMarsRoversPhotos(rover.value!!).photos
        val fecha = photos.value?.get(0)?.earthDate?.split("-")
        _anno.value = fecha?.get(0)
        _mes.value = fecha?.get(1)
        _dia.value = fecha?.get(2)

    }

    fun getFotos(){
        if (dia.value.isNullOrBlank() && mes.value.isNullOrBlank() && anno.value.isNullOrBlank()){
            _status.value = NasaApiStatus.LOADING
            viewModelScope.launch {
                try {
                    getLatestMarsFotos()
                    _status.value = NasaApiStatus.DONE
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    _status.value = NasaApiStatus.ERROR
                    Log.d("xxMrpvm", "error de descarga: ${e.message}")
                    Toast.makeText(getApplication(), e.message, Toast.LENGTH_LONG).show()
                }
            }
        }else {
            if (dia.value?.let { Integer.parseInt(it) } in 1..31 &&
                mes.value?.let { Integer.parseInt(it) } in 1..12 &&
                anno.value?.let { Integer.parseInt(it) } in 2004..2022) {
                        _status.value = NasaApiStatus.LOADING
                        viewModelScope.launch {
                            try {
                                getMarsPhotos()
                                _status.value = NasaApiStatus.DONE
                            } catch (ce: CancellationException) {
                                throw ce
                            } catch (e: Exception) {
                                _status.value = NasaApiStatus.ERROR
                                Log.d("xxMrpvm", "error de descarga: ${e.message}")
                                Toast.makeText(
                                    getApplication(),
                                    "Fecha fuera de rango\n${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
            }else{
                Toast.makeText(getApplication(),"Fecha no válida", Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun getMarsPhotos(){
        val fotos = repository.getMarsPhotos(rover.value!!, "${anno.value}-${mes.value}-${dia.value}")
        if (fotos.photos.isNotEmpty())
            _photos.value = fotos.photos
        else
            Toast.makeText(getApplication(), "Fecha no disponible", Toast.LENGTH_LONG).show()
    }

    suspend fun getRoverManifest(){
        _navigateToRoverManifest.value = repository.getRoverManifest(rover.value!!).manifest.asDomainModel()
    }

    fun goToRoverManifest(){
        viewModelScope.launch {
            try {
                getRoverManifest()
            }catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Log.d("xxMrpvm", "error de descarga: ${e.message}")
                Toast.makeText(getApplication(), "Sin acceso a Internet", Toast.LENGTH_LONG).show()
            }
        }

    }
    fun navigated(){
        _navigateToRoverManifest.value = null
    }
}





class MarsRoverPhotosViewModelFactory(private val application: Application) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarsRoverPhotosViewModel::class.java))
            return MarsRoverPhotosViewModel(application) as T
        throw IllegalArgumentException("Unknow viewModel class\nNO es un MarsRoverPhotoViewModel")
    }
}