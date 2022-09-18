package com.gonpas.nasaapis.ui.apods

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.*
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.repository.NasaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.reflect.jvm.internal.impl.resolve.constants.IntValue

class MasApodsViewModel(val app: Application) : AndroidViewModel(app) {

    val repository = NasaRepository(getDatabase(app))
    var fecha ="%s-%s-%s"

    private val _apod = MutableLiveData<DomainApod?>()
    val apod: LiveData<DomainApod?>
        get() = _apod

    private val _dia = MutableLiveData<String>()
    val dia: LiveData<String>
        get() = _dia

    private val _mes = MutableLiveData<String>()
    val mes: LiveData<String>
        get() = _mes

    private val _anno = MutableLiveData<String>()
    val anno: LiveData<String>
        get() = _anno

    fun buscarPorFecha(){
        Log.d("xxMavm","Buscar por fecha: ${String.format(fecha, anno.value, mes.value, dia.value)}")
        if (dia.value?.let { Integer.parseInt(it) } in 1..31){
            if (mes.value?.let { Integer.parseInt(it) } in 1..12){
                if (anno.value?.let { Integer.parseInt(it) } in 1980..2022){
                    Log.d("xxMavm","fecha: ${fecha.format(anno.value, mes.value, dia.value)}")
                    viewModelScope.launch {
                        try {
                            buscar()
                        }catch (ce: CancellationException) {
                            throw ce    // NECESARIO PARA CANCELAR EL SCOPE DE LA RUTINA
                        }catch (e: Exception){
                            Log.e("xxAvm", "Error de red: ${e.message}")
                            Toast.makeText(app, "No hay acceso a Internet", Toast.LENGTH_LONG).show()
                        }

                    }
                }
            }
        }
    }

    fun setDia(dia: String){    _dia.value = dia    }
    fun setMes(mes: String){    _mes.value = mes    }
    fun setAnno(anno: String){  _anno.value = anno  }

    val buscarBtnEnable = !_dia.value.isNullOrBlank()  && !_mes.value.isNullOrBlank() && !_anno.value.isNullOrBlank()

    suspend fun buscar(){
        val apod = repository.getApodByDate(fecha.format(anno.value, mes.value, dia.value))
        if(apod != null)
            _apod.value = apod
        else
            Toast.makeText(getApplication(), "Fecha fuera de rando", Toast.LENGTH_LONG).show()
    }

    fun navigated(){
        _apod.value = null
    }

}