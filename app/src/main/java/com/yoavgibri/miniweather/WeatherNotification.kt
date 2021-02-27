package com.yoavgibri.miniweather

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.yoavgibri.miniweather.activities.MainActivity
import com.yoavgibri.miniweather.activities.SettingsActivity
import com.yoavgibri.miniweather.broadcastReceivers.RefreshButtonReceiver
import com.yoavgibri.miniweather.models.OpenWeather
import java.text.SimpleDateFormat
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


        return NotificationCompat.Builder(context, channelID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCustomContentView(notificationLayout)
                .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                .setTicker("")
                .setChannelId(channelID)
                .setVisibility(VISIBILITY_SECRET)

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

    fun updateWeather(weather: OpenWeather) {
        try {
            val iconName = weather.weather[0].icon
            val city = weather.name
            val temp = weather.main.temp.toFloat().toInt()
            val description = weather.weather[0].description

            val currentTime = getCurrentTimeString()

            val message = "$temp°C     $description"


            val iconRes = Do.getResIconByIconName(iconName)
            val animationViewId = Do.getProgressBarViewIdByIconName(iconName)
            if (iconRes == -1) throw Exception("LUBroadcastReceiver - onWeather - resIcon is -1")


            Do.hideAllAnimations(notificationLayout)
            notificationLayout.setTextViewText(R.id.textViewCity, city)
            notificationLayout.setTextViewText(R.id.textViewTemperature, temp.toString() + Do.getDegreesSymbol())
            notificationLayout.setTextViewText(R.id.textViewDescription, description)
            notificationLayout.setTextViewText(R.id.textViewLastUpdate, currentTime)
            notificationLayout.setViewVisibility(animationViewId, View.VISIBLE)
            notificationLayout.setProgressBar(animationViewId, 2, 1, true)


            val updateWeatherIntent: PendingIntent = PendingIntent.getBroadcast(context, 420, Intent(context, RefreshButtonReceiver::class.java), 0)
            notificationLayout.setOnClickPendingIntent(R.id.buttonRefresh, updateWeatherIntent)


            val notification: Notification = builder
                    .setSmallIcon(iconRes)
                    //.setLargeIcon(Do.textAsBitmap("11", 100f, Color.WHITE)) //does not work
                    .setCustomContentView(notificationLayout)
                    .build()

            if (SP.getBoolean(context.getString(R.string.pref_ismiui), true)) {
                setMiuiCustomizationAllow(notification)
            }


            notificationManager.notify(STATUS_NOTIFICATION_ID, notification)

            Do.logToFile("WeatherNotification - updateWeather - $message", context)

        } catch (e: Exception) {
            Do.logError(e.message, context)
        }
    }

    private fun getCurrentTimeString(): String? {
        val defFormatValue = if (DateFormat.is24HourFormat(context)) SettingsActivity.TimeFormat.hours_24 else SettingsActivity.TimeFormat.hours_12
        val timeFormat = SP.getString(context.getString(R.string.sp_key_time_format), defFormatValue.toString())
        val pattern = if (timeFormat == SettingsActivity.TimeFormat.hours_24.toString()) "HH:mm" else "hh:mm a"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Calendar.getInstance().time)
    }

    @SuppressLint("PrivateApi")
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
            SP.putBoolean(context.getString(R.string.pref_ismiui), false)
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


}
