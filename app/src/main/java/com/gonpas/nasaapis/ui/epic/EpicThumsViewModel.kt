package com.gonpas.nasaapis.ui.epic

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.DomainEpic
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

    init {
        getLastEpic()
    }

    fun getLastEpic(){
        //viewModelScope.launch {
            try {
                _epics = repository.getLastEpic().asLiveDataListDomainEpic() as MutableLiveData<List<DomainEpic>>
            }catch (ce: CancellationException) {
                throw ce    // NECESARIO PARA CANCELAR EL SCOPE DE LA RUTINA
            }catch (e: Exception){
                Log.e("xxAvm", "Error de red: ${e.message}")
                Toast.makeText(app, "${ app.getString(R.string.no_network) } \n ${e.message}", Toast.LENGTH_LONG).show()
            }
       // }
    }

}



class EpicThumbsViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EpicThumbsViewModel::class.java))
            return EpicThumbsViewModel(application) as T
        throw IllegalArgumentException("Not a EpicThumbsViewModel\nUnknown ViewModel class")

    }
}