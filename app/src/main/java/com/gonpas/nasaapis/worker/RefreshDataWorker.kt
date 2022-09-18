package com.gonpas.nasaapis.worker

import android.content.Context
import android.content.ContextParams
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.repository.NasaRepository
import retrofit2.HttpException

class RefreshDataWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = NasaRepository(database)

        try {
            repository.getTodayApod()
        }catch (e: HttpException){
            Log.d("xxRdw","RefreshDataWorker retry later")
            return Result.retry()
        }
        Log.d("xxRdw","RefreshDataWorker success")
        return Result.success()
    }

    companion object{
        const val WORK_NAME = "com.gonpas.nasaapis.worker.RefreshDataWorker"
    }

}