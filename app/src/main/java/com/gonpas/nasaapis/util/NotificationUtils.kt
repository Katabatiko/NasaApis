package com.gonpas.nasaapis.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.gonpas.nasaapis.R
import com.gonpas.nasaapis.domain.DomainApod

// Notification ID.
private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0


/**
 * Builds and delivers the notification.
 *
 * @param applicationContext, activity context.
 */
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context, todayApod: DomainApod) {

//    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    /*val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )*/

    val contentPendingIntent = NavDeepLinkBuilder(applicationContext)
        .setGraph(R.navigation.navigation)
        .setDestination(R.id.todayApodFragment)
        .setArguments(bundleOf("apod" to todayApod))
        .createPendingIntent()

    contentPendingIntent.apply { PendingIntent.FLAG_UPDATE_CURRENT }

    val apodIcon = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.branch_apod)
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(apodIcon)
        .bigLargeIcon(null)


    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.apod_notification_channel_id)
    )
        .setSmallIcon(R.drawable.apod_icon_colored)
        .setContentTitle(applicationContext.getString(R.string.iad))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)

        .setStyle(bigPicStyle)
        .setLargeIcon(apodIcon)

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotification(){
    cancel(NOTIFICATION_ID)
}