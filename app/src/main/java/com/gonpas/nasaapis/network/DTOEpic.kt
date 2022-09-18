package com.gonpas.nasaapis.network


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.gonpas.nasaapis.domain.DomainEpic
import com.squareup.moshi.*

/*//@JsonClass(generateAdapter = true)
data class EpicDTO(
    @get:Json(name = "identifier")
    val identifier: String,
    @get:Json(name = "caption")
    val caption: String,
    @get:Json(name = "image")
    val image: String,
    @get:Json(name = "version")
    val version: String,
    @get:Json(name = "date")
    val date: String
)*/

@JsonClass(generateAdapter = true)
data class EpicDTO (
    val identifier: String,
    val caption: String,
    val image: String,
    @Transient
    val version: String = "v1",
    @Transient
    @Json(name = "centroid_coordinates") val centroidCoordinates: Centroid = Centroid(0f, 0f),
    @Transient
    @Json(name = "dscovr_j2000_position") val dscovrPosition: J2000 = J2000(0f, 0f, 0f),
    @Transient
    @Json(name = "lunar_j2000_position") val lunarPosition: J2000 = J2000(0f, 0f, 0f),
    @Transient
    @Json(name = "sun_j2000_position") val sunPosition: J2000 = J2000(0f, 0f, 0f),
    @Transient
    @Json(name = "attitude_quaternions") val attitudeQuaternions: Quaternions = Quaternions(0f, 0f, 0f, 0f),
    val date: String,
    @Transient
    val coords: Coords = Coords(centroidCoordinates, dscovrPosition, lunarPosition, sunPosition,attitudeQuaternions)
)

// Geographical coordinates that the satellite is looking at
@JsonClass(generateAdapter = true)
data class Centroid (
    val lat: Float,
    val lon: Float
)
// space position
@JsonClass(generateAdapter = true)
data class J2000 (
    val x: Float,
    val y: Float,
    val z: Float
)
// Satellite attitude
@JsonClass(generateAdapter = true)
data class Quaternions (
    val q0: Float,
    val q1: Float,
    val q2: Float,
    val q3: Float
)
@JsonClass(generateAdapter = true)
data class Coords (
    @Json(name = "centroid_coordinates") val centroidCoord: Centroid,
    @Json(name = "dscovr_j2000_position") val dscovrPos: J2000,
    @Json(name = "lunar_j2000_position") val lunarPos: J2000,
    @Json(name = "sun_j2000_position") val sunPos: J2000,
    @Json(name = "attitude_quaternions") val attitudeQuater: Quaternions
)

class EpicAdapter: JsonAdapter<EpicDTO>() {
    @FromJson
    override fun fromJson(reader: JsonReader): EpicDTO {
        val options: JsonReader.Options = JsonReader.Options.of("identifier", "caption", "image","date")
        with(reader) {
            var identifier :String? = null
            var caption: String? = null
            var image: String? = null
            var date: String? = null
            beginObject()
            while (hasNext()) {
                when (reader.selectName(options)) {
                    0 -> identifier = readJsonValue() as String
                    1 -> caption = readJsonValue() as String
                    2 -> image = readJsonValue() as String
                    3 -> date = readJsonValue() as String
                }
            }
            endObject()
            Log.d("xxDtoE","$identifier - $image - $date")
           return EpicDTO(identifier!!, caption!!, image!!, date = date!!)
        }
    }

    override fun toJson(writer: JsonWriter, value: EpicDTO?) {
        throw UnsupportedOperationException()
    }
}

fun EpicDTO.asDomainEpic(): DomainEpic{
    return DomainEpic(
            identifier = this.identifier,
            caption = this.caption,
            imageName = this.image,
            date = this.date
        )
}

fun List<EpicDTO>.asListDomainEpic(): List<DomainEpic>{
    return map {
        DomainEpic(
            identifier = it.identifier,
            caption = it.caption,
            imageName = it.image,
            date = it.date
        )
    }
}

fun LiveData<List<EpicDTO>>.asLiveDataListDomainEpic(): LiveData<List<DomainEpic>>{
    val domainEpicList: LiveData<List<DomainEpic>> =
    map {
        it.asListDomainEpic()
    }
    return domainEpicList
}
