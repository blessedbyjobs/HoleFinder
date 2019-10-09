package android.blessed.com.holefinder.global

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.blessed.com.holefinder.R
import android.blessed.com.holefinder.services.TrackingService
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.Settings

import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

internal class NotificationHelper(private val mContext: Context) {

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun createNotification(title: String, message: String): Notification {
        val resultIntent = Intent(mContext, TrackingService::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val resultPendingIntent = PendingIntent.getActivity(mContext,
                0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(mContext)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)

        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        assert(notificationManager != null)
        builder.setChannelId(NOTIFICATION_CHANNEL_ID)
        notificationManager.createNotificationChannel(notificationChannel)

        return NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()

    }

    companion object {
        private val NOTIFICATION_CHANNEL_ID = "10001"
    }
}
