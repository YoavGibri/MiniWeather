package com.yoavgibri.miniweather

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.yoavgibri.miniweather.broadcastReceivers.LocationUpdatesBroadcastReceiver
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.yoavgibri.miniweather.activities.SettingsActivity
import timber.log.Timber


/**
 * Created by Yoav on 25/11/17.
 */
class Do {
    @SuppressLint("SimpleDateFormat")

    companion object {
        fun logToFile(text: String, context: Context, showToast: Boolean = false) {

            val file = File("${Environment.getExternalStorageDirectory()}/WeatherNotificationsLog.txt")
            if (file.exists() && file.length() > 100000) {
                file.delete()
            }

            val date = SimpleDateFormat("dd MM yy 'at' kk:mm").format(Calendar.getInstance().time)

            val stringBuffer = StringBuffer()
            stringBuffer.append("$date - ")
            stringBuffer.append(text)
            stringBuffer.append("\n")



            if (SP.getBoolean(App.context.getString(R.string.pref_key_write_log), false) && context is Activity) {
                Permissions.checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    writeLog(file, stringBuffer.toString())
                }
            }
            if (showToast) {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }

            Timber.d(text)

        }

        private fun writeLog(file: File, text: String) {
            if (file.exists()) {
                file.appendText(text)
            } else {
                file.writeText(text)
            }
        }

        fun logError(text: String?, context: Context) {
            logToFile("Error: $text", context)
        }

        fun getPendingIntent(context: Context): PendingIntent? {
            // Note: for apps targeting API level 25 ("Nougat") or lower, either
            // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
            // location updates. For apps targeting API level O, only
            // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
            // started in the background in "O".

            // TODO(developer): uncomment to use PendingIntent.getService().
//        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
//        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            val intent = Intent(context, LocationUpdatesBroadcastReceiver::class.java)
//            intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun getIsRegisteredForLocationUpdates(context: Context): Boolean {
            return SP.getBoolean(context.getString(R.string.key_register_for_updates), false)
        }

        fun setIsRegisteredForLocationUpdates(context: Context, isRegistered: Boolean) {
            SP.putBoolean(context.getString(R.string.key_register_for_updates), isRegistered)
        }

        fun getResIconByIconName(iconName: String?): Int {
            var resIcon1 = -1
            when (iconName) {
                "01d" -> resIcon1 = R.drawable.animated_sun              //clear sky day
                "01n" -> resIcon1 = R.drawable.animated_moon              //clear sky night
                "02d" -> resIcon1 = R.drawable.animated_cloudy           //few clouds day
                "02n" -> resIcon1 = R.drawable.animated_cloudy_night           //few clouds night
                "03d", "03n" -> resIcon1 = R.drawable.animated_clouds    //scattered clouds
                "04d", "04n" -> resIcon1 = R.drawable.animated_clouds    //broken clouds
                "09d", "09n" -> resIcon1 = R.drawable.animated_rain      //shower rain
                "10d" -> resIcon1 = R.drawable.animated_rain             //rain day
                "10n" -> resIcon1 = R.drawable.animated_rain             //rain night
                "11d", "11n" -> resIcon1 = R.drawable.animated_lightning  //thunderstorm
                "13d", "13n" -> resIcon1 = R.drawable.animated_cloudy    //snow
                "50d", "50n" -> resIcon1 = R.drawable.animated_clouds    //mist
            }
            return resIcon1
        }

        fun getProgressBarViewIdByIconName(iconName: String?): Int {
            var viewId = -1
            when (iconName) {
                "01d" -> viewId = R.id.animationSun              //clear sky day
                "01n" -> viewId = R.id.animationMoon              //clear sky night
                "02d" -> viewId = R.id.animationCloudy           //few clouds day
                "02n" -> viewId = R.id.animationCloudyNight           //few clouds night
                "03d", "03n" -> viewId = R.id.animationClouds    //scattered clouds
                "04d", "04n" -> viewId = R.id.animationClouds    //broken clouds
                "09d", "09n" -> viewId = R.id.animationRain      //shower rain
                "10d" -> viewId = R.id.animationRain             //rain day
                "10n" -> viewId = R.id.animationRain             //rain night
                "11d", "11n" -> viewId = R.id.animationLightning  //thunderstorm
                "13d", "13n" -> viewId = R.id.animationCloudy    //snow
                "50d", "50n" -> viewId = R.id.animationClouds    //mist
            }
            return viewId
        }

        fun hideAllAnimations(notificationLayout: RemoteViews) {
            notificationLayout.setViewVisibility(R.id.animationSun, View.GONE)
            notificationLayout.setViewVisibility(R.id.animationMoon, View.GONE)
            notificationLayout.setViewVisibility(R.id.animationCloudy, View.GONE)
            notificationLayout.setViewVisibility(R.id.animationCloudyNight, View.GONE)
            notificationLayout.setViewVisibility(R.id.animationClouds, View.GONE)
            notificationLayout.setViewVisibility(R.id.animationRain, View.GONE)
            notificationLayout.setViewVisibility(R.id.animationLightning, View.GONE)
        }

        fun saveLocationToSharedPreferences(latitude: Double, longitude: Double) {
            SP.putFloat(LocationHelper.KEY_LAST_KNOWN_LATITUDE, latitude.toFloat())
            SP.putFloat(LocationHelper.KEY_LAST_KNOWN_LONGITUDE, longitude.toFloat())
        }

        private fun getDefaultUnitSystem(): String {
            val currentLocale = Locale.getDefault()

            return when (currentLocale.country.toUpperCase(currentLocale)) {
                // UK, UK, Myanmar, Liberia,
                "US", "GB", "MM", "LR" -> SettingsActivity.UnitSystem.imperial.toString()
                else -> SettingsActivity.UnitSystem.metric.toString()

            }

        }

        fun getDegreesSymbol(): String {
            return when (getUnitFormat()) {
                "Metric" -> "°C"
                "Imperial" -> "°F"
                "Default" -> "K"
                else -> "°"
            }
        }

        fun getUnitFormat(): String = SP.getString(App.context.getString(R.string.sp_key_units_format), getDefaultUnitSystem())

    }
}