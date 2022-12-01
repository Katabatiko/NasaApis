package com.gonpas.nasaapis.ui.marsroverphotos

import android.app.Application
import androidx.lifecycle.*
import com.gonpas.nasaapis.database.MarsPhotoDb
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.repository.NasaRepository
import com.gonpas.nasaapis.ui.apods.NasaApiStatus
import kotlinx.coroutines.launch

class MarsPhotosListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NasaRepository(getDatabase(application))

    val fotosList : LiveData<List<MarsPhotoDb>> = repository.getMarsPhotosFromDb()

    private val _status = MutableLiveData<NasaApiStatus>()
    val status: LiveData<NasaApiStatus>
        get() = _status

    init {
        _status.value = NasaApiStatus.LOADING
    }

    fun setStatusDone()  {
        _status.value = NasaApiStatus.DONE
    }

    fun removeFoto(fotoId: Int){
        viewModelScope.launch {
            repository.removeMarsFoto(fotoId)
        }
    }

}

class MarsPhotosListViewModelFactory(private val application: Application) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarsPhotosListViewModel::class.java))
            return MarsPhotosListViewModel(application) as T

        throw IllegalArgumentException("Unknown ViewModel class\nNOT a MarsPhotosListViewModel")
    }
}