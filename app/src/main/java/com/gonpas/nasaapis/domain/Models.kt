package com.gonpas.nasaapis.domain

import android.os.Parcelable
import com.gonpas.nasaapis.util.smartTruncate
import kotlinx.android.parcel.Parcelize

/**
 * Los domain objects son los objetos que maneja kotlin y que serán los que
 * manejen la app y se muestren en la pantalla
 */
@Parcelize
data class DomainApod(
    val apodId: Long,
    val title: String,
    val copyright: String,
    val date: String,
    val explanation: String,
    var url: String,
    var hdurl: String,
    val mediaType: String,
    val serviceVersion: String
): Parcelable{
    /**
     * Short description is used for displaying truncated descriptions in the UI
     */
    val shortDescription: String
        get() = explanation.smartTruncate(145)

    val isVideo = mediaType == "video"
}

@Parcelize
data class DomainEpic(
    val identifier: String,
    val caption: String,
    val imageName: String,
    val date: String
): Parcelable{

    /*private fun makeUrlHd(): String{

    }*/
}




