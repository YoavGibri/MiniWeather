package com.yoavgibri.miniweather.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult
import com.yoavgibri.miniweather.*
import java.math.BigDecimal

/**
 * Created by Yoav on 10/12/17.
 */
/**
 * Receiver for handling location updates.
 *
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */
class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "LUBroadcastReceiver"

    companion object {
        val ACTION_PROCESS_UPDATES = BuildConfig.APPLICATION_ID + ".action.PROCESS_UPDATES"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Do.logToFile("LUBroadcastReceiver - onReceive", context)

        if (intent != null) {

//            if (intent.action == ACTION_PROCESS_UPDATES) {

            val result = LocationResult.extractResult(intent)
            if (result != null) {
                val locations = result.locations
                if (locations.size > 0) {

                    val location = locations[0]

                    val currentLat = BigDecimal(location.latitude).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
                    val currentLong = BigDecimal(location.longitude).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()

                    Do.saveLocationToSharedPreferences(currentLat, currentLong)
                } else {
                    Do.logError("LUBroadcastReceiver - onReceive - locations list is empty", context)
                }
            } else {
                Do.logError("LUBroadcastReceiver - onReceive - result is null", context)
            }

            // remark because getCurrentWeatherJson is calls from the alarm manager
//            WeatherManager(context).getCurrentWeatherJson(object : WeatherManager.OnWeatherLoad {
//                override fun onWeather(weather: OpenWeather) {
//                    Do.logToFile("LUBroadcastReceiver - onWeather", context)
//                    val notification = WeatherNotification(context)
//                    notification.cancelNotification()
//                    notification.updateWeather(weather)
//                }
//            })


//            } else {
//                Do.logError("LUBroadcastReceiver - onReceive - action is ${intent.action}", context)
//            }
        } else {
            Do.logError("LUBroadcastReceiver - onReceive - intent is null", context)
        }


    }
}