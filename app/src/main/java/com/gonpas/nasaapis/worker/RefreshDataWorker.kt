package com.gonpas.nasaapis.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.database.getDatabase
import com.gonpas.nasaapis.domain.DomainApod
import com.gonpas.nasaapis.repository.NasaRepository
import com.gonpas.nasaapis.util.extraerFecha
import com.gonpas.nasaapis.util.sendNotification
import retrofit2.HttpException

private const val TAG = "xxRdw"
class RefreshDataWorker(val appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = getDatabase(appContext)
        val repository = NasaRepository(database)
        val todayApod: DomainApod
        val fecha: String
        val descargado: Boolean

        try {
            Log.d(TAG,"RefreshDataWorker iniciado")
            descargado = repository.getTodayApod()
            if (descargado) {
                // gestion de notificaciones
                todayApod = repository.getLastApod()
                fecha = extraerFecha(todayApod.date)
                Log.d(TAG, "RefreshDataWorker success")
                val notificationManager = ContextCompat.getSystemService(
                    appContext,
                    NotificationManager::class.java
                ) as NotificationManager
                createChannel(
                    appContext.getString(R.string.apod_notification_channel_id),
                    appContext.getString(R.string.apod_notification_channel_name)
                )
                notificationManager.sendNotification(
                    appContext.getString(R.string.autodescarga, fecha),
                    appContext,
                    todayApod
                )
            }
        }catch (e: HttpException){
            Log.d(TAG,"RefreshDataWorker retry later")
            return Result.retry()
        }
        return Result.success()
    }

    companion object{
        const val WORK_NAME = "com.gonpas.nasaapis.worker.RefreshDataWorker"
    }

    // canal de notificaciones
    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                // TODO: Step 2.4 change importance
                NotificationManager.IMPORTANCE_HIGH
            )
            // TODO: Step 2.6 disable badges for this channel

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Imágen Astronómica del Día"

            val notificationManager = appContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}