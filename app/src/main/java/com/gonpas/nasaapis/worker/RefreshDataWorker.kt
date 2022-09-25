package com.gonpas.nasaapis.worker

import android.content.Context
import android.content.ContextParams
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.repository.NasaRepository
import retrofit2.HttpException

private const val TAG = "xxRdw"
class RefreshDataWorker(val appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = getDatabase(appContext)
        val repository = NasaRepository(database)

        try {
            Log.d(TAG,"RefreshDataWorker iniciado")
            repository.getTodayApod()

        }catch (e: HttpException){
            Log.d(TAG,"RefreshDataWorker retry later")
            return Result.retry()
        }
        Log.d(TAG,"RefreshDataWorker success")
        return Result.success()
    }

    companion object{
        const val WORK_NAME = "com.gonpas.nasaapis.worker.RefreshDataWorker"
    }

}