package com.gonpas.nasaapis.ui.todayApod

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gonpas.nasaapis.domain.DomainApod

class TodayApodViewModelFactory (
    private val apod: DomainApod,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodayApodViewModel::class.java)) {
            return TodayApodViewModel(apod, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class\nNOT a TodayApodViewModel")
    }
}