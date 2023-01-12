package com.gonpas.nasaapis.ui.apods

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("unchecked_cast")
class ApodsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApodsViewModel::class.java))
            return ApodsViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class\nNOT a ApodViewModel")
    }
}

//@Suppress("unchecked_cast")
//class ApodsViewModelFactory(private val nasaRepository: NasaRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(ApodsViewModel::class.java))
//            return ApodsViewModel(nasaRepository) as T
//        throw IllegalArgumentException("Unknown ViewModel class\nNOT a ApodViewModel")
//    }
//}

//@Suppress("unchecked_cast")
//class ApodsViewModelFactory(private val nasaRepository: NasaRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(ApodsViewModel::class.java))
//            return ApodsViewModel(nasaRepository) as T
//        throw IllegalArgumentException("Unknown ViewModel class\nNOT a ApodViewModel")
//    }
//}