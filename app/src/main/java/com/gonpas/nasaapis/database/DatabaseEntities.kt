package com.gonpas.nasaapis.database

import androidx.lifecycle.LiveData
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

fun LiveData<List<MarsPhotoDb>>.asListDomainMarsPhotos(): LiveData<List<DomainMarsPhoto>>{
    return map(this){
        it.map {
            DomainMarsPhoto(
                marsPhotoId = it.marsPhotoId,
                sol = it.sol,
                camera = it.camera,
                imgSrc = it.imgSrc,
                earthDate = it.earthDate,
                rover = it.rover,
                roverStatus = "",
                saved = true
            )
        }
    }
}

@Entity(primaryKeys = ["rover", "fecha"])
data class FechaVista constructor(
    val rover: String,
    val fecha: String,       // aaaa-MM-dd
    val sol: Int?,
    val totalFotos: Int?,
    val disponible: Boolean
){
    fun toTexto(): String {
        return "$rover·$fecha·$sol·$disponible"
    }
}
// Migration de 3 a 4
/*val MIGRATION_3_4 = object : Migration(3,4){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE FechaVista ADD COLUMN totalFotos INT")
        val fechas = database.query("SELECT fecha FROM FechaVista")
        fechas.moveToFirst()
        while (fechas.moveToNext()){
            val fecha = fechas.getString(0)
            val numFotos = 0
            database.execSQL("UPDATE FechaVista SET totalFotos= numFotos  WHERE fecha= $fecha")

        }
    }
}*/

fun LiveData<List<FechaVista>>.asListDomainFechaVista(): LiveData<List<DomainFechaVista>>{
    return map(this){ it.map {
        DomainFechaVista(
            rover = it.rover,
            fecha = it.fecha,
            sol = it.sol,
            totalFotos= it.totalFotos,
            disponible = it.disponible
        )
        }
    }
}
