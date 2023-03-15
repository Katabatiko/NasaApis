package com.gonpas.nasaapis.ui.apods

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.repository.NasaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

private const val TAG = "xxMavm"

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
        val sdf = SimpleDateFormat("yyyy")
        val todayYear = sdf.format(System.currentTimeMillis())
//        Log.d(TAG, "Año: $todayYear")
        val thisYear = Integer.parseInt(todayYear)
   //     Log.d(TAG,"Buscar por fecha: ${String.format(fecha, anno.value, mes.value, dia.value)}")
        if (dia.value?.let { Integer.parseInt(it) } in 1..31){
            if (mes.value?.let { Integer.parseInt(it) } in 1..12){
                if (anno.value?.let { Integer.parseInt(it) } in 1995..thisYear){
                    Log.d(TAG,"fecha: ${fecha.format(anno.value, mes.value, dia.value)}")
                    viewModelScope.launch {
                        try {
                            buscar()
                        }catch (ce: CancellationException) {
                            throw ce    // NECESARIO PARA CANCELAR EL SCOPE DE LA RUTINA
                        }catch (e: Exception){
                            Log.e(TAG, "Error de red: ${e.message}")
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


    suspend fun buscar(){
        val apod = repository.getApodByDate(fecha.format(anno.value, mes.value, dia.value))
        if(apod != null)
            _apod.value = apod
        else
            Toast.makeText(app, "Fecha fuera de rango ${ fecha.format(dia.value, mes.value, anno.value) }", Toast.LENGTH_LONG).show()
    }

    fun navigated(){
        _apod.value = null
    }

}