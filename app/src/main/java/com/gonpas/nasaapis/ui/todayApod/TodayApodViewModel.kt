package com.gonpas.nasaapis.ui.todayApod

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.*
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.repository.NasaRepository
import kotlinx.coroutines.launch

class TodayApodViewModel(apod: DomainApod, app: Application) : AndroidViewModel(app) {

    private val repository = NasaRepository(getDatabase(app))

    private val _todayApod = MutableLiveData<DomainApod>()
    val todayApod: LiveData<DomainApod>
        get() = _todayApod

    private val _navigateUp = MutableLiveData<Boolean>()
    val navigateUp: LiveData<Boolean>
        get() = _navigateUp

    private val _launchVideo = MutableLiveData<Boolean>()
    val launchVideo: LiveData<Boolean>
        get() = _launchVideo

    init {
        _todayApod.value = apod
    }

    fun delApod(){
        viewModelScope.launch {
            val actualApod = todayApod.value ?: return@launch
                 removeApod(actualApod.apodId)
        }
        navigateBack()
    }

    private suspend fun removeApod(key: Long){
            repository.removeApod(key)
    }

    fun navigateBack(){
        _navigateUp.value = true
        Log.d("xxTavm","invocado navigateBack()")
    }

    fun navigated(){
        _navigateUp.value = false
        Log.d("xxTavm","invocado navigated()")
    }

    fun launchVideo(){
        _launchVideo.value = true
    }

    fun videoLaunched(){
        _launchVideo.value = false
    }

}