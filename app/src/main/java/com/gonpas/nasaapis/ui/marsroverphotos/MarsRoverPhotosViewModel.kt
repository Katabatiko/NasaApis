package com.gonpas.nasaapis.ui.marsroverphotos

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.database.asListDomainMarsPhotos
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.*
import com.gonpas.nasaapis.network.*
import com.gonpas.nasaapis.repository.NasaRepository
import com.gonpas.nasaapis.ui.apods.NasaApiStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

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
    private val _roverStatus = MutableLiveData<String>()
    val roverStatus: LiveData<String>
        get() = _roverStatus

    private val _photos = MutableLiveData<List<DomainMarsPhoto>>()
    val photos: LiveData<List<DomainMarsPhoto>>
        get() = _photos

    val photosId: LiveData<List<Int>>


    lateinit var savedFotosList : LiveData<List<DomainMarsPhoto>>// = repository.getMarsPhotosFromDb().asListDomainMarsPhotos()

    lateinit var _fechasPerseverance: LiveData<List<DomainFechaVista>>

    lateinit var _fechasCuriosity: LiveData<List<DomainFechaVista>>

    lateinit var _fechasOpportunity: LiveData<List<DomainFechaVista>>

    lateinit var _fechasSpirit: LiveData<List<DomainFechaVista>>

    private val _showAlertDialog = MutableLiveData<Boolean>()
    val showAlertDialog: LiveData<Boolean>
        get() = _showAlertDialog

    private val _navigateToRoverManifest = MutableLiveData<DomainRover?>()
    val navigateToRoverManifest: LiveData<DomainRover?>
        get() = _navigateToRoverManifest


    private val _status = MutableLiveData<NasaApiStatus>()
    val status: LiveData<NasaApiStatus>
        get() = _status


    init {
        _navigateToRoverManifest.value = null
//        _goToSavedMarsPhotos.value = listOf()
        _rover.value = "perseverance"
        _roverStatus.value = "active"
        _showAlertDialog.value = false
        initFechasRovers()
        getLatestFotos()
        photosId = repository.getAllMarsPhotosIdsFromDb()
    }

    fun setStatusDone()  {
        _status.value = NasaApiStatus.DONE
    }

    fun setDia(dia: String) {
        _dia.value = dia
    }
    fun setMes(mes: String) {
        _mes.value = mes
    }
    fun setAnno(anno: String){
        _anno.value = anno
    }

    fun radioBtnChecked(view: View){
        _rover.value = when(view.id){
            R.id.perseverance -> "perseverance"
            R.id.curiosity -> "curiosity"
            R.id.opportunity -> "opportunity"
            else -> "spirit"
        }
        _anno.value = ""
        _mes.value =  ""
        _dia.value =  ""
        _roverStatus.value = ""
    }

    fun initFechasRovers(){
        _fechasPerseverance = repository.getFechasByRover("perseverance")
//        Log.d(TAG, "fechas Perseverance: ${_fechasPerseverance.value}")
        _fechasCuriosity = repository.getFechasByRover("curiosity")
//        Log.d(TAG, "fechas Curiosity: ${_fechasCuriosity.value}")
        _fechasOpportunity = repository.getFechasByRover("opportunity")
//        Log.d(TAG, "fechas Opportunity: ${_fechasOpportunity.value}")
        _fechasSpirit = repository.getFechasByRover("spirit")
//        Log.d(TAG, "fechas Spirit ${_fechasSpirit.value}")
    }

    fun alertDialogShowed(){
        _showAlertDialog.value = false
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
//            Log.d(TAG,"obteniendo ultimas fotos de marte")
            _photos.value = repository.getLatestMarsRoversPhotos(rover.value!!).photos.asList().asDomainModel()
            val fecha = photos.value?.get(0)?.earthDate
            _roverStatus.value = photos.value?.get(0)?.roverStatus
            // registro de fecha vista
            val partes = fecha?.split("-")
            _anno.value = partes?.get(0)
            _mes.value = partes?.get(1)
            _dia.value = partes?.get(2)
            if (testFecha("${anno.value}-${mes.value}-${dia.value}")) {
                Log.d(TAG,"evaluando fotos guardadas")
                evalSavedPhotos()
            }else{
                repository.insertFechaVista(rover.value!!, fecha!!, photos.value!!.get(0).sol, true)
            }
        }catch (ce: CancellationException) {
            throw ce    // NECESARIO PARA CANCELAR EL SCOPE DE LA RUTINA
        }catch (e: Exception){
            Log.e(TAG, "Error de red: ${e.message}")
            Toast.makeText(getApplication(), "Sin acceso a Internet", Toast.LENGTH_LONG).show()
        }

    }

    fun getFotos(force: Boolean = false){
        if (dia.value.isNullOrBlank() || mes.value.isNullOrBlank() || anno.value.isNullOrBlank()){
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
            val sdf = SimpleDateFormat("yyyy")
            val todayYear = sdf.format(System.currentTimeMillis())
            val thisYear = Integer.parseInt(todayYear)
            if (dia.value?.let { Integer.parseInt(it) } in 1..31 &&
                mes.value?.let { Integer.parseInt(it) } in 1..12 &&
                anno.value?.let { Integer.parseInt(it) } in 2004..thisYear) {
                    if(!testFecha("${anno.value}-${mes.value}-${dia.value}") || force){
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
//                        Log.d(TAG,"Fecha nueva")
                    }
                    else{
//                        Log.d(TAG,"Fecha ya visitada")
                        _showAlertDialog.value = true
                    }

            }else{
                Toast.makeText(getApplication(),"Fecha no válida", Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun getMarsPhotos(){
        try {
            val fotos = repository.getMarsPhotos(rover.value!!, "${anno.value}-${mes.value}-${dia.value}")
            if (fotos.photos.isNotEmpty()) {
                _photos.value = fotos.photos.asList().asDomainModel()
                _roverStatus.value = photos.value?.get(0)?.roverStatus
                if (testFecha("${anno.value}-${mes.value}-${dia.value}")) {
                    Log.d(TAG,"evaluando fotos guardadas")
                    evalSavedPhotos()
                }else{
                    Log.d(TAG,"testFecha false: ${anno.value}-${mes.value}-${dia.value}")
                    repository.insertFechaVista(rover.value!!, "${anno.value}-${mes.value}-${dia.value}",fotos.photos.get(0).sol, true )
                }
            }
            else {
                repository.insertFechaVista(rover.value!!, "${anno.value}-${mes.value}-${dia.value}",null, false )
                Toast.makeText(getApplication(), "Fecha no disponible", Toast.LENGTH_LONG).show()
            }
        }catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "error de descarga: ${e.message}")
            Toast.makeText(getApplication(), "Sin acceso a Internet \n${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun evalSavedPhotos(){
//        Log.d(TAG,"evaluando ${_photos.value?.size} fotos actuales --> photosId size: ${photosId.value!!.size}")
        _photos.value!!.forEach {
            /*val actual = it.saved
            Log.d(TAG,"id: ${it.marsPhotoId}")
            val nuevo = photosId.value!!.contains(it.marsPhotoId)
            if(actual != nuevo) {
                Log.d(TAG, "cambio en  ${it.marsPhotoId}")
                it.saved = nuevo
            }*/
            it.saved = photosId.value!!.contains(it.marsPhotoId)
        }
    }

    fun testFecha(fecha: String): Boolean{
        return when(rover.value){
            "perseverance" -> {
                _fechasPerseverance.value!!.asListOfString().contains(fecha)
            }
            "opportunity" -> {
                _fechasOpportunity.value!!.asListOfString().contains(fecha)
            }
            "curiosity" -> {
                _fechasCuriosity.value!!.asListOfString().contains(fecha)
            }
            else -> {
                _fechasSpirit.value!!.asListOfString().contains(fecha)
            }
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

    fun loadSavedPhotos(){
        savedFotosList = repository.getMarsPhotosFromDb().asListDomainMarsPhotos()
        _status.value = NasaApiStatus.LOADING
    }


    fun guardarFoto(foto: DomainMarsPhoto){
        viewModelScope.launch {
            savePhoto(foto)
        }
    }

    suspend fun savePhoto(foto: DomainMarsPhoto){
        repository.saveMarsPhoto(foto.asDatabaseModel())
    }

    fun removeFoto(foto: DomainMarsPhoto){
        viewModelScope.launch {
            repository.removeMarsFoto(foto.marsPhotoId)
        }
        // solo interesa cuando es de la fecha actual
        if (foto.earthDate == "${anno.value}-${mes.value}-${dia.value}"){
            findActualPhotoRemoved(foto.marsPhotoId)
        }

    }

    fun findActualPhotoRemoved(fotoId: Int){
        _photos.value?.forEach{
            if (fotoId == it.marsPhotoId){
//                Log.d(TAG,"foto saved: ${foto.saved}")
                it.saved = false
            }
        }
    }

}





class MarsRoverPhotosViewModelFactory(private val application: Application) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarsRoverPhotosViewModel::class.java))
            return MarsRoverPhotosViewModel(application) as T
        throw IllegalArgumentException("Unknow viewModel class\nNO es un MarsRoverPhotoViewModel")
    }
}