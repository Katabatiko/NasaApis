package com.gonpas.nasaapis.ui.epic

import android.app.Application
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.*
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.DomainEpic
import com.gonpas.nasaapis.network.asListDomainEpic
import com.gonpas.nasaapis.network.asLiveDataListDomainEpic
import com.gonpas.nasaapis.repository.NasaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class EpicThumbsViewModel(application: Application) : AndroidViewModel(application) {
    val app = application
    private val repository = NasaRepository(getDatabase(application))

    private var _epics = MutableLiveData<List<DomainEpic>>()
    val  epics: LiveData<List<DomainEpic>>
        get() = _epics

    private var _anno = MutableLiveData<String>()
    val anno: LiveData<String>
        get() = _anno
    private var _mes = MutableLiveData<String>()
    val mes: LiveData<String>
        get() = _mes
    private var _dia = MutableLiveData<String>()
    val dia: LiveData<String>
        get() = _dia


    init {
        getLastEpic()
    }

    fun getLastEpic(){
        viewModelScope.launch {
            try {
                _epics.value = repository.getLastEpic().asListDomainEpic()
                Log.d("xxEtvm","last _epics size: ${_epics.value?.size}")
                val fecha = _epics.value!![0].date.split(" ")[0].split("-")
                Log.d("xxEtvm","fecha: $fecha")
                _anno.value = fecha[0]
                _mes.value = fecha[1]
                _dia.value = fecha[2]
            }catch (ce: CancellationException) {
                throw ce    // NECESARIO PARA CANCELAR EL SCOPE DE LA RUTINA
            }catch (e: Exception){
                Log.e("xxEtvm", "Error de red: ${e.message}")
                Toast.makeText(app, "${ app.getString(R.string.no_network) } \n ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setAnno(anno: String){
        _anno.value = anno
        Log.d("xxEtvm","setAño: ${_anno.value}")
    }
    fun setMes(mes: String){
        _mes.value = mes
        Log.d("xxEtvm","setMes: ${_mes.value}")
    }
    fun setDia(dia: String){
        _dia.value = dia
        Log.d("xxEtvm","setDia: ${_dia.value}")
    }

    fun getEpicByDate(){
        val date = "%s-%s-%s".format(anno.value, mes.value, dia.value)
        Log.d("xxEtvm","fecha formateada: $date")
        viewModelScope.launch {
            try {
                _epics.value = repository.getNaturalEpicByDate(date).asListDomainEpic()
                Log.d("xxEtvm","by date _epics size: ${_epics.value?.size}")
                if (!_epics.value.isNullOrEmpty()){
                    val fecha = _epics.value!![0].date.split(" ")[0].split("-")
                    _anno.value = fecha[0]
                    _mes.value = fecha[1]
                    _dia.value = fecha[2]
                }else{
                    Toast.makeText(app, "Fecha no disponible", Toast.LENGTH_LONG).show()
                }
            }catch (ce: CancellationException) {
                throw ce    // NECESARIO PARA CANCELAR EL SCOPE DE LA RUTINA
            }catch (e: Exception){
                Log.e("xxEtvm", "Error de red: ${e.message}")
                Toast.makeText(app, "${ app.getString(R.string.no_network) } \n ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getEpicsFullImage(){

    }

}



class EpicThumbsViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EpicThumbsViewModel::class.java))
            return EpicThumbsViewModel(application) as T
        throw IllegalArgumentException("Not a EpicThumbsViewModel\nUnknown ViewModel class")

    }
}