package com.gonpas.nasaapis.network

import com.gonpas.nasaapis.database.ApodDb
import com.squareup.moshi.Json

/**
 * Son DataTransferObjects, responsables de parsear las respuestas del servidor
 * o farmatear los objetos para enviar al servidor. Hay que convertirlos en
 * objetos domain para usarlos
 */
data class ApodDto(
    val copyright: String = "Nasa",
    val date: String,
    val explanation: String,
    val url: String,
    val hdurl: String = url,
    @Json(name= "media_type") val mediaType: String,
    @Json (name = "thumbnail_url") val thumbUrl: String = "",
    @Json(name= "service_version") val serviceVersion: String,
    val title: String
){
    fun isVideo (): Boolean{
        return mediaType == "video"
    }
}

/**
 * Convert network result to database object
 */
fun ApodDto.asDatabaseModel(): ApodDb {
    val finalUrl: String
    val finalHdurl: String
    if (this.isVideo()){
        finalUrl = this.thumbUrl
        finalHdurl = this.url
    }else{
        finalUrl = this.url
        finalHdurl = this.hdurl
    }
    return ApodDb(
        url = finalUrl,
        title = this.title,
        hdurl = finalHdurl,
        copyright = this.copyright,
        date = this.date,
        explanation = this.explanation,
        mediaType = this.mediaType,
        serviceVersion = this.serviceVersion
    )
}

/**
 * Convert network result to domain object
 */
/*fun ApodDto.asDomainModel(): DomainApod {
    return DomainApod(
        title = this.title,
        copyright = this.copyright,
        date = this.date,
        explanation = this.explanation,
        url = this.url,
        hdurl = this.hdurl,
        mediaType = this.mediaType,
        serviceVersion = this.serviceVersion
    )
}*/
