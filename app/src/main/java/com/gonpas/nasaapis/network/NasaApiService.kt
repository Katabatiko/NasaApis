package com.gonpas.nasaapis.network

import androidx.lifecycle.LiveData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

// dirección base a la que se añadirán los endpoint
private const val API_KEY = "T2KPOpwHM9BcidtrJGbe2GuoFwDU2EMicbV07Dcw"
private const val APOD_BASE_URL = "https://api.nasa.gov/planetary/"
private const val EPIC_BASE_URL = "https://api.nasa.gov/EPIC/api/"

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


/*fun retrofitNaturalThumbFotoBuild(
    year: String,
    month: String,
    day: String,
    imageType: String,
    imageName: String
): Retrofit {
    val base_url =
        "https://epic.gsfc.nasa.gov/archive/natural/$year/$month/$day/thumb/$imageName.$imageType"
    return Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(base_url)
        .build()
}*/

// la interface define como comunicarse con el web service
interface NasaApiService {

    // APOD: Astronomy Picture of the Day
    // entre paréntesis se pone el endpoint que se añadirá al BASE_URL
    @GET("apod")
    suspend fun getApod(@Query("api_key") api_key: String = API_KEY, @Query("thumbs") thumbs: Boolean = true): ApodDto

    @GET("apod")
    suspend fun getApodByDate(@Query("date") date: String, @Query("thumbs") thumbs: Boolean = true, @Query("api_key") api_key: String = API_KEY): ApodDto?

    @GET("apod")
    suspend fun getRandomApods(@Query("count") count: Int = 5, @Query("thumbs") thumbs: Boolean = true, @Query("api_key") api_key: String = API_KEY): List<ApodDto>

    // EPIC: Earth Poluchromatic Imaging Camera
    @GET("natural")
    fun getLastNaturalEpic(@Query("api_key") api_key: String = API_KEY): LiveData<List<EpicDTO>>
}

// para inicializar el servicio retrofit. Como consume muchos recursos se inicializa por lazy (sólo cuando se necesita)
object NasaApi {
    val retrofitApodService: NasaApiService by lazy {
        retrofitApod.create(NasaApiService::class.java)
    }

    val retrofitEpicService: NasaApiService by lazy {
        retrofitEpic.create(NasaApiService::class.java)
    }

}