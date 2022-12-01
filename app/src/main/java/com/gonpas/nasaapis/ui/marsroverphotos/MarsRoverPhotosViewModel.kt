package com.gonpas.nasaapis.ui.marsroverphotos

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.DomainRover
import com.gonpas.nasaapis.network.*
import com.gonpas.nasaapis.repository.NasaRepository
import com.gonpas.nasaapis.ui.apods.NasaApiStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val TAG ="xxMrpvm"
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

    /*private val _guardarFoto = MutableLiveData<Boolean>()
    val guardarFoto: LiveData<Boolean>
        get() = _guardarFoto*/


    init {
        _navigateToRoverManifest.value = null
        _rover.value = "perseverance"
//        _guardarFoto.value = false
        getLatestFotos()
    }

    fun setDia(dia: String) {
        _dia.value = dia
//        Log.d(TAG,"setDia: ${_dia.value}")
    }
    fun setMes(mes: String) {
        _mes.value = mes
//        Log.d(TAG,"setMes: ${_mes.value}")
    }
    fun setAnno(anno: String){
        _anno.value = anno
//        Log.d(TAG,"setAño: ${_anno.value}")
    }

    fun radioBtnChecked(view: View){
       // Log.d(TAG,"seleccionado: ${view.id}")
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
                Log.e(TAG, "error de descarga: ${e.message}")
                Toast.makeText(getApplication(), "Sin acceso a internet\n${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun getLatestMarsFotos(){
        try {
            _photos.value = repository.getLatestMarsRoversPhotos(rover.value!!).photos
            val fecha = photos.value?.get(0)?.earthDate?.split("-")
            _anno.value = fecha?.get(0)
            _mes.value = fecha?.get(1)
            _dia.value = fecha?.get(2)
        }catch (ce: CancellationException) {
            throw ce    // NECESARIO PARA CANCELAR EL SCOPE DE LA RUTINA
        }catch (e: Exception){
            Log.e(TAG, "Error de red: ${e.message}")
            Toast.makeText(getApplication(), "Sin acceso a Internet", Toast.LENGTH_LONG).show()
        }

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
                    Log.e(TAG, "error de descarga: ${e.message}")
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
                                Log.e(TAG, "error de descarga: ${e.message}")
                                Toast.makeText(
                                    getApplication(),
                                    "Sin acceso a internet\n${e.message}",
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
        try {
            val fotos = repository.getMarsPhotos(rover.value!!, "${anno.value}-${mes.value}-${dia.value}")
            if (fotos.photos.isNotEmpty())
                _photos.value = fotos.photos
            else
                Toast.makeText(getApplication(), "Fecha no disponible", Toast.LENGTH_LONG).show()
        }catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "error de descarga: ${e.message}")
            Toast.makeText(getApplication(), "Sin acceso a Internet", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun getRoverManifest(){
        try {
            _navigateToRoverManifest.value =
                repository.getRoverManifest(rover.value!!).manifest.asDomainModel()
        }catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "error de descarga: ${e.message}")
            Toast.makeText(getApplication(), "Sin acceso a Internet", Toast.LENGTH_LONG).show()
        }
    }

    fun goToRoverManifest(){
        viewModelScope.launch {
            try {
                getRoverManifest()
            }catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Log.e(TAG, "error de descarga: ${e.message}")
                Toast.makeText(getApplication(), "Sin acceso a Internet", Toast.LENGTH_LONG).show()
            }
        }

    }
    fun navigated(){
        _navigateToRoverManifest.value = null
    }

/*    val guardando = Transformations.map(_guardarFoto) {
        it
    }*/

    /*fun guardarFoto(){
        Log.d(TAG,"clicked guardar foto")
        _guardarFoto.value = true
    }*/
    /*fun fotoGuardada(){
        _guardarFoto.value = false
    }*/

    fun guardarFoto(foto: RoversPhotosDTO){
        viewModelScope.launch {
            savePhoto(foto)
        }
    }

    suspend fun savePhoto(foto: RoversPhotosDTO){
        repository.saveMarsPhoto(foto.asDatabaseModel())
    }
}





class MarsRoverPhotosViewModelFactory(private val application: Application) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarsRoverPhotosViewModel::class.java))
            return MarsRoverPhotosViewModel(application) as T
        throw IllegalArgumentException("Unknow viewModel class\nNO es un MarsRoverPhotoViewModel")
    }
}