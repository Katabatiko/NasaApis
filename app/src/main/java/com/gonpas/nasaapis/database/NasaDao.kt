package com.gonpas.nasaapis.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NasaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertApod(apod: ApodDb): Long

    @Query("delete from ApodDb where apodId = :key")
    suspend fun removeApod(key: Long)

    @Query("select * from ApodDb order by date desc")
    fun getAllApods(): LiveData<List<ApodDb>>

    @Query("select * from ApodDb order by date desc limit 1")
    suspend fun getLastApod(): ApodDb

    @Query("select * from ApodDb order by apodId desc Limit :key")
    suspend fun getLastsInserterApods(key: Int = 5): List<ApodDb>

    @Query("select * from ApodDb where apodId = :key")
    suspend fun getApodById(key: Long): ApodDb

    @Query("select * from ApodDb where date = :date")
    suspend fun getApodByDate(date: String): ApodDb?

    /**
     * Mars photos
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMarsPhoto(marsPhoto: MarsPhotoDb)

    @Query("delete from MarsPhotoDb where marsPhotoId = :key")
    suspend fun removeMarsPhoto(key: Int)

    @Query("select * from MarsPhotoDb order by earthDate desc")
    fun getAllMarsPhotos(): LiveData<List<MarsPhotoDb>>

    @Query("select marsPhotoId from MarsPhotoDb")
    fun getAllMarsPhotoId(): LiveData<List<Int>>

    /**
     * Mars fotos fechas visitadas
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFechaVista(fechaVista: FechaVista)

    @Query("select * from FechaVista group by rover order by fecha desc")
    fun getAllFechas(): LiveData<List<FechaVista>>

    @Query("select * from FechaVista where rover = :key order by fecha desc")
    fun getFechasByRover(key: String): LiveData<List<FechaVista>>
}