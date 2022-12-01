package com.gonpas.nasaapis.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.domain.DomainMarsPhoto
import com.gonpas.nasaapis.network.Camera
import com.gonpas.nasaapis.network.Rover
import com.squareup.moshi.Json


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
)

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


/*@Entity
data class FechasVisitadas constructor(
    val rover: String,
    val fecha: String
)*/

/*@Entity
data class FechasVisitadas constructor(
    val rover: String,
    val fecha: String
)*/

/*@Entity
data class FechasVisitadas constructor(
    val rover: String,
    val fecha: String
)*/

@Entity
data class MarsPhotoDb constructor(
    @PrimaryKey
    val marsPhotoId: Int,
    val sol: Int,
    val camera: String,
    val imgSrc: String,
    val earthDate: String,
    val rover: String
)

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

/*@Entity
data class FechasVisitadas constructor(
    val rover: String,
    val fecha: String
)*/

/*@Entity
data class FechasVisitadas constructor(
    val rover: String,
    val fecha: String
)*/

/*@Entity
data class FechasVisitadas constructor(
    val rover: String,
    val fecha: String
)*/
