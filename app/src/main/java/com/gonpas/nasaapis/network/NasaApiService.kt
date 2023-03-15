package com.gonpas.nasaapis.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// dirección base a la que se añadirán los endpoint
private const val API_KEY = "DEMO_KEY"
private const val APOD_BASE_URL = "https://api.nasa.gov/planetary/"
private const val EPIC_BASE_URL = "https://api.nasa.gov/EPIC/api/"
private const val MARS_ROVERS_URL = "https://api.nasa.gov/mars-photos/api/v1/"

/**
 * Rovers activos sobre Marte
 */
private const val PERSEVERANCE = "perseverance"
private const val CURIOSITY = "curiosity"
private const val OPPORTUNITY = "opportunity"
private const val SPIRIT = "spirit"

/**
 * Cámaras de los rovers
 * FHAZ, RHAZ, MAST, CHEMCAM, MAHLI, MARDI, NAVCAM, PANCAM, MINITES
 */
// cámara del mastil (curiosity)
private const val MAST = "mast"
//  cámara con lentes de mano (curiosity)
private const val MAHLI = "mahli"
// cámara de navegación (curiosity, pportunity, spirit)
private const val NAVCAM = "navcam"
// cámara panorámica (opportunity, spirit)
private const val PANCAM = "pancam"


val lon_navarrevisca = -4.89222
val lat_navarrevisca = 40.36246

val lon_hoyocasero = -4.97696
val lat_hoyocasero = 40.39798

// creación del objeto moshi con el adapter para kotlin
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/*private val moshiEpic = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .add(EpicAdapter())
    .build()*/

// objeto retrofit, que necesita la uri del web service, y un converter factory.
// El convertidor le dice a retrofit que hacer con los datos que recibe
private val retrofitApod: Retrofit by lazy{
    Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(APOD_BASE_URL)
    .build()
}

private val retrofitEpic: Retrofit by lazy {
    Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(EPIC_BASE_URL)
        .build()
}

private val retrofitMarsRovers: Retrofit by lazy {
    Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(MARS_ROVERS_URL)
        .build()
}


// la interface define como comunicarse con el web service
interface NasaApiService {

    /** APOD: Astronomy Picture of the Day
    * entre paréntesis se pone el endpoint que se añadirá al BASE_URL
    */
    @GET("apod")
    suspend fun getApod(@Query("api_key") api_key: String = API_KEY, @Query("thumbs") thumbs: Boolean = true): ApodDto

    @GET("apod")
    suspend fun getApodByDate(@Query("date") date: String, @Query("thumbs") thumbs: Boolean = true, @Query("api_key") api_key: String = API_KEY): ApodDto?

    @GET("apod")
    suspend fun getRandomApods(@Query("count") count: Int = 5, @Query("thumbs") thumbs: Boolean = true, @Query("api_key") api_key: String = API_KEY): List<ApodDto>

    /**
     * EPIC: Earth Polychromatic Imaging Camera
      */
    @GET("{collection}")
    suspend fun getLastsEpic(@Path("collection") collection: String, @Query("api_key") api_key: String = API_KEY): List<EpicDTO>

    @GET("{collection}/date/{date}")
    suspend fun getEpicsByDate(@Path("collection") collection: String, @Path("date") date: String, @Query("api_key") api_key: String = API_KEY): List<EpicDTO>

    /**
     * Pics from Mars
     */
    @GET("rovers/{rover}/photos")
    suspend fun getRoverPhotos(@Path("rover") rover: String = CURIOSITY, @Query("earth_date") earth_date: String, @Query("api_key") api_key: String = API_KEY): Photos

    @GET("rovers/{rover}/latest_photos")
    suspend fun getRoverLatestPhotos(@Path("rover") rover: String, @Query("api_key") api_key: String = API_KEY): LatestPhotos
    
    @GET("manifests/{rover}")
    suspend fun getRoverManifest(@Path("rover") rover: String, @Query("api_key") api_key: String = API_KEY): PhotoManifest
}

// para inicializar el servicio retrofit. Como consume muchos recursos se inicializa por lazy (sólo cuando se necesita)
object NasaApi {
    val retrofitApodService: NasaApiService by lazy {
        retrofitApod.create(NasaApiService::class.java)
    }

    val retrofitEpicService: NasaApiService by lazy {
        retrofitEpic.create(NasaApiService::class.java)
    }

    val retrofitMarsRoversService: NasaApiService by lazy {
        retrofitMarsRovers.create(NasaApiService::class.java)
    }

}