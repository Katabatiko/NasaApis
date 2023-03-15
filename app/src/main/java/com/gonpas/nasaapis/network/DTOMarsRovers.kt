package com.gonpas.nasaapis.network

import com.gonpas.nasaapis.database.MarsPhotoDb
import com.gonpas.nasaapis.domain.DomainMarsPhoto
import com.gonpas.nasaapis.domain.DomainRover
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.collections.List

/**
 * La API devuelve un JSON con una única key: photos, que contiene un array de fotos
 *
 */
@JsonClass(generateAdapter = true)
data class Photos (
    val photos: Array<RoversPhotosDTO>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Photos

        if (!photos.contentEquals(other.photos)) return false

        return true
    }

    override fun hashCode(): Int {
        return photos.contentHashCode()
    }

    /*fun Photos.asListOfRoverFotos () : List<RoversPhotosDTO>{
        var fotos = listOf<RoversPhotosDTO>()
        photos.map { roverFoto ->
            fotos =fotos.plus(roverFoto)
        }
        return fotos
    }*/
}

@JsonClass(generateAdapter = true)
data class LatestPhotos (
    @Json(name = "latest_photos") val photos: Array<RoversPhotosDTO>
        ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LatestPhotos

        if (!photos.contentEquals(other.photos)) return false

        return true
    }

    override fun hashCode(): Int {
        return photos.contentHashCode()
    }
}


@JsonClass(generateAdapter = true)
data class RoversPhotosDTO (
    val id: Int,
    val sol: Int,
    val camera: Camera,
    @Json(name = "img_src") val imgSrc: String,
    @Json(name = "earth_date") val earthDate: String,
    val rover: Rover
)

fun List<RoversPhotosDTO>.asDomainModel(): List<DomainMarsPhoto>{
    return map{
        DomainMarsPhoto(
            marsPhotoId = it.id,
            sol = it.sol,
            camera = it.camera.fullName,
            imgSrc = it.imgSrc,
            earthDate = it.earthDate,
            rover = it.rover.name,
            roverStatus = it.rover.status
        )
    }
}

fun RoversPhotosDTO.asDatabaseModel(): MarsPhotoDb{
    return MarsPhotoDb(
        marsPhotoId = this.id,
        sol = this.sol,
        camera = this.camera.fullName,
        imgSrc = this.imgSrc,
        earthDate = this.earthDate,
        rover = this.rover.name
    )
}

@JsonClass(generateAdapter = true)
data class Camera(
    val id: Int,
    val name: String,
    @Json(name = "rover_id") val roverId: Int,
    @Json(name = "full_name") val fullName: String
)

@JsonClass(generateAdapter = true)
data class Rover(
    val id: Int,
    val name: String,
    @Json(name = "landing_date") val landingDate: String,
    @Json(name = "launch_date") val launchDate: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class PhotoManifest(
    @Json(name = "photo_manifest") val manifest: ManifestRoverDTO
)

@JsonClass(generateAdapter = true)
data class ManifestRoverDTO (
    val name: String,
    @Json(name = "landing_date") val landingDate: String,
    @Json(name = "launch_date") val launchDate: String,
    val status: String,
    @Json(name = "max_sol") val maxSol: Int,
    @Json(name = "max_date") val maxDate: String,
    @Json(name = "total_photos") val totalPhotos: Int,
    @Transient
    val photos: Photos? = null
){
    data class Photos (
        val sol: Int,
        @Json(name = "earth_date") val earthDate: String,
        @Json(name = "total_photos") val totalPhotos: Int,
        @Transient
        val cameras: Array<String> = arrayOf()
            ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Photos

            if (!cameras.contentEquals(other.cameras)) return false

            return true
        }

        override fun hashCode(): Int {
            return cameras.contentHashCode()
        }
    }
}

fun ManifestRoverDTO.asDomainModel(): DomainRover{
    return DomainRover(
        name = this.name,
        landingDate = this.landingDate,
        launchDate = this.launchDate,
        status = this.status,
        maxSol = this.maxSol,
        maxDate = this.maxDate,
        totalPhotos = this.totalPhotos
    )
}