package com.yoavgibri.miniweather.broadcastReceivers

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.yoavgibri.miniweather.*
import com.yoavgibri.miniweather.models.OpenWeather

/**
 * Created by Yoav on 27/03/18.
 */
class RefreshButtonReceiver : BroadcastReceiver() {
    val TAG: String = "RefreshButtonReceiver"


    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            Do.logToFile("RefreshButtonReceiver - OnReceive", context!!)
            doOnReceive(context)
        } catch (e: Exception) {
            Do.logError(e.message, context!!)
        }
    }

    companion object {

//        const val ACTION_PROCESS_UPDATES: String = BuildConfig.APPLICATION_ID + "action.PROCESS.UPDATES"

        @SuppressLint("MissingPermission")
        fun doOnReceive(context: Context) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            val onWeather: WeatherManager.OnWeatherLoad = object : WeatherManager.OnWeatherLoad {
                override fun onWeather(weather: OpenWeather) {
                    val notificationManager = WeatherNotification(context)
                    notificationManager.updateWeather(weather)
                }
            }

            fusedLocationClient?.lastLocation?.addOnSuccessListener {
                Do.logToFile("RefreshButtonReceiver - OnReceive - LastLocation OnSuccess", context)
                if (it != null) {
                    Do.saveLocationToSharedPreferences(context, it.latitude, it.longitude)
                } else {
                    Do.logError("RefreshButtonReceiver - OnReceive - LastLocation OnSuccess - it is null", context)
                }
                WeatherManager(context).getCurrentWeatherJson(onWeather)
            }

            fusedLocationClient?.lastLocation?.addOnFailureListener {
                Do.logError(it.message, context)
                WeatherManager(context).getCurrentWeatherJson(onWeather)
            }
        }

    }


}