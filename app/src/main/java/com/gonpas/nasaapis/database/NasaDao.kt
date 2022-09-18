package com.gonpas.nasaapis.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    suspend fun getLastApod(): ApodDb?

    @Query("select * from ApodDb order by apodId desc Limit :key")
    suspend fun getLastsInserterApods(key: Int = 5): List<ApodDb>

    @Query("select * from ApodDb where apodId = :key")
    suspend fun getApodById(key: Long): ApodDb

    @Query("select * from ApodDb where date = :date")
    suspend fun getApodByDate(date: String): ApodDb?
}