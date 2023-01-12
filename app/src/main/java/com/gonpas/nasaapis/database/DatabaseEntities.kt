package com.gonpas.nasaapis.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.domain.DomainFechaVista
import com.gonpas.nasaapis.domain.DomainMarsPhoto


@Entity
data class ApodDb constructor(
    @PrimaryKey(autoGenerate = true)
    var apodId: Long = 0L,
    val title: String,
    val url: String,
    val hdurl: String,
    val copyright: String,
    val date: String,
    val explanation: String,
    val mediaType: String,
    val serviceVersion: String
){
    fun toTexto(): String {
        return "$apodId·$title·$url·$hdurl·$copyright·$date·$explanation·$mediaType·$serviceVersion"
    }

    fun resetId(){

    }
}

/**
 * Convert DatabaseApod to domain entities
 */
fun ApodDb.asDomainModel(): DomainApod {
    return DomainApod(
        apodId = this.apodId,
        title = this.title,
        copyright = this.copyright,
        date = this.date,
        explanation = this.explanation,
        url = this.url,
        hdurl = this.hdurl,
        mediaType = this.mediaType,
        serviceVersion = this.serviceVersion
    )
}

fun List<ApodDb>.asListDomainModel(): List<DomainApod> {
    return map {
        DomainApod(
            apodId = it.apodId,
            title = it.title,
            copyright = it.copyright,
            date = it.date,
            explanation = it.explanation,
            url = it.url,
            hdurl = it.hdurl,
            mediaType = it.mediaType,
            serviceVersion = it.serviceVersion
        )
    }
}

@Entity
data class MarsPhotoDb constructor(
    @PrimaryKey
    val marsPhotoId: Int,
    val sol: Int,
    val camera: String,
    val imgSrc: String,
    val earthDate: String,
    val rover: String
){
    fun toTexto(): String{
        return "$marsPhotoId·$sol·$camera·$imgSrc·$earthDate·$rover "
    }
}

fun List<MarsPhotoDb>.asListDomainMarsPhotos(): List<DomainMarsPhoto>{
    return map{
        DomainMarsPhoto(
            marsPhotoId = it.marsPhotoId,
            sol = it.sol,
            camera = it.camera,
            imgSrc = it.imgSrc,
            earthDate = it.earthDate,
            rover = it.rover
        )
    }
}

@Entity(primaryKeys = ["rover", "fecha"])
data class FechaVista constructor(
    val rover: String,
    val fecha: String,       // aaaa-MM-dd
    val sol: Int?,
    val disponible: Boolean
){
    fun toTexto(): String {
        return "$rover·$fecha·$sol·$disponible"
    }
}

fun LiveData<List<FechaVista>>.asListDomainFechaVista(): LiveData<List<DomainFechaVista>>{
    return map(this){ it.map {
        DomainFechaVista(
            rover = it.rover,
            fecha = it.fecha,
            sol = it.sol,
            disponible = it.disponible
        )
        }
    }
}
