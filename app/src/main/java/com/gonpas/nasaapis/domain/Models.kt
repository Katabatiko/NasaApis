package com.gonpas.nasaapis.domain

import android.os.Parcelable
import com.gonpas.nasaapis.database.MarsPhotoDb
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

}

@Parcelize
data class DomainRover(
    val name: String,
    val landingDate: String,
    val launchDate: String,
    val status: String,
    val maxSol: Int,
    val maxDate: String,
    val totalPhotos: Int
): Parcelable

@Parcelize
data class DomainMarsPhoto(
    val marsPhotoId: Int,
    val sol: Int,
    val camera: String,
    val imgSrc: String,
    val earthDate: String,
    val rover: String,
    val roverStatus: String,
    var saved: Boolean = false
): Parcelable

fun DomainMarsPhoto.asDatabaseModel(): MarsPhotoDb{
    return MarsPhotoDb(
        marsPhotoId = this.marsPhotoId,
        sol = this.sol,
        camera = this.camera,
        imgSrc = this.imgSrc,
        earthDate = this.earthDate,
        rover = this.rover
    )
}

@Parcelize
data class DomainFechaVista(
    val rover: String,
    val fecha: String,       // aaaa-MM-dd
    val sol: Int?,
    val disponible: Boolean
): Parcelable

fun List<DomainFechaVista>.asListOfString(): List<String>{
    return map {
        it.fecha
    }
}