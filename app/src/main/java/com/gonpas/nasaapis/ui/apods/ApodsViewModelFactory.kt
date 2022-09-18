package com.gonpas.nasaapis.ui.apods

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ApodsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApodsViewModel::class.java))
            return ApodsViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class\nNOT a ApodViewModel")
    }
}