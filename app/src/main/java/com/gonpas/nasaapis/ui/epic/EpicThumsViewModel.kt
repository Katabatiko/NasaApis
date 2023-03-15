package com.gonpas.nasaapis.ui.epic

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.DomainEpic
import com.gonpas.nasaapis.network.asListDomainEpic
import com.gonpas.nasaapis.repository.NasaRepository
import com.gonpas.nasaapis.ui.apods.NasaApiStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val TAG = "xxEtvm"

class EpicThumbsViewModel(application: Application) : AndroidViewModel(application) {
    val app = application
    private val repository = NasaRepository(getDatabase(application))

    private var _epics = MutableLiveData<List<DomainEpic>>()
    val  epics: LiveData<List<DomainEpic>>
        get() = _epics

    private var _coleccion = MutableLiveData<String>()
    val coleccion: LiveData<String>
        get() = _coleccion

    private var _anno = MutableLiveData<String>()
    val anno: LiveData<String>
        get() = _anno
    private var _mes = MutableLiveData<String>()
    val mes: LiveData<String>
        get() = _mes
    private var _dia = MutableLiveData<String>()
    val dia: LiveData<String>
        get() = _dia

    private var _navigateToFullScreenEpic = MutableLiveData<DomainEpic?>()
    val navigateToFullScreenEpic: LiveData<DomainEpic?>
        get() = _navigateToFullScreenEpic

    private var _navigateToSliderEpics = MutableLiveData<List<DomainEpic>>()
    val navigateToSliderEpics: LiveData<List<DomainEpic>>
        get() = _navigateToSliderEpics


    private val _status = MutableLiveData<NasaApiStatus>()
    val status: LiveData<NasaApiStatus>
        get() = _status

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String>
        get() = _errorMsg


    init {
        _coleccion.value = "enhanced"
        getLastEpic()
    }

    fun setCollection(natural: Boolean){
        _coleccion.value = if(natural)  "natural" else "enhanced"
    }

    fun getLastEpic(){
        _status.value = NasaApiStatus.LOADING

        Log.d(TAG,"coleccion: ${_coleccion.value}")
        viewModelScope.launch {
            try {
                _epics.value = repository.getLastEpic(_coleccion.value!!).asListDomainEpic()
         //       Log.d(TAG,"last _epics size: ${_epics.value?.size}")
                val fecha = _epics.value!![0].date.split(" ")[0].split("-")
       //         Log.d(TAG,"fecha: $fecha")
                _anno.value = fecha[0]
                _mes.value = fecha[1]
                _dia.value = fecha[2]
                _status.value = NasaApiStatus.DONE
            }catch (ce: CancellationException) {
                throw ce    // NECESARIO PARA CANCELAR EL SCOPE DE LA RUTINA
            }catch (e: Exception){
                _status.value = NasaApiStatus.ERROR
                Log.e(TAG, "Error de red: ${e.message}")
                Toast.makeText(app, "${ app.getString(R.string.no_network) } \n ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setAnno(anno: String){
        _anno.value = anno
//        Log.d(TAG,"setAño: ${_anno.value}")
    }
    fun setMes(mes: String){
        _mes.value = mes
//        Log.d(TAG,"setMes: ${_mes.value}")
    }
    fun setDia(dia: String){
        _dia.value = dia
//        Log.d(TAG,"setDia: ${_dia.value}")
    }

    fun getEpicByDate(){
        _status.value = NasaApiStatus.LOADING
        val date = "%s-%s-%s".format(anno.value, mes.value, dia.value)
        Log.d(TAG,"fecha formateada: $date")
        Log.d(TAG,"coleccion: ${_coleccion.value}")
        viewModelScope.launch {
            try {
                _epics.value = repository.getEpicsByDate(_coleccion.value!!, date).asListDomainEpic()
     //           Log.d(TAG,"by date _epics size: ${_epics.value?.size}")
                if (!_epics.value.isNullOrEmpty()){
                    val fecha = _epics.value!![0].date.split(" ")[0].split("-")
                    _anno.value = fecha[0]
                    _mes.value = fecha[1]
                    _dia.value = fecha[2]
                    _status.value = NasaApiStatus.DONE
                }else{
                    Toast.makeText(app, "Fecha no disponible", Toast.LENGTH_LONG).show()
                }
            }catch (ce: CancellationException) {
                throw ce    // NECESARIO PARA CANCELAR EL SCOPE DE LA RUTINA
            }catch (e: Exception){
                _status.value = NasaApiStatus.ERROR
                Log.e(TAG, "Error de red: ${e.message}")
                Toast.makeText(app, "${ app.getString(R.string.no_network) } \n ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getUrl(epic: DomainEpic): String {
        var url = "https://epic.gsfc.nasa.gov/archive/%s/%s/%s/%s/thumbs/%s.jpg"
        val fecha = epic.date.split(" ")[0].split("-")
        return url.format(coleccion.value, fecha[0], fecha[1],fecha[2], epic.imageName)
    }

    fun goFullscreen(epic: DomainEpic){
        _navigateToFullScreenEpic.value = epic
    }
    fun navigated(){
        _navigateToFullScreenEpic.value = null
    }

    fun goSlider(){
        Log.d(TAG,"Go slider")
        _navigateToSliderEpics.value = _epics.value
    }
    fun navigatedToSlider(){
        _navigateToSliderEpics.value = listOf<DomainEpic>()
    }

    var btnAnimationEnable = epics.map{
        it.isNotEmpty()
    }

}



class EpicThumbsViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EpicThumbsViewModel::class.java))
            return EpicThumbsViewModel(application) as T
        throw IllegalArgumentException("Not a EpicThumbsViewModel\nUnknown ViewModel class")

    }
}