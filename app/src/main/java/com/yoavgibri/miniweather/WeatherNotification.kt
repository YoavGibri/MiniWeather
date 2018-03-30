package com.yoavgibri.miniweather

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.view.View
import com.yoavgibri.miniweather.activities.MainActivity
import com.yoavgibri.miniweather.models.OpenWeather
import android.widget.RemoteViews
import com.yoavgibri.miniweather.broadcastReceivers.LocationUpdatesBroadcastReceiver
import com.yoavgibri.miniweather.broadcastReceivers.RefreshButtonReceiver
import com.yoavgibri.miniweather.broadcastReceivers.RefreshButtonReceiver.Companion.ACTION_PROCESS_UPDATES
import java.util.*


/**
 * Created by Yoav on 25/11/17.
 */


class WeatherNotification(val context: Context) {

    private val STATUS_NOTIFICATION_ID: Int = 123
    private var builder: NotificationCompat.Builder
    private var notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var notificationLayout: RemoteViews = RemoteViews(context.packageName, R.layout.notification_custom_view)

    init {
        builder = initNotificationBuilder()
    }

    private fun initNotificationBuilder(): NotificationCompat.Builder {

        var channelID = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelID = initNotificationChannel()
        }
        //val notificationLayout = RemoteViews(context.packageName, R.layout.notification_custom_view)

        //val buttonRefresh

        return NotificationCompat.Builder(context, channelID)
                .setAutoCancel(false)
                .setOngoing(true)
//                .setStyle(android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                .setTicker("")
                .setSmallIcon(R.drawable.animated_walking_man)
                .setChannelId(channelID)
                .setSound(null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initNotificationChannel(): String {
        val channelID = "765"
        val name = context.getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.setSound(null, null)

//        channel.description = "yadda yadda"

        notificationManager.createNotificationChannel(channel)
        return channelID
    }


    fun show() {
        notificationManager.notify(STATUS_NOTIFICATION_ID, builder.build())
    }

    private var isMiui: Boolean = false

    fun updateWeather(weather: OpenWeather) {
        try {
            val iconName = weather.weather[0].icon
            val city = weather.name
            val temp = weather.main.temp.toFloat().toInt()
            val description = weather.weather[0].description

            val message = "$temp°C     $description"


            val iconRes = Do.getResIconByIconName(iconName)
            val animationViewId = Do.getProgressBarViewIdByIconName(iconName)
            if (iconRes == -1) throw Exception("LUBroadcastReceiver - onWeather - resIcon is -1")


            Do.hideAllAnimations(notificationLayout)
            notificationLayout.setTextViewText(R.id.textViewCity, city)
//            notificationLayout.setTextViewText(R.id.textViewCity, Calendar.getInstance().time.toString())
            notificationLayout.setTextViewText(R.id.textViewTemperature, temp.toString())
            notificationLayout.setTextViewText(R.id.textViewDescription, description)
            notificationLayout.setViewVisibility(animationViewId, 0)
            notificationLayout.setProgressBar(animationViewId, 2, 1, true)

            val updateWeatherIntent: PendingIntent = PendingIntent.getBroadcast(context, 420, Intent(context, RefreshButtonReceiver::class.java), 0)
            notificationLayout.setOnClickPendingIntent(R.id.buttonRefresh, updateWeatherIntent)


            val notification: Notification = builder
                    .setSmallIcon(iconRes)
                    .setCustomContentView(notificationLayout)
                    .build()

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_ismiui), true)) {
                setMiuiCustomizationAllow(notification)
            }


            notificationManager.notify(STATUS_NOTIFICATION_ID, notification)
            Do.logToFile("WeatherNotification - updateWeather - " + message, context)
        } catch (e: Exception) {
            Do.logError(e.message, context)
        }
    }

    private fun setMiuiCustomizationAllow(notification: Notification) {
        try {
            notification.color = context.getColor(R.color.white)
            val miuiNotificationClass = Class.forName("android.app.MiuiNotification")
            val miuiNotification = miuiNotificationClass.newInstance()
            var field = miuiNotification.javaClass.getDeclaredField("customizedIcon")
            field.isAccessible = true

            field.set(miuiNotification, true)
            field = notification::class.java.getField("extraNotification")
            field.isAccessible = true

            field.set(notification, miuiNotification)
        } catch (e: Exception) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(context.getString(R.string.pref_ismiui), false).apply()
        }
    }


    fun showIconOnly(icon: Int) {
        builder.setSmallIcon(icon)
                .setContentTitle("")
        notificationManager.notify(STATUS_NOTIFICATION_ID, builder.build())
    }


    fun cancelNotification() {
        if (isShowing()) {
            notificationManager.cancel(STATUS_NOTIFICATION_ID)
        }
    }

    fun isShowing(): Boolean {
        val notifications = notificationManager.activeNotifications
        return notifications.any { it.id == STATUS_NOTIFICATION_ID }
    }

    fun onRefreshClick(v: View) {

    }

}
