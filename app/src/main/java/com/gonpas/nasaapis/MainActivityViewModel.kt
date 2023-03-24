package com.gonpas.nasaapis

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.gonpas.nasaapis.database.ApodDb
import com.gonpas.nasaapis.database.FechaVista
import com.gonpas.nasaapis.database.MarsPhotoDb
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.repository.NasaRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.*

private const val TAG = "xxMavm"
class MainActivityViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = NasaRepository(getDatabase(app))

    lateinit var apods: LiveData<List<ApodDb>>
    lateinit var marsFotos: LiveData<List<MarsPhotoDb>>
    lateinit var fechasMarte: LiveData<List<FechaVista>>

    private val _showErrorFileDialog = MutableLiveData<Boolean>()
    val showErrorFileDialog: LiveData<Boolean>
        get() = _showErrorFileDialog


    init {
        _showErrorFileDialog.value = false
        /*apods = repository.getApodsFromDb()
        marsFotos = repository.getMarsPhotosFromDb()
        fechasMarte = repository.getAllMarsFechas()*/
    }

    fun obtenerDatos(){
        apods = repository.getApodsFromDb()
        marsFotos = repository.getMarsPhotosFromDb()
        fechasMarte = repository.getAllMarsFechas()
    }

    fun editFile(uri: Uri, jsonString: String){
        Log.d(TAG,"editFile uri: $uri")
//        Log.d(TAG,"editFile jsonString: $jsonString")
        val contentResolver = app.contentResolver

        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(jsonString.toByteArray())
                }
            }
        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    fun <Any> dataToJson(data: List<Any>, clase: String): String{
        var gson = Gson()
        var jsonString = gson.toJsonTree(data).toString()
        jsonString = "$clase·$jsonString"

//        Log.d(TAG, "json out: $jsonString")
        return jsonString
    }

    fun rebuildData(uri: Uri){
        Log.d(TAG,"making json de ${uri.path}")
        val contentResolver = app.contentResolver

//        var jsonArray: JSONArray? = null
        var jsonString = ""
        var clase = ""
        val datos: String

        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(inputStream.reader()).use {
                val leido = it.readText().split("·")
                clase = leido[0]
                datos = leido[1]
                jsonString = datos
//                jsonArray = JSONArray(datos)
            }
        }
//        Log.d(TAG,"nº elementos: ${jsonArray?.length()}")
//        Log.d(TAG,"clase: $clase")
//        Log.d(TAG,"jsonArray: $jsonArray")

        val gson = GsonBuilder().create()

//        val numItems = jsonArray?.length()
        val objetos: List<Any>

        when(clase){
            "ApodDb" -> {
                objetos = gson.fromJson(jsonString, Array<ApodDb>::class.java).toList()
                Log.d(TAG,"apods del backup: ${objetos.size}")
                objetos.map {
                    it.apodId = 0L
                }
                viewModelScope.launch {
                    for (apod in objetos){
                        Log.d(TAG,repository.insertApod(apod).toString())
                    }
                }
            }
            "MarsPhotoDb" -> {
                objetos = gson.fromJson(jsonString, Array<MarsPhotoDb>::class.java).toList()
                Log.d(TAG,"fotos del backup: ${objetos.size}")
                viewModelScope.launch {
                    for (foto in objetos){
                        repository.saveMarsPhoto(foto)
                    }
                }
            }
            "FechaVista" -> {
                objetos = gson.fromJson(jsonString, Array<FechaVista>::class.java).toList()
                Log.d(TAG,"fechas del backup: ${objetos.size}")
                viewModelScope.launch {
                    for (fecha in objetos){
                        repository.insertFechaVista(fecha.rover, fecha.fecha, fecha.sol, fecha.totalFotos, true)
                    }
                }
            }
            else -> {
                _showErrorFileDialog.value = true
                Log.e(TAG, "Datos no validos")
            }
        }
    }

}

class MainActivityViewModelFactory(private val app: Application): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityViewModel(app) as T
    }
}