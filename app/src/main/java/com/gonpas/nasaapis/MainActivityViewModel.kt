package com.gonpas.nasaapis

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.gonpas.nasaapis.database.ApodDb
import com.gonpas.nasaapis.database.FechaVista
import com.gonpas.nasaapis.database.MarsPhotoDb
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.repository.NasaRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.*

private const val TAG = "xxMavm"
class MainActivityViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = NasaRepository(getDatabase(app))

    lateinit var apods: List<ApodDb>
    private lateinit var marsFotos: List<MarsPhotoDb>
    private lateinit var fechasMarte: List<FechaVista>

    val datosBackupRecibidos = MutableLiveData(false)
    var apodsCount = 0
    var marsPhotosCount = 0
    var fechasVistasCount = 0
    val showRestoreInfo = MutableLiveData(false)
    val showBackupInfo = MutableLiveData(false)

    private val _showErrorFileDialog = MutableLiveData<Boolean>()
    val showErrorFileDialog: LiveData<Boolean>
        get() = _showErrorFileDialog


    init {
        _showErrorFileDialog.value = false
    }

    fun obtenerDatos(){

        viewModelScope.launch {
            val recibidos = retrieveDataAsync()
            Log.d(TAG,"recibidos?: $recibidos")
            datosBackupRecibidos.value = recibidos
            if (recibidos) {
                apodsCount = apods.size
                marsPhotosCount = marsFotos.size
                fechasVistasCount = fechasMarte.size
            }
        }
    }

    fun resetCountData() {
        apodsCount = 0
        marsPhotosCount = 0
        fechasVistasCount = 0
        showRestoreInfo.value = false
        showBackupInfo.value = false
    }

    private suspend fun retrieveDataAsync(): Boolean = coroutineScope {
        val deferredApods = async { apods = repository.getApodsAsync() }
        val deferredMarsPhotos = async {
            marsFotos = repository.getMarsPhotosAsync()
//            Log.d(TAG,"fotos recibidas: $marsFotos")
        }
        val deferredFechasVistas = async { fechasMarte = repository.getFechasAsync() }
        deferredApods.await()
        deferredMarsPhotos.await()
        deferredFechasVistas.await()

        return@coroutineScope apods.isNotEmpty() || marsFotos.isNotEmpty() || fechasMarte.isNotEmpty()
    }

    fun editFile(uri: Uri){
//        Log.d(TAG,"editFile uri: $uri")
        val jsonString = dataToJson()
        val contentResolver = app.contentResolver

        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { fos ->
                    fos.write(jsonString.toByteArray())
                }
            }
            showBackupInfo.value = true
        }catch (e: FileNotFoundException){
            e.printStackTrace()
            Log.d(TAG,e.toString())
        }catch (e: IOException){
            e.printStackTrace()
            Log.e(TAG,"No se pudo escribir en el archivo: $e")
        }
    }

    private fun dataToJson(): String{
        val gson = Gson()
        var stringJson = """{"Apods":"""
        stringJson = stringJson.plus(gson.toJsonTree(apods).toString())
        stringJson = stringJson.plus(""","MarsPhotos":""")
        stringJson = stringJson.plus(gson.toJsonTree(marsFotos).toString())
        stringJson = stringJson.plus(""","MarsDates":""")
        stringJson = stringJson.plus(gson.toJsonTree(fechasMarte).toString())
        stringJson = stringJson.plus("}")

//        Log.d(TAG, "json out: $jsonString")
        return stringJson
    }

    fun rebuildData(uri: Uri) {
//        Log.d(TAG, "making json de ${uri.path}")
        val contentResolver = app.contentResolver

        var stringJson = ""

        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(inputStream.reader()).use {
                stringJson = it.readText()
            }
        }

        val gson = GsonBuilder().create()

        try {
            val jsonObject = JSONObject(stringJson)

            val jsonApods = jsonObject.getJSONArray("Apods")
            val numApods = jsonApods.length()
//            Log.d(TAG, "apods del backup: $numApods")
            val jsonMarsPhotos = jsonObject.getJSONArray("MarsPhotos")
            var numPhotos = jsonMarsPhotos.length()
//            Log.d(TAG, "marsFotos del backup: $numPhotos")
            val jsonFechas = jsonObject.getJSONArray("MarsDates")
            var numFechas = jsonFechas.length()
//            Log.d(TAG, "fechas del backup: $numFechas")

            viewModelScope.launch {
                for (i in 0 until numApods) {
                    val apod = gson.fromJson(jsonApods.getString(i), ApodDb::class.java)
                    apod.apodId = 0L
                    repository.insertApod(apod)

                }

                for (i in 0 until numPhotos) {
                    val foto = gson.fromJson(jsonMarsPhotos.getString(i), MarsPhotoDb::class.java)
                    val rowId = repository.saveMarsPhoto(foto)
                    if (rowId == -1L)   numPhotos--
                }

                for (i in 0 until numFechas) {
                    val fecha = gson.fromJson(jsonFechas.getString(i), FechaVista::class.java)
                    val rowId = repository.insertFechaVista(
                        fecha.rover,
                        fecha.fecha,
                        fecha.sol,
                        fecha.totalFotos,
                        fecha.disponible
                    )
                    if (rowId == -1L)   numFechas--
                }
                Log.d(TAG,"insertados $numApods Apods, $numPhotos MarsFotos y $numFechas Fechas vistas")
                apodsCount = numApods
                marsPhotosCount = numPhotos
                fechasVistasCount = numFechas

                showRestoreInfo.value = true
            }


        } catch (e: Exception) {
            Log.d(TAG, "Error de recuperacion: $e")
            Toast.makeText(app, "${app.getText(R.string.error_restore)} $e", Toast.LENGTH_LONG)
                .show()
        }
    }
}

class MainActivityViewModelFactory(private val app: Application): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityViewModel(app) as T
    }
}