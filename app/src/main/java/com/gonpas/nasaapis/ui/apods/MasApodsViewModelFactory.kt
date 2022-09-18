package com.gonpas.nasaapis.ui.apods

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MasApodsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MasApodsViewModel::class.java))
            return MasApodsViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class\nNOT a ApodViewModel")
    }
}